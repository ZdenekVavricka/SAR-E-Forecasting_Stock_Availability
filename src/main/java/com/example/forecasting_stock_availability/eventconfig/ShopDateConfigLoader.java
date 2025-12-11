package com.example.forecasting_stock_availability.eventconfig;

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

/**
 * Loads and provides access to shop event date configurations from classpath JSON.
 */
@Component
public class ShopDateConfigLoader {

    /**
     * Jackson mapper used to parse configuration JSON.
     */
    private final ObjectMapper mapper = new ObjectMapper();
    /**
     * Loaded configuration per shop with sorted event dates.
     */
    @Setter
    @Getter
    private List<ShopDateConfig> shopsConfigs;

    /**
     * Loads configurations from the classpath resource {@code events_configuration.json} and sorts dates per shop.
     *
     * @throws Exception when resource cannot be read or parsed
     */
    @PostConstruct
    public void load() throws Exception {
        ClassPathResource resource = new ClassPathResource("events_configuration.json");
        shopsConfigs = mapper.readValue(resource.getInputStream(),
                new TypeReference<>() {
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

    /**
     * Determines which event dates for a shop fall within the interval [date, date+interval].
     *
     * @param shopID   shop identifier
     * @param date     start date (YYYY-MM-DD)
     * @param interval number of days to include (inclusive end)
     * @return map where keys are event dates and values are {@code true} when within interval
     */
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
