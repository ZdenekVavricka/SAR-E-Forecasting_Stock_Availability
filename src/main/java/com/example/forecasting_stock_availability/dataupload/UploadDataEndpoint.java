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


@Component
@RestController
public class UploadDataEndpoint {

    @Autowired
    InventoryRecordsManager inventoryRecordsManager;


    //curl -X PUT "http://localhost:8080/upload"   -H "Content-Type: application/json"   --data "@example_data.json"
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



            inventoryRecord.setRecordID(inventoryRecord.getDate() + "-" + inventoryRecord.getShopID() + "-" + inventoryRecord.getItemID());
        }

        inventoryRecordsManager.saveInventoryRecords(inventoryRecords);

        return ResponseEntity.ok("Uploaded: " + inventoryRecords.size() + " records.");
    }


    public boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
