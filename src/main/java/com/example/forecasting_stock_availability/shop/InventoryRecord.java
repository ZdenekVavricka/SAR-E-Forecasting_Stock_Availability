package com.example.forecasting_stock_availability.shop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class InventoryRecord {

    String date;

    private String shopID;

    private String itemID;

    private String name;

    private String unitType;

    private Long currentLevel;

    private Long soldItems;

    public Long getStartingStock(){
        return currentLevel + soldItems;
    }
}
