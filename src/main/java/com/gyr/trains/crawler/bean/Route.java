package com.gyr.trains.crawler.bean;

import lombok.Data;

@Data
public class Route {
    String start_station_name;
    String end_station_name;
    String start_time;
    String arrive_time;
    String station_train_code;
    String from_station_no;
    String to_station_no;
    String train_no;

    public Route(String start_station_name, String end_station_name, String startTime, String arrive_time, String station_train_code, String from_station_no, String to_station_no, String train_no) {
        this.start_station_name = start_station_name;
        this.end_station_name = end_station_name;
        this.start_time = startTime;
        this.arrive_time = arrive_time;
        this.station_train_code = station_train_code;
        this.from_station_no = from_station_no;
        this.to_station_no = to_station_no;
        this.train_no = train_no;
    }
}
