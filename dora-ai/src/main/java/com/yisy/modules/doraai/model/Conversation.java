package com.yisy.modules.doraai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a chat conversation session.
 * <p>
 * Tracks message history for a single chat session, maintaining context
 * for multi-turn conversations with the AI.
 * </p>
 */
public class Conversation {

    private final String sessionId;
    private final List<Message> messages;
    private final long createdAt;
    private String projectId;

    /**
     * Represents a message in a conversation.
     */
    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    public Conversation() {
        this.sessionId = UUID.randomUUID().toString();
        this.messages = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public Conversation(String sessionId) {
        this.sessionId = sessionId;
        this.messages = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * Adds a user message to the conversation.
     */
    public void addUserMessage(String content) {
        messages.add(new Message("user", content));
    }

    /**
     * Adds an AI response to the conversation.
     */
    public void addAiMessage(String content) {
        messages.add(new Message("assistant", content));
    }

    /**
     * Returns the number of messages in the conversation.
     */
    public int getMessageCount() {
        return messages.size();
    }

    /**
     * Clears the message history.
     */
    public void clearMessages() {
        messages.clear();
    }
}
