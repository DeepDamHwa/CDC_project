package com.example.cdcconsumer.domain.post;

import com.example.cdcconsumer.global.infra.kafka.in.NewPayloadData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NewPostPayloadData extends NewPayloadData {
    private String operation;
    private Long postIdx;
    private Long userIdx;
}