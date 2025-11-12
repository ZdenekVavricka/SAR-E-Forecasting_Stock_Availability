package com.example.forecasting_stock_availability.data_client;

import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Setter
public class DateObject {

    @Setter @Getter
    private String date;

    @Setter @Getter
    private int dayNumber;

    @Setter @Getter
    private String dayInWeek;

    @Setter @Getter
    private int monthNumber;

    @Setter @Getter
    private List<String> nominativ;

    @Setter @Getter
    private int year;

    @Setter @Getter
    private String name;

    @Setter
    private String isHoliday;

    @Setter @Getter
    private String holidayName;

    public boolean getHoliday() {
        return isHoliday.equals("true");
    }

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
