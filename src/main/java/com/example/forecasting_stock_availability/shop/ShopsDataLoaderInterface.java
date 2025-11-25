package com.example.forecasting_stock_availability.shop;

import java.util.HashMap;
import java.util.List;

public interface ShopsDataLoaderInterface {

    List<InventoryRecord> loadData();

    List<InventoryRecord> getInventoryRecords(SearchItemBean search);

    String getDateOfTheOldestItem();

    int getCurrentDayItemStock(SearchItemBean search);

    int getItemsRestockCount(SearchItemBean search);

    public HashMap<String, Boolean> hasEventDuringDate(String shopID, String date, int interval);


}
