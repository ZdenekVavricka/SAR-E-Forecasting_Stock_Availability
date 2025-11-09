package com.example.forecasting_stock_availability;

import com.example.forecasting_stock_availability.data_client.DateObject;
import com.example.forecasting_stock_availability.data_client.HolidayApi;
import com.example.forecasting_stock_availability.data_client.HolidayDataInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class ForecastingStockAvailabilityApplication extends SpringBootServletInitializer {

    @Autowired
    private HolidayDataInterface holidayApi;


    public static void main(String[] args) {
        SpringApplication.run(ForecastingStockAvailabilityApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        //List<DateObject> a = getDate();
        //return a.toString();
        return "HAIIIIIIIIIIIIIII";
    }

    @GetMapping("/hello2")
    public String hello2( ) {
        return holidayApi.getDay().toString();
    }
}