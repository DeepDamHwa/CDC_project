package com.example.cdc_payload.domain.emoji.repository;

import com.example.cdc_payload.domain.emoji.model.Emoji;
import com.example.cdc_payload.domain.interaction.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmojiRepository extends JpaRepository<Emoji, Long> {
    @Query(value = "SELECT * FROM emoji WHERE \"ROWID\" = :rowid", nativeQuery = true)
    Emoji findByRowId(@Param("rowid") String rowid);
}
