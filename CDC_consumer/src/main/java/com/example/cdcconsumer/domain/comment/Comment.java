package com.example.cdcconsumer.domain.comment;

import com.example.cdcconsumer.domain.interaction.model.Interaction;
import com.example.cdcconsumer.domain.post.Post;
import com.example.cdcconsumer.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String content;
    private Boolean isMine;

    @ManyToOne
    @JoinColumn(name = "post_idx")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_idx")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "comment")
    private List<Interaction> interactions = new ArrayList<>();

    //자기참조
    @Builder.Default
    @OneToMany(mappedBy = "parent")
    private List<Comment> replies = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "parent_idx")
    private Comment parent;

    public Comment(String[] logs){
        this.idx = Long.parseLong(logs[0]);
        this.content = logs[1];
        this.post = Post.builder().idx(Long.parseLong(logs[2])).build();
        this.user = User.builder().idx(Long.parseLong(logs[3])).build();
        this.parent = Comment.builder().idx(Long.parseLong(logs[4])).build();
    }
}
