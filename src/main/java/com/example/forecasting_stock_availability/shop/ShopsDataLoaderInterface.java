package com.example.forecasting_stock_availability.shop;

import java.util.List;

public interface ShopsDataLoaderInterface {

    List<InventoryRecord> loadData();

    List<InventoryRecord> getInventoryRecordsByDate(String date);

    List<InventoryRecord> getInventoryRecordsByShop(String shopID);

    List<InventoryRecord> getInventoryRecordsByItem(String itemID);
}
