package com.example.forecasting_stock_availability.shop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a daily inventory record for a specific item in a shop.
 */
@Setter
@Getter
@ToString

@Document(collection = "inventoryRecords")
public class InventoryRecord {


    /**
     * Composed identifier in form {@code date-shopID-itemID}.
     */
    @Id
    String recordID;

    /**
     * Record date (YYYY-MM-DD).
     */
    String date;

    /**
     * Unique shop identifier.
     */
    private String shopID;

    /**
     * Unique item identifier.
     */
    private String itemID;

    /**
     * Human-readable item name.
     */
    private String name;

    /**
     * Unit of measure (e.g., pcs).
     */
    private String unitType;

    /**
     * Current stock level at the end of the day.
     */
    private int currentLevel;

    /**
     * Number of items sold during the day.
     */
    private int soldItems;

    /**
     * black friday, or cyber monday. NOT HOLLIDAY. Either 0 or 1 (false, true)
     */
    private String duringEvent;

    /**
     * Number of items restocked during the day.
     */
    private int restockCount;

    /**
     * Returns starting stock for the day (current level + sold items).
     *
     * @return starting stock
     */
    public int getStartingStock(){
        return currentLevel + soldItems;
    }
}
