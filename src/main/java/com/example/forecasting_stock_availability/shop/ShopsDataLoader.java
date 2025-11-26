package com.example.forecasting_stock_availability.shop;


import com.example.forecasting_stock_availability.DB.InventoryRecordsManager;
import com.example.forecasting_stock_availability.endpoins.ShopsEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class ShopsDataLoader implements ShopsDataLoaderInterface {
    @Autowired
    ShopsEndpoints shopsEndpoints;

    @Autowired
    InventoryRecordsManager inventoryRecordsManager;


    public List<InventoryRecord> inventoryRecords = null;

    private List<InventoryRecord> inventoryRecordMapper(List<String> list) {
        List<InventoryRecord> inventory = new ArrayList<>();

        //Skipping the first one
        for (int i = 1; i < list.size(); i++) {
            String[] tokens = list.get(i).split(",");
            InventoryRecord record = new InventoryRecord();
            record.setRecordID(""+(i-1));
            record.setDate(tokens[0]);
            record.setShopID(tokens[1]);
            record.setItemID(tokens[2]);
            record.setName(tokens[3]);
            record.setDuringEvent(tokens[12]);
            record.setUnitType("ks");
            record.setCurrentLevel(Integer.parseInt(tokens[5]));
            record.setSoldItems(Integer.parseInt(tokens[6]));

            inventory.add(record);
        }

        return inventory;
    }

    @Override
    public List<InventoryRecord> loadData() {


        if (inventoryRecords == null) {
            System.out.println("LOADING");

            List<String> list = null;

//            try {
//                list = Files.readAllLines(Path.of("./data/retail_store_inventory.csv"), StandardCharsets.UTF_8);
//            } catch (IOException e) {
//                e.getStackTrace();
//            }

//            inventoryRecords = inventoryRecordMapper(list);


            inventoryRecords = inventoryRecordsManager.getAllInventoryRecords();

            return inventoryRecords;
        }

        System.out.println("CASHED");

        return inventoryRecords;
    }

    @Override
    public List<InventoryRecord> getInventoryRecords(SearchItemBean search) {
        loadData();

        Stream<InventoryRecord> stream = inventoryRecords.stream();

        if (search.getPredictDate() != null) {
            stream = stream.filter(item -> item.getDate().equals(search.getPredictDate()));
        }

        if (search.getShopID() != null) {
            stream = stream.filter(item -> item.getShopID().equals(search.getShopID()));
        }

        if (search.getItemID() != null) {
            stream = stream.filter(item -> item.getItemID().equals(search.getItemID()));
        }

        if (search.getDataStartDate() != null) {
            LocalDate startDate = LocalDate.parse(search.getDataStartDate()).minusDays(1);
            stream = stream.filter(item -> LocalDate.parse(item.getDate()).isAfter(startDate));
        }

        if (search.getDataEndDate() != null) {
            LocalDate endDate = LocalDate.parse(search.getDataEndDate()).plusDays(1);
            stream = stream.filter(item -> LocalDate.parse(item.getDate()).isBefore(endDate));
        }

        return stream.collect(Collectors.toList());
    }

    @Override
    public String getDateOfTheOldestItem() {
        loadData();
        return inventoryRecords.getFirst().getDate();
    }

    @Override
    public int getCurrentDayItemStock(SearchItemBean search) {
        //only use search.getItemID() and search.getShopID()

        String endpointURL = shopsEndpoints.getEndpointURL(search.getShopID(), ShopsEndpoints.Endpoints.getCurrentDayItemStock);

        return 300;
    }

    public int getItemsRestockCount(SearchItemBean search) {
        String endpointURL = shopsEndpoints.getEndpointURL(search.getShopID(), ShopsEndpoints.Endpoints.getItemsRestockCount);

        Random r = new Random(search.getItemID().hashCode() + search.getShopID().hashCode());
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            list.add(r.nextInt(150) - 1);
        }
        return list.get(LocalDate.parse(search.getPredictDate()).getDayOfMonth() - 1);

    }

    /**
     * is there an event in the shop during the specified date
     */
    public HashMap<String, Boolean> hasEventDuringDate(String shopID, String date, int interval) {
        String endpointURL = shopsEndpoints.getEndpointURL(shopID, ShopsEndpoints.Endpoints.hasEventDuringDate);

        Random r = new Random(date.hashCode());
        HashMap<String, Boolean> map = new HashMap<>();


        LocalDate startDate = LocalDate.parse(date);
        for (int i = 0; i < interval; i++) {
            LocalDate nextDay = startDate.plusDays(i);
            if (r.nextInt(100) > 90) {
                map.put(nextDay.toString(), true);
            }
        }

        return map;
    }
}
