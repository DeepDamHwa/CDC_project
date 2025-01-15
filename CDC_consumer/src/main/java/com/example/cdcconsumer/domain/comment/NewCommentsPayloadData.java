package com.example.cdcconsumer.domain.comment;

import com.example.cdcconsumer.global.infra.kafka.in.NewPayloadData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentsPayloadData extends NewPayloadData {
    private String operation;
    private Long commentsIdx;
    private Boolean isMine;
    private Long postIdx;
    private Long userIdx;
    private Long parentIdx;
    private String content;
}