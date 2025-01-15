package com.example.cdcconsumer.domain.emoji;

import com.example.cdcconsumer.global.infra.kafka.in.NewPayloadData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewEmojiPayloadData extends NewPayloadData {
    private String operation;
    private Long emojiIdx;
    private String name;
}