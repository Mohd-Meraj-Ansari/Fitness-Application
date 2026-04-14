package com.fitness.aiservice.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;


@Data
public class Activity {
    private String id;
    private String userId;
    private String type;
    private int duration;
    private int caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
