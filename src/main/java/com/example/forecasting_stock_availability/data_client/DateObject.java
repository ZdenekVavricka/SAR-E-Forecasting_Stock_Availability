package com.example.forecasting_stock_availability.data_client;

import lombok.Getter;
import lombok.Setter;
import java.util.List;


/**
 * DTO representing calendar/holiday data returned by the external holidays API.
 */
@Setter
public class DateObject {

    /**
     * ISO 8601 date in format YYYY-MM-DD.
     */
    @Setter @Getter
    private String date;

    /**
     * Day of month.
     */
    @Setter @Getter
    private int dayNumber;

    /**
     * Day of week.
     */
    @Setter @Getter
    private String dayInWeek;

    /**
     * Month number.
     */
    @Setter @Getter
    private int monthNumber;

    /**
     * Name-day values (Czech "svátky") for the date.
     */
    @Setter @Getter
    private List<String> nominativ;

    /**
     * Year part of the date.
     */
    @Setter @Getter
    private int year;

    /**
     * Human-readable name (if present) for the date/event.
     */
    @Setter @Getter
    private String name;

    /**
     * String flag from the API indicating a public holiday ("true"/"false").
     */
    @Setter
    private String isHoliday;

    /**
     * Public holiday name if {@code isHoliday} is true.
     */
    @Setter @Getter
    private String holidayName;

    /**
     * Convenience boolean indicating whether the day is a public holiday.
     *
     * @return true if holiday, false otherwise
     */
    public boolean getHoliday() {
        return isHoliday.equals("true");
    }

    /**
     * Returns a string representation for debugging/logging.
     */
    @Override
    public String toString() {
        return "DateObject{" +
                "date='" + date + '\'' +
                ", dayNumber=" + dayNumber +
                ", dayInWeek='" + dayInWeek + '\'' +
                ", monthNumber=" + monthNumber +
                ", nominativ=" + nominativ +
                ", year=" + year +
                ", name='" + name + '\'' +
                ", isHoliday=" + isHoliday +
                ", holidayName='" + holidayName + '\'' +
                '}';
    }
}
