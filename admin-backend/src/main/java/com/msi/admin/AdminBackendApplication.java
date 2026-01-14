package com.msi.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AdminBackendApplication {
    private static final Logger log = LoggerFactory.getLogger(AdminBackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AdminBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner logRedisConfig(@Value("${spring.data.redis.host}") String redisHost,
                                            @Value("${spring.data.redis.port}") int redisPort) {
        return args -> {
            log.info("============================================================");
            log.info("Redis Configuration Check:");
            log.info("Host: {}", redisHost);
            log.info("Port: {}", redisPort);
            log.info("============================================================");
        };
    }
}

