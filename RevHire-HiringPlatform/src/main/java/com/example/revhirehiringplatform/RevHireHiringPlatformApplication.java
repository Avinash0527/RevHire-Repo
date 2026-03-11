package com.example.revhirehiringplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class RevHireHiringPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(RevHireHiringPlatformApplication.class, args);
    }

}
