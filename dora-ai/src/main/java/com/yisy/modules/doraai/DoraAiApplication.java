package com.yisy.modules.doraai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for the DoraAI application.
 * <p>
 * DoraAI is a software development AI platform that provides code understanding,
 * code generation, bug detection, and chat-based interaction with a local LLM
 * via Ollama.
 * </p>
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.yisy.modules.doraai",
    "com.yisy.modules.doradl"
})
public class DoraAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoraAiApplication.class, args);
    }
}
