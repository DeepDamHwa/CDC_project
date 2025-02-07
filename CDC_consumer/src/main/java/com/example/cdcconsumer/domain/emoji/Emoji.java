package com.example.cdcconsumer.domain.emoji;

import com.example.cdcconsumer.domain.interaction.model.Interaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emoji {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String name;

    @OneToMany(mappedBy = "emoji")
    private List<Interaction> interactions;
}
