package com.example.cdcconsumer.domain.interaction.repository;

import com.example.cdcconsumer.domain.interaction.model.Interaction;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction,Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query(value = "SELECT i FROM Interaction i WHERE i.idx= :idx FOR UPDATE", nativeQuery = true)
    Optional<Interaction> findById(Long idx);
}
