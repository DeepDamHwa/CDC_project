package com.example.cdc_payload.domain.interaction.model;

import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewInteractionPayloadData{
    private String operation;
    private Long interactionIdx;
    private Long userIdx;
    private Long commentIdx;
    private Long emojiIdx;
}