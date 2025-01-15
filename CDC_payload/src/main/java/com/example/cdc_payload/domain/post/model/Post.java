package com.example.cdc_payload.domain.post.model;

import com.example.cdc_payload.domain.EventEntity;
import com.example.cdc_payload.domain.comment.model.Comments;
import com.example.cdc_payload.domain.user.model.Users;
import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Entity
@Getter

public class Post{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;


    @ManyToOne
    @JoinColumn(name = "user_idx")
    private Users user;

    @OneToMany(mappedBy = "post")
    private List<Comments> comments = new ArrayList<>();

    public NewPostPayloadData toDto(String operation) {
        return NewPostPayloadData.builder()
                .operation(operation)
                .postIdx(idx)
                .userIdx(user.getIdx())
                .build();
    }
}
