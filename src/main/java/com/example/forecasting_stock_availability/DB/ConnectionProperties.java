package com.example.forecasting_stock_availability.DB;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "database")
public class ConnectionProperties {
    @Getter @Setter
    String port;

    @Getter @Setter
    String address;

    @Getter @Setter
    String user;

    @Getter @Setter
    String password;


}
