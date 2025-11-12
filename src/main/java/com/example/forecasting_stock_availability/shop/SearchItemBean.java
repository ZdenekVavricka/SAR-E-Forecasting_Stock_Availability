package com.example.forecasting_stock_availability.shop;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchItemBean {
    private String date;
    private String shopID;
    private String itemID;

    public SearchItemBean(String date, String shopID, String itemID) {
        this.date = date;
        this.shopID = shopID;
        this.itemID = itemID;
    }


    public SearchItemBean(String shopID, String itemID){
        this.shopID = shopID;
        this.itemID = itemID;
    }

    public SearchItemBean(String shopID){
        this.shopID = shopID;
    }

    public SearchItemBean(){
    }

}
