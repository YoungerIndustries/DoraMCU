package com.yisy.modules.doramcu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Single Spring Boot entry point for DoraMCU.
 * This is a headless server launcher — the Web UI (browser) is the only interface.
 * The Java desktop UI has been deprecated and removed.
 *
 * Usage:
 *   ./gradlew bootRun          # Starts server; open http://localhost:8080 in browser
 *   java -jar doraMCU.jar      # Same, runs as standalone JAR
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = {"com.yisy.modules.doramcu", "com.yisy.modules.doradl", "com.yisy.modules.doraai"})
public class DoraMcuLauncher {

    private static final Logger log = LoggerFactory.getLogger(DoraMcuLauncher.class);

    public static void main(String[] args) {
        log.info("Starting DoraMCU server — Web UI available at http://localhost:8080");
        SpringApplication app = new SpringApplication(DoraMcuLauncher.class);
        Environment env = app.run(args).getEnvironment();

        // Warn if running in dev profile without explicit opt-in
        String[] activeProfiles = env.getActiveProfiles();
        boolean isDev = activeProfiles != null && java.util.Arrays.asList(activeProfiles).contains("dev");
        if (isDev) {
            String devOptIn = env.getProperty("doramcu.development");
            if (!"true".equalsIgnoreCase(devOptIn)) {
                log.warn("========================================");
                log.warn("Running in 'dev' profile without explicit opt-in!");
                log.warn("Set 'doramcu.development=true' to acknowledge dev mode.");
                log.warn("Do NOT use 'dev' profile in production/staging.");
                log.warn("========================================");
            }
        }
        log.info("DoraMCU server started successfully");
    }
}
