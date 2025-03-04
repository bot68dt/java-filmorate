package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j

public class FilmorateApplication {
    public static void main(String[] args) {
        SpringApplication.run(FilmorateApplication.class, args);
    }
}