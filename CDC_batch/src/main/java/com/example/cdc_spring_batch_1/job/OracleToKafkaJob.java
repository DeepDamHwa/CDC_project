package com.example.cdc_spring_batch_1.job;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class OracleToKafkaJob {

    private final JdbcTemplate jdbcTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String KAFKA_TOPIC = "change_data_log";

    @Bean
    public Job simpleJob(JobRepository jobRepository, Step readOracleLogStep, Step sendToKafkaStep, Step saveLastWorkStep, Step readLastWorkStep) {
        return new JobBuilder("simpleJob", jobRepository)
                .start(readLastWorkStep)
                .next(readOracleLogStep)
                .next(sendToKafkaStep)
                .next(saveLastWorkStep)
                .build();
    }

    // 1) SAVE_WORK 테이블에서 마지막 데이터(Offset) 읽기 (ExecutionContext에 저장)
    @Bean
    public Step readLastWorkStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
        return new StepBuilder("readLastWorkStep", jobRepository)
                .tasklet(readLastWorkTasklet(), transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
    @Bean
    public Tasklet readLastWorkTasklet() {
        return ((contribution, chunkContext) -> {

            log.info(">>>>> Starting Read Last Work");
            // save work 테이블에서 이전 배치 작업 내용 추출
            String readSaveWorkSql = String.format(
                    """
                    SELECT IDX, RS_ID, OPERATION, TABLE_NAME, REDO_VER
                    FROM save_work
                    WHERE idx = (SELECT MAX(idx) FROM save_work)
                    """
            );
            try {
                Map<String, Object> lastWork = jdbcTemplate.queryForMap(readSaveWorkSql);

                if (lastWork != null) {
                    log.info("Last Work Entry Retrieved: {}", lastWork);

                    log.info("IDX: {}", lastWork.get("IDX"));
                    log.info("RS_ID: {}", lastWork.get("RS_ID"));
                    log.info("OPERATION: {}", lastWork.get("OPERATION"));
                    log.info("TABLE_NAME: {}", lastWork.get("TABLE_NAME"));
                    log.info("REDO_VER: {}", lastWork.get("REDO_VER"));

                    // save work 내용을 ExecutionContext에 저장하여 (Key:lastWork) 다음 단계에서 사용할 수 있도록 설정
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                            .put("lastWork", lastWork);
                } else {    // TODO: lastWork가 null일때 어떻게 처리할 것인가? -> SAVE_WORK 테이블에 데이터가 아예 없을때
                    log.warn("No entries found in SAVE_WORK table.");
                }
            } catch (Exception e) {
                log.error("Error retrieving last work entry: {}", e.getMessage(), e);
                throw e;
            }
            log.info(">>>>> Finished Reading Last Work");
            return RepeatStatus.FINISHED;
        });
    }


    // 2) SAVE_WORK 에서 받은 Offset 을 통해 Oracle DB의 트랜잭션 로그를 읽기
    @Bean
    public Step readOracleLogStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("readOracleLogDataStep", jobRepository)
                .tasklet(readOracleLogStepTasklet(), transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
    @Bean
    public Tasklet readOracleLogStepTasklet() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> Starting Oracle LogMiner Operations");

            // 2-1) 현재 가용중인 REDO 로그 파일 번호 조회
            String currentRedoLogFile = getCurrentRedoLogFile(jdbcTemplate); // 현재 동작중인 로그파일 URL 조회 : C:\APP\~~~\~~~\REDO01.LOG
            int numberOfCurrentVersion = Integer.parseInt(currentRedoLogFile.substring(currentRedoLogFile.lastIndexOf(".LOG") - 1, currentRedoLogFile.lastIndexOf(".LOG"))); // 번호만 추출 : 1

            log.info(">>>>> Number of Current REDO Log File: {}", numberOfCurrentVersion);

            // 2-2) JobExecutionContext에서 last work offset 데이터 가져오기
            // [ Offset 활용 리스트 ]
            // -> RS_ID : 데이터 번호
            // -> REDO_VER : 파일 번호
            // -> TRANS_IDX : 이전 미처리 트랜잭션 idx 번호

            ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
            Map<String, Object> lastWorkResults = (Map<String, Object>) jobExecutionContext.get("lastWork");    //Execution에 저장한 lastwork 가져오기
            String lastRsId = ((String) lastWorkResults.get("RS_ID")).trim();   //RS_ID 조회
            int lastWorkRedoVersion = Integer.parseInt(String.valueOf(lastWorkResults.get("REDO_VER")));    //과거 마지막 로그파일 번호 조회


            // 2-3) ACTIVE TRANSACTION 부터 처리하기 -> 이전 배치에서 제외됐던 활성 트랜잭션 데이터들 처리
            // activeTransStepByStep method안에서 카프카까지 전송하는 로직 실행 후, active_trans 테이블에서 처리된 마지막 row의 idx값 반환
            // 데이터 처리 로직을 메서드 안에서 다 처리하고 idx값만 반환한 이유는 추후 save_work에 저장해야 하기 때문
            List<Map<String, Object>> transactionLogContentsResults = activeTransStepByStep();

            // 2-4) JobExecutionContext에 트랜잭션 데이터 내용 저장
            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                    .put("commitTransactionData", transactionLogContentsResults);


            // 2-5) 로그 데이터 순차 처리
            List<Map<String, Object>> logContentsResults = new ArrayList<>();

            // 같을 땐 그대로
            if(numberOfCurrentVersion == lastWorkRedoVersion){
                log.info("progress same Redo Log Logic");
                logContentsResults = stepByStep(currentRedoLogFile, lastRsId);

            } else{ // 다를 땐 마지막 배치 로그파일 번호부터 현재 로그파일 번호 진행
                // REDO Log파일 돌아가는 순서대로 로직 처리 (1 -> 2 -> 3 -> 1)
                log.info("progress different Redo Log Logic");
                while(true){
                    currentRedoLogFile = currentRedoLogFile.substring(0,currentRedoLogFile.lastIndexOf(".LOG") - 1) + lastWorkRedoVersion + ".LOG";

                    for (Map<String, Object> row : stepByStep(currentRedoLogFile, lastRsId)) {
                        logContentsResults.add(row);
                    }
                    if(numberOfCurrentVersion == lastWorkRedoVersion){
                        break;
                    }

                    lastWorkRedoVersion++;
                    if(lastWorkRedoVersion % 3 == 1){
                        lastWorkRedoVersion = 1;
                    }
                }
            }

            // 2-6) JobExecutionContext에 로그 데이터 내용 저장
            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                    .put("logContentsResults", logContentsResults);

            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                    .put("numberOfCurrentVersion", numberOfCurrentVersion);

            log.info(">>>>> Saved log contents to JobExecutionContext");
            log.info(">>>>> Finished Oracle LogMiner Operations");

            log.info("총 처리된 로그 데이터 량 : " + logContentsResults.size());
            return RepeatStatus.FINISHED;
        });
    }

    // 3) 로그 데이터 결과를 카프카에 보내기
    @Bean
    public Step sendToKafkaStep(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new StepBuilder("sendToKafkaStep", jobRepository)
                .tasklet(sendToKafkaTasklet(),transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Tasklet sendToKafkaTasklet() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> Starting Kafka Transmission");
            ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

            // 3-1) JobExecutionContext에서 처리할 트랜잭션 데이터 가져오기
            List<Map<String, Object>> transactionLogContentsResults = (List<Map<String, Object>>) jobExecutionContext.get("commitTransactionData");

            if (transactionLogContentsResults == null || transactionLogContentsResults.isEmpty()) {
                log.warn("카프카로 전송한 트랜잭션 데이터 없음");
            } else {
                // Kafka로 데이터 순차 전송
                for (Map<String, Object> row : transactionLogContentsResults) {
                    kafkaTemplate.send(KAFKA_TOPIC, (String) row.get("ROW_ID"), row);
                    log.info("카프에 전송한 뒤늦게 처리된 트랜잭션 데이터: {}", row);
                }
            }

            // 3-2) JobExecutionContext에서 로그 데이터 가져오기
            List<Map<String, Object>> logContentsResults = (List<Map<String, Object>>) jobExecutionContext.get("logContentsResults");

            if (logContentsResults == null || logContentsResults.isEmpty()) {
                log.warn("No log contents found in ExecutionContext");
            } else {
                // Kafka로 데이터 순차 전송
                for (Map<String, Object> row : logContentsResults) {
                    kafkaTemplate.send(KAFKA_TOPIC, (String) row.get("ROW_ID"), row);
                    log.info("Sent log entry to Kafka: {}", row);
                }
            }

            log.info("카프카로 보낸 총 트랜잭션 데이터 량 : " + transactionLogContentsResults.size());
            log.info("카프카로 보낸 총 데이터 량 : " + logContentsResults.size());
            log.info(">>>>> Finished Kafka Transmission");
            return RepeatStatus.FINISHED;
        });
    }


    // 4) 카프카 전송이 완료되면, LAST_WORK 테이블에 기록
    @Bean
    public Step saveLastWorkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new StepBuilder("saveLastWorkStep", jobRepository)
                .tasklet(saveLastWorkTasklet(), transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Tasklet saveLastWorkTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> Starting Saving Last Work");

            ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
            List<Map<String, Object>> logContentsResults = (List<Map<String, Object>>) jobExecutionContext.get("logContentsResults");
            Long activeTransLastIdx = (Long) jobExecutionContext.get("activeTransLastIdx");
            int numberOfCurrentVersion = (int) jobExecutionContext.get("numberOfCurrentVersion");   // C:\APP\SIHYUN\PRODUCT\21C\ORADATA\XE\REDO02.LOG

            if (logContentsResults != null && !logContentsResults.isEmpty()) {
                Map<String, Object> lastLogEntry = logContentsResults.get(logContentsResults.size() - 1);
                log.info("Last Log Entry: {}", lastLogEntry);
                log.info("CurrentRedo LogFile Entry: {}", numberOfCurrentVersion);

                saveLogToDb(lastLogEntry, numberOfCurrentVersion, activeTransLastIdx); // DB에 저장
            } else {
                log.warn("No log contents available to save.");
            }
            log.info(">>>>> Finished Saving Last Work");
            return RepeatStatus.FINISHED;
        };
    }

    // 가용중인 로그 파일번호 조회
    public String getCurrentRedoLogFile(JdbcTemplate jdbcTemplate) {
        String query = """
                    SELECT B.MEMBER AS LOGFILE_PATH
                    FROM V$LOG A
                    JOIN V$LOGFILE B ON A.GROUP# = B.GROUP#
                    WHERE A.STATUS = 'CURRENT'
                """;

        return jdbcTemplate.queryForObject(query, String.class);
    }

    private void saveLogToDb(Map<String, Object> lastLogEntry, int numberOfCurrentVersion, Long activeTransLastIdx) {

        //TODO : 실패에 대한 예외처리
        String insertSql = "INSERT INTO SAVE_WORK (RS_ID, OPERATION, TABLE_NAME, REDO_VER) VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(insertSql,
                lastLogEntry.get("RS_ID"),
                lastLogEntry.get("OPERATION"),
                lastLogEntry.get("TABLE_NAME"),
                numberOfCurrentVersion);

        log.info("Successfully saved last work log into SAVE_WORK table.");
    }

    // 로그 데이터 처리 메인로직
    public List<Map<String, Object>> stepByStep(String currentRedoLogFile, String lastRsId) {
        // 1: DBMS_LOGMNR.ADD_LOGFILE 실행
        String addLogFileSql = String.format(
                "BEGIN DBMS_LOGMNR.ADD_LOGFILE(" +
                        "LOGFILENAME => '%s', " +
                        "OPTIONS => DBMS_LOGMNR.NEW); END;", currentRedoLogFile     //로그마이너에게 경로 심어주기
        );
        jdbcTemplate.execute(addLogFileSql);
        log.info(">>>>> Executed DBMS_LOGMNR.ADD_LOGFILE with file: {}", currentRedoLogFile);

        // 2: DBMS_LOGMNR.START_LOGMNR 실행
        String startLogMinerSql = "BEGIN DBMS_LOGMNR.START_LOGMNR(" +
                "OPTIONS => DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG); END;";
        jdbcTemplate.execute(startLogMinerSql);
        log.info(">>>>> Executed DBMS_LOGMNR.START_LOGMNR");

        // 3: 진행중인 트랜잭션이 있는지 조회
        // TODO : 왜 조회하지?
        String selectActiveTransactionSql = "SELECT ROW_ID, RS_ID, OPERATION, SEG_OWNER, TABLE_NAME, XIDUSN, XIDSLT, SQL_REDO, XID " +
                "FROM V$LOGMNR_CONTENTS " +
                "WHERE TABLE_NAME IN ('USERS', 'COMMENTS', 'EMOJI', 'INTERACTION', 'POST', 'ROLE') " +
                "AND XID IN (SELECT XID FROM V$TRANSACTION WHERE STATUS = 'ACTIVE')";

        List<Map<String, Object>> activeTransactionResults = jdbcTemplate.queryForList(selectActiveTransactionSql);

        // 4: 만약 진행중인 트랜잭션이 존재하면, 해당 트랜잭션 정보 저장
        // TODO : 어디에?
        // TODO : 테이블에 값이 있으면 저장을 안하는가? -> 있어도 함
        // TODO : 테이블에 null 처리 된 채 있으면, 저장하지 말고 continue 때리기
        if(!activeTransactionResults.isEmpty()) {
            log.info("진행중인 트랜잭션 데이터 갯수: {} ", activeTransactionResults.size());
            Object xid = null;
            for(Map<String, Object> row : activeTransactionResults) {
                String numberOfRedoLogFile = currentRedoLogFile.substring(currentRedoLogFile.length() - 5, currentRedoLogFile.length() - 4);
                Object currentXid = null;
                if (row.get("XID") != null) {
                    currentXid = row.get("XID");

                    if (currentXid != null) {
                        String xidHex = bytesToHex((byte[]) xid);
                        String currentXidHex = bytesToHex((byte[]) currentXid);

                        if (!xidHex.equals(currentXidHex)) {
                            xid = currentXid;
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }

                String checkActiveTransactionSql = "SELECT COUNT(*) FROM ACTIVE_TRANS WHERE XID = ?";
                Integer count = jdbcTemplate.queryForObject(checkActiveTransactionSql, Integer.class, xid);

                if (count == null || count == 0) {
                    String insertSql = "INSERT INTO ACTIVE_TRANS (IDX, XID, REDO_VER) VALUES (NULL, ?, ?)";
                    jdbcTemplate.update(insertSql, xid, numberOfRedoLogFile);
                }
            }
        }

        // 5: Offset을 활용한 로그 데이터 조회 (트랜잭션 제외)
        String selectLogContentsSql = "SELECT ROW_ID, RS_ID, OPERATION, SEG_OWNER, TABLE_NAME, XIDUSN, XIDSLT, SQL_REDO " +
                "FROM V$LOGMNR_CONTENTS " +
                "WHERE TABLE_NAME IN ('USERS', 'COMMENTS', 'EMOJI', 'INTERACTION', 'POST', 'ROLE') " +
                "AND TRIM(RS_ID) > '" + lastRsId + "' " +
                "AND (XIDUSN, XIDSLT) NOT IN (SELECT XIDUSN, XIDSLOT FROM V$TRANSACTION WHERE STATUS = 'ACTIVE')";

        List<Map<String, Object>> logContentsResults = jdbcTemplate.queryForList(selectLogContentsSql);

        // Step 4: 결과 출력
        for (Map<String, Object> row : logContentsResults) {
            log.info("ROW_ID: {}, RS_ID: {}, OPERATION: {}, SEG_OWNER: {}, TABLE_NAME: {}, XIDUSN: {}, XIDSLT: {}, SQL_REDO: {}",
                    row.get("ROW_ID"), row.get("RS_ID"), row.get("OPERATION"), row.get("SEG_OWNER"), row.get("TABLE_NAME"), row.get("XIDUSN"), row.get("XIDSLT"), row.get("SQL_REDO"));
        }

        return logContentsResults;
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // 이전 배치 작업에서 제외된 트랜잭션 데이터 처리 로직
    public List<Map<String,Object>> activeTransStepByStep() {
        //TODO : ACTIVE_TRANS에서 미처리된 데이터들 확인 (XID, STATUS)
        String readTransTableSql = """
            SELECT IDX, XID, STATUS,REDO_VER
            FROM ACTIVE_TRANS
            WHERE STATUS IS NULL
            ORDER BY IDX
            """;

        // 이전에 미처리 해서 처리해야 할 트랜잭션 데이터 리스트
        List<Map<String, Object>> unProcessedTransactionData = jdbcTemplate.queryForList(readTransTableSql);


        // 미처리된 트랜잭션 데이터가 없으면, 스킵
        if (unProcessedTransactionData == null) {
            return null;
        }

        // 미처리된 트랜잭션 데이터 목록들 출력
        for(Map<String,Object> x : unProcessedTransactionData) {
            log.info("미처리된 모든 트랜잭션 데이터 목록");
            log.info("IDX: {}", x.get("IDX"));
            log.info("XID: {}", x.get("XID"));
            log.info("STATUS: {}", x.get("STATUS"));
            log.info("REDO_VER:{}",x.get("REDO_VER"));
        }

        //TODO 미처리 된 데이터들이 현재 트랜잭션 테이블에 있는지 조회
        String selectActiveTransactionSql = ""
                + "SELECT XID "
                + "FROM V$TRANSACTION "
                + "WHERE STATUS = 'ACTIVE'";

        List<Map<String, Object>> activeTransactionData = jdbcTemplate.queryForList(selectActiveTransactionSql);


        // 현재 진행 중인 XID 목록을 Set으로 변환하여 조회 성능 개선
        List<String> activeXid = new ArrayList<>();
        for(Map<String,Object> x : activeTransactionData){
            String StringXID = bytesToHex((byte[])x.get("XID")).toLowerCase();
            activeXid.add(StringXID);
        }

        // unProcessedTransactionData 리스트에서 activeXid에 존재하는 XID를 가진 항목 제거
        Iterator<Map<String, Object>> iterator = unProcessedTransactionData.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> data = iterator.next();
            String xid = (String) data.get("XID");

            // 조건에 맞으면 해당 데이터를 제거
            if (activeXid.contains(xid.toLowerCase().trim())) {
                iterator.remove();
            }
        }

        // 필터링된 미처리 트랜잭션 데이터 출력
        for(Map<String,Object> x : unProcessedTransactionData) {
            log.info("필터링 된 트랜잭션 데이터 목록");
            log.info("IDX: {}", x.get("IDX"));
            log.info("XID: {}", x.get("XID"));
            log.info("STATUS: {}", x.get("STATUS"));
        }

        //TODO: ACTIVE_TRANS 는 처리 상태로 변경하면 안됨 -> 여전히 FALSE 상태로 냅두기 -> 아무행동 X

        //로그파일 경로 출력
        String currentRedoLogFile = getCurrentRedoLogFile(jdbcTemplate); // 현재 동작중인 로그파일 URL 조회 : C:\APP\~~~\~~~\REDO01.LOG


        //TODO : for문 돌리기

        List<Map<String, Object>> logContentsResults = new ArrayList<>();   //처리할 트랜잭션 목록 리스트

        for(Map<String,Object> tasksToProcessData : unProcessedTransactionData){

            log.info("{}:" + tasksToProcessData.get("IDX"));

            currentRedoLogFile = currentRedoLogFile.substring(0,currentRedoLogFile.lastIndexOf(".LOG") - 1) + tasksToProcessData.get("REDO_VER") + ".LOG";
            // 1: DBMS_LOGMNR.ADD_LOGFILE 실행
            String addLogFileSql = String.format(
                    "BEGIN DBMS_LOGMNR.ADD_LOGFILE(" +
                            "LOGFILENAME => '%s', " +
                            "OPTIONS => DBMS_LOGMNR.NEW); END;", currentRedoLogFile     //로그마이너에게 경로 심어주기
            );
            jdbcTemplate.execute(addLogFileSql);

            // 2: DBMS_LOGMNR.START_LOGMNR 실행
            String startLogMinerSql = "BEGIN DBMS_LOGMNR.START_LOGMNR(" +
                    "OPTIONS => DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG); END;";
            jdbcTemplate.execute(startLogMinerSql);


            String xidValue = (String)tasksToProcessData.get("XID");
            //TODO 아래의 쿼리를 실행해서 COMMIT 인지 ROLLBACK 인지 확인해 : XID 변수 처리
            String checkCommitOrRollback = String.format(
                    """
                    SELECT OPERATION
                    FROM V$LOGMNR_CONTENTS
                    WHERE XID = '%s' AND OPERATION IN ('ROLLBACK', 'COMMIT')
                    ORDER BY RS_ID DESC
                    """, xidValue
            );
            Map<String, Object> commitOrRollback = jdbcTemplate.queryForMap(checkCommitOrRollback);

            //TODO 만일 COMMIT 이다.
            if(commitOrRollback.get("OPERATION").equals("COMMIT")){
                System.out.println("COMMIT 들어옴");
                // 로그 파일에서 조회
                String selectCommitRedoLogData = "SELECT XID, ROW_ID, RS_ID, OPERATION, TABLE_NAME, SQL_REDO " +
                        "FROM V$LOGMNR_CONTENTS " +
                        "WHERE TABLE_NAME IN ('USERS', 'COMMENTS', 'EMOJI', 'INTERACTION', 'POST', 'ROLE') " +
                        "AND XID = ?";
                List<Map<String, Object>> activeTransactionResults = jdbcTemplate.queryForList(selectCommitRedoLogData,xidValue);

                // 조회된 로그파일에서 logContentsResults에 담아주기

                for(Map<String,Object> x : activeTransactionResults){
                    logContentsResults.add(x);
                }
                String updateCommitSql = "UPDATE ACTIVE_TRANS SET STATUS = 'COMMIT' WHERE XID = ?";
                jdbcTemplate.update(updateCommitSql, xidValue);  // DB에 STATUS를 COMMIT으로 설정
            }
            //TODO 만일, ROLLBACK이다.
            else if(commitOrRollback.get("OPERATION").equals("ROLLBACK")){
                System.out.println("ROLLBACK 들어옴");
                String updateRollbackSql = "UPDATE ACTIVE_TRANS SET STATUS = 'ROLLBACK' WHERE XID = ?";
                jdbcTemplate.update(updateRollbackSql, xidValue);  // DB에 STATUS를 ROLLBACK으로 설정
            }else{
                System.out.println("아무것도 안들어옴");
            }
        }

        System.out.println("처리된 트랜잭션 데이터 목록");
        for(Map<String,Object> x : logContentsResults){
            System.out.println(x.get("RS_ID"));
        }


        return logContentsResults;
    }
}
