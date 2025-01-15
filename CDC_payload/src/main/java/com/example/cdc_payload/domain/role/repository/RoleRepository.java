package com.example.cdc_payload.domain.role.repository;

import com.example.cdc_payload.domain.interaction.model.Interaction;
import com.example.cdc_payload.domain.role.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role,Long> {
    @Query(value = "SELECT * FROM role WHERE \"ROWID\" = :rowid", nativeQuery = true)
    Role findByRowId(@Param("rowid") String rowid);
}
