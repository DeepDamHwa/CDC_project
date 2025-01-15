package com.example.cdc_payload.domain.comment.model;

import com.example.cdc_payload.domain.interaction.model.Interaction;
import com.example.cdc_payload.domain.post.model.NewPostPayloadData;
import com.example.cdc_payload.domain.post.model.Post;
import com.example.cdc_payload.domain.user.model.Users;
import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewCommentsPayloadData{
    private String operation;
    private Long commentsIdx;
//    private Boolean isMine;
    private Long postIdx;
    private Long userIdx;
    private Long parentIdx;
    private String content;
}