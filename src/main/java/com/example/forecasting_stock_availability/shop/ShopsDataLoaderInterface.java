package com.example.forecasting_stock_availability.shop;

import java.util.List;

public interface ShopsDataLoaderInterface {

    List<InventoryRecord> loadData();

    List<InventoryRecord> getInventoryRecords(SearchItemBean search);

    String getDateOfTheOldestItem();

    int getCurrentDayItemStock(SearchItemBean search);

    int getItemsRestockCount(SearchItemBean search);
}
