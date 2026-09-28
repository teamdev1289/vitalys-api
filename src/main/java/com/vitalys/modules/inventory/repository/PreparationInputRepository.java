package com.vitalys.modules.inventory.repository;

import com.vitalys.modules.inventory.entity.PreparationInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PreparationInputRepository extends JpaRepository<PreparationInput, Long> {
    List<PreparationInput> findByPreparationId(Long preparationId);
}
