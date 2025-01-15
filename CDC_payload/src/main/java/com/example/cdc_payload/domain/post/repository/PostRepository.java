package com.example.cdc_payload.domain.post.repository;

import com.example.cdc_payload.domain.interaction.model.Interaction;
import com.example.cdc_payload.domain.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post,Long> {
    @Query(value = "SELECT * FROM post WHERE \"ROWID\" = :rowid", nativeQuery = true)
    Post findByRowId(@Param("rowid") String rowid);
}
