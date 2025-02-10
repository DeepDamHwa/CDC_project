package com.example.cdcconsumer.global.infra.kafka.out;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataProducer {
    private final KafkaTemplate<String,Object> kafkaTemplate;
    public void sendNewInteractionCaptureMessage(Object newInteractionData){
        kafkaTemplate.send("change_log", newInteractionData);
    }

    public void sendCommentDeadLetter(Object newInteractionData){
        kafkaTemplate.send("comment_dead_letter", newInteractionData);
    }

    public void sendEmojiDeadLetter(Object newInteractionData){
        kafkaTemplate.send("emoji_dead_letter", newInteractionData);
    }

    public void sendInteractionDeadLetter(Object newInteractionData){
        kafkaTemplate.send("interaction_dead_letter", newInteractionData);
    }

    public void sendUserDeadLetter(Object newInteractionData){
        kafkaTemplate.send("user_dead_letter", newInteractionData);
    }

    public void sendPostDeadLetter(Object newInteractionData){
        kafkaTemplate.send("post_dead_letter", newInteractionData);
    }
    public void sendRoleDeadLetter(Object newInteractionData){
        kafkaTemplate.send("role_dead_letter", newInteractionData);
    }
}
