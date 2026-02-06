package com.example.vidal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class VidalHealthAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(VidalHealthAppApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner runner(WebhookRunner webhookRunner) {
        return args -> webhookRunner.runOnStartup();
    }
}
