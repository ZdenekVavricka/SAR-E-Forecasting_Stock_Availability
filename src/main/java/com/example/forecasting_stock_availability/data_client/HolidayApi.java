package com.example.forecasting_stock_availability.data_client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class HolidayApi implements HolidayDataInterface {

    @Value("${holidays.getHolidays}")
    private String API_URL;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HolidayApi(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    /**
     * Only returns information about current day
     *
     * @return List of DateObject
     */
    public List<DateObject> getDay() {
        String url = API_URL + "day";

        return getData(url);
    }

    /**
     * Only returns information about asked date
     *
     * @param date - date in format YYYY-MM-DD
     * @return List of DateObject
     */
    public List<DateObject> getDay(String date) {
        String url = API_URL + "day/" + date;

        return getData(url);
    }

    /***
     * Returns information about next week starting from given date
     * @param date - date in format YYYY-MM-DD
     * @return List of DateObject
     */
    public List<DateObject> getWeek(String date) {

        String url = API_URL + "week/" + date;

        return getData(url);
    }

    /**
     * Returns information about interval of days starting from given date
     *
     * @param date - date in format YYYY-MM-DD
     * @param interval - number of days from date
     * @return List of DateObject
     */

    public List<DateObject> getDateInterval(String date, int interval) {
        String url = API_URL + "day/" + date + "/interval/" + interval;
        return getData(url);
    }

    /**
     * Returns acquired data from svatkyapi.cz
     *
     * @param uri - url of Endpoint
     * @return List of DataObject
     */
    private List<DateObject> getData(String uri) {

        String json = restTemplate.getForObject(uri, String.class);
        try {
            // Check if the JSON starts with '[' → it's an array
            assert json != null;
            if (json.trim().startsWith("[")) {
                CollectionType listType = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, DateObject.class);
                return objectMapper.readValue(json, listType);
            } else {
                DateObject obj = objectMapper.readValue(json, DateObject.class);
                return List.of(obj);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
