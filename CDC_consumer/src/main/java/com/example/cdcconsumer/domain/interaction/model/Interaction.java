package com.example.cdcconsumer.domain.interaction.model;

import com.example.cdcconsumer.domain.comment.Comment;
import com.example.cdcconsumer.domain.emoji.Emoji;
import com.example.cdcconsumer.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

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

    //idx+","+COMMENTS.getIdx()+","+USERS.getIdx()+","+ emoji.getIdx();
    public Interaction(String[] logs){
        this.idx = Long.parseLong(logs[0]);
        this.comment = Comment.builder().idx(Long.parseLong(logs[1])).build();
        this.user = User.builder().idx(Long.parseLong(logs[2])).build();
        this.emoji = Emoji.builder().idx(Long.parseLong(logs[3])).build();
    }
}
