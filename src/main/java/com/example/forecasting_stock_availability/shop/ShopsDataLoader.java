package com.example.forecasting_stock_availability.shop;


import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


@Component
public class ShopsDataLoader implements ShopsDataLoaderInterface {

    public List<InventoryRecord> inventoryRecords = null;

    private List<InventoryRecord> inventoryRecordMapper(List<String> list) {
        List<InventoryRecord> inventory = new ArrayList<>();

        //Skipping the first one
        for (int i = 1; i < list.size(); i++) {
            String[] tokens = list.get(i).split(",");
            InventoryRecord record = new InventoryRecord();
            record.setDate(tokens[0]);
            record.setShopID(tokens[1]);
            record.setItemID(tokens[2]);
            record.setName(tokens[3]);
            record.setUnitType("ks");
            record.setCurrentLevel(Long.parseLong(tokens[5]));
            record.setSoldItems(Long.parseLong(tokens[6]));

            inventory.add(record);
        }

        return inventory;
    }

    @Override
    public List<InventoryRecord> loadData() {
        System.out.println("LOADING");
        if (inventoryRecords == null){
            System.out.println("ACTUALLY LOADING");

            List<String> list = null;

            try {
                list = Files.readAllLines(Path.of("./data/retail_store_inventory.csv"), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.getStackTrace();
            }

            inventoryRecords = inventoryRecordMapper(list);

            return inventoryRecords;
        }
        System.out.println("RETURNING CASHED");

        return inventoryRecords;
    }
}
