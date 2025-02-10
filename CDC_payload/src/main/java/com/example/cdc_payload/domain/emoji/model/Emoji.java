package com.example.cdc_payload.domain.emoji.model;

import com.example.cdc_payload.domain.EventEntity;
import com.example.cdc_payload.domain.interaction.model.Interaction;
import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emoji{
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String name;

    @OneToMany(mappedBy = "emoji")
    private List<Interaction> interactions;

    public String logToString(){
        return idx+","+name;
    }

    public NewEmojiPayloadData toDto(String operation) {
        return NewEmojiPayloadData.builder()
                .operation(operation)
                .emojiIdx(idx)
                .name(name)
                .build();
    }
}
