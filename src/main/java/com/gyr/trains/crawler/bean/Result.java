package com.gyr.trains.crawler.bean;

import lombok.Data;

@Data
public class Result {
    String station_train_code;
    int start_station_id;
    String start_station_name;
    int end_station_id;
    String end_station_name;
    String start_time;
    String arrive_time;
    String price;
}
