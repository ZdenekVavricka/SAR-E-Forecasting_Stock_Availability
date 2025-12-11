package com.example.forecasting_stock_availability.DB;

import com.example.forecasting_stock_availability.shop.InventoryRecord;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Spring Data MongoDB repository for {@link InventoryRecord} documents.
 */
public interface InventoryRecordsRepository extends MongoRepository<InventoryRecord, String> {


    /**
     * Finds records for a given shop and item in the inclusive date range.
     *
     * @param shopID    shop identifier
     * @param itemID    item identifier
     * @param startDate start date (YYYY-MM-DD)
     * @param endDate   end date (YYYY-MM-DD)
     * @return list of matching records
     */
    @Query("{'shopID': ?0, 'itemID': ?1, 'date': { $gte: ?2, $lte: ?3 } }")
    List<InventoryRecord> findByShopItemStartDateEndDate(String shopID, String itemID, String startDate, String endDate);


}
