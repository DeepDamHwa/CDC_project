package com.example.cdc_payload.domain.user.model;

import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewUsersPayloadData extends NewPayloadData {
    private String operation;
    private Long userIdx;
    private String name;
    private Long roleIdx;
}