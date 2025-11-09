package com.example.forecasting_stock_availability.data_client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalDataClient {

    public static String getDate(){
        final String uri = "https://svatkyapi.cz/api/day/2020-01-01/interval/365";

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);

        return result;
    }


}
