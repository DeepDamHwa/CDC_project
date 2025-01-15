package com.example.cdc_payload.domain.post.model;

import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewPostPayloadData{
    private String operation;
    private Long postIdx;
    private Long userIdx;
}