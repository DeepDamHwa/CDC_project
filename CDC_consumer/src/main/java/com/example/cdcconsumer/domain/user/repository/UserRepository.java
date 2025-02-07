package com.example.cdcconsumer.domain.user.repository;

import com.example.cdcconsumer.domain.user.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User,Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query(value = "SELECT u FROM User u WHERE u.idx= :idx FOR UPDATE", nativeQuery = true)
    Optional<User> findById(Long idx);
}
