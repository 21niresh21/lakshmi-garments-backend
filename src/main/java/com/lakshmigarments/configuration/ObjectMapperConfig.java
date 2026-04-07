package com.lakshmigarments.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ObjectMapperConfig {

    @Bean
    ObjectMapper objectMapper() {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.registerModule(new JavaTimeModule());
        
        // Optional: serialize dates as ISO-8601 strings, not timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
