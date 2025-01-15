package com.example.cdcconsumer.domain.interaction.repository;

import com.example.cdcconsumer.domain.interaction.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction,Long> {

}
