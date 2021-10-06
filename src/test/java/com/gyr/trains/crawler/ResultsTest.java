package com.gyr.trains.crawler;

import com.gyr.trains.crawler.bean.Result;
import com.gyr.trains.algorithm.Graph;
import com.gyr.trains.algorithm.Line;
import com.gyr.trains.algorithm.Station;
import com.gyr.trains.algorithm.Traveler;
import com.gyr.trains.mapper.ResultsMapper;
import com.gyr.trains.mapper.StationsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class ResultsTest {
    @Autowired
    ResultsMapper resultsMapper;

    @Autowired
    StationsMapper stationsMapper;

    @Test
    void resultTest() throws ParseException, IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        List<Result> resultList = resultsMapper.getAllResults(dateFormat.format(new Date()));
        List<Line> lineList = new ArrayList<>();
        for (Result result : resultList) {
            if (
                    result.getStation_train_code().contains("G")
                            || result.getStation_train_code().contains("D")
                            || result.getStation_train_code().contains("K")
            ) {
                Station startStation = new Station(result.getStart_station_id(), result.getStart_station_name());
                Station endStation = new Station(result.getEnd_station_id(), result.getEnd_station_name());
                Date startTime = sf.parse(result.getStart_time());
                Date endTime = sf.parse(result.getArrive_time());
                if (startTime.compareTo(endTime) > 0) continue;

                String priceString = result.getPrice().substring(result.getPrice().indexOf("/") + 1);
                if (priceString.equals("无")) continue;
                String seatType = result.getPrice().substring(0, 1);
                switch (seatType) {
                    case "O":
                        seatType = "二等座";
                        break;
                    case "1":
                        seatType = "硬座";
                        break;
                    case "2":
                        seatType = "软座";
                        break;
                    case "3":
                        seatType = "硬卧";
                        break;
                }
                if (seatType.equals("软座")) continue;
                double price = Double.parseDouble(priceString) / 10.0;
                String id = result.getStation_train_code();
                Line line = new Line(startStation, endStation, startTime, endTime, seatType, price, id);
                lineList.add(line);
            }
        }
        List<Station> stationList = stationsMapper.getAllStations();
        Graph graph = new Graph();
        graph.setStations(stationList);
        graph.build(lineList);
        Traveler traveler = new Traveler(graph);
        Station startStation = new Station(7, "重庆北");
        Date startDate = sf.parse("00:00");
        traveler.start(startStation);
    }
}
