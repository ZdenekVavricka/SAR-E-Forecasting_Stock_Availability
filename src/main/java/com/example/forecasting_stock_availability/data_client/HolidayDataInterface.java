package com.example.forecasting_stock_availability.data_client;

import java.util.List;

/**
 * Abstraction for providing holiday data.
 */
public interface HolidayDataInterface {

    /**
     * Returns information about the current day.
     *
     * @return list of {@link DateObject}
     */
    List<DateObject> getDay();

    /**
     * Returns information about the given date.
     *
     * @param date date in format YYYY-MM-DD
     * @return list of {@link DateObject}
     */
    List<DateObject> getDay(String date);

    /**
     * Returns information about the week starting from the given date.
     *
     * @param date date in format YYYY-MM-DD
     * @return list of {@link DateObject}
     */
    List<DateObject> getWeek(String date);

    /**
     * Returns information about an interval of days starting from the given date.
     *
     * @param date     date in format YYYY-MM-DD
     * @param interval number of days from the start date
     * @return list of {@link DateObject}
     */
    List<DateObject> getDateInterval(String date, int interval);
}
