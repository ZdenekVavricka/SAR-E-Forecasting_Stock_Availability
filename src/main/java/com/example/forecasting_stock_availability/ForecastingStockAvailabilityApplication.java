package com.example.forecasting_stock_availability;

import com.example.forecasting_stock_availability.DB.InventoryRecordsRepository;
import com.example.forecasting_stock_availability.eventconfig.ShopDateConfigLoader;
import com.example.forecasting_stock_availability.shop.InventoryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
@RestController
@EnableScheduling
public class ForecastingStockAvailabilityApplication extends SpringBootServletInitializer {




    @Autowired
    InventoryRecordsRepository inventoryRecordsRepository;

    @Autowired
    ShopDateConfigLoader loader;

    public static List<InventoryRecord> inventoryRecords = null;

    public static void main(String[] args) {
        SpringApplication.run(ForecastingStockAvailabilityApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello " + name;
    }


    @GetMapping("/delete-all")
    public String deleteAll() {
        inventoryRecordsRepository.deleteAll();

        return "deleted all DB";
    }

    /**
     * THIS IS ONLY FOR TESTING. IT UPLOADS CVS FROM DATA FOLDER
     */
    @GetMapping("/upload-csv")
    public String uploadCSV() {

        List<InventoryRecord> a = loadData();
        System.out.println("loaded = " + a.size());
        System.out.println("saving to DB");
        inventoryRecordsRepository.saveAll(a);
        return "uploaded CSV into DB";
    }

    @GetMapping("/test")
    public String shopDates() {

        return loader.getShopsConfigs() + "";
    }


    /// ///for CSV LOADING INTO DB - TESTING PURPOSES

    private static List<InventoryRecord> inventoryRecordMapper(List<String> list) {
        List<InventoryRecord> inventory = new ArrayList<>();

        //Skipping the first one
        for (int i = 1; i < list.size(); i++) {
            String[] tokens = list.get(i).split(",");
            InventoryRecord record = new InventoryRecord();
            record.setDate(tokens[0]);
            record.setShopID(tokens[1]);
            record.setItemID(tokens[2]);
            record.setName(tokens[3]);
            record.setDuringEvent(tokens[12]);
            record.setUnitType("ks");
            record.setCurrentLevel(Integer.parseInt(tokens[5]));
            record.setSoldItems(Integer.parseInt(tokens[6]));
            record.setRestockCount(Integer.parseInt(tokens[7]));
            record.setRecordID(record.getDate() + "-" + record.getShopID() + "-" + record.getItemID());

            inventory.add(record);
        }

        return inventory;
    }


    private static List<InventoryRecord> loadData() {

        if (inventoryRecords == null) {
            System.out.println("LOADING");

            List<String> list = null;

            try {
                list = Files.readAllLines(Path.of("./data/retail_store_inventory.csv"), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.getStackTrace();
            }

            inventoryRecords = inventoryRecordMapper(list);
        }

        System.out.println("CASHED");

        return inventoryRecords;
    }


}