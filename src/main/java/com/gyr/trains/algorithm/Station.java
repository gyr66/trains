package com.gyr.trains.algorithm;

import lombok.Data;

@Data
public class Station {
    int id;
    String name;

    public Station(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
