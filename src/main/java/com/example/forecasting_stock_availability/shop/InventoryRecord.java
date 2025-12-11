package com.example.forecasting_stock_availability.shop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@ToString

@Document(collection = "inventoryRecords")
public class InventoryRecord {

    @Id
    String recordID;

    String date;

    private String shopID;

    private String itemID;

    private String name;

    private String unitType;

    private int currentLevel;

    private int soldItems;

    /**
     * black friday, or cyber monday. NOT HOLLIDAY. Either 0 or 1 (false, true)
     */
    private String duringEvent;

    private int restockCount;

    public int getStartingStock(){
        return currentLevel + soldItems;
    }
}
