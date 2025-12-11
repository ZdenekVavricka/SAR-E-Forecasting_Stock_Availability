package com.example.forecasting_stock_availability;

import com.example.forecasting_stock_availability.DB.InventoryRecordsRepository;
import com.example.forecasting_stock_availability.shop.InventoryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * Spring Boot entry point and simple test endpoints for the Forecasting Stock Availability service.
 *
 * <p>Note: This class contains a few helper endpoints used for manual testing (CSV upload, etc.).
 */
@SpringBootApplication
@RestController
@EnableScheduling
public class ForecastingStockAvailabilityApplication extends SpringBootServletInitializer {

    /**
     * Repository for persisting and querying {@link com.example.forecasting_stock_availability.shop.InventoryRecord} documents.
     */
    @Autowired
    InventoryRecordsRepository inventoryRecordsRepository;

    /**
     * In-memory cache of inventory records loaded from CSV (testing only).
     */
    public static List<InventoryRecord> inventoryRecords = null;

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ForecastingStockAvailabilityApplication.class, args);
    }




    /**
     * Deletes all inventory records from the database (testing only).
     *
     * @return confirmation text
     */
    @GetMapping("/delete-all")
    public String deleteAll() {
        inventoryRecordsRepository.deleteAll();
        return "deleted all DB";
    }

    /**
     * Loads sample CSV from the local data folder and saves it to the database (testing only).
     *
     * @return confirmation text with basic progress
     */
    @GetMapping("/upload-csv")
    public String uploadCSV() {

        List<InventoryRecord> a = loadData();
        System.out.println("loaded = " + a.size());
        System.out.println("saving to DB");
        inventoryRecordsRepository.saveAll(a);
        return "uploaded CSV into DB";
    }


    /**
     * Maps a CSV file represented as a list of lines into a list of {@link InventoryRecord} objects.
     * The first line is assumed to be a header and is skipped (testing only).
     *
     * @param list lines of a CSV file
     * @return list of mapped inventory records
     */
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


    /**
     * Loads and caches inventory records from the CSV file in the data folder.
     * Subsequent calls return the cached list (testing only).
     *
     * @return list of inventory records
     */
    private static List<InventoryRecord> loadData() {

        if (inventoryRecords == null) {

            List<String> list = null;

            try {
                list = Files.readAllLines(Path.of("./data/retail_store_inventory.csv"), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.getStackTrace();
            }
            if (list == null){
                inventoryRecords = new ArrayList<>();
            }else {
                inventoryRecords = inventoryRecordMapper(list);
            }
        }


        return inventoryRecords;
    }


}