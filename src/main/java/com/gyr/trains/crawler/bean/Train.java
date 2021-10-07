package com.gyr.trains.crawler.bean;

import lombok.Data;

@Data
public class Train {
    String Date;
    String station_train_code;
    String train_no;
    String from_station;
    String to_station;
    int total_num;

    public Train(String date, String station_train_code, String train_no, String from_station, String to_station, int total_num) {
        Date = date;
        this.station_train_code = station_train_code;
        this.train_no = train_no;
        this.from_station = from_station;
        this.to_station = to_station;
        this.total_num = total_num;
    }
}
