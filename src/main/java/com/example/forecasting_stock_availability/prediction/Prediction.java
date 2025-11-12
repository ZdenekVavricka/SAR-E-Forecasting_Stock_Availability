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
    // existují záznamy o položklách o svátkách


    @Autowired
    private HolidayDataInterface holidayApi;

    @Autowired
    private ShopsDataLoaderInterface shopsApi;

    //date, shop, item
    @GetMapping("/predict/{date}/{shop}/{item}")
    public String predictDate(@PathVariable String date, @PathVariable(value = "shop") String shop, @PathVariable String item) {
        //TODO validate inputs?


        List<InventoryRecord> inventoryRecords = shopsApi.getInventoryRecords(new SearchItemBean(shop));
        String oldestDataDateString = shopsApi.getDateOfTheOldestItem();
        LocalDate oldestDataDate = LocalDate.parse(oldestDataDateString);

        LocalDate currentDate = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(oldestDataDate, currentDate);

        List<DateObject> allDates = holidayApi.getDateInterval(oldestDataDateString, (int) daysBetween);
        //vyfiltrovat dny, co maji holliday = true + den pred a den po

        HashMap<String, Boolean> daysWeDontWantToUse = getHolidays(allDates);

        //init lists for days
        List<HashMap<String, List<InventoryRecord>>> sameItemsInDayOfWeek = new ArrayList<>();

        HashMap<String, Integer> itemAverages = new HashMap<>();
        HashMap<String, Integer> itemAveragesHolidays = new HashMap<>();


        for (int i = 0; i < 7; i++) {
            sameItemsInDayOfWeek.add(new HashMap<>());
        }

        //FIXME nedělat to pro všechny položky, ale pouze pro parametry z hlavičky metody

        // sorts items into 7 boxes (days) by their ID (hashmap)
        for (InventoryRecord inventoryRecord : inventoryRecords) {
            LocalDate itemDate = LocalDate.parse(inventoryRecord.getDate());
            int itemDayValue = itemDate.getDayOfWeek().getValue();
            HashMap<String, List<InventoryRecord>> hashMap = sameItemsInDayOfWeek.get(itemDayValue - 1); //list for specific day of the week
            List<InventoryRecord> list = hashMap.getOrDefault(inventoryRecord.getItemID(), new ArrayList<>());
            list.add(inventoryRecord);
            hashMap.put(inventoryRecord.getItemID(), list);
        }

        for (HashMap<String, List<InventoryRecord>> hashMap : sameItemsInDayOfWeek) { //per day
            // get all the items with the same ID

            hashMap.forEach((id, allItemsWithSameID) -> {
                int stockSold = 0;
                int stockSoldHoliday = 0;
                int countNormal = 0;
                int countHoliday = 0;
                for (InventoryRecord inventoryRecord : allItemsWithSameID) {
                    if (daysWeDontWantToUse.get(inventoryRecord.getDate()) == null) {
                        stockSold += inventoryRecord.getSoldItems();
                        countNormal++;
                    } else {
                        stockSoldHoliday += inventoryRecord.getSoldItems();
                        countHoliday++;
                    }
                }
                int average = (int) (stockSold / (double) countNormal); //FIXME could crash /0
                int averageHoliday = (int) (stockSoldHoliday / (double) countHoliday); //FIXME could crash /0
                itemAverages.put(id, average);
                itemAveragesHolidays.put(id, averageHoliday);
            });
        }


        //pokud je den predikce dále nž 7 dní, tak kšá
        //jinak získáme počet dní v budoucu
        //pro každý den udělat predikci a na základě této predikce udělat další predikci
        //Si = Si-1 − Di + Ri


        //what day is date
        LocalDate orderDate = LocalDate.parse(date);


        long daysBetweenNowAndAskedPrediction = ChronoUnit.DAYS.between(currentDate, orderDate);



        if (daysBetweenNowAndAskedPrediction > 7) {
            return "Can't predict more than 7 days into the future!!!";
        }

        List<DateObject> futurePossibleHolidays = holidayApi.getWeek(date);
        HashMap<String, Boolean> futureHolidays = getHolidays(futurePossibleHolidays);

        int current = shopsApi.getCurrentDayItemStock(new SearchItemBean(shop, item));

        for (int i = 0; i <= daysBetweenNowAndAskedPrediction; i++) {
            LocalDate dayAhead = ChronoUnit.DAYS.addTo(currentDate, i);

            if (futureHolidays.get(dayAhead.toString()) == null) {
                current -= itemAverages.getOrDefault(item, 0);
            } else {
                current -= itemAveragesHolidays.getOrDefault(item, 0);
            }
            System.out.println("i = " + i);
            System.out.println("current pred restock = " + current);

            //do not add restock on the last iteration
            if (i==daysBetweenNowAndAskedPrediction){
                break;
            }
            //pridat restock ke current
            current += shopsApi.getItemsRestockCount(new SearchItemBean(ChronoUnit.DAYS.addTo(currentDate, i+1).toString(), shop, item));
            System.out.println("current po restock = " + current);

        }

        if(current<0){
            return "Predicted stock is below 0";
        }


        //TODO ask holliday api for future events


        return "date = " + date + ", item = " + item + ", shop =  " + shop + ", average = " + itemAverages.get(item) + ", average holiday = " + itemAveragesHolidays.get(item) + ", Prediction = " + current;
    }

    private static HashMap<String, Boolean> getHolidays(List<DateObject> allDates) {
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
        return daysWeDontWantToUse;
    }


}
