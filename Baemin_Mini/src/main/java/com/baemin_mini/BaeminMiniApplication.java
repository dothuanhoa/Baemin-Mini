package com.baemin_mini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class BaeminMiniApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaeminMiniApplication.class, args);
    }

}