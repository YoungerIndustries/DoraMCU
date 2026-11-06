package com.yisy.modules.doramcu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = {"com.yisy.modules.doramcu", "com.yisy.modules.doradl", "com.yisy.modules.doraai"})
public class DoraMcuApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoraMcuApplication.class, args);
    }

}
