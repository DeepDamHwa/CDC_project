package com.example.cdcconsumer.domain.user;

import com.example.cdcconsumer.global.infra.kafka.in.NewPayloadData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NewUsersPayloadData extends NewPayloadData {
    private String operation;
    private Long userIdx;
    private String name;
    private Long roleIdx;
}