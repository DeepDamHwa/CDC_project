package com.example.cdcconsumer.domain.emoji.repository;

import com.example.cdcconsumer.domain.emoji.Emoji;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmojiRepository extends JpaRepository<Emoji,Long> {
}
