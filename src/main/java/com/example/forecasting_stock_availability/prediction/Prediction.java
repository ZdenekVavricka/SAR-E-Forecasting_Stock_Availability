package com.example.forecasting_stock_availability.prediction;

import com.example.forecasting_stock_availability.data_client.DateObject;
import com.example.forecasting_stock_availability.data_client.HolidayDataInterface;
import com.example.forecasting_stock_availability.shop.InventoryRecord;
import com.example.forecasting_stock_availability.shop.SearchItemBean;
import com.example.forecasting_stock_availability.shop.ShopsDataLoaderInterface;
import lombok.NonNull;
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

    enum Event {
        DAY_OF_WEEK,
        HOLIDAYS,
        QUARTERS,
        MONTHS,
        HOLIDAYS_AFTER_BEFORE,
        EVENT
    }


    @Autowired
    private HolidayDataInterface holidayApi;

    @Autowired
    private ShopsDataLoaderInterface shopsApi;

    //date, shop, item
    // add unit of measure
    @GetMapping("/predict/{date}/{shop}/{item}")
    public String predictDate(@PathVariable(value = "date") String date, @PathVariable(value = "shop") String shop, @PathVariable(value = "item") String item) {
        return "prediction = " + predict(new SearchItemBean(date, shop, item));
    }

    private String predict(SearchItemBean search) {
        //TODO validate inputs?


        List<InventoryRecord> inventoryRecords = shopsApi.getInventoryRecords(new SearchItemBean(search.getShopID()));
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


        //what day is date
        LocalDate orderDate = LocalDate.parse(search.getDate());


        long daysBetweenNowAndAskedPrediction = ChronoUnit.DAYS.between(currentDate, orderDate);


        if (daysBetweenNowAndAskedPrediction > 7) {
            return "Cannot predict more than 7 days into the future";
        }

        List<DateObject> futurePossibleHolidays = holidayApi.getWeek(search.getDate());
        HashMap<String, Boolean> futureHolidays = getHolidays(futurePossibleHolidays);

        int current = shopsApi.getCurrentDayItemStock(new SearchItemBean(search.getShopID(), search.getItemID()));

        for (int i = 0; i <= daysBetweenNowAndAskedPrediction; i++) {
            LocalDate dayAhead = ChronoUnit.DAYS.addTo(currentDate, i);

            if (futureHolidays.get(dayAhead.toString()) == null) {
                current -= itemAverages.getOrDefault(search.getItemID(), 0);
            } else {
                current -= itemAveragesHolidays.getOrDefault(search.getItemID(), 0);
            }

            //do not add restock on the last iteration
            if (i == daysBetweenNowAndAskedPrediction) {
                break;
            }
            //add restoct to current stock
            current += shopsApi.getItemsRestockCount(new SearchItemBean(ChronoUnit.DAYS.addTo(currentDate, i + 1).toString(), search.getShopID(), search.getItemID()));

        }


        //TODO ask holliday api for future events

        System.out.println("date = " + search.getDate() + ", item = " + search.getItemID() + ", shop =  " + search.getShopID() + ", average = " + itemAverages.get(search.getItemID()) + ", average holiday = " + itemAveragesHolidays.get(search.getItemID()) + ", Prediction = " + current);

        //TODO maybe return JSON with the predicted number
        return "Predicted stock: " + current;
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

    /**
     * inventoryRecords - must be for sepcific item and shop
     */
    private static HashMap<String, Boolean> boxify(List<InventoryRecord> inventoryRecords, @NonNull Event event) {

        switch (event) {
            case DAY_OF_WEEK:
                // 7 škatulek podle dní
                calcAveragesByWeek(inventoryRecords);
                break;
            case HOLIDAYS:
                // 1 škatulka svátky
                calcAveragesByHolidays(inventoryRecords);
                break;
            case HOLIDAYS_AFTER_BEFORE:
                // 1 škatulka svátky
                calcAveragesByHolidaysAfterBefore(inventoryRecords);
                break;
            case QUARTERS:
                // 4 škatulka kvartály
                calcAveragesByQuarters(inventoryRecords);
                break;
            case MONTHS:
                // 12 škatulek měsíce
                calcAveragesByMonths(inventoryRecords);
                break;
            case EVENT:
                // 1 škatulka event
                calcAveragesByEvent(inventoryRecords);
                break;
        }

        return null;
    }

    private static HashMap<Integer, Integer> calcAveragesByWeek(List<InventoryRecord> inventoryRecords) {
        HashMap<Integer, List<InventoryRecord>> weeksInventory = new HashMap<>();
        HashMap<Integer, Integer> weeksAverages = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            weeksInventory.put(i, new ArrayList<>());
        }

        for (InventoryRecord inventoryRecord : inventoryRecords) {
            String stringDate = inventoryRecord.getDate();
            LocalDate date = LocalDate.parse(stringDate);
            weeksInventory.get(date.getDayOfWeek().getValue() - 1).add(inventoryRecord);
        }

        weeksInventory.forEach((key, value) -> {
            int soldItems = 0;

            for (InventoryRecord inventoryRecord : value) {
                soldItems += inventoryRecord.getSoldItems();
            }

            if (value.isEmpty()) {
                weeksAverages.put(key, 0);
            } else {
                int averageSold = (int) (soldItems / (double) value.size());
                weeksAverages.put(key, averageSold);
            }
        });

        return weeksAverages;
    }

    private static HashMap<Integer, Integer> calcAveragesByHolidays(List<InventoryRecord> inventoryRecords) {
        return null;
    }

    private static HashMap<Integer, Integer> calcAveragesByHolidaysAfterBefore(List<InventoryRecord> inventoryRecords) {
        return null;
    }

    private static HashMap<Integer, Integer> calcAveragesByQuarters(List<InventoryRecord> inventoryRecords) {
        HashMap<Integer, List<InventoryRecord>> monthsInventory = new HashMap<>();
        HashMap<Integer, List<InventoryRecord>> quartersInventory = new HashMap<>();
        HashMap<Integer, Integer> quartersAverages = new HashMap<>();

        for (int i = 0; i < 4; i++) {
            quartersInventory.put(i, new ArrayList<>());
        }

        for (int i = 0; i < 12; i++) {
            monthsInventory.put(i, new ArrayList<>());
        }

        for (InventoryRecord inventoryRecord : inventoryRecords) {
            String stringDate = inventoryRecord.getDate();
            LocalDate date = LocalDate.parse(stringDate);
            monthsInventory.get(date.getMonthValue() - 1).add(inventoryRecord);
        }

        for (int i = 0; i < 12; i += 3) {
            List<InventoryRecord> month1 = monthsInventory.get(i);
            List<InventoryRecord> month2 = monthsInventory.get(i + 1);
            List<InventoryRecord> month3 = monthsInventory.get(i + 2);

            List<InventoryRecord> quarter = quartersInventory.get(i/3);
            quarter.addAll(month1);
            quarter.addAll(month2);
            quarter.addAll(month3);

            quartersInventory.put(i / 3, quarter);
        }

        quartersInventory.forEach((key, value) -> {
            int soldItems = 0;

            for (InventoryRecord inventoryRecord : value) {
                soldItems += inventoryRecord.getSoldItems();
            }

            if (value.isEmpty()) {
                quartersAverages.put(key, 0);
            } else {
                int averageSold = (int) (soldItems / (double) value.size());
                quartersAverages.put(key, averageSold);
            }
        });

        return quartersAverages;
    }

    private static HashMap<Integer, Integer> calcAveragesByMonths(List<InventoryRecord> inventoryRecords) {
        HashMap<Integer, List<InventoryRecord>> monthsInventory = new HashMap<>();
        HashMap<Integer, Integer> monthsAverages = new HashMap<>();

        for (int i = 0; i < 12; i++) {
            monthsInventory.put(i, new ArrayList<>());
        }

        for (InventoryRecord inventoryRecord : inventoryRecords) {
            String stringDate = inventoryRecord.getDate();
            LocalDate date = LocalDate.parse(stringDate);
            monthsInventory.get(date.getMonthValue() - 1).add(inventoryRecord);
        }

        monthsInventory.forEach((key, value) -> {
            int soldItems = 0;

            for (InventoryRecord inventoryRecord : value) {
                soldItems += inventoryRecord.getSoldItems();
            }

            if (value.isEmpty()) {
                monthsAverages.put(key, 0);
            } else {
                int averageSold = (int) (soldItems / (double) value.size());
                monthsAverages.put(key, averageSold);
            }
        });

        return monthsAverages;
    }

    private static HashMap<Integer, Integer> calcAveragesByEvent(List<InventoryRecord> inventoryRecords) {
        return null;
    }

}
