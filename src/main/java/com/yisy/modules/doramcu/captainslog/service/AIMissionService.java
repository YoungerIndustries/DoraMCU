package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.GenerateMissionRequest;
import com.yisy.modules.doramcu.captainslog.dto.GeneratedMissionResponse;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogFocus;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogValue;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogMissionRepository;
import com.yisy.modules.doraai.service.AiProvider;
import com.yisy.modules.doraai.service.AiProviderFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles AI-based mission generation for the Captain's Log system.
 * <p>
 * Extracted from {@link CaptainLogService} to isolate AI interaction,
 * prompt building, and JSON parsing from the core service logic.
 * </p>
 */
@Component
public class AIMissionService {

    private static final Logger log = LoggerFactory.getLogger(AIMissionService.class);

    private final CaptainLogCharacterRepository characterRepository;
    private final CaptainLogMissionRepository missionRepository;
    private final AiProviderFactory aiProviderFactory;

    public AIMissionService(CaptainLogCharacterRepository characterRepository,
                            CaptainLogMissionRepository missionRepository,
                            AiProviderFactory aiProviderFactory) {
        this.characterRepository = characterRepository;
        this.missionRepository = missionRepository;
        this.aiProviderFactory = aiProviderFactory;
    }

    /**
     * Uses AI to generate a complete mission for a character.
     * <p>
     * The AI considers the character's profile (rank, assignment, attributes, disciplines,
     * values, focuses), mission history, and any optional theme or type constraints to create
     * a rich 3-act mission with narrative prompts, skill checks, and oracle questions.
     * </p>
     *
     * @param request the generation request containing character ID and optional constraints
     * @return the AI-generated mission proposal
     */
    @Transactional(readOnly = true)
    public GeneratedMissionResponse generateMissionWithAI(GenerateMissionRequest request) {
        log.info("Generating AI mission for character {} with type={} theme={}",
                request.getCharacterId(), request.getType(), request.getTheme());

        // Fetch character
        CaptainLogCharacter character = characterRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + request.getCharacterId()));

        // Force lazy initialization of collections while still in transaction
        if (character.getFocuses() != null) character.getFocuses().size();
        if (character.getValues() != null) character.getValues().size();

        // Fetch mission history
        List<CaptainLogMission> missionHistory = missionRepository
                .findByCharacterIdOrderByMissionNumberAsc(request.getCharacterId());

        // Build the prompt
        String prompt = buildMissionPrompt(character, missionHistory, request);

        // Select AI provider
        AiProvider provider = aiProviderFactory.getProviderWithFallback(request.getProvider());

        // Call the AI
        List<AiProvider.Message> messages = Collections.singletonList(
                new AiProvider.Message("user", prompt));
        String aiResponse = provider.chat(null, messages);
        log.debug("AI raw response (first 500 chars): {}",
                aiResponse != null && aiResponse.length() > 500 ? aiResponse.substring(0, 500) + "..." : aiResponse);

        // Parse the AI response into a structured mission
        GeneratedMissionResponse response = parseGeneratedMission(aiResponse, request, provider.getProviderId());
        log.info("AI generated mission: '{}' (type: {})", response.getName(), response.getType());
        return response;
    }

    /**
     * Builds a detailed prompt for AI mission generation based on the character's profile.
     */
    String buildMissionPrompt(CaptainLogCharacter character,
                              List<CaptainLogMission> missionHistory,
                              GenerateMissionRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a mission generator for 'Captain's Log: Solo Roleplaying Game', ");
        sb.append("a solo RPG based on the Star Trek Adventures / Modiphius 2d20 system.\n\n");

        // Character profile
        sb.append("=== CHARACTER PROFILE ===\n");
        sb.append("Name: ").append(character.getName()).append("\n");
        sb.append("Rank: ").append(character.getRank()).append("\n");
        sb.append("Assignment: ").append(character.getAssignment()).append("\n");
        sb.append("Environment: ").append(character.getEnvironment()).append("\n");
        sb.append("Outlook: ").append(character.getOutlook()).append("\n");
        sb.append("Education: ").append(character.getEducation()).append("\n");

        if (character.getTrait1() != null && !character.getTrait1().isBlank()) {
            sb.append("Traits: ");
            List<String> traits = new ArrayList<>();
            if (character.getTrait1() != null && !character.getTrait1().isBlank()) traits.add(character.getTrait1());
            if (character.getTrait2() != null && !character.getTrait2().isBlank()) traits.add(character.getTrait2());
            if (character.getTrait3() != null && !character.getTrait3().isBlank()) traits.add(character.getTrait3());
            sb.append(String.join(", ", traits)).append("\n");
        }

        sb.append("\nAttributes:\n");
        sb.append("  Fitness: ").append(character.getFitness()).append("\n");
        sb.append("  Technical: ").append(character.getTechnical()).append("\n");
        sb.append("  Strength: ").append(character.getStrength()).append("\n");
        sb.append("  Reason: ").append(character.getReason()).append("\n");
        sb.append("  Insight: ").append(character.getInsight()).append("\n");
        sb.append("  Presence: ").append(character.getPresence()).append("\n");

        sb.append("\nDisciplines:\n");
        sb.append("  Command: ").append(character.getCommand()).append("\n");
        sb.append("  Leadership: ").append(character.getLeadership()).append("\n");
        sb.append("  Science: ").append(character.getScience()).append("\n");
        sb.append("  Security: ").append(character.getSecurity()).append("\n");
        sb.append("  Medicine: ").append(character.getMedicine()).append("\n");
        sb.append("  Operations: ").append(character.getOperations()).append("\n");

        if (!character.getFocuses().isEmpty()) {
            sb.append("\nFocuses:\n");
            for (CaptainLogFocus focus : character.getFocuses()) {
                sb.append("  ").append(focus.getDiscipline()).append(": ").append(focus.getName()).append("\n");
            }
        }

        if (!character.getValues().isEmpty()) {
            sb.append("\nValues:\n");
            for (CaptainLogValue value : character.getValues()) {
                sb.append("  - ").append(value.getDescription()).append("\n");
            }
        }

        // Mission history
        if (!missionHistory.isEmpty()) {
            sb.append("\n=== MISSION HISTORY ===\n");
            for (CaptainLogMission mission : missionHistory) {
                sb.append("Mission #").append(mission.getMissionNumber()).append(": ")
                        .append(mission.getName());
                if (mission.getType() != null) {
                    sb.append(" (").append(mission.getType()).append(")");
                }
                sb.append(" - Status: ").append(mission.getStatus());
                if (mission.getSummary() != null && !mission.getSummary().isBlank()) {
                    sb.append(" - Summary: ").append(mission.getSummary());
                }
                sb.append("\n");
            }
        }

        // Constraints
        sb.append("\n=== MISSION CONSTRAINTS ===\n");
        int nextMission = missionHistory.size() + 1;
        sb.append("Next mission number: ").append(nextMission).append("\n");

        if (request.getType() != null) {
            sb.append("Mission type: ").append(request.getType()).append("\n");
        } else {
            sb.append("Mission type: Choose any type that fits the character's profile and story arc.\n");
        }

        if (request.getTheme() != null && !request.getTheme().isBlank()) {
            sb.append("Theme: ").append(request.getTheme()).append("\n");
        }

        if (request.getPrompt() != null && !request.getPrompt().isBlank()) {
            sb.append("Additional context: ").append(request.getPrompt()).append("\n");
        }

        // Output format
        sb.append("\n=== OUTPUT FORMAT ===\n");
        sb.append("Generate a complete mission and return it in the following JSON format:\n\n");
        sb.append("{\n");
        sb.append("  \"name\": \"Mission name (creative, Star Trek-style)\",\n");
        sb.append("  \"type\": \"One of: EXPLORATION, DIPLOMACY, COMBAT, SCIENCE, RESCUE, INVESTIGATION, TRADE, FIRST_CONTACT, CRISIS, PERSONAL\",\n");
        sb.append("  \"synopsis\": \"1-2 sentence mission overview\",\n");
        sb.append("  \"act1Prompt\": \"Act 1 (Opening Log): The mission hook and initial situation. Describe the scenario that draws the character into this mission. Include 1-2 specific details that set the scene.\",\n");
        sb.append("  \"act2Prompt\": \"Act 2 (Supplementary Logs): The rising action and complications. Introduce unexpected challenges, moral dilemmas, or twists that test the character's abilities and values.\",\n");
        sb.append("  \"act3Prompt\": \"Act 3 (Closing Log): The resolution and aftermath. How does the mission conclude? What are the consequences? What has the character learned or changed?\",\n");
        sb.append("  \"skillChecks\": \"Suggested skill checks in format: DISCIPLINE (Target Number) - Description. Provide 3-5 checks spread across the acts, using the character's strongest disciplines where appropriate.\",\n");
        sb.append("  \"oracleQuestions\": \"Suggested yes/no oracle questions to ask during the mission. Provide 3-5 questions that drive the narrative forward.\",\n");
        sb.append("  \"keyCharacters\": \"Key NPCs or entities involved. List 2-3 characters with brief descriptions.\",\n");
        sb.append("  \"valuesAtStake\": \"Which character values might be used or challenged during this mission. Reference the character's values where possible.\",\n");
        sb.append("}\n\n");

        sb.append("IMPORTANT: Return ONLY valid JSON. Do not include markdown code fences, explanations, or any text outside the JSON object.\n");
        sb.append("Make the mission engaging, thematically appropriate for Star Trek Adventures, and tailored to this character's unique profile.\n");
        sb.append("Consider the character's assignment, strongest disciplines, and values when designing challenges and opportunities.\n");

        return sb.toString();
    }

    /**
     * Parses the AI's JSON response into a GeneratedMissionResponse using Jackson.
     * Falls back to a simple text-based mission if JSON parsing fails.
     */
    GeneratedMissionResponse parseGeneratedMission(String aiResponse,
                                                   GenerateMissionRequest request,
                                                   String providerId) {
        GeneratedMissionResponse response = new GeneratedMissionResponse();
        response.setProvider(providerId);

        // Clean the response: strip markdown fences and text preamble
        String cleaned = aiResponse.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) cleaned = cleaned.substring(firstNewline + 1);
            int lastBacktick = cleaned.lastIndexOf("```");
            if (lastBacktick > 0) cleaned = cleaned.substring(0, lastBacktick);
            cleaned = cleaned.trim();
        }
        int jsonStart = cleaned.indexOf('{');
        int jsonEnd = cleaned.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(cleaned);

            JsonNode node;
            node = root.get("name");
            response.setName(node != null && !node.isNull() ? node.asText() : null);
            node = root.get("synopsis");
            response.setSynopsis(node != null && !node.isNull() ? node.asText() : null);
            node = root.get("act1Prompt");
            response.setAct1Prompt(node != null && !node.isNull() ? node.asText() : null);
            node = root.get("act2Prompt");
            response.setAct2Prompt(node != null && !node.isNull() ? node.asText() : null);
            node = root.get("act3Prompt");
            response.setAct3Prompt(node != null && !node.isNull() ? node.asText() : null);
            node = root.get("skillChecks");
            response.setSkillChecks(node != null && !node.isNull() ? node.asText() : null);
            node = root.get("oracleQuestions");
            response.setOracleQuestions(node != null && !node.isNull() ? node.asText() : null);
            node = root.get("keyCharacters");
            response.setKeyCharacters(node != null && !node.isNull() ? node.asText() : null);
            node = root.get("valuesAtStake");
            response.setValuesAtStake(node != null && !node.isNull() ? node.asText() : null);

            node = root.get("type");
            if (node != null && !node.isNull()) {
                try {
                    response.setType(CaptainLogMission.MissionType.valueOf(node.asText().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown mission type from AI: {}", node.asText());
                    response.setType(request.getType() != null ? request.getType() :
                            CaptainLogMission.MissionType.EXPLORATION);
                }
            } else if (request.getType() != null) {
                response.setType(request.getType());
            } else {
                response.setType(CaptainLogMission.MissionType.EXPLORATION);
            }

            if (response.getName() == null || response.getName().isBlank()) {
                response.setName("Mission " + (request.getCharacterId() != null ? request.getCharacterId() : "?"));
            }

        } catch (Exception e) {
            log.warn("Failed to parse AI mission response, using raw text", e);
            response.setName("AI-Generated Mission");
            response.setType(request.getType() != null ? request.getType() :
                    CaptainLogMission.MissionType.EXPLORATION);
            response.setSynopsis(aiResponse.length() > 200 ? aiResponse.substring(0, 200) + "..." : aiResponse);
            response.setAct1Prompt(aiResponse);
        }

        return response;
    }
}
