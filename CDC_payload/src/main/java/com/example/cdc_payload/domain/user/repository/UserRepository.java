package com.example.cdc_payload.domain.user.repository;

import com.example.cdc_payload.domain.interaction.model.Interaction;
import com.example.cdc_payload.domain.user.model.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Users, Long> {
    @Query(value = "SELECT * FROM users WHERE \"ROWID\" = :rowid", nativeQuery = true)
    Optional<Users> findByRowId(@Param("rowid") String rowid);
}
