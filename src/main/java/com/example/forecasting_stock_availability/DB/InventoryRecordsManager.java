package com.example.forecasting_stock_availability.DB;

import com.example.forecasting_stock_availability.shop.InventoryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryRecordsManager {

    @Autowired
    private InventoryRecordsRepository inventoryRecordsRepository;

    public List<InventoryRecord> getAllInventoryRecords() {

        return inventoryRecordsRepository.findAll();
    }

    public void saveInventoryRecords(List<InventoryRecord> inventoryRecords) {
        inventoryRecordsRepository.saveAll(inventoryRecords);

    }

    public List<InventoryRecord> findByShopItemStartDateEndDate(String shopID, String itemID, String startDate, String endDate) {
        return inventoryRecordsRepository.findByShopItemStartDateEndDate(shopID, itemID, startDate, endDate);
    }


    public InventoryRecord findById(String s) {
        return inventoryRecordsRepository.findById(s).orElse(null);
    }

    public int getItemsRestockCount(String shopID, String itemID, String date) {
        InventoryRecord inventoryRecord = inventoryRecordsRepository.findById(shopID + "-" + itemID + "-" + date).orElse(null);
        if (inventoryRecord == null) {
            return 0;
        }

        return inventoryRecord.getRestockCount();

    }
}
