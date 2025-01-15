package com.example.cdcconsumer.domain.comment.repository;

import com.example.cdcconsumer.domain.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long> {
}
