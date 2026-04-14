package com.fitness.aiservice.service;

import com.fitness.aiservice.entity.Activity;
import com.fitness.aiservice.entity.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityMessageListener {

    @Autowired
    private ActivityAIService activityAIService;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity) {
        log.info("Received activity for processing {}", activity.getId());
//    log.info("Generate Recommendation: {}", activityAIService.generateRecommendation(activity));
        Recommendation recommendation = activityAIService.generateRecommendation(activity);
        log.info("FINAL OBJECT BEFORE SAVE: {}", recommendation);
        recommendationRepository.save(recommendation);

        log.info("SAVED TO DB: {}", recommendation.getId());

        Recommendation fromDb = recommendationRepository.findById(recommendation.getId()).orElse(null);
        log.info("FETCHED FROM DB: {}", fromDb);
    }
}

