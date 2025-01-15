package com.example.cdc_payload.domain.emoji.model;

import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewEmojiPayloadData{
    private String operation;
    private Long emojiIdx;
    private String name;
}