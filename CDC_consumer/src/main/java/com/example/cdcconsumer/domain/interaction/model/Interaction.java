package com.example.cdcconsumer.domain.interaction.model;

import com.example.cdcconsumer.domain.comment.Comment;
import com.example.cdcconsumer.domain.emoji.Emoji;
import com.example.cdcconsumer.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {
    @Id
    private Long idx;

    @ManyToOne
    @JoinColumn(name = "comment_idx")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "user_idx")
    private User user;

    @ManyToOne
    @JoinColumn(name = "emoji_idx")
    private Emoji emoji;
}
