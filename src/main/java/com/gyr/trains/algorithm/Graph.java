package com.gyr.trains.algorithm;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Graph {
    List<Station> stations;
    List<List<Line>> map = new ArrayList<>();

    public void setStations(List<Station> stations) {
        this.stations = stations;
        int stationCnt = stations.size();
        for (int i = 0; i <= stationCnt; i++) {
            map.add(new ArrayList<>());
        }
    }

    public void build(List<Line> lines) {
        for (Line line : lines)
            map.get(line.startStation.id).add(line);
    }

}
