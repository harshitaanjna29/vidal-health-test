package com.example.vidal.repo;

import com.example.vidal.model.SolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolutionRepository extends JpaRepository<SolutionEntity, Long> {
}
