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

    @OneToMany(mappedBy = "comment")
    private List<Interaction> interactions = new ArrayList<>();

    //자기참조
    @OneToMany(mappedBy = "parent")
    private List<Comment> replies = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "parent_idx")
    private Comment parent;
}
