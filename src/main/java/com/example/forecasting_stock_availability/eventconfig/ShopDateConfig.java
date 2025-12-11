package com.example.forecasting_stock_availability.eventconfig;

import lombok.Data;

import java.util.List;

@Data
public class ShopDateConfig {
    private String shopID;
    private List<String> dates;
}



