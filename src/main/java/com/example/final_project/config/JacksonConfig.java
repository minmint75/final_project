package com.example.final_project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        
        // Register JavaTimeModule for LocalDateTime support
        mapper.registerModule(new JavaTimeModule());
        
        // Disable writing dates as timestamps (use ISO-8601 instead)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // Disable error on empty beans (for Hibernate proxies)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return mapper;
    }
}
