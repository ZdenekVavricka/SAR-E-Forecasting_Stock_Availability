package com.example.forecasting_stock_availability.endpoins;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "shops")
public class ShopsEndpoints {

    public enum Endpoints {
        getAllData,
        getCurrentDayItemStock,
        getItemsRestockCount,
        hasEventDuringDate
    }

    @Getter @Setter
    private List<String> getAllData;
    @Getter @Setter
    private List<String> getCurrentDayItemStock;
    @Getter @Setter
    private List<String> getItemsRestockCount;
    @Getter @Setter
    private List<String> hasEventDuringDate;
    @Getter @Setter
    private List<String> shopID;

    private HashMap<String, HashMap<String, String>> shopsEndpointsUrls = new HashMap<>();

    @PostConstruct
    public void init() {
        for (int i = 0; i < getAllData.size(); i++) {
            HashMap<String, String> endpoints = new HashMap<>();

            String shopID = getShopID().get(i);
            endpoints.put("getAllData", getAllData.get(i));
            endpoints.put("getCurrentDayItemStock", getCurrentDayItemStock.get(i));
            endpoints.put("getItemsRestockCount", getItemsRestockCount.get(i));
            endpoints.put("hasEventDuringDate", hasEventDuringDate.get(i));

            shopsEndpointsUrls.put(shopID, endpoints);

        }

    }

    // Get the URL of a particular shop and endpoint
    public String getEndpointURL(String shopID, Endpoints endpoint) {
        HashMap<String, String> endpoints = shopsEndpointsUrls.get(shopID);
        return (endpoints != null) ? endpoints.get(endpoint.name()) : null;
    }
}
