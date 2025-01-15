package com.example.cdcconsumer.domain.user.repository;

import com.example.cdcconsumer.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
