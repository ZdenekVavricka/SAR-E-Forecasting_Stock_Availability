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

import java.time.LocalDate;
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
        HOLIDAYS_BEFORE,
        HOLIDAYS_AFTER,
        EVENT
    }

    @Autowired
    private HolidayDataInterface holidayApi;

    @Autowired
    private ShopsDataLoaderInterface shopsApi;

    // date, shop, item
    // add unit of measure
    @GetMapping("/predict/{date}/{shop}/{item}")
    public String predictDate(@PathVariable(value = "date") String date, @PathVariable(value = "shop") String shop, @PathVariable(value = "item") String item) {
        return "prediction = " + predict(new SearchItemBean(date, shop, item));
    }

    private String predict(SearchItemBean search) {
        List<InventoryRecord> inventoryRecords = shopsApi.getInventoryRecords(new SearchItemBean(search.getShopID()));
        return null;
    }

    /*
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
*/
    private HashMap<String, Boolean> getDayBeforeHolidays(List<DateObject> allDates) {
        HashMap<String, Boolean> daysWeDontWantToUse = new HashMap<>();
        for (int i = 0; i < allDates.size(); i++) {
            DateObject dateObject = allDates.get(i);
            if (dateObject.getHoliday()) {
                if (i != 0) {
                    daysWeDontWantToUse.put(allDates.get(i - 1).getDate(), true);
                }
            }
        }
        return daysWeDontWantToUse;
    }

    private HashMap<String, Boolean> getDayAfterHolidays(List<DateObject> allDates) {
        HashMap<String, Boolean> daysWeDontWantToUse = new HashMap<>();
        for (int i = 0; i < allDates.size(); i++) {
            DateObject dateObject = allDates.get(i);
            if (dateObject.getHoliday()) {
                if (i != allDates.size() - 1) {
                    daysWeDontWantToUse.put(allDates.get(i + 1).getDate(), true);
                }
            }
        }
        return daysWeDontWantToUse;
    }

    private HashMap<String, Boolean> getHolidaysOnly(List<DateObject> allDates) {
        HashMap<String, Boolean> daysWeDontWantToUse = new HashMap<>();
        for (int i = 0; i < allDates.size(); i++) {
            DateObject dateObject = allDates.get(i);
            if (dateObject.getHoliday()) {
                daysWeDontWantToUse.put(dateObject.getDate(), true);
            }
        }
        return daysWeDontWantToUse;
    }

    /**
     * inventoryRecords - must be for sepcific item and shop
     */
    private HashMap<String, Boolean> boxify(List<InventoryRecord> inventoryRecords, @NonNull Event event) {

        switch (event) {
            case DAY_OF_WEEK:
                // 7 škatulek podle dní
                calcAveragesByWeek(inventoryRecords);
                break;
            case HOLIDAYS:
                // 1 škatulka svátky
                calcAveragesByHolidays(inventoryRecords);
                break;
            case HOLIDAYS_BEFORE:
                // 1 škatulka dny před svátky
                calcAveragesByHolidaysBefore(inventoryRecords);
                break;
            case HOLIDAYS_AFTER:
                // 1 škatulka dny po svátcích
                calcAveragesByHolidaysAfter(inventoryRecords);
                break;
            case QUARTERS:
                // 4 škatulky kvartály
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

    private HashMap<Integer, Integer> calcAveragesByWeek(List<InventoryRecord> inventoryRecords) {
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

    private HashMap<Integer, Integer> calcAveragesByHolidays(List<InventoryRecord> inventoryRecords) {
        HashMap<Integer, Integer> holidaysAverages = new HashMap<>();

        HashMap<Integer, List<InventoryRecord>> holidaysInventory = new HashMap<>();
        //init
        holidaysInventory.put(0, new ArrayList<>());

        LocalDate oldestDate = inventoryRecords.stream().map(ir -> LocalDate.parse(ir.getDate())).min(LocalDate::compareTo).orElse(null);
        LocalDate latestDate = inventoryRecords.stream().map(ir -> LocalDate.parse(ir.getDate())).max(LocalDate::compareTo).orElse(null);

        long daysBetween = ChronoUnit.DAYS.between(oldestDate, latestDate);
        List<DateObject> allDates = holidayApi.getDateInterval(oldestDate.toString(), (int) daysBetween);

        HashMap<String, Boolean> holidays = getHolidaysOnly(allDates);

        for (InventoryRecord inventoryRecord : inventoryRecords) {
            if (holidays.get(inventoryRecord.getDate())!= null) {
                holidaysInventory.get(0).add(inventoryRecord);
            }
        }

        holidaysInventory.forEach((key, value) -> {
            int soldItems = 0;

            for (InventoryRecord inventoryRecord : value) {
                soldItems += inventoryRecord.getSoldItems();
            }

            if (value.isEmpty()) {
                holidaysAverages.put(key, 0);
            } else {
                int averageSold = (int) (soldItems / (double) value.size());
                holidaysAverages.put(key, averageSold);
            }
        });

        return holidaysAverages;
    }

    private HashMap<Integer, Integer> calcAveragesByHolidaysBefore(List<InventoryRecord> inventoryRecords) {
        HashMap<Integer, Integer> holidaysBeforeAverages = new HashMap<>();

        HashMap<Integer, List<InventoryRecord>> holidaysBeforeInventory = new HashMap<>();
        //init
        holidaysBeforeInventory.put(0, new ArrayList<>());

        LocalDate oldestDate = inventoryRecords.stream().map(ir -> LocalDate.parse(ir.getDate())).min(LocalDate::compareTo).orElse(null);
        LocalDate latestDate = inventoryRecords.stream().map(ir -> LocalDate.parse(ir.getDate())).max(LocalDate::compareTo).orElse(null);

        long daysBetween = ChronoUnit.DAYS.between(oldestDate, latestDate);
        List<DateObject> allDates = holidayApi.getDateInterval(oldestDate.toString(), (int) daysBetween);

        HashMap<String, Boolean> holidays = getDayBeforeHolidays(allDates);

        for (InventoryRecord inventoryRecord : inventoryRecords) {
            if (holidays.get(inventoryRecord.getDate())!= null) {
                holidaysBeforeInventory.get(0).add(inventoryRecord);
            }
        }

        holidaysBeforeInventory.forEach((key, value) -> {
            int soldItems = 0;

            for (InventoryRecord inventoryRecord : value) {
                soldItems += inventoryRecord.getSoldItems();
            }

            if (value.isEmpty()) {
                holidaysBeforeAverages.put(key, 0);
            } else {
                int averageSold = (int) (soldItems / (double) value.size());
                holidaysBeforeAverages.put(key, averageSold);
            }
        });

        return holidaysBeforeAverages;
    }
    
    private HashMap<Integer, Integer> calcAveragesByHolidaysAfter(List<InventoryRecord> inventoryRecords) {
        HashMap<Integer, Integer> holidaysAfterAverages = new HashMap<>();

        HashMap<Integer, List<InventoryRecord>> holidaysAfterInventory = new HashMap<>();
        //init
        holidaysAfterInventory.put(0, new ArrayList<>());

        LocalDate oldestDate = inventoryRecords.stream().map(ir -> LocalDate.parse(ir.getDate())).min(LocalDate::compareTo).orElse(null);
        LocalDate latestDate = inventoryRecords.stream().map(ir -> LocalDate.parse(ir.getDate())).max(LocalDate::compareTo).orElse(null);

        long daysBetween = ChronoUnit.DAYS.between(oldestDate, latestDate);
        List<DateObject> allDates = holidayApi.getDateInterval(oldestDate.toString(), (int) daysBetween);

        HashMap<String, Boolean> holidays = getDayAfterHolidays(allDates);

        for (InventoryRecord inventoryRecord : inventoryRecords) {
            if (holidays.get(inventoryRecord.getDate())!= null) {
                holidaysAfterInventory.get(0).add(inventoryRecord);
            }
        }

        holidaysAfterInventory.forEach((key, value) -> {
            int soldItems = 0;

            for (InventoryRecord inventoryRecord : value) {
                soldItems += inventoryRecord.getSoldItems();
            }

            if (value.isEmpty()) {
                holidaysAfterAverages.put(key, 0);
            } else {
                int averageSold = (int) (soldItems / (double) value.size());
                holidaysAfterAverages.put(key, averageSold);
            }
        });

        return holidaysAfterAverages;
    }

    private HashMap<Integer, Integer> calcAveragesByQuarters(List<InventoryRecord> inventoryRecords) {
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

            List<InventoryRecord> quarter = quartersInventory.get(i / 3);
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

    private HashMap<Integer, Integer> calcAveragesByMonths(List<InventoryRecord> inventoryRecords) {
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

    private HashMap<Integer, Integer> calcAveragesByEvent(List<InventoryRecord> inventoryRecords) {
        //TODO we need an endpoint
        return null;
    }

}
