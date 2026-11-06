package com.yisy.modules.doramcu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            String dbProduct = "RDBMS";
            String dbVersion = "unknown";
            try {
                if (connection != null && connection.getMetaData() != null) {
                    String prod = connection.getMetaData().getDatabaseProductName();
                    if (prod != null && !prod.isBlank()) {
                        dbProduct = prod;
                    }
                    String ver = connection.getMetaData().getDatabaseProductVersion();
                    if (ver != null && !ver.isBlank()) {
                        dbVersion = ver;
                    }
                }
            } catch (Exception ignored) {
            }
            return Health.up()
                    .withDetail("database", dbProduct)
                    .withDetail("version", dbVersion)
                    .withDetail("connection", "connected")
                    .build();
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("database", "RDBMS")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
