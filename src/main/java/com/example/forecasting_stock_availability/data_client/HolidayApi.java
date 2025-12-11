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

/**
 * Client for retrieving holiday and calendar data from an external API.
 */
@Component
public class HolidayApi implements HolidayDataInterface {

    /**
     * Base URL of the holidays API (configured via properties: holidays.getHolidays).
     */
    @Value("${holidays.getHolidays}")
    private String API_URL;
    /** Rest template used for HTTP requests. */
    private final RestTemplate restTemplate;
    /** Jackson object mapper for JSON parsing. */
    private final ObjectMapper objectMapper;

    /**
     * Constructs the API client with the provided {@link ObjectMapper}.
     *
     * @param objectMapper mapper used to deserialize JSON responses
     */
    public HolidayApi(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }


    /**
     * Returns information about the current day.
     *
     * @return list of {@link DateObject} entries
     */
    public List<DateObject> getDay() {
        String url = API_URL + "day";

        return getData(url);
    }


    /**
     * Returns information about the given date.
     *
     * @param date date in format YYYY-MM-DD
     * @return list of {@link DateObject} entries
     */
    public List<DateObject> getDay(String date) {
        String url = API_URL + "day/" + date;

        return getData(url);
    }


    /**
     * Returns information about the week starting from the given date.
     *
     * @param date date in format YYYY-MM-DD
     * @return list of {@link DateObject} entries
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

    /**
     * Returns information about an interval of days starting from the given date.
     *
     * @param date     date in format YYYY-MM-DD
     * @param interval number of days from the start date
     * @return list of {@link DateObject} entries
     */
    public List<DateObject> getDateInterval(String date, int interval) {
        String url = API_URL + "day/" + date + "/interval/" + interval;
        return getData(url);
    }

    /**
     * Executes the request and parses the JSON response into {@link DateObject} items.
     *
     * @param uri endpoint URL
     * @return list of {@link DateObject}
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
            return Collections.emptyList();
        }
    }
}
