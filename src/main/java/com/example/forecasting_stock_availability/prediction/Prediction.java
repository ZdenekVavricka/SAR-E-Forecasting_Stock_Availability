package com.example.forecasting_stock_availability.prediction;

import com.example.forecasting_stock_availability.data_client.DateObject;
import com.example.forecasting_stock_availability.data_client.HolidayDataInterface;
import com.example.forecasting_stock_availability.shop.InventoryRecord;
import com.example.forecasting_stock_availability.shop.SearchItemBean;
import com.example.forecasting_stock_availability.shop.ShopsDataLoaderInterface;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties(prefix = "prediction")
public class Prediction {

    @Getter
    @Setter
    private double dayOfWeekMultiplier;
    @Getter
    @Setter
    private double holidaysMultiplier;
    @Getter
    @Setter
    private double quartersMultiplier;
    @Getter
    @Setter
    private double monthsMultiplier;
    @Getter
    @Setter
    private double holidaysBeforeMultiplier;
    @Getter
    @Setter
    private double holidaysAfterMultiplier;
    @Getter
    @Setter
    private double eventMultiplier;

    enum Events {
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


        SearchItemBean searchItemBean = new SearchItemBean();
        LocalDate currentDate = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();

        //is date in the future or today
        LocalDate predictDate = LocalDate.parse(date);

        long daysBetween = ChronoUnit.DAYS.between(currentDate, predictDate);


        if (daysBetween < 0) {
            return "Invalid date. Provide must not be in past";
        }

        if (daysBetween > 7) {
            return "Cannot predict more than 7 days into the future";
        }

        searchItemBean.setShopID(shop);
        searchItemBean.setItemID(item);
        searchItemBean.setDataEndDate(currentDate.toString());
        searchItemBean.setDataStartDate(currentDate.minusDays(365 * 2).toString());

        try {
            int predictionResult = predict(searchItemBean, predictDate);
            return "prediction = " + predictionResult;
        } catch (RuntimeException e) {
            System.out.println(e);
            return "Not enough data found for prediction!" + e;
        }

    }

