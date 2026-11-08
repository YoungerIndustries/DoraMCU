package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

import java.util.Map;

/**
 * Represents a single choice in a lifepath step.
 * <p>
 * Each choice has a narrative description and determines the character's
 * attributes, disciplines, background traits, and other properties.
 * </p>
 */
public class LifepathChoice {

    /** Unique identifier for this choice within the step. */
    private String id;

    /** Short title of the choice. */
    private String title;

    /** Narrative description of what this choice means. */
    private String description;

    /** Attribute bonuses granted by this choice (attribute name -> bonus value). */
    private Map<CaptainLogCharacter.Attribute, Integer> attributeBonuses;

    /** Discipline bonuses granted by this choice (discipline name -> bonus value). */
    private Map<CaptainLogCharacter.Discipline, Integer> disciplineBonuses;

    /** Background trait set by this choice (if applicable). */
    private String backgroundTrait;

    /** Environment set by this choice (if applicable). */
    private CaptainLogCharacter.Environment environment;

    /** Outlook set by this choice (if applicable). */
    private CaptainLogCharacter.Outlook outlook;

    /** Education set by this choice (if applicable). */
    private CaptainLogCharacter.Education education;

    /** Assignment set by this choice (if applicable). */
    private CaptainLogCharacter.Assignment assignment;

    /** Rank set by this choice (if applicable). */
    private CaptainLogCharacter.Rank rank;

    /** Focus name added by this choice (if applicable). */
    private String focusName;

    /** Focus discipline for the focus (if applicable). */
    private CaptainLogCharacter.Discipline focusDiscipline;

    /** Value description added by this choice (if applicable). */
    private String valueDescription;

    public LifepathChoice() {
    }

    public LifepathChoice(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    /** Constructor with attribute bonuses only (used for species selection). */
    public LifepathChoice(String id, String title, String description,
                          Map<CaptainLogCharacter.Attribute, Integer> attributeBonuses) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.attributeBonuses = attributeBonuses;
    }

    public LifepathChoice(String id, String title, String description,
                          Map<CaptainLogCharacter.Attribute, Integer> attributeBonuses,
                          Map<CaptainLogCharacter.Discipline, Integer> disciplineBonuses,
                          String backgroundTrait,
                          CaptainLogCharacter.Environment environment,
                          CaptainLogCharacter.Outlook outlook,
                          CaptainLogCharacter.Education education,
                          CaptainLogCharacter.Assignment assignment,
                          CaptainLogCharacter.Rank rank,
                          String focusName,
                          CaptainLogCharacter.Discipline focusDiscipline,
                          String valueDescription) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.attributeBonuses = attributeBonuses;
        this.disciplineBonuses = disciplineBonuses;
        this.backgroundTrait = backgroundTrait;
        this.environment = environment;
        this.outlook = outlook;
        this.education = education;
        this.assignment = assignment;
        this.rank = rank;
        this.focusName = focusName;
        this.focusDiscipline = focusDiscipline;
        this.valueDescription = valueDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<CaptainLogCharacter.Attribute, Integer> getAttributeBonuses() {
        return attributeBonuses;
    }

    public void setAttributeBonuses(Map<CaptainLogCharacter.Attribute, Integer> attributeBonuses) {
        this.attributeBonuses = attributeBonuses;
    }

    public Map<CaptainLogCharacter.Discipline, Integer> getDisciplineBonuses() {
        return disciplineBonuses;
    }

    public void setDisciplineBonuses(Map<CaptainLogCharacter.Discipline, Integer> disciplineBonuses) {
        this.disciplineBonuses = disciplineBonuses;
    }

    public String getBackgroundTrait() {
        return backgroundTrait;
    }

    public void setBackgroundTrait(String backgroundTrait) {
        this.backgroundTrait = backgroundTrait;
    }

    public CaptainLogCharacter.Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(CaptainLogCharacter.Environment environment) {
        this.environment = environment;
    }

    public CaptainLogCharacter.Outlook getOutlook() {
        return outlook;
    }

    public void setOutlook(CaptainLogCharacter.Outlook outlook) {
        this.outlook = outlook;
    }

    public CaptainLogCharacter.Education getEducation() {
        return education;
    }

    public void setEducation(CaptainLogCharacter.Education education) {
        this.education = education;
    }

    public CaptainLogCharacter.Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(CaptainLogCharacter.Assignment assignment) {
        this.assignment = assignment;
    }

    public CaptainLogCharacter.Rank getRank() {
        return rank;
    }

    public void setRank(CaptainLogCharacter.Rank rank) {
        this.rank = rank;
    }

    public String getFocusName() {
        return focusName;
    }

    public void setFocusName(String focusName) {
        this.focusName = focusName;
    }

    public CaptainLogCharacter.Discipline getFocusDiscipline() {
        return focusDiscipline;
    }

    public void setFocusDiscipline(CaptainLogCharacter.Discipline focusDiscipline) {
        this.focusDiscipline = focusDiscipline;
    }

    public String getValueDescription() {
        return valueDescription;
    }

    public void setValueDescription(String valueDescription) {
        this.valueDescription = valueDescription;
    }
}
