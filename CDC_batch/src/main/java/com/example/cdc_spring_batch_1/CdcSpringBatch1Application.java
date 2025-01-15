package com.example.cdc_spring_batch_1;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CdcSpringBatch1Application {

    public static void main(String[] args) {
        SpringApplication.run(CdcSpringBatch1Application.class, args);
    }

}
