package com.example.forecasting_stock_availability.shop;

import com.example.forecasting_stock_availability.DB.InventoryRecordsManager;
import com.example.forecasting_stock_availability.endpoins.ShopsEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component

public class DailyShopDataCrawl {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    ShopsEndpoints shopsEndpoints;

    @Autowired
    InventoryRecordsManager inventoryRecordsManager;

    @Scheduled(cron = "0 59 23 * * *")
//    @Scheduled(cron = "0 * * * * *")
    public void callEndpoint() {
        System.out.println("CRON JOB CRAWL");

        Random r = new Random(0);

        for (String shopID : shopsEndpoints.getShopID()) {
            String shopURLtoGetAllData = shopsEndpoints.getEndpointURL(shopID, ShopsEndpoints.Endpoints.getAllData);

            System.out.println("SNAZIM SE VOLAT " +shopURLtoGetAllData);
            try {
                String response = restTemplate.getForObject(shopURLtoGetAllData, String.class);
                System.out.println("Response: " + response);
            } catch (Exception e) {
                e.getStackTrace();
                System.err.println("Error calling endpoint: " + e.getMessage());
            }

            List<InventoryRecord> inventoryRecords = new ArrayList<>();

            for (int j = 0; j < 500; j++) {
                InventoryRecord inventoryRecord = new InventoryRecord();
                String date = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate().toString();
                String itemID = "P000" + (r.nextInt(8)+1);
                String name = "electronics";
                String units = "ks";
                int currentLevel = r.nextInt(5,200);
                int soldItems = r.nextInt(0,currentLevel);

                inventoryRecord.setRecordID(j+"-"+date+"-"+shopID+"-"+itemID);
                inventoryRecord.setDate(date);
                inventoryRecord.setShopID(shopID);
                inventoryRecord.setItemID(itemID);
                inventoryRecord.setName(name);
                inventoryRecord.setUnitType(units);
                inventoryRecord.setCurrentLevel(currentLevel);
                inventoryRecord.setSoldItems(soldItems);
                inventoryRecords.add(inventoryRecord);
            }

            System.out.println("SAVING INTO DB");

            inventoryRecordsManager.saveInventoryRecords(inventoryRecords);

            System.out.println("CRON JOB FINISHED");

        }
    }


    //ziskame data pro dnesek
    //uložit data do DB


}
