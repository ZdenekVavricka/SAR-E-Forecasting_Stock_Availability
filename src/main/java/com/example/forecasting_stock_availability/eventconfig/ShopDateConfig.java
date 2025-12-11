package com.example.forecasting_stock_availability.eventconfig;

import lombok.Data;

import java.util.List;

/**
 * Configuration of special event dates for a single shop.
 */
@Data
public class ShopDateConfig {
    /**
     * Unique identifier of the shop.
     */
    private String shopID;
    /**
     * Sorted list of event dates (YYYY-MM-DD) for the shop.
     */
    private List<String> dates;
}



