package com.example.forecasting_stock_availability.data_client;

import java.util.List;

public interface HolidayDataInterface {

    List<DateObject> getDay();

    List<DateObject> getDay(String date);

    List<DateObject> getWeek(String date);

    List<DateObject> getDateInterval(String date, int interval);
}
