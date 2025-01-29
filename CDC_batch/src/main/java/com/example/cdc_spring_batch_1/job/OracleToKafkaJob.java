package com.example.cdc_spring_batch_1.job;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Bean
    public Step readLastWorkStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleStep1", jobRepository)
                .tasklet(readLastWorkTasklet(), transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
    @Bean
    public Tasklet readLastWorkTasklet() {
        return ((contribution, chunkContext) -> {

            // 1) SAVE_WORK 에서 마지막 데이터(Offset) 읽기.
            log.info(">>>>> Starting Read Last Work");
            String readSaveWorkSql = String.format(
                    """
                    SELECT IDX, RS_ID, OPERATION, TABLE_NAME, REDO_VER, TRANS_IDX
                    FROM save_work
                    WHERE idx = (SELECT MAX(idx) FROM save_work)
                    """
            );
            try {
                // TODO: lastWork가 null일때 어떻게 처리할 것인가? -> SAVE_WORK 테이블에 데이터가 아예 없을때
                // ->01, 02, 03 파일 다 찾아서 rs_id min값 부터 while문 돌리기
                Map<String, Object> lastWork = jdbcTemplate.queryForMap(readSaveWorkSql);

                if (lastWork != null) {
                    log.info("Last Work Entry Retrieved: {}", lastWork);

                    log.info("IDX: {}", lastWork.get("IDX"));
                    log.info("RS_ID: {}", lastWork.get("RS_ID"));
                    log.info("OPERATION: {}", lastWork.get("OPERATION"));
                    log.info("TABLE_NAME: {}", lastWork.get("TABLE_NAME"));
                    log.info("REDO_VER: {}", lastWork.get("REDO_VER"));
                    log.info("TRANS_IDX: {}", lastWork.get("TRANS_IDX"));

                    // ExecutionContext에 저장하여 다음 단계에서 사용할 수 있도록 설정
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                            .put("lastWork", lastWork);
                } else {
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


    // 1) Oracle DB의 트랜잭션 로그를 읽기
    @Bean
    public Step readOracleLogStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleStep2", jobRepository)
                .tasklet(readOracleLogStepTasklet(), transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
    @Bean
    public Tasklet readOracleLogStepTasklet() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> Starting Oracle LogMiner Operations");

            //TODO : last Work의 redo log 번호와 , current log 번호 비교

            // Step 0: 현재 활성화된 REDO 로그 파일 경로 조회
            String currentRedoLogFile = getCurrentRedoLogFile(jdbcTemplate); // 풀 경로
            log.info(">>>>> Current REDO Log File: {}", currentRedoLogFile);

            //TODO 아래 한 줄 지우기
            String currentVersion = currentRedoLogFile.substring(currentRedoLogFile.lastIndexOf("\\") + 1); // REDO01.LOG

            int numberOfCurrentVersion = Integer.parseInt(currentRedoLogFile.substring(currentRedoLogFile.lastIndexOf(".LOG") - 1, currentRedoLogFile.lastIndexOf(".LOG"))); // 1
            log.info(">>>>> Number of Current REDO Log File: {}", numberOfCurrentVersion);

            // JobExecutionContext에서 last work 로그 파일 데이터 가져오기
            ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
            Map<String, Object> lastWorkResults = (Map<String, Object>) jobExecutionContext.get("lastWork");
            String lastRsId = ((String) lastWorkResults.get("RS_ID")).trim();

            //TODO 아래 한 줄 지우기
//            String redoVersion = (String)lastWorkResults.get("REDO_VER");      // REDO01.LOG

//            int redoVersion = (int) lastWorkResults.get("REDO_VER");
            int redoVersion = Integer.parseInt(String.valueOf(lastWorkResults.get("REDO_VER")));

            log.info(currentVersion);
            log.info(String.valueOf(redoVersion));
            log.info("Using Last RS_ID: {}", lastRsId);

            // ACTIVE TRANSACTION 부터 처리하기
            // activeTransStepByStep method안에서 카프카까지 전송하는 로직 실행 후, active_trans 테이블에서 처리된 마지막 row의 idx값 반환
            // 데이터 처리 로직을 메서드 안에서 다 처리하고 idx값만 반환한 이유는 추후 save_work에 저장해야 하기 때문

            BigDecimal transIdx = (BigDecimal) lastWorkResults.get("TRANS_IDX");
            Long activeTransLastIdx;
            if (transIdx != null) {
                activeTransLastIdx = activeTransStepByStep(transIdx.longValue());
            } else {
                System.out.println("TRANS_IDX is null!");
                activeTransLastIdx = activeTransStepByStep(0L);
            }

            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                    .put("activeTransLastIdx", activeTransLastIdx);


            List<Map<String, Object>> logContentsResults = new ArrayList<>();

            if(numberOfCurrentVersion == redoVersion){
                // TODO : 기존 로직에서 쿼리만 RS_ID >= save_work table.RS_ID -> 시작 지점 설정
                log.info("YESSSSSSSS!!!!");

                //아래 로직 그대로. -> 메서드(Tasklet)로 바꾸는게 나을듯

                // 아래 로직에서 currnetRedoLogFile가 아니라, redoVersion 으로 바꿔야 할 듯
                // redoVersion을 매개 변수로 넘기면 더욱 좋을 듯
                logContentsResults = stepByStep(currentRedoLogFile, lastRsId);

            } else{
                // TODO : REDO Log파일 돌아가는 순서 == 1 -> 2 -> 3 / 현재 버전 처리 -> 다음 버전 처리 순
                log.warn("NOOOOOOOOO!!!!");
                while(true){
                    currentRedoLogFile = currentRedoLogFile.substring(0,currentRedoLogFile.lastIndexOf(".LOG") - 1) + redoVersion + ".LOG";
                    log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + currentRedoLogFile);

                    for (Map<String, Object> row : stepByStep(currentRedoLogFile, lastRsId)) {
                        logContentsResults.add(row);
                    }
                    if(numberOfCurrentVersion== redoVersion){
                        log.info("로직 끝!");
                        break;
                    }

                    redoVersion++;
                    if(redoVersion % 3 == 1){
                        redoVersion = 1;
                    }

//                    lastIdext(".LOG");
//                    redoVersion = Integer.toString(Integer.parseInt(redoVersion.substring(6, 7)));


//                    아래 로직 그대로 -> 메서드(Tasklet)로 바꾸는게 나을듯
//                    redoVersion을 계속 숫자처럼 증가해야 해서 형변환 해야하니까 걍 int 형으로 따로 받아놓는게 좋을 듯?

                    // <대전제> : redo03 -> redo01 로 넘어가도 rs_id 는 redo03 마지막데이터 기준 오름차순부터 시작 가능하다.
                    // -> 따라서 쿼리문을 따로 변경 할 필요가 없음 (첫번째 인덱스부터 시작해라 라고 안하고 last rs_id 박아도 보장됨)
                    // -> 물론 예외 상황 : 데이터베이스 재시작 / Redo Log 파일의 손상 복구 / 특정 구성 변경 과 같은 경우엔 보장이 안될 순 있음
                }

            }

            // Step 5: JobExecutionContext에 데이터 저장
            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                    .put("logContentsResults", logContentsResults);

//            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
//                    .put("currentRedoLogFile", currentRedoLogFile);

            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                    .put("numberOfCurrentVersion", numberOfCurrentVersion);

            log.info(">>>>> Saved log contents to JobExecutionContext");
            log.info(">>>>> Finished Oracle LogMiner Operations");

            log.info("총 처리된 로그 데이터 량 : " + logContentsResults.size());
            return RepeatStatus.FINISHED;
        });
    }
    // 2) 로그 결과를 카프카에 보내기
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

            // JobExecutionContext에서 데이터 가져오기
            ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
            List<Map<String, Object>> logContentsResults = (List<Map<String, Object>>) jobExecutionContext.get("logContentsResults");

            // 결과가 없을 경우 경고 로그 출력
            if (logContentsResults == null || logContentsResults.isEmpty()) {
                log.warn("No log contents found in ExecutionContext");
            } else {
                // Kafka로 데이터 전송
//                List<Map<String, Object>> saveLastWorkResults = new ArrayList<>();
                for (Map<String, Object> row : logContentsResults) {
                    kafkaTemplate.send(KAFKA_TOPIC, (String) row.get("ROW_ID"), row);
                    log.info("Sent log entry to Kafka: {}", row);
//                    saveLastWorkResults.add(row);
                }

                // JobExecutionContext에 전송된 데이터를 누적 저장
//                jobExecutionContext.put("saveLastWorkResults", saveLastWorkResults);
            }

            log.info("카프카로 보낸 총 데이터 량 : " + logContentsResults.size());
            log.info(">>>>> Finished Kafka Transmission");
            return RepeatStatus.FINISHED;
        });
    }

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

            // JobExecutionContext에서 데이터 가져오기
            ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
            List<Map<String, Object>> logContentsResults = (List<Map<String, Object>>) jobExecutionContext.get("logContentsResults");
            Long activeTransLastIdx = (Long) jobExecutionContext.get("activeTransLastIdx");
            int numberOfCurrentVersion = (int) jobExecutionContext.get("numberOfCurrentVersion");   // C:\APP\SIHYUN\PRODUCT\21C\ORADATA\XE\REDO02.LOG
