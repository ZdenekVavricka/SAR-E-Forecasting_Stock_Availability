package com.example.forecasting_stock_availability.DB;

import com.example.forecasting_stock_availability.shop.InventoryRecord;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryRecordsRepository extends MongoRepository<InventoryRecord, String> {




}
