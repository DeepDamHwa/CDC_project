package com.example.cdc_payload.domain.interaction.model;

import com.example.cdc_payload.domain.EventEntity;
import com.example.cdc_payload.domain.comment.model.Comments;
import com.example.cdc_payload.domain.emoji.model.Emoji;
import com.example.cdc_payload.domain.user.model.Users;
import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction{
    @Id
    private Long idx;

    @ManyToOne
    @JoinColumn(name = "comment_idx")
    private Comments COMMENTS;

    @ManyToOne
    @JoinColumn(name = "user_idx")
    private Users USERS;

    @ManyToOne
    @JoinColumn(name = "emoji_idx")
    private Emoji emoji;

    public NewInteractionPayloadData toDto(String operation) {
        return NewInteractionPayloadData.builder()
                .operation(operation)
                .interactionIdx(idx)
                .commentIdx(COMMENTS.getIdx())
                .userIdx(USERS.getIdx())
                .emojiIdx(emoji.getIdx())
                .build();
    }

    public String logToString() {
        return idx+","+COMMENTS.getIdx()+","+USERS.getIdx()+","+ emoji.getIdx();
    }
}