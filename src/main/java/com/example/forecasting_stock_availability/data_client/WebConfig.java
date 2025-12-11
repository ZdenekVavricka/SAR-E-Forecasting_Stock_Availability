package com.example.forecasting_stock_availability.data_client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Web-related Spring configuration.
 */
@Configuration
public class WebConfig {

    /**
     * Provides a {@link RestTemplate} bean for making HTTP calls.
     *
     * @return configured {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
