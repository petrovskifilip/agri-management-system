package com.finki.agrimanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgriManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriManagementSystemApplication.class, args);
    }

}
