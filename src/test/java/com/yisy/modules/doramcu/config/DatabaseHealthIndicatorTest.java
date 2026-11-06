package com.yisy.modules.doramcu.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("DatabaseHealthIndicator Tests")
class DatabaseHealthIndicatorTest {

    private DataSource dataSource;
    private Connection connection;
    private DatabaseHealthIndicator indicator;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        indicator = new DatabaseHealthIndicator(dataSource);
    }

    // ---- health - UP scenario ----

    @Test
    @DisplayName("health - should return UP when connection succeeds")
    void health_shouldReturnUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);

        Health health = indicator.health();

        assertNotNull(health);
        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    @DisplayName("health - should include database detail when UP")
    void health_shouldIncludeDatabaseDetail() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);

        Health health = indicator.health();

        assertNotNull(health.getDetails().get("database"));
        assertEquals("connected", health.getDetails().get("connection"));
    }

    @Test
    @DisplayName("health - should close connection after check")
    void health_shouldCloseConnection() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);

        indicator.health();

        verify(connection).close();
    }

    // ---- health - DOWN scenario ----

    @Test
    @DisplayName("health - should return DOWN when connection fails")
    void health_shouldReturnDown() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        Health health = indicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    @DisplayName("health - should include error message when DOWN")
    void health_shouldIncludeErrorMessage() throws Exception {
        String errorMsg = "Connection refused";
        when(dataSource.getConnection()).thenThrow(new SQLException(errorMsg));

        Health health = indicator.health();

        assertEquals("RDBMS", health.getDetails().get("database"));
        assertEquals(errorMsg, health.getDetails().get("error"));
    }

    @Test
    @DisplayName("health - should handle SQLException with empty message")
    void health_shouldHandleEmptyError() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException(""));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("", health.getDetails().get("error"));
    }
}
