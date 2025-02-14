package com.example.cdc_payload.domain.comment.model;

import com.example.cdc_payload.domain.EventEntity;
import com.example.cdc_payload.domain.interaction.model.Interaction;
import com.example.cdc_payload.domain.post.model.Post;
import com.example.cdc_payload.domain.user.model.Users;
import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "COMMENTS")
public class Comments{
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String content;
//    private Boolean isMine;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @ManyToOne
    @JoinColumn(name = "post_idx")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_idx")
    private Users user;

    @OneToMany(mappedBy = "COMMENTS")
    private List<Interaction> interactions = new ArrayList<>();

    //자기참조
    @OneToMany(mappedBy = "parent")
    private List<Comments> replies = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "parent_idx")
    private Comments parent;

//    public NewCommentsPayloadData toDto(String operation) {
//        return NewCommentsPayloadData.builder()
//                .operation(operation)
//                .commentsIdx(idx)
////                .isMine(isMine)
//                .postIdx(post.getIdx())
//                .userIdx(user.getIdx())
//                .parentIdx(parent.getIdx())
//                .content(content)
//                .build();
//    }

    public String logToString(){
        return idx+","+content+","+post.getIdx()+","+user.getIdx()+","+parent.getIdx();
    }

//    public NewPayloadData toDto(String opteration){
//        return NewPayloadData.builder()
//                .operation(opteration)
//                .tableName("comments")
//                .log(logToString())
//                .build();
//    }
}
