package com.example.cdc_spring_batch_1;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@EnableKafka
public class BatchJobRunner implements CommandLineRunner {
    private final JobLauncher jobLauncher;
    private final ApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        System.out.println("배치 작업 시작");

        try {
            // Kafka 연결 대기
            waitForKafkaConnection();

            // Kafka 연결이 완료되면 배치 작업 실행
            Job job = (Job) applicationContext.getBean("dataJob");
            System.out.println("배치 작업: " + job.getName());

            // JobParameters 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            // Job 실행
            jobLauncher.run(job, jobParameters);
            System.out.println("배치 작업 성공");
        } catch (Exception e) {
            System.out.println("배치 작업 실패");
            e.printStackTrace();
        } finally {
            // 어플리케이션 종료
            System.exit(0);
        }
    }

    private void waitForKafkaConnection() {
        boolean isKafkaConnected = false;
        int retryCount = 0;
        int maxRetries = Integer.MAX_VALUE; // 무한 재시도 (원하면 제한 가능)

        while (!isKafkaConnected && retryCount < maxRetries) {
            try (KafkaConsumer<String, String> consumer = createKafkaConsumer()) {
                consumer.listTopics();  // Kafka 토픽을 조회하여 연결 확인
                isKafkaConnected = true;
                System.out.println("Kafka 연결 성공!");
            } catch (WakeupException we) {
                System.out.println("Kafka 종료 신호 감지. 연결 대기 중단.");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.out.println("Kafka 연결 대기 중... (" + ++retryCount + "회 재시도)");
                System.out.println("오류: " + e.getMessage());
                try {
                    Thread.sleep(5000);  // 5초 대기 후 재시도
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (!isKafkaConnected) {
            System.out.println("Kafka 연결 실패. 애플리케이션 종료.");
            System.exit(1);
        }
    }


    private KafkaConsumer<String, String> createKafkaConsumer() {
        // Kafka consumer 설정을 위한 Map 생성
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "222.112.156.89:9093");  // Kafka 서버 주소
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "change_log_group");  // Consumer group ID
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);  // Key deserializer
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);  // Value deserializer

        // ConsumerFactory 생성
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        // KafkaConsumer 객체 반환
        return (KafkaConsumer<String, String>) consumerFactory.createConsumer();
    }
}
