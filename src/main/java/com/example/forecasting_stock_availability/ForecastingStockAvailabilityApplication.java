package com.example.forecasting_stock_availability;

import com.example.forecasting_stock_availability.data_client.HolidayDataInterface;
import com.example.forecasting_stock_availability.shop.SearchItemBean;
import com.example.forecasting_stock_availability.shop.ShopsDataLoaderInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class ForecastingStockAvailabilityApplication extends SpringBootServletInitializer {

    @Autowired
    private HolidayDataInterface holidayApi;

    @Autowired
    private ShopsDataLoaderInterface shopsApi;

    public static void main(String[] args) {
        SpringApplication.run(ForecastingStockAvailabilityApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello " + name;
    }

    @GetMapping("/hello2")
    public String hello2() {
        return holidayApi.getDay().toString();
    }

    @GetMapping("/hello3")
    public String hello() {
        return shopsApi.loadData().toString();
    }

    @GetMapping({"/hello5",
            "/hello5/{date}",
            "/hello5/{date}/{shopID}",
            "/hello5/{date}/{shopID}/{itemID}"})
    public String hello5(@PathVariable(value = "date", required = false) String date,
                         @PathVariable(value = "shopID",required = false) String shopID,
                         @PathVariable(value = "itemID",required = false) String itemID) {

        SearchItemBean searchItemBean = new SearchItemBean(date, shopID, itemID);
           return shopsApi.getInventoryRecords(searchItemBean).toString();
    }

}