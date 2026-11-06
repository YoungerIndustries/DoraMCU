package com.yisy.modules.doramcu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Simple Spring Boot entry point for DoraMCU.
 * This is a headless server launcher — the Web UI (browser) is the only interface.
 * The Java desktop UI has been deprecated and removed.
 *
 * Usage:
 *   ./gradlew bootRun          # Starts server; open http://localhost:8080 in browser
 *   java -jar doraMCU.jar      # Same, runs as standalone JAR
 */
@SpringBootApplication
@EnableCaching
public class DoraMcuLauncher {

    private static final Logger log = LoggerFactory.getLogger(DoraMcuLauncher.class);

    public static void main(String[] args) {
        log.info("Starting DoraMCU server — Web UI available at http://localhost:8080");
        SpringApplication.run(DoraMcuLauncher.class, args);
        log.info("DoraMCU server started successfully");
    }
}
