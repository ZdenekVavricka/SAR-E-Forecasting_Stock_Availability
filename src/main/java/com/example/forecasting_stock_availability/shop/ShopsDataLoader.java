package com.example.forecasting_stock_availability.shop;


import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

            try {
                list = Files.readAllLines(Path.of("./data/retail_store_inventory.csv"), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.getStackTrace();
            }

            inventoryRecords = inventoryRecordMapper(list);

            return inventoryRecords;
        }

        System.out.println("CASHED");

        return inventoryRecords;
    }

    @Override
    public List<InventoryRecord> getInventoryRecords(SearchItemBean search) {
        loadData();

        List<InventoryRecord> inventory = new ArrayList<>();
        Stream<InventoryRecord> stream = inventoryRecords.stream();

        if (search.getDate() != null) {
            stream = stream.filter(item -> item.getDate().equals(search.getDate()));
        }

        if (search.getShopID() != null) {
            stream = stream.filter(item -> item.getShopID().equals(search.getShopID()));
        }

        if (search.getItemID() != null) {
            stream = stream.filter(item -> item.getItemID().equals(search.getItemID()));
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
        return 300;
    }

    public int getItemsRestockCount(SearchItemBean search){
        return 80;
    }


}
