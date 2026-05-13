package com.vitaai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VitaAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VitaAiApplication.class, args);
    }
}
