package com.gestionespese.service;

import com.gestionespese.dto.goal.*;
import com.gestionespese.model.Goal;
import com.gestionespese.model.User;
import com.gestionespese.repository.GoalRepository;
import com.gestionespese.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalService(GoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    public List<GoalDto> getGoals(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return goalRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public GoalDto createGoal(String email, GoalCreateRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Goal goal = new Goal(
                user,
                request.title(),
                request.targetAmount(),
                request.deadline());
        return mapToDto(goalRepository.save(goal));
    }

    public GoalDto updateGoal(String id, GoalUpdateRequest request) {
        Goal goal = goalRepository.findById(id).orElseThrow();
        if (request.title() != null)
            goal.setName(request.title());
        if (request.targetAmount() != null)
            goal.setTargetAmount(request.targetAmount());
        if (request.currentAmount() != null)
            goal.setCurrentAmount(request.currentAmount());
        if (request.deadline() != null)
            goal.setDeadline(request.deadline());
        return mapToDto(goalRepository.save(goal));
    }

    public void deleteGoal(String id) {
        goalRepository.deleteById(id);
    }

    public GoalDto getGoal(String id) {
        return mapToDto(goalRepository.findById(id).orElseThrow());
    }

    private GoalDto mapToDto(Goal g) {
        return new GoalDto(
                g.getId(),
                g.getUser().getId().toString(),
                g.getName(),
                g.getTargetAmount(),
                g.getCurrentAmount(),
                g.getDeadline(),
                null // CreatedAt
        );
    }
}
