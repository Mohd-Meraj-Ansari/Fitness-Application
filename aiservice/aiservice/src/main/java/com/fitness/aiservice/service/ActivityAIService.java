package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.entity.Activity;
import com.fitness.aiservice.entity.Recommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ActivityAIService {

    @Autowired
    private GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("RESPONSE FROM AI : {}", aiResponse);

        return processAiActivity(activity,aiResponse);
    }

    private Recommendation processAiActivity(Activity activity, String aiResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(aiResponse);
            JsonNode textNode = jsonNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim();

        log.info("PARSED RESPONSE FROM AI :{}",jsonContent);

            JsonNode analysisJson = objectMapper.readTree(jsonContent);
            JsonNode analisisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analisisNode, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analisisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analisisNode, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analisisNode, "caloriesBurned", "Calories Burned:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafetyGuidelines(analysisJson.path("safety"));


            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safetyMeasures(safety)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType())
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("continue with your current routine"))
                .suggestions(Collections.singletonList("consider consulting a fitness professional"))
                .safetyMeasures(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to you body"
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if(safetyNode.isArray())
        {
            safetyNode.forEach(item ->{
                safety.add(item.asText());
            });
        }
        return safety.isEmpty()? Collections.singletonList("follow general guidelines") : safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if(suggestionsNode.isArray())
        {
            suggestionsNode.forEach(suggestion ->{
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s: %s" ,workout,description));
            });
        }
        return suggestions.isEmpty()? Collections.singletonList("No specific suggestions provided") : suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if(improvementsNode.isArray())
        {
            improvementsNode.forEach(improvement ->{
                String area = improvement.path("area").asText();
                String detail = improvement.path("recommendation").asText();
                improvements.add(String.format("%s: %s" ,area,detail));
            });
        }

        return improvements.isEmpty()? Collections.singletonList("No specific improvements provided") : improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analisisNode, String key, String prefix) {
        if(!analisisNode.path(key).isMissingNode())
        {
            fullAnalysis.append(prefix)
                    .append(analisisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {

        return String.format("""
                        Analyze this fitness activity and provide detailed recommendation in the following EXACT JSON format:
                        {
                        "analysis":{
                        "overall": "overall analysis here",
                        "pace": "pace analysis here",
                        "heartRate": "heart rate analysis here",
                        "caloriesBurned": "calories analysis here"
                        },
                        "improvements":[
                             {
                                 "area": "area name",
                                 "recommendation": "detailed recommendation"
                             }
                        ],
                        "suggestions":[
                             {
                                 "workout": "workout name",
                                 "description": "detailed workout description"
                             }
                        ],
                        "safety":[
                             "safety point 1",
                             "safety point 2"
                         ]
                        }
                        
                        Analyze this activity:
                        Activity type: %s
                        Duration: %d minutes
                        Calories Burned: %d
                        Additional Metrics: %s
                        
                        provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
                        Ensure the response follows the EXACT JSON format shown above.
                        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }

}