    private int predict(SearchItemBean search, LocalDate predictDate) {
        // get data using search bean
        List<InventoryRecord> inventoryRecords = shopsApi.getInventoryRecords(search);

        System.out.println("inventoryRecordsSize: " + inventoryRecords.size());

        if (inventoryRecords.isEmpty()) {
            throw new RuntimeException("No inventory records found");
        }

        LocalDate currentDate = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
        int daysBetween = (int) ChronoUnit.DAYS.between(currentDate, predictDate);


        // boxify for every enum
        HashMap<Events, HashMap<Integer, Integer>> averagesPerEvents = new HashMap<>();
        for (Events event : Events.values()) {
            averagesPerEvents.put(event, boxify(inventoryRecords, event));
        }

        SearchItemBean currentStoctSearchBean = new SearchItemBean();
        currentStoctSearchBean.setShopID(search.getShopID());
        currentStoctSearchBean.setItemID(search.getItemID());

        List<DateObject> possibleHolidays = holidayApi.getDateInterval(currentDate.toString(), daysBetween + 1);
        HashMap<String, Boolean> holidaysOnly = getHolidaysOnly(possibleHolidays);
        HashMap<String, Boolean> holidaysBefore = getDayBeforeHolidays(possibleHolidays);
        HashMap<String, Boolean> holidaysAfter = getDayAfterHolidays(possibleHolidays);

        /*date, true - if there is an event during the date*/
        HashMap<String, Boolean> events = shopsApi.hasEventDuringDate(search.getShopID(), currentDate.toString(), daysBetween + 1);

        int currentStock = shopsApi.getCurrentDayItemStock(currentStoctSearchBean);


        //prediction for the future
        for (int i = 0; i <= daysBetween; i++) {

            List<Double> listAveragesToMakeAverageFrom = new ArrayList<>();

            LocalDate dateForPrediction = currentDate.plusDays(i);
            int day = dateForPrediction.getDayOfWeek().getValue() - 1;
            int month = dateForPrediction.getMonth().getValue() - 1;
            int quarter = month / 3;

            HashMap<Integer, Integer> daysOfWeeksAverages = averagesPerEvents.get(Events.DAY_OF_WEEK);
            double averageForPredictedDayOfWeek = daysOfWeeksAverages.get(day) * dayOfWeekMultiplier;
            listAveragesToMakeAverageFrom.add(averageForPredictedDayOfWeek);

            HashMap<Integer, Integer> monthsAverages = averagesPerEvents.get(Events.MONTHS);
            double averageForPredictedMonths = monthsAverages.get(month) * monthsMultiplier;
            listAveragesToMakeAverageFrom.add(averageForPredictedMonths);

            HashMap<Integer, Integer> quarterSAverages = averagesPerEvents.get(Events.QUARTERS);
            double averageForPredictedQuarters = quarterSAverages.get(quarter) * quartersMultiplier;
            listAveragesToMakeAverageFrom.add(averageForPredictedQuarters);

            if (holidaysBefore.get(dateForPrediction.toString()) != null) {
                double averageForPredictedHolidaysBefore = averagesPerEvents.get(Events.HOLIDAYS_BEFORE).get(0) * holidaysBeforeMultiplier;
                listAveragesToMakeAverageFrom.add(averageForPredictedHolidaysBefore);
            }

            if (holidaysOnly.get(dateForPrediction.toString()) != null) {
                double averageForPredictedHolidaysOnly = averagesPerEvents.get(Events.HOLIDAYS).get(0) * holidaysMultiplier;
                listAveragesToMakeAverageFrom.add(averageForPredictedHolidaysOnly);
            }

            if (holidaysAfter.get(dateForPrediction.toString()) != null) {
                double averageForPredictedHolidaysAfter = averagesPerEvents.get(Events.HOLIDAYS_AFTER).get(0) * holidaysAfterMultiplier;
                listAveragesToMakeAverageFrom.add(averageForPredictedHolidaysAfter);
            }

            if (events.get(dateForPrediction.toString()) != null) {
                double averageForPredictedEvent = averagesPerEvents.get(Events.EVENT).get(0) * eventMultiplier;
                listAveragesToMakeAverageFrom.add(averageForPredictedEvent);
            }


            int tempAverageCount = 0;

            for (Double averageForEvents : listAveragesToMakeAverageFrom) {
                tempAverageCount += averageForEvents;
            }

            int finalAverageForDay = tempAverageCount / listAveragesToMakeAverageFrom.size();

            //restock
            SearchItemBean restockSearchBean = new SearchItemBean();
            restockSearchBean.setShopID(search.getShopID());
            restockSearchBean.setItemID(search.getItemID());
            restockSearchBean.setPredictDate(ChronoUnit.DAYS.addTo(currentDate, i + 1).toString());

            currentStock += shopsApi.getItemsRestockCount(restockSearchBean);

            currentStock -= finalAverageForDay;
        }

        return currentStock;
    }

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
    private HashMap<Integer, Integer> boxify(List<InventoryRecord> inventoryRecords, @NonNull Prediction.Events event) {

        return switch (event) {
            case DAY_OF_WEEK -> calcAveragesByWeek(inventoryRecords);
            case HOLIDAYS -> calcAveragesByHolidays(inventoryRecords);
            case HOLIDAYS_BEFORE -> calcAveragesByHolidaysBefore(inventoryRecords);
            case HOLIDAYS_AFTER -> calcAveragesByHolidaysAfter(inventoryRecords);
            case QUARTERS -> calcAveragesByQuarters(inventoryRecords);
            case MONTHS -> calcAveragesByMonths(inventoryRecords);
            case EVENT -> calcAveragesByEvent(inventoryRecords);
        };

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
            if (holidays.get(inventoryRecord.getDate()) != null && holidays.get(inventoryRecord.getDate())) {
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
            if (holidays.get(inventoryRecord.getDate()) != null && holidays.get(inventoryRecord.getDate())) {
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
            if (holidays.get(inventoryRecord.getDate()) != null && holidays.get(inventoryRecord.getDate())) {
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
        HashMap<Integer, Integer> eventAverages = new HashMap<>();

        HashMap<Integer, List<InventoryRecord>> eventInventory = new HashMap<>();
        //init
        eventInventory.put(0, new ArrayList<>());

        for (InventoryRecord inventoryRecord : inventoryRecords) {
            if (inventoryRecord.getDuringEvent().equals("1")) {
                eventInventory.get(0).add(inventoryRecord);
            }
        }

        eventInventory.forEach((key, value) -> {
            int soldItems = 0;

            for (InventoryRecord inventoryRecord : value) {
                soldItems += inventoryRecord.getSoldItems();
            }

            if (value.isEmpty()) {
                eventAverages.put(key, 0);
            } else {
                int averageSold = (int) (soldItems / (double) value.size());
                eventAverages.put(key, averageSold);
            }
        });

        return eventAverages;
    }
}
