package com.example.forecasting_stock_availability.DB;

import com.example.forecasting_stock_availability.shop.InventoryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for managing {@link com.example.forecasting_stock_availability.shop.InventoryRecord} entities.
 */
@Service
public class InventoryRecordsManager {

    /**
     * Spring Data repository for inventory records.
     */
    @Autowired
    private InventoryRecordsRepository inventoryRecordsRepository;

    /**
     * Fetches all inventory records from the database.
     *
     * @return list of all inventory records
     */
    public List<InventoryRecord> getAllInventoryRecords() {
        return inventoryRecordsRepository.findAll();
    }

    /**
     * Save a list of inventory records.
     *
     * @param inventoryRecords list of records to save
     */
    public void saveInventoryRecords(List<InventoryRecord> inventoryRecords) {
        inventoryRecordsRepository.saveAll(inventoryRecords);
    }

    /**
     * Finds records for a shop and item within the provided date interval (inclusive).
     *
     * @param shopID   shop identifier
     * @param itemID   item identifier
     * @param startDate start date (YYYY-MM-DD)
     * @param endDate   end date (YYYY-MM-DD)
     * @return list of matching inventory records
     */
    public List<InventoryRecord> findByShopItemStartDateEndDate(String shopID, String itemID, String startDate, String endDate) {
        return inventoryRecordsRepository.findByShopItemStartDateEndDate(shopID, itemID, startDate, endDate);
    }


    /**
     * Retrieves a record by its composed identifier.
     *
     * @param composedID record identifier (date-shopID-itemID)
     * @return inventory record or null if not found
     */
    public InventoryRecord findById(String composedID) {
        return inventoryRecordsRepository.findById(composedID).orElse(null);
    }

    /**
     * Returns restock count for a specific item on a specific date in a shop.
     *
     * @param date   date (YYYY-MM-DD)
     * @param shopID shop identifier
     * @param itemID item identifier
     * @return restock count or 0 if no record exists
     */
    public int getItemsRestockCount(String date, String shopID, String itemID) {
        InventoryRecord inventoryRecord = inventoryRecordsRepository.findById(date + "-" + shopID + "-" + itemID).orElse(null);
        if (inventoryRecord == null) {
            return 0;
        }
        return inventoryRecord.getRestockCount();
    }
}