//            String currentVersion = currentRedoLogFile.substring(currentRedoLogFile.lastIndexOf("\\") + 1); // REDO02.LOG

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

    private void saveLogToDb(Map<String, Object> lastLogEntry, int numberOfCurrentVersion, Long activeTransLastIdx) {

        //TODO : 실패에 대한 예외처리
        String insertSql = "INSERT INTO SAVE_WORK (RS_ID, OPERATION, TABLE_NAME, REDO_VER, TRANS_IDX) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(insertSql,
                lastLogEntry.get("RS_ID"),
                lastLogEntry.get("OPERATION"),
                lastLogEntry.get("TABLE_NAME"),
                numberOfCurrentVersion,
                activeTransLastIdx);

        log.info("Successfully saved last work log into SAVE_WORK table.");
    }

    public String getCurrentRedoLogFile(JdbcTemplate jdbcTemplate) {
        String query = """
                    SELECT B.MEMBER AS LOGFILE_PATH
                    FROM V$LOG A
                    JOIN V$LOGFILE B ON A.GROUP# = B.GROUP#
                    WHERE A.STATUS = 'CURRENT'
                """;

        return jdbcTemplate.queryForObject(query, String.class);
    }

    public List<Map<String, Object>> stepByStep(String currentRedoLogFile, String lastRsId) {
        // Step 1: DBMS_LOGMNR.ADD_LOGFILE 실행
        String addLogFileSql = String.format(
                "BEGIN DBMS_LOGMNR.ADD_LOGFILE(" +
                        "LOGFILENAME => '%s', " +
                        "OPTIONS => DBMS_LOGMNR.NEW); END;", currentRedoLogFile
        );
        jdbcTemplate.execute(addLogFileSql);
        log.info(">>>>> Executed DBMS_LOGMNR.ADD_LOGFILE with file: {}", currentRedoLogFile);

        // Step 2: DBMS_LOGMNR.START_LOGMNR 실행
        String startLogMinerSql = "BEGIN DBMS_LOGMNR.START_LOGMNR(" +
                "OPTIONS => DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG); END;";
        jdbcTemplate.execute(startLogMinerSql);
        log.info(">>>>> Executed DBMS_LOGMNR.START_LOGMNR");

        // Step 3: V$LOGMNR_CONTENTS 테이블에서 데이터 가져오기
//            String selectLogContentsSql = String.format( "SELECT RS_ID, OPERATION, SEG_OWNER, TABLE_NAME, XIDUSN, XIDSLT, SQL_REDO " +
//                    "FROM V$LOGMNR_CONTENTS " +
//                    "WHERE TABLE_NAME IN ('USERS', 'COMMENTS', 'EMOJI', 'INTERACTION', 'POST', 'ROLE') " +
//                    "AND TRIM(RS_ID) >= '%s' " +
//                    "AND (XIDUSN, XIDSLT) NOT IN (SELECT XIDUSN, XIDSLOT FROM V$TRANSACTION WHERE STATUS = 'ACTIVE')", lastRsId);
//            List<Map<String, Object>> logContentsResults = jdbcTemplate.queryForList(selectLogContentsSql);

        // 진행중인 트랜잭션이 있는지 조회
        String selectActiveTransactionSql = "SELECT ROW_ID, RS_ID, OPERATION, SEG_OWNER, TABLE_NAME, XIDUSN, XIDSLT, SQL_REDO " +
                "FROM V$LOGMNR_CONTENTS " +
                "WHERE TABLE_NAME IN ('USERS', 'COMMENTS', 'EMOJI', 'INTERACTION', 'POST', 'ROLE') " +
                "AND TRIM(RS_ID) > '" + lastRsId + "' " +
                "AND (XIDUSN, XIDSLT) IN (SELECT XIDUSN, XIDSLOT FROM V$TRANSACTION WHERE STATUS = 'ACTIVE')";

        List<Map<String, Object>> activeTransactionResults = jdbcTemplate.queryForList(selectActiveTransactionSql);

        // 만약 진행중인 트랜잭션이 존재하면, 해당 트랜잭션 정보 저장
        if(!activeTransactionResults.isEmpty()) {
            for(Map<String, Object> row : activeTransactionResults) {
                String numberOfRedoLogFile = currentRedoLogFile.substring(currentRedoLogFile.length() - 5, currentRedoLogFile.length() - 4);
                String insertSql = "INSERT INTO ACTIVE_TRANS (IDX, XIDUSN, XIDSLT, REDO_VER) VALUES (NULL, ?, ?, ?)";

                jdbcTemplate.update(insertSql,
                        row.get("XIDUSN"),
                        row.get("XIDSLT"),
                        numberOfRedoLogFile);
            }
        }

        String selectLogContentsSql = "SELECT ROW_ID, RS_ID, OPERATION, SEG_OWNER, TABLE_NAME, XIDUSN, XIDSLT, SQL_REDO " +
                "FROM V$LOGMNR_CONTENTS " +
                "WHERE TABLE_NAME IN ('USERS', 'COMMENTS', 'EMOJI', 'INTERACTION', 'POST', 'ROLE') " +
                "AND TRIM(RS_ID) > '" + lastRsId + "' " +
                "AND (XIDUSN, XIDSLT) NOT IN (SELECT XIDUSN, XIDSLOT FROM V$TRANSACTION WHERE STATUS = 'ACTIVE')";

//            log.info("!!!!!!!!!!!!!!!!!!: " + selectLogContentsSql);
        List<Map<String, Object>> logContentsResults = jdbcTemplate.queryForList(selectLogContentsSql);

        // Step 4: 결과 출력
        for (Map<String, Object> row : logContentsResults) {
            log.info("ROW_ID: {}, RS_ID: {}, OPERATION: {}, SEG_OWNER: {}, TABLE_NAME: {}, XIDUSN: {}, XIDSLT: {}, SQL_REDO: {}",
                    row.get("ROW_ID"), row.get("RS_ID"), row.get("OPERATION"), row.get("SEG_OWNER"), row.get("TABLE_NAME"), row.get("XIDUSN"), row.get("XIDSLT"), row.get("SQL_REDO"));
        }

        return logContentsResults;
    }

    public Long activeTransStepByStep(Long transIdx) {
        String readActiveTransSql;
        if (transIdx == 0L) {
            readActiveTransSql = "SELECT IDX, XIDUSN, XIDSLT, REDO_VER " +
                    "FROM ( " +
                    "    SELECT IDX, XIDUSN, XIDSLT, REDO_VER, " +
                    "           ROW_NUMBER() OVER (PARTITION BY XIDUSN, XIDSLT, REDO_VER ORDER BY IDX DESC) as rn " +
                    "    FROM active_trans " +
                    ") subquery " +
                    "WHERE rn = 1 " +
                    "ORDER BY IDX";
        } else {
            readActiveTransSql = "SELECT IDX, XIDUSN, XIDSLT, REDO_VER " +
                    "FROM ( " +
                    "    SELECT IDX, XIDUSN, XIDSLT, REDO_VER, " +
                    "           ROW_NUMBER() OVER (PARTITION BY XIDUSN, XIDSLT, REDO_VER ORDER BY IDX DESC) as rn " +
                    "    FROM active_trans " +
                    "    WHERE IDX > " + transIdx + " " +
                    ") subquery " +
                    "WHERE rn = 1 " +
                    "ORDER BY IDX";
        }

        List<Map<String, Object>> activeTransactionResults = jdbcTemplate.queryForList(readActiveTransSql);

        long lastIdx = 0L;

        if (!activeTransactionResults.isEmpty()) {
            for (Map<String, Object> result : activeTransactionResults) {
                long idx = ((BigDecimal) result.get("IDX")).longValue();

                if (idx > lastIdx) {
                    lastIdx = idx;
                }
            }
        }

        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  " + transIdx);
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  " + lastIdx);

        String prefix = "C:\\APP\\SIHYUN\\PRODUCT\\21C\\ORADATA\\XE\\REDO0";
        String suffix = ".LOG";

        List<Map<String, Object>> logContentsResults = new ArrayList<>();

        for(Map<String, Object> row : activeTransactionResults) {
            // Step 1: DBMS_LOGMNR.ADD_LOGFILE 실행
            String addLogFileSql = String.format(
                    "BEGIN DBMS_LOGMNR.ADD_LOGFILE(" +
                            "LOGFILENAME => '%s', " +
                            "OPTIONS => DBMS_LOGMNR.NEW); END;", prefix + row.get("REDO_VER") + suffix
            );
            jdbcTemplate.execute(addLogFileSql);

            // Step 2: DBMS_LOGMNR.START_LOGMNR 실행
            String startLogMinerSql = "BEGIN DBMS_LOGMNR.START_LOGMNR(" +
                    "OPTIONS => DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG); END;";
            jdbcTemplate.execute(startLogMinerSql);

            String selectLogContentsSql = "SELECT ROW_ID, RS_ID, OPERATION, SEG_OWNER, TABLE_NAME, XIDUSN, XIDSLT, SQL_REDO " +
                    "FROM V$LOGMNR_CONTENTS " +
                    "WHERE TABLE_NAME IN ('USERS', 'COMMENTS', 'EMOJI', 'INTERACTION', 'POST', 'ROLE') " +
                    "AND XIDUSN = " + row.get("XIDUSN") + " AND XIDSLT = " + row.get("XIDSLT");

            logContentsResults = jdbcTemplate.queryForList(selectLogContentsSql);
        }

        for (Map<String, Object> row : logContentsResults) {
            kafkaTemplate.send(KAFKA_TOPIC, (String) row.get("ROW_ID"), row);
            log.info("Sent log entry from active Transaction to Kafka: {}", row);
//                    saveLastWorkResults.add(row);
        }
        log.info("카프카로 보낸 총 데이터 량 : " + logContentsResults.size());
        log.info(">>>>> Finished Kafka Transmission");


        return lastIdx;
    }
}
