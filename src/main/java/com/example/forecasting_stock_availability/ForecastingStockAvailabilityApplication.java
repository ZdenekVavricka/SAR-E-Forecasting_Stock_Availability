package com.example.forecasting_stock_availability;

import com.example.forecasting_stock_availability.DB.InventoryRecordsManager;
import com.example.forecasting_stock_availability.DB.InventoryRecordsRepository;
import com.example.forecasting_stock_availability.data_client.HolidayDataInterface;
import com.example.forecasting_stock_availability.shop.InventoryRecord;
import com.example.forecasting_stock_availability.shop.ShopsDataLoaderInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@SpringBootApplication
@RestController
@EnableScheduling
public class ForecastingStockAvailabilityApplication extends SpringBootServletInitializer {

    @Autowired
    private HolidayDataInterface holidayApi;

    @Autowired
    private ShopsDataLoaderInterface shopsApi;
    @Autowired

    InventoryRecordsRepository inventoryRecordsRepository;



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
    public String hello3() {
        return shopsApi.loadData().toString();
    }

    @GetMapping("/delete-all")
    public String deleteAll() {
        inventoryRecordsRepository.deleteAll();



        return "deleted all DB";
    }

    @GetMapping("/upload-csv")
    public String uploadCSV() {

        List<InventoryRecord> a = shopsApi.loadData();
        System.out.println("loaded = " + a.size());
        System.out.println("saving to DB");
        inventoryRecordsRepository.saveAll(a);
        return "uploaded CSV into DB";
    }
}