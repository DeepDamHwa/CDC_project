package com.example.cdc_payload.domain.comment.repository;

import com.example.cdc_payload.domain.comment.model.Comments;
import com.example.cdc_payload.domain.interaction.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    @Query(value = "SELECT * FROM comments WHERE \"ROWID\" = :rowid", nativeQuery = true)
    Comments findByRowId(@Param("rowid") String rowid);
}
