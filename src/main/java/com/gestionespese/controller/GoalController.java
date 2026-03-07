package com.gestionespese.controller;

import com.gestionespese.dto.goal.*;
import com.gestionespese.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<List<GoalDto>> getGoals(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(goalService.getGoals(email));
    }

    @PostMapping
    public ResponseEntity<GoalDto> createGoal(
            Authentication authentication,
            @Valid @RequestBody GoalCreateRequest request) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoal(email, request));
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDto> getGoal(@PathVariable String goalId) {
        return ResponseEntity.ok(goalService.getGoal(goalId));
    }

    @PatchMapping("/{goalId}")
    public ResponseEntity<GoalDto> updateGoal(
            @PathVariable String goalId,
            @Valid @RequestBody GoalUpdateRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(goalId, request));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(@PathVariable String goalId) {
        goalService.deleteGoal(goalId);
        return ResponseEntity.noContent().build();
    }
}
