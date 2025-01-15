package com.example.cdc_payload.domain.role.model;

import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewRolePayloadData{
    private String operation;
    private Long roleIdx;
    private String name;
}