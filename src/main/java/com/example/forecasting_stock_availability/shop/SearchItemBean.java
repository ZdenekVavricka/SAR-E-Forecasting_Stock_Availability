package com.example.forecasting_stock_availability.shop;

import lombok.Data;

@Data
public class SearchItemBean {
    private String predictDate;
    private String dataStartDate;
    private String dataEndDate;
    private String shopID;
    private String itemID;


}
