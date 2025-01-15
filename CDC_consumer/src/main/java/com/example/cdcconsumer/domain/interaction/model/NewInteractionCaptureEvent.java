package com.example.cdcconsumer.domain.interaction.model;

import com.example.cdcconsumer.domain.comment.Comment;
import com.example.cdcconsumer.domain.emoji.Emoji;
import com.example.cdcconsumer.domain.user.User;
import lombok.Getter;

@Getter
public class NewInteractionCaptureEvent {
    private Long idx;
    private Long commentIdx;
    private Long userIdx;
    private Long emojiIdx;

//    public Interaction toEntity(Comment comment, User user, Emoji emoji){
//        return Interaction.builder()
//                .idx(idx)
//                .comment(comment)
//                .user(user)
//                .emoji(emoji)
//                .build();
//    }

    public Interaction toEntity(){
        return Interaction.builder()
                .idx(idx)
                .comment(Comment.builder().idx(commentIdx).build())
                .user(User.builder().idx(userIdx).build())
                .emoji(Emoji.builder().idx(emojiIdx).build())
                .build();
    }


}

