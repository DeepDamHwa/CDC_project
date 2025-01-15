package com.example.cdc_payload.domain;


import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;

public interface EventEntity {
    public NewPayloadData toDto(String operation);
}
