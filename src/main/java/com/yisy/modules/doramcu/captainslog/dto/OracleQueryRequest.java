package com.yisy.modules.doramcu.captainslog.dto;

/**
 * DTO for querying the oracle.
 */
public class OracleQueryRequest {

    /** The question being asked of the oracle. */
    private String question;

    /** Whether the question expects a simple yes/no or allows Both/Neither. */
    private boolean allowBothNeither = true;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public boolean isAllowBothNeither() {
        return allowBothNeither;
    }

    public void setAllowBothNeither(boolean allowBothNeither) {
        this.allowBothNeither = allowBothNeither;
    }
}
