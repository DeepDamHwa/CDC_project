package com.example.cdcconsumer.global.infra.kafka.out;

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
