package com.gestionespese.dto.goal;

import java.util.List;

public record GoalProgressDto(
    String goalId,
    Double completion,
    List<String> suggestedActions
) {
}
