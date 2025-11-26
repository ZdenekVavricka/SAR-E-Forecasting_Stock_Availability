package com.example.forecasting_stock_availability.DB;

import com.example.forecasting_stock_availability.shop.InventoryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryRecordsManager {

    @Autowired
    private  InventoryRecordsRepository inventoryRecordsRepository;

    public List<InventoryRecord> getAllInventoryRecords() {
        return inventoryRecordsRepository.findAll();
    }

    public void saveInventoryRecords(List<InventoryRecord> inventoryRecords) {
        inventoryRecordsRepository.saveAll(inventoryRecords);
    }
}
