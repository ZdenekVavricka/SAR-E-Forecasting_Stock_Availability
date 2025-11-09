package com.example.forecasting_stock_availability.data_client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component

public class HolidayApi implements HolidayDataInterface{
    static String API_URL = "https://svatkyapi.cz/api/";

    /**
     * Only returns information about current day
     *
     * @return current day
     */
    public List<DateObject> getDay() {
        String url = API_URL + "day";

        return getData(url, false);
    }

    /**
     * Only returns information about asked date
     *
     * @param date
     * @return
     */
    public List<DateObject> getDay(String date) {
        String url = API_URL + "day/" + date;

        return getData(url, false);
    }

    /***
     * Returns information about next week starting from given date
     * @return
     */
    public List<DateObject> getWeek(String date) {

        String url = API_URL + "week/" + date;

        return getData(url, true);
    }

    /**
     * Returns information about interval of days starting from given date
     *
     * @param date
     * @param interval
     * @return
     */

    public List<DateObject> getDateInterval(String date, int interval) {
        String url = API_URL + "day/" + date + "/interval/" + interval;
        return getData(url, true);
    }

    private List<DateObject> getData(String uri, boolean array) {
        RestTemplate restTemplate = new RestTemplate();

        // The API returns an array of objects
        if (array) {
            ResponseEntity<DateObject[]> response = restTemplate.getForEntity(uri, DateObject[].class);
            return Arrays.asList(response.getBody());
        }

        ResponseEntity<DateObject> response = restTemplate.getForEntity(uri, DateObject.class);
        return Arrays.asList(response.getBody());
    }
}
