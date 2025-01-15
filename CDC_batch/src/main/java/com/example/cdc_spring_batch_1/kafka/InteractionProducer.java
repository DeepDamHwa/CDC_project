package com.example.cdc_spring_batch_1.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InteractionProducer {

    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void sendNewInteractionCaptureMessage(Object newInteractionData){
        kafkaTemplate.send("change_log", newInteractionData);
    }

}
