package com.hurek.wenbot.configuration;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppConfig {

    @Bean
    public Random random() {
        return new Random();
    }
}
