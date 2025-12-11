package com.example.forecasting_stock_availability.shop;

import java.util.HashMap;
import java.util.List;

public interface ShopsDataLoaderInterface {

    List<InventoryRecord> loadData();

    public HashMap<String, Boolean> hasEventDuringDate(String shopID, String date, int interval);
}
