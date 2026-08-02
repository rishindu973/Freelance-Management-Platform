package com.freelance.freelancepm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FreelancePmApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreelancePmApplication.class, args);
    }

}
