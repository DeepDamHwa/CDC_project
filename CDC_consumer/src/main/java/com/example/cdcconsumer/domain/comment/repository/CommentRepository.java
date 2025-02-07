package com.example.cdcconsumer.domain.comment.repository;

import com.example.cdcconsumer.domain.comment.Comment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment,Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query(value = "SELECT c FROM Comment c WHERE c.idx= :idx FOR UPDATE", nativeQuery = true)
    Optional<Comment> findById(Long idx);
}
