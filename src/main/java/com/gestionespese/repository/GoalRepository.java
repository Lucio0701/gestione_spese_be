package com.gestionespese.repository;

import com.gestionespese.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, String> {
    List<Goal> findByUserId(Long userId);
}
