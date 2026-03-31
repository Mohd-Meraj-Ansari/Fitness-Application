package com.fitness.activityservice.dto;

import com.fitness.activityservice.enums.ActivityType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ActivityRequest {
    private String userId;
    private ActivityType type;
    private int duration;
    private int caloriesBurned;
    private LocalDateTime startTime;
    private Map<String,Object> additionalMetrics;

}
