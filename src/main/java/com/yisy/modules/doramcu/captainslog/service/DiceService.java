package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.DiceRollResponse;
import com.yisy.modules.doramcu.captainslog.dto.OracleQueryRequest;
import com.yisy.modules.doramcu.captainslog.dto.StarshipTaskAttemptRequest;
import com.yisy.modules.doramcu.captainslog.dto.TaskAttemptRequest;
import com.yisy.modules.doramcu.captainslog.dto.TaskAttemptResponse;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogEntry;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles 2d20 dice rolling and oracle queries for the Captain's Log system.
 * <p>
 * Implements the correct Modiphius 2d20 "count successes" mechanic:
 * <ul>
 *   <li>Target Number = Attribute value + Discipline value</li>
 *   <li>Each die ≤ Target Number counts as 1 success</li>
 *   <li>A roll of 1 always counts as 2 successes</li>
 *   <li>If a Focus applies, a die ≤ Discipline value counts as 2 successes</li>
 *   <li>A roll of 20 is a complication (generates Threat), not a success</li>
 *   <li>Successes ≥ Difficulty = task succeeded</li>
 *   <li>Excess successes generate Momentum (max 1 in Captain's Log)</li>
 *   <li>Boons and threats are extra dice; keep the best 2 (no numeric modifier)</li>
 * </ul>
 * </p>
 * <p>
 * Extracted from {@link CaptainLogService} to isolate the dice mechanics
 * from character, mission, and AI logic.
 * </p>
 */
@Component
public class DiceService {

    private static final Logger log = LoggerFactory.getLogger(DiceService.class);

    private final Random random = new Random();
    private final ProbabilityMatrixService probabilityMatrixService = new ProbabilityMatrixService();
    private final AtomicInteger complicationCounter = new AtomicInteger(0);

    // ==================== 2d20 Task Attempt System (Correct Implementation) ====================

    /**
     * Performs a task attempt using the correct 2d20 "count successes" mechanic.
     * <p>
     * Algorithm:
     * <ol>
     *   <li>Calculate Target Number = Attribute + Discipline</li>
     *   <li>Roll 2d20 (plus any boon/threat dice)</li>
     *   <li>Sort all dice descending, keep the best 2</li>
     *   <li>For each of the 2 kept dice:
     *     <ul>
     *       <li>If die = 20 → complication (Threat), not a success</li>
     *       <li>If die = 1 → 2 successes (automatic)</li>
     *       <li>If die ≤ Target Number → 1 success</li>
     *       <li>If Focus applies and die ≤ Discipline value → 2 successes (instead of 1)</li>
     *       <li>Otherwise → 0 successes</li>
     *     </ul>
     *   </li>
     *   <li>Compare total successes to difficulty</li>
     *   <li>Excess successes generate Momentum (max 1)</li>
     * </ol>
     * </p>
     *
     * @param request the task attempt request with character, attribute, discipline, etc.
     * @param character the character whose stats to use
     * @return the task attempt result
     */
    public TaskAttemptResponse rollTaskAttempt(TaskAttemptRequest request, CaptainLogCharacter character) {
        log.debug("Task attempt: character={}, attribute={}, discipline={}, difficulty={}",
                character.getName(), request.getAttribute(), request.getDiscipline(), request.getDifficulty());

        // 1. Calculate Target Number = Attribute + Discipline
        int attributeValue = character.getAttributeValue(request.getAttribute());
        int disciplineRating = character.getDisciplineRating(request.getDiscipline());
        int targetNumber = attributeValue + disciplineRating;

        // 2. Check for focus bonus
        boolean hasFocus = request.getFocus() != null &&
                character.hasFocus(request.getDiscipline(), request.getFocus());

        // 3. Handle momentum/threat spending
        int boons = request.getBoons();
        int threats = request.getThreats();
        if (request.isSpendThreatForBoon() && threats > 0) {
            boons++;
            threats--;
        }

        // 4. Roll dice: 2 base + boons + threats
        int totalDice = 2 + boons + threats;
        List<Integer> allRolls = new ArrayList<>();
        for (int i = 0; i < totalDice; i++) {
            allRolls.add(random.nextInt(20) + 1);
        }

        // 5. Sort descending and keep best 2
        allRolls.sort(Collections.reverseOrder());
        List<Integer> keptDice = allRolls.subList(0, Math.min(2, allRolls.size()));

        // 6. Count successes
        int successes = 0;
        int complications = 0;
        for (int die : keptDice) {
            if (die == 20) {
                // Natural 20 = complication, not a success
                complications++;
            } else if (die == 1) {
                // Natural 1 = 2 successes
                successes += 2;
            } else if (hasFocus && die <= disciplineRating) {
                // Focus bonus: die ≤ discipline value = 2 successes
                successes += 2;
            } else if (die <= targetNumber) {
                // Normal success
                successes++;
            }
            // Otherwise: 0 successes
        }

        // 7. Handle momentum reroll
        if (request.isSpendMomentumReroll()) {
            // Reroll the worst die
            int worstIndex = keptDice.size() - 1;
            int reroll = random.nextInt(20) + 1;
            log.debug("Momentum reroll: die {} → {}", keptDice.get(worstIndex), reroll);
            // Recalculate with reroll
            successes = 0;
            complications = 0;
            List<Integer> finalDice = new ArrayList<>(keptDice);
            finalDice.set(worstIndex, reroll);
            for (int die : finalDice) {
                if (die == 20) {
                    complications++;
                } else if (die == 1) {
                    successes += 2;
                } else if (hasFocus && die <= disciplineRating) {
                    successes += 2;
                } else if (die <= targetNumber) {
                    successes++;
                }
            }
            keptDice = finalDice;
        }

        // 8. Determine success
        boolean success = successes >= request.getDifficulty();

        // 9. Calculate momentum (excess successes, max 1 in Captain's Log)
        int momentumGenerated = 0;
        if (success) {
            momentumGenerated = Math.min(1, successes - request.getDifficulty());
        }

        // 10. Generate Threat on zero-success failure (per official rules)
        int threatFromFailure = (!success && successes == 0) ? 1 : 0;
        int totalThreat = complications + threatFromFailure;

        // 11. Build response
        TaskAttemptResponse response = new TaskAttemptResponse();
        response.setTargetNumber(targetNumber);
        response.setDifficulty(request.getDifficulty());
        response.setDice(keptDice);
        response.setBoons(boons);
        response.setThreats(threats);
        response.setSuccesses(successes);
        response.setSuccess(success);
        response.setMomentumGenerated(momentumGenerated);
        response.setThreatGenerated(totalThreat);
        response.setComplication(totalThreat > 0);

        // 12. Determine complication description
        String complicationDescription = null;
        if (complications > 0) {
            // Roll on the complication table for a narrative complication
            ProbabilityMatrixService.Complication complication = probabilityMatrixService.rollComplication();
            complicationDescription = complication.getDescription();
        } else if (threatFromFailure > 0) {
            complicationDescription = "Failure generates Threat: the situation worsens as the task fails completely.";
        }
        response.setComplicationDescription(complicationDescription);

        // 13. Build description
        StringBuilder desc = new StringBuilder();
        if (complications > 0) {
            desc.append("Complication! ").append(complicationDescription).append(" ");
        }
        if (success) {
            if (successes >= request.getDifficulty() * 2) {
                desc.append("Spectacular success with ").append(successes).append(" successes!");
            } else {
                desc.append("Success with ").append(successes).append(" successes.");
            }
        } else {
            desc.append("Failure with only ").append(successes).append(" successes (needed ").append(request.getDifficulty()).append(").");
            if (threatFromFailure > 0) {
                desc.append(" Generates 1 Threat from complete failure.");
            }
        }
        if (momentumGenerated > 0) {
            desc.append(" Generates 1 Momentum.");
        }
        if (complications > 0) {
            desc.append(" Generates ").append(complications).append(" Threat from complication.");
        }

        response.setDescription(desc.toString());
        log.debug("Task attempt result: {}", response);
        return response;
    }

    // ==================== Legacy 2d20 Roll (Backward Compatible) ====================

    /**
     * Performs a simple 2d20 roll for backward compatibility.
     * <p>
     * This method uses the correct 2d20 under/over mechanic:
     * each die is compared individually to the target number.
     * A die ≤ target number counts as a success.
     * A die = 20 is a complication.
     * A die = 1 counts as 2 successes.
     * </p>
     *
     * @param targetNumber the target number (attribute + discipline)
     * @param boons        number of boon dice (extra dice, keep best 2)
     * @param threats      number of threat dice (extra dice, keep best 2)
     * @return the dice roll result
     */
    public DiceRollResponse rollSkillCheck(int targetNumber, int boons, int threats) {
        log.debug("Rolling 2d20: target={}, boons={}, threats={}", targetNumber, boons, threats);

        // Roll 2 + boons + threats dice, keep best 2
        int totalDice = 2 + boons + threats;
        List<Integer> allRolls = new ArrayList<>();
        for (int i = 0; i < totalDice; i++) {
            allRolls.add(random.nextInt(20) + 1);
        }
        allRolls.sort(Collections.reverseOrder());
        int die1 = allRolls.get(0);
        int die2 = allRolls.get(1);

        // Count successes (correct 2d20 mechanic)
        int successes = 0;
        int complications = 0;
        for (int die : List.of(die1, die2)) {
            if (die == 20) {
                complications++;
            } else if (die == 1) {
                successes += 2;
            } else if (die <= targetNumber) {
                successes++;
            }
        }

        // Default difficulty 1
        boolean success = successes >= 1;

        // Build description
        String description;
        if (complications > 0 && !success) {
            description = "Failure with complication. The check fails and something goes wrong.";
        } else if (complications > 0) {
            description = "Success with complication. The check succeeds but something unexpected happens.";
        } else if (successes >= 2) {
            description = "Strong success! The check succeeds with room to spare.";
        } else if (success) {
            description = "Success. The check meets the target.";
        } else {
            description = "Failure. The check does not meet the target.";
        }

        DiceRollResponse response = new DiceRollResponse();
        response.setDie1(die1);
        response.setDie2(die2);
        response.setTotal(successes); // total now represents successes, not sum
        response.setTargetNumber(targetNumber);
        response.setBoons(boons);
        response.setThreats(threats);
        response.setModifier(0); // no numeric modifier in correct 2d20
        response.setSuccess(success);
        response.setCriticalSuccess(successes >= 2); // 2+ successes = strong success
        response.setCriticalFailure(complications > 0 && !success);
        response.setDescription(description);

        return response;
    }

    /**
     * Performs a simple 2d20 roll with default target number.
     */
    public DiceRollResponse rollSkillCheck() {
        return rollSkillCheck(11, 0, 0);
    }

    // ==================== Oracle System ====================

    /**
     * Queries the oracle with a yes/no question.
     * <p>
     * Uses the 2d20 oracle table:
     * 2-7: No, 8-9: No (Minus), 10-11: Both/Neither,
     * 12-13: Yes (Plus), 14-19: Yes, 20: Yes (Critical)
     * </p>
     */
    public DiceRollResponse queryOracle(OracleQueryRequest request) {
        log.info("Oracle query: {}", request.getQuestion());

        // Roll 2d20 for oracle
        int d1 = random.nextInt(20) + 1;
        int d2 = random.nextInt(20) + 1;
        int total = d1 + d2;

        CaptainLogEntry.OracleAnswer answer;
        String description;

        if (total <= 7) {
            answer = CaptainLogEntry.OracleAnswer.NO;
            description = "No. The answer is clear: it is not so.";
        } else if (total <= 9) {
            answer = CaptainLogEntry.OracleAnswer.NO_MINUS;
            description = "No, but with a complication. Something unexpected happens.";
        } else if (total <= 11) {
            if (request.isAllowBothNeither()) {
                answer = CaptainLogEntry.OracleAnswer.BOTH;
                description = "Both/Neither. The situation is more complex than expected.";
            } else {
                answer = CaptainLogEntry.OracleAnswer.NO;
                description = "No. The answer leans negative.";
            }
        } else if (total <= 13) {
            answer = CaptainLogEntry.OracleAnswer.YES_PLUS;
            description = "Yes, and with a bonus. Something favorable occurs.";
        } else if (total <= 19) {
            answer = CaptainLogEntry.OracleAnswer.YES;
            description = "Yes. The answer is clear: it is so.";
        } else {
            answer = CaptainLogEntry.OracleAnswer.YES_PLUS;
            description = "Critical Yes! An extraordinary and favorable outcome.";
        }

        DiceRollResponse response = new DiceRollResponse();
        response.setDie1(d1);
        response.setDie2(d2);
        response.setTotal(total);
        response.setTargetNumber(0);
        response.setBoons(0);
        response.setThreats(0);
        response.setModifier(0);
        response.setSuccess(answer == CaptainLogEntry.OracleAnswer.YES ||
                           answer == CaptainLogEntry.OracleAnswer.YES_PLUS);
        response.setCriticalSuccess(total == 20);
        response.setCriticalFailure(total == 2);
        response.setOracleAnswer(answer);
        response.setDescription(description);

        return response;
    }

    /**
     * Rolls on a random table (for prompt cards, encounters, etc.).
     *
     * @param tableSize the size of the table (e.g., 6 for d6, 10 for d10, 20 for d20)
     * @return the result (1-based index)
     */
    public int rollTable(int tableSize) {
        return random.nextInt(tableSize) + 1;
    }

    // ==================== Starship Task Attempt System ====================

    /**
     * Performs a starship system task attempt using the correct 2d20 "count successes" mechanic.
     * <p>
     * For starships, the Target Number = System rating + Department rating.
     * This reuses the core 2d20 dice logic from {@link #rollTaskAttempt(TaskAttemptRequest, CaptainLogCharacter)}.
     * </p>
     *
     * @param request the starship task attempt request
     * @param starship the starship whose stats to use
     * @return the task attempt result
     */
    public TaskAttemptResponse rollStarshipTaskAttempt(StarshipTaskAttemptRequest request, CaptainLogStarship starship) {
        log.debug("Starship task attempt: ship={}, system={}, department={}, difficulty={}",
                starship.getName(), request.getSystem(), request.getDepartment(), request.getDifficulty());

        // 1. Calculate Target Number = System + Department
        int systemRating = starship.getSystemRating(request.getSystem());
        int departmentRating = starship.getDepartmentRating(request.getDepartment());
        int targetNumber = systemRating + departmentRating;

        // 2. Handle momentum/threat spending
        int boons = request.getBoons();
        int threats = request.getThreats();
        if (request.isSpendThreatForBoon() && threats > 0) {
            boons++;
            threats--;
        }

        // 3. Roll dice: 2 base + boons + threats
        int totalDice = 2 + boons + threats;
        List<Integer> allRolls = new ArrayList<>();
        for (int i = 0; i < totalDice; i++) {
            allRolls.add(random.nextInt(20) + 1);
        }

        // 4. Sort descending and keep best 2
        allRolls.sort(Collections.reverseOrder());
        List<Integer> keptDice = allRolls.subList(0, Math.min(2, allRolls.size()));

        // 5. Count successes (no focus bonus for starships)
        int successes = 0;
        int complications = 0;
        for (int die : keptDice) {
            if (die == 20) {
                complications++;
            } else if (die == 1) {
                successes += 2;
            } else if (die <= targetNumber) {
                successes++;
            }
        }

        // 6. Handle momentum reroll
        if (request.isSpendMomentumReroll()) {
            int worstIndex = keptDice.size() - 1;
            int reroll = random.nextInt(20) + 1;
            log.debug("Momentum reroll: die {} → {}", keptDice.get(worstIndex), reroll);
            successes = 0;
            complications = 0;
            List<Integer> finalDice = new ArrayList<>(keptDice);
            finalDice.set(worstIndex, reroll);
            for (int die : finalDice) {
                if (die == 20) {
                    complications++;
                } else if (die == 1) {
                    successes += 2;
                } else if (die <= targetNumber) {
                    successes++;
                }
            }
            keptDice = finalDice;
        }

        // 7. Determine success
        boolean success = successes >= request.getDifficulty();

        // 8. Calculate momentum (excess successes, max 1 in Captain's Log)
        int momentumGenerated = 0;
        if (success) {
            momentumGenerated = Math.min(1, successes - request.getDifficulty());
        }

        // 9. Generate Threat on zero-success failure (per official rules)
        int threatFromFailure = (!success && successes == 0) ? 1 : 0;
        int totalThreat = complications + threatFromFailure;

        // 10. Apply stress on failure
        if (!success && request.isApplyStressOnFailure()) {
            starship.addSystemStress(request.getSystem(), 1);
            log.debug("Starship {} system {} took 1 stress (now {})",
                    starship.getName(), request.getSystem(), starship.getSystemStress(request.getSystem()));
        }

        // 11. Build response
        TaskAttemptResponse response = new TaskAttemptResponse();
        response.setTargetNumber(targetNumber);
        response.setDifficulty(request.getDifficulty());
        response.setDice(new ArrayList<>(keptDice));
        response.setBoons(boons);
        response.setThreats(threats);
        response.setSuccesses(successes);
        response.setSuccess(success);
        response.setMomentumGenerated(momentumGenerated);
        response.setThreatGenerated(totalThreat);
        response.setComplication(totalThreat > 0);

        // 12. Determine complication description
        String complicationDescription = null;
        if (complications > 0) {
            // Roll on the complication table for a narrative complication
            ProbabilityMatrixService.Complication complication = probabilityMatrixService.rollComplication();
            complicationDescription = "Starship " + request.getSystem().getDisplayName().toLowerCase() +
                    " complication: " + complication.getDescription().toLowerCase();
        } else if (threatFromFailure > 0) {
            complicationDescription = "Starship failure generates Threat: the " +
                    request.getSystem().getDisplayName().toLowerCase() + " systems fail to perform.";
        }
        response.setComplicationDescription(complicationDescription);

        // 13. Build description
        StringBuilder desc = new StringBuilder();
        desc.append("Starship ").append(starship.getName()).append(" — ")
                .append(request.getSystem().getDisplayName()).append(" + ")
                .append(request.getDepartment().getDisplayName());
        if (complications > 0) {
            desc.append(". Complication! ").append(complicationDescription).append(" ");
        }
        if (success) {
            desc.append(": Success with ").append(successes).append(" successes.");
        } else {
            desc.append(": Failure with only ").append(successes)
                    .append(" successes (needed ").append(request.getDifficulty()).append("). ");
            if (request.isApplyStressOnFailure()) {
                desc.append("System takes 1 stress. ");
            }
            if (threatFromFailure > 0) {
                desc.append("Generates 1 Threat from complete failure.");
            }
        }
        if (momentumGenerated > 0) {
            desc.append(" Generates 1 Momentum.");
        }
        if (complications > 0) {
            desc.append(" Generates ").append(complications).append(" Threat from complication.");
        }

        response.setDescription(desc.toString());
        log.debug("Starship task attempt result: {}", response);
        return response;
    }

    /**
     * Performs a direct 2d20 task attempt without requiring a character object.
     * <p>
     * Used by services like StarshipCombatService that need raw dice rolls
     * with a specified target number, without looking up character stats.
     * </p>
     *
     * @param targetNumber the target number for the roll
     * @param difficulty   the number of successes needed
     * @param focus        optional focus name (ignored for direct rolls, but kept for API consistency)
     * @param boons        number of boon dice
     * @param threats      number of threat dice
     * @param isShipTask   whether this is a starship task (affects complication descriptions)
     * @return the task attempt result
     */
    public TaskAttemptResponse rollTaskAttemptDirect(int targetNumber, int difficulty,
                                                      String focus, int boons, int threats,
                                                      boolean isShipTask) {
        log.debug("Direct task attempt: target={}, difficulty={}, boons={}, threats={}",
                targetNumber, difficulty, boons, threats);

        // Roll dice: 2 base + boons + threats
        int totalDice = 2 + boons + threats;
        List<Integer> allRolls = new ArrayList<>();
        for (int i = 0; i < totalDice; i++) {
            allRolls.add(random.nextInt(20) + 1);
        }

        // Sort descending and keep best 2
        allRolls.sort(Collections.reverseOrder());
        List<Integer> keptDice = allRolls.subList(0, Math.min(2, allRolls.size()));

        // Count successes
        int successes = 0;
        int complications = 0;
        for (int die : keptDice) {
            if (die == 20) {
                complications++;
            } else if (die == 1) {
                successes += 2;
            } else if (die <= targetNumber) {
                successes++;
            }
        }

        // Determine success
        boolean success = successes >= difficulty;

        // Calculate momentum (excess successes, max 1)
        int momentumGenerated = 0;
        if (success) {
            momentumGenerated = Math.min(1, successes - difficulty);
        }

        // Generate Threat on zero-success failure
        int threatFromFailure = (!success && successes == 0) ? 1 : 0;
        int totalThreat = complications + threatFromFailure;

        // Build response
        TaskAttemptResponse response = new TaskAttemptResponse();
        response.setTargetNumber(targetNumber);
        response.setDifficulty(difficulty);
        response.setDice(new ArrayList<>(keptDice));
        response.setBoons(boons);
        response.setThreats(threats);
        response.setSuccesses(successes);
        response.setSuccess(success);
        response.setMomentumGenerated(momentumGenerated);
        response.setThreatGenerated(totalThreat);
        response.setComplication(totalThreat > 0);

        // Complication description
        String complicationDescription = null;
        if (complications > 0) {
            ProbabilityMatrixService.Complication complication = probabilityMatrixService.rollComplication();
            complicationDescription = isShipTask
                    ? "Starship complication: " + complication.getDescription().toLowerCase()
                    : complication.getDescription();
        } else if (threatFromFailure > 0) {
            complicationDescription = "Failure generates Threat: the situation worsens.";
        }
        response.setComplicationDescription(complicationDescription);

        // Description
        StringBuilder desc = new StringBuilder();
        if (complications > 0) {
            desc.append("Complication! ").append(complicationDescription).append(" ");
        }
        if (success) {
            desc.append("Success with ").append(successes).append(" successes.");
        } else {
            desc.append("Failure with ").append(successes).append(" successes (needed ").append(difficulty).append(").");
        }
        if (momentumGenerated > 0) {
            desc.append(" Generates 1 Momentum.");
        }
        response.setDescription(desc.toString());

        return response;
    }

    /**
     * Generates a random prompt card for mission inspiration.
     */
    public String generatePromptCard() {
        String[] prompts = {
            "A distress signal leads to an unexpected discovery.",
            "First contact with a species that defies classification.",
            "A political crisis threatens to escalate into war.",
            "An ancient artifact holds the key to a long-lost technology.",
            "A crew member's secret is revealed at the worst moment.",
            "A spatial anomaly creates a time dilation effect.",
            "A rival faction competes for the same objective.",
            "A moral dilemma challenges the crew's values.",
            "A seemingly routine mission hides a deeper conspiracy.",
            "A natural disaster threatens a nearby colony.",
            "An old enemy resurfaces with a new threat.",
            "A diplomatic mission requires unusual tactics.",
            "A scientific discovery has dangerous implications.",
            "A lost starship is found with its crew intact.",
            "A planet's ecosystem is collapsing — can it be saved?",
            "A cyber attack targets the ship's computer systems.",
            "A refugee fleet seeks asylum in Federation space.",
            "A wormhole opens to an uncharted region.",
            "A saboteur is among the crew.",
            "A race against time to prevent a catastrophe."
        };
        return prompts[random.nextInt(prompts.length)];
    }
}
