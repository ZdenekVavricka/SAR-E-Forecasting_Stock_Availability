package com.example.forecasting_stock_availability.prediction;

import com.example.forecasting_stock_availability.data_client.DateObject;
import com.example.forecasting_stock_availability.data_client.HolidayDataInterface;
import com.example.forecasting_stock_availability.shop.InventoryRecord;
import com.example.forecasting_stock_availability.shop.SearchItemBean;
import com.example.forecasting_stock_availability.shop.ShopsDataLoaderInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@RestController
public class Prediction {


    //běží program per obchod nebo má mít přehled o všech obchodech
    //chceme brát všechny data co jsou k dispozici, nebo jen třeba tři roky zpět a na základě nich predikovat


    @Autowired
    private HolidayDataInterface holidayApi;

    @Autowired
    private ShopsDataLoaderInterface shopsApi;

    //date, shop, item
    @GetMapping("/predict/{date}/{shop}/{item}")
    public String predictDate(@PathVariable String date, @PathVariable(value = "shop") String shop, @PathVariable String item) {
        //TODO validate inputs?


        List<InventoryRecord> inventoryRecords = shopsApi.getInventoryRecords(new SearchItemBean(null, shop, null));

        //TODO zeptat se api na svatky asi cely rook
        //TODO zjistit si prvni den kdy jsou data od nejakeho obchodu a od toho data vzit do soucasnosti vsechny svatky

        String oldestDataDateString = shopsApi.getDateOfTheOldestItem();
        LocalDate dateLocalDate = LocalDate.parse(oldestDataDateString);

        LocalDate currentDate = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(dateLocalDate, currentDate);

        List<DateObject> allDates = holidayApi.getDateInterval(oldestDataDateString, (int) daysBetween);
        //vyfiltrovat dny, co maji holliday = true + den pred a den po


        HashMap<String, Boolean> daysWeDontWantToUse = new HashMap<>();
        for (int i = 0; i < allDates.size(); i++) {
            DateObject dateObject = allDates.get(i);
            if (dateObject.getHoliday()) {
                if (i != 0) {
                    daysWeDontWantToUse.put(allDates.get(i - 1).getDate(), true);
                }
                if (i != allDates.size() - 1) {
                    daysWeDontWantToUse.put(allDates.get(i + 1).getDate(), true);
                }
                daysWeDontWantToUse.put(dateObject.getDate(), true);
            }
        }


        //init lists for days
        List<List<InventoryRecord>> sortByDay = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            sortByDay.add(new ArrayList<>());
        }

        //
        for (InventoryRecord inventoryRecord : inventoryRecords) {
            LocalDate itemDate = LocalDate.parse(inventoryRecord.getDate());
            int itemDayValue = itemDate.getDayOfWeek().getValue();
            List<InventoryRecord> list = sortByDay.get(itemDayValue - 1);//list for specific day of the week
            list.add(inventoryRecord);
        }


        for (List<InventoryRecord> list : sortByDay) { //per day
            // get all the items with the same ID

            list.stream().filter(inventoryRecord -> inventoryRecord.getItemID());

            long stockSold = 0;
            for (InventoryRecord inventoryRecord : list) { // all items from the day of the week (ex Monday)
                //zeptat se hashmapy zdali pouzit den ci ne
                if (daysWeDontWantToUse.get(inventoryRecord.getDate())) {
                    continue;
                }

                //pridat do histogramu
                stockSold += inventoryRecord.getSoldItems();
            }

        }


        //analyse per obchod


        //TODO
        return "TODO = " + date + " " + shop + " " + item;
    }


}
