package com.gyr.trains.crawler.bean;

import lombok.Data;

@Data
public class Price {
    String tran_no;
    String from_station_no;
    String to_station_no;
    String price;

    public Price(String tran_no, String from_station_no, String to_station_no, String price) {
        this.tran_no = tran_no;
        this.from_station_no = from_station_no;
        this.to_station_no = to_station_no;
        this.price = price;
    }
}
