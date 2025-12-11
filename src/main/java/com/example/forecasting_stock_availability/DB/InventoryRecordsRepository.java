package com.example.forecasting_stock_availability.DB;

import com.example.forecasting_stock_availability.shop.InventoryRecord;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface InventoryRecordsRepository extends MongoRepository<InventoryRecord, String> {


    @Query("{'shopID': ?0, 'itemID': ?1, 'date': { $gte: ?2, $lte: ?3 } }")
    List<InventoryRecord> findByShopItemStartDateEndDate(String shopID, String itemID, String startDate, String endDate);


}
