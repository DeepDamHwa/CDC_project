package com.example.cdc_payload.global.infra.kafka.out;

import com.example.cdc_payload.domain.comment.model.NewCommentsPayloadData;
import com.example.cdc_payload.domain.emoji.model.NewEmojiPayloadData;
import com.example.cdc_payload.domain.interaction.model.NewInteractionPayloadData;
import com.example.cdc_payload.domain.post.model.NewPostPayloadData;
import com.example.cdc_payload.domain.role.model.NewRolePayloadData;
import com.example.cdc_payload.domain.user.model.NewUsersPayloadData;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayloadLogProducer {
    private final KafkaTemplate<String,Object> kafkaTemplate;
    public void sendNewPayloadLogCaptureMessage(NewPayloadData newPayloadData){
        kafkaTemplate.send("payload_log", newPayloadData);
        //"payload_log"
    }
    public void sendNewCommentsPayloadLogCaptureMessage(NewCommentsPayloadData newCommentsPayloadData, String topic){
        kafkaTemplate.send(topic, newCommentsPayloadData);
        //"payload_log"
    }
    public void sendNewEmojiPayloadLogCaptureMessage(NewEmojiPayloadData newEmojiPayloadData, String topic){
        kafkaTemplate.send(topic, newEmojiPayloadData);
        //"payload_log"
    }
    public void sendNewInteractionPayloadLogCaptureMessage(NewInteractionPayloadData newInteractionPayloadData, String topic){
        kafkaTemplate.send(topic, newInteractionPayloadData);
        //"payload_log"
    }
    public void sendNewPostPayloadLogCaptureMessage(NewPostPayloadData newPostPayloadData, String topic){
        kafkaTemplate.send(topic, newPostPayloadData);
        //"payload_log"
    }
    public void sendNewRolePayloadLogCaptureMessage(NewRolePayloadData newRolePayloadData, String topic){
        kafkaTemplate.send(topic, newRolePayloadData);
        //"payload_log"
    }
    public void sendNewUserPayloadLogCaptureMessage(NewUsersPayloadData newUsersPayloadData, String topic){
        kafkaTemplate.send(topic, newUsersPayloadData);
        //"payload_log"
    }
}