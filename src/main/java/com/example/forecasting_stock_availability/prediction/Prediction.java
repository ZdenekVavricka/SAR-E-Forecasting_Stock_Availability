package com.example.forecasting_stock_availability.prediction;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
public class Prediction {

    //date, shop, item
    @GetMapping("/predict/{date}/{shop}/{item}")
    public static String predictDate(@PathVariable String date, @PathVariable String shop, @PathVariable String item) {
        //validate inputs


        //TODO
        return "TODO = " + date + " " + shop + " " + item;
    }

}
