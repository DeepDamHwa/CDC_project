package com.example.cdcconsumer.domain.post;

import com.example.cdcconsumer.domain.comment.Comment;
import com.example.cdcconsumer.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @ManyToOne
    @JoinColumn(name = "user_idx")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    //433,TO_TIMESTAMP(25/02/11 01:53:38.774487,TO_TIMESTAMP(25/02/11 01:53:38.774514,59
    public Post(String[] logs){
        this.idx = Long.parseLong(logs[0]);
        this.createdAt = LocalDateTime.parse(logs[1]);
        this.modifiedAt = LocalDateTime.parse(logs[2]);
        this.user = User.builder().idx(Long.parseLong(logs[3])).build();
    }
}
