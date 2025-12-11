package com.example.forecasting_stock_availability.shop;

import lombok.Data;

/**
 * Request bean specifying inputs for prediction and data window.
 */
@Data
public class SearchItemBean {
    /**
     * Target date to predict (YYYY-MM-DD).
     */
    private String predictDate;
    /**
     * Start date for historical data window (YYYY-MM-DD).
     */
    private String dataStartDate;
    /**
     * End date for historical data window (YYYY-MM-DD).
     */
    private String dataEndDate;
    /**
     * Shop identifier.
     */
    private String shopID;
    /**
     * Item identifier.
     */
    private String itemID;
}
