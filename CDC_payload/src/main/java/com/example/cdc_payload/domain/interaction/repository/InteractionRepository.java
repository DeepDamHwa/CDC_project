package com.example.cdc_payload.domain.interaction.repository;

import com.example.cdc_payload.domain.interaction.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

//    @Query("SELECT i FROM Interaction i WHERE ROWID = :rowid")
//    Interaction findByRowId(@Param("rowid") String rowid);

//    @Query(value =
//            "SELECT * FROM interaction i "
//            + "LEFT JOIN \"USER\" u ON u.idx = i.user_idx "
//            + "LEFT JOIN \"COMMENT\" c ON c.idx = i.comment_idx "
//            + "WHERE i.ROWID = :rowid", nativeQuery = true)
//    Interaction findByRowId(@Param("rowid") String rowid);

    @Query(value = "SELECT * FROM interaction WHERE \"ROWID\" = :rowid", nativeQuery = true)
    Interaction findByRowId(@Param("rowid") String rowid);
}
