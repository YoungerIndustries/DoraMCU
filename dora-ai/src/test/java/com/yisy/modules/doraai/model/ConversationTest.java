package com.yisy.modules.doraai.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConversationTest {

    private Conversation conversation;

    @BeforeEach
    void setUp() {
        conversation = new Conversation("test-session-123");
    }

    @Test
    void constructor_generatesSessionIdIfNull() {
        Conversation conv = new Conversation();

        assertNotNull(conv.getSessionId());
        assertFalse(conv.getSessionId().isBlank());
    }

    @Test
    void constructor_setsSessionId() {
        assertEquals("test-session-123", conversation.getSessionId());
    }

    @Test
    void constructor_initializesEmptyMessages() {
        assertTrue(conversation.getMessages().isEmpty());
        assertEquals(0, conversation.getMessageCount());
    }

    @Test
    void constructor_setsCreatedAt() {
        long before = System.currentTimeMillis() - 1000;
        long after = System.currentTimeMillis() + 1000;

        assertTrue(conversation.getCreatedAt() >= before);
        assertTrue(conversation.getCreatedAt() <= after);
    }

    @Test
    void addUserMessage_addsUserMessage() {
        conversation.addUserMessage("Hello, AI!");

        assertEquals(1, conversation.getMessageCount());
        assertEquals("user", conversation.getMessages().get(0).getRole());
        assertEquals("Hello, AI!", conversation.getMessages().get(0).getContent());
    }

    @Test
    void addAiMessage_addsAssistantMessage() {
        conversation.addAiMessage("Hello, user!");

        assertEquals(1, conversation.getMessageCount());
        assertEquals("assistant", conversation.getMessages().get(0).getRole());
        assertEquals("Hello, user!", conversation.getMessages().get(0).getContent());
    }

    @Test
    void addMultipleMessages_preservesOrder() {
        conversation.addUserMessage("Question 1");
        conversation.addAiMessage("Answer 1");
        conversation.addUserMessage("Question 2");

        assertEquals(3, conversation.getMessageCount());
        assertEquals("user", conversation.getMessages().get(0).getRole());
        assertEquals("assistant", conversation.getMessages().get(1).getRole());
        assertEquals("user", conversation.getMessages().get(2).getRole());
    }

    @Test
    void setProjectId_setsProjectId() {
        conversation.setProjectId("project-456");

        assertEquals("project-456", conversation.getProjectId());
    }

    @Test
    void getProjectId_nullByDefault() {
        assertNull(conversation.getProjectId());
    }

    @Test
    void clearMessages_removesAllMessages() {
        conversation.addUserMessage("Message 1");
        conversation.addAiMessage("Message 2");

        conversation.clearMessages();

        assertEquals(0, conversation.getMessageCount());
        assertTrue(conversation.getMessages().isEmpty());
    }

    @Test
    void clearMessages_doesNotAffectSessionId() {
        conversation.addUserMessage("Message 1");
        conversation.clearMessages();

        assertEquals("test-session-123", conversation.getSessionId());
    }

    @Test
    void message_getters_returnCorrectValues() {
        Conversation.Message message = new Conversation.Message("user", "Test content");

        assertEquals("user", message.getRole());
        assertEquals("Test content", message.getContent());
    }
}
