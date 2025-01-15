package com.example.cdcconsumer.domain.interaction.model;

import com.example.cdcconsumer.global.infra.kafka.in.NewPayloadData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewInteractionPayloadData extends NewPayloadData {
    private String operation;
    private Long interactionIdx;
    private Long userIdx;
    private Long commentIdx;
    private Long emojiIdx;
}