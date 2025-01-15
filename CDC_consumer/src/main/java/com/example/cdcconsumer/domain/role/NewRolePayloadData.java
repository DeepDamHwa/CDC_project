package com.example.cdcconsumer.domain.role;

import com.example.cdcconsumer.global.infra.kafka.in.NewPayloadData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NewRolePayloadData extends NewPayloadData {
    private String operation;
    private Long roleIdx;
    private String name;
}