package edu.gatech.cs6310.powergrid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PowergridApplication {

    public static void main(String[] args) {
        SpringApplication.run(PowergridApplication.class, args);
    }
}
