package com.example.cdcconsumer.domain.emoji.repository;

import com.example.cdcconsumer.domain.emoji.Emoji;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface EmojiRepository extends JpaRepository<Emoji,Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//@Query(value = "SELECT e FROM Emoji e WHERE e.idx= :idx FOR UPDATE", nativeQuery = true)
    Optional<Emoji> findById(Long idx);
}
