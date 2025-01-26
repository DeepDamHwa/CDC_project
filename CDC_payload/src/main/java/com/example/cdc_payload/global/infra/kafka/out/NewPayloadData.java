package com.example.cdc_payload.global.infra.kafka.out;

import com.example.cdc_payload.domain.interaction.model.Interaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewPayloadData {
    private String operation;
    private String tableName;
    private String log;
}