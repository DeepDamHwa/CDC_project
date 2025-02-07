package com.example.cdcconsumer.domain.post.repository;

import com.example.cdcconsumer.domain.post.Post;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post,Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query(value = "SELECT p FROM Post p WHERE p.idx= :idx FOR UPDATE", nativeQuery = true)
    Optional<Post> findById(Long idx);
}
