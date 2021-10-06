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
}
