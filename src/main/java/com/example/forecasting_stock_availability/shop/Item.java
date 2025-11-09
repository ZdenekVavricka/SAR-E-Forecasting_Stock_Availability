package com.example.forecasting_stock_availability.shop;

import lombok.Getter;
import lombok.Setter;

public class Item {
    @Setter @Getter
    private Long id;
    @Setter @Getter
    private String name;
    @Setter @Getter
    private String quantity;
    @Setter @Getter
    private String unitType;

}
