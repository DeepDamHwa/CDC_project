package com.example.cdcconsumer.domain.role.repository;

import com.example.cdcconsumer.domain.role.Role;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role,Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query(value = "SELECT r FROM Role r WHERE r.idx= :idx FOR UPDATE", nativeQuery = true)
    Optional<Role> findById(Long idx);
}
