package com.example.forecasting_stock_availability.dataupload;

import com.example.forecasting_stock_availability.DB.InventoryRecordsManager;
import com.example.forecasting_stock_availability.shop.InventoryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;


/**
 * REST endpoint for uploading inventory records in bulk as JSON.
 */
@Component
@RestController
public class UploadDataEndpoint {

    /**
     * Service managing persistence of inventory records.
     */
    @Autowired
    InventoryRecordsManager inventoryRecordsManager;


    /**
     * Uploads a list of inventory records. Performs basic validation and assigns composed IDs.
     *
     * <p>Example curl:
     * curl -X PUT "http://localhost:8080/upload" -H "Content-Type: application/json" --data "@example_data_restock.json"
     *
     * @param inventoryRecords list of records to upload
     * @return HTTP 200 on success with count, or 400 with validation error message
     */
    @PutMapping("/upload")
    public ResponseEntity<?> uploadJson(@RequestBody List<InventoryRecord> inventoryRecords) {

        for (InventoryRecord inventoryRecord : inventoryRecords) {
            if (inventoryRecord.getDate() == null) {
                return ResponseEntity.badRequest()
                        .body("Date is missing for record: " + inventoryRecord);
            }

            if (!isValidDate(inventoryRecord.getDate())) {
                return ResponseEntity.badRequest()
                        .body("Date format is invalid for record: " + inventoryRecord);
            }

            if (inventoryRecord.getShopID() == null) {
                return ResponseEntity.badRequest()
                        .body("ShopID is missing for record: " + inventoryRecord);
            }

            if (inventoryRecord.getItemID() == null) {
                return ResponseEntity.badRequest()
                        .body("ItemID is missing for record: " + inventoryRecord);
            }


            if (inventoryRecord.getDuringEvent() == null) {
                return ResponseEntity.badRequest()
                        .body("UnitType is missing for record: " + inventoryRecord);
            }

            String newID = inventoryRecord.getDate() + "-" + inventoryRecord.getShopID() + "-" + inventoryRecord.getItemID();
            inventoryRecord.setRecordID(newID);
        }

        inventoryRecordsManager.saveInventoryRecords(inventoryRecords);

        return ResponseEntity.ok("Uploaded: " + inventoryRecords.size() + " records.");
    }


    /**
     * Validates ISO date format (YYYY-MM-DD).
     *
     * @param dateStr date string to validate
     * @return true if valid ISO date, false otherwise
     */
    public boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
