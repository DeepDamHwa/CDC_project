package com.example.cdcconsumer.global.infra.kafka.in;

import com.example.cdcconsumer.domain.interaction.model.Interaction;
import lombok.Getter;

@Getter
public class NewPayloadData {
    private String operation;
    private String tableName;
    private String log;
//    private Long interactionIdx;
//    private Long userIdx;
//    private Long commentIdx;
//    private Long emojiIdx;
}
