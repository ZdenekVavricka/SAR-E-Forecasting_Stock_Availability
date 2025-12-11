package com.example.forecasting_stock_availability.eventconfig;


import com.example.forecasting_stock_availability.endpoins.ShopsEndpoints;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Component

public class ShopDateConfigLoader {

    private final ObjectMapper mapper = new ObjectMapper();
    @Setter
    @Getter
    private List<ShopDateConfig> shopsConfigs;

    @PostConstruct
    public void load() throws Exception {
        ClassPathResource resource = new ClassPathResource("events_configuration.json");
        shopsConfigs = mapper.readValue(resource.getInputStream(),
                new TypeReference<List<ShopDateConfig>>() {
                });

        // Sort the dates for each shop
        shopsConfigs.forEach(config -> {
            List<String> sorted = config.getDates().stream()
                    .map(LocalDate::parse)
                    .sorted()
                    .map(LocalDate::toString)
                    .toList();
            config.setDates(sorted);
        });
    }

    public HashMap<String, Boolean> hasEventDuringDate(String shopID, String date, int interval) {
        HashMap<String, Boolean> map = new HashMap<>();

        ShopDateConfig curentShop = null;
        for (ShopDateConfig shopsConfig : shopsConfigs) {
            if (shopsConfig.getShopID().equals(shopID)) {
                curentShop = shopsConfig;
                break;
            }
        }

        if (curentShop == null) {
            return map;
        }

        LocalDate startDate = LocalDate.parse(date);
        LocalDate endDate = startDate.plusDays(interval);

        for (String eventDateString : curentShop.getDates()) {
            LocalDate eventDate = LocalDate.parse(eventDateString);
            boolean isBetween = (!eventDate.isBefore(startDate)) && (!eventDate.isAfter(endDate));
            if (isBetween) {
                map.put(eventDate.toString(), true);
            }
            if (eventDate.equals(endDate)) {
                break;
            }
        }


        return map;
    }
}
