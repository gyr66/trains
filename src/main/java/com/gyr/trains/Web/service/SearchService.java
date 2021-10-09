package com.gyr.trains.Web.service;

import com.gyr.trains.Web.bean.Info;
import com.gyr.trains.Web.bean.Plan;
import com.gyr.trains.Web.bean.Query;
import com.gyr.trains.algorithm.*;
import com.gyr.trains.crawler.bean.Result;
import com.gyr.trains.mapper.ResultsMapper;
import com.gyr.trains.mapper.StationsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    @Autowired
    ResultsMapper resultsMapper;

    @Autowired
    StationsMapper stationsMapper;

    @Autowired
    Graph graph;

    SimpleDateFormat sf = new SimpleDateFormat("HH:mm");

    List<Station> stationList;
    List<Line> lineList = new ArrayList<>();

    @PostConstruct
    public void initialize() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        stationList = stationsMapper.getAllStations();
        List<Result> resultList = resultsMapper.getAllResults("results_" + dateFormat.format(new Date()));
        lineList = new ArrayList<>();
        for (Result result : resultList) {
            if (!result.getStation_train_code().contains("Y") && !result.getStation_train_code().contains("S")) {
                Station startStation = new Station(result.getStart_station_id(), result.getStart_station_name());
                Station endStation = new Station(result.getEnd_station_id(), result.getEnd_station_name());
                Date startTime = sf.parse(result.getStart_time());
                Date endTime = sf.parse(result.getArrive_time());
                if (startTime.compareTo(endTime) > 0) continue; // 滤掉跨天的段
                String priceString = result.getPrice().substring(result.getPrice().indexOf("/") + 1);
                if (priceString.equals("无")) continue;
                if (startStation.getId() == 0 || endStation.getId() == 0) continue; // 去掉站表中没有的路线
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
                if (seatType.equals("软座")) continue; // 去掉软座
                double price = Double.parseDouble(priceString) / 10.0;
                String id = result.getStation_train_code();
                Line line = new Line(startStation, endStation, startTime, endTime, seatType, price, id);
                lineList.add(line);
            }
        }
        graph.setStations(stationList);
        graph.build(lineList);
    }

    public List<Plan> searchPlans(Query query) throws ParseException, IOException {
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");

        // traveler在图上跑
        Traveler traveler = new Traveler(graph);
        Station startStation = stationsMapper.getStationByName(query.getStartStation());
        Station endStation = stationsMapper.getStationByName(query.getEndStation());
        Date earliestDepartureTime = sf.parse(query.getEarliestDepartureTime());
        Date latestArrivalTime = sf.parse(query.getLatestArrivalTime());
        Map<Station, List<Scheme>> stationResultMap = traveler.start(startStation, earliestDepartureTime, latestArrivalTime, query.getMaxTransferTimes(), query.getMinMinutesForSameStationTransfer());
        List<Scheme> schemes = stationResultMap.get(endStation);

        // 下面对得到的schemes进行处理
        List<Plan> planList = new ArrayList<>();
        // 获取花费最小的Plan
        String type = "最少花费";
        double cost = 1e9;
        Scheme minCostScheme = null;
        if (schemes != null) {
            for (Scheme scheme : schemes) {
                if (scheme.getCost() < cost) {
                    minCostScheme = scheme;
                    cost = minCostScheme.getCost();
                }
            }
        }
        Info info = new Info();
        if (minCostScheme != null) {
            info.setPrice(minCostScheme.getCost());
            info.setArrivalTime(sf.format(minCostScheme.getArrivalTime()));
            info.setTransferTimes(minCostScheme.getTransferTimes() - 1);
        }
        Plan plan = new Plan(type, minCostScheme, info);
        planList.add(plan);

        // 获取到达最早的Plan
        type = "最早到达";
        Date arrivalTime = sf.parse("24:00");
        Scheme earliestArrivalScheme = null;
        if (schemes != null) {
            for (Scheme scheme : schemes) {
                if (scheme.getArrivalTime().compareTo(arrivalTime) < 0) {
                    earliestArrivalScheme = scheme;
                    arrivalTime = earliestArrivalScheme.getArrivalTime();
                }
            }
        }
        dealScheme(sf, planList, type, earliestArrivalScheme);

        // 获取换乘次数最少的Plan
        type = "换乘最少";
        int transferTimes = 1000;
        Scheme minTransferTimesScheme = null;
        if (schemes != null) {
            for (Scheme scheme : schemes) {
                if (scheme.getTransferTimes() < transferTimes) {
                    minTransferTimesScheme = scheme;
                    transferTimes = minTransferTimesScheme.getTransferTimes();
                }
            }
        }
        dealScheme(sf, planList, type, minTransferTimesScheme);

        // 总耗时最短的方案
        type = "总耗时最短";
        long totalTime = Long.MAX_VALUE;
        Scheme minTotalTime = null;
        if (schemes != null) {
            for (Scheme scheme : schemes) {
                if (scheme.getTotalTime() < totalTime) {
                    minTotalTime = scheme;
                    totalTime = scheme.getTotalTime();
                }
            }
        }
        dealScheme(sf, planList, type, minTotalTime);

        // 与期望时间最接近的方案
        type = "与期望到达时间最接近";
        long diffTime = Long.MAX_VALUE;
        Scheme closestScheme = null;
        if (schemes != null) {
            for (Scheme scheme : schemes) {
                if (scheme.getDifferenceFromExpectedArrivalTime() < diffTime) {
                    closestScheme = scheme;
                    diffTime = scheme.getDifferenceFromExpectedArrivalTime();
                }
            }
        }
        dealScheme(sf, planList, type, minTotalTime);

        // 其他方案
        if (schemes != null) {
            int index = 0;
            for (Scheme scheme : schemes) {
                if (scheme == minCostScheme || scheme == earliestArrivalScheme || scheme == minTransferTimesScheme) continue;
                index++;
                type = "其他";
                type = type + index;
                dealScheme(sf, planList, type, scheme);
            }
        }
        return planList;
    }

    private void dealScheme(SimpleDateFormat sf, List<Plan> planList, String type, Scheme minTransferTimesScheme) {
        Info info;
        Plan plan;
        info = new Info();
        if (minTransferTimesScheme != null) {
            info.setPrice(minTransferTimesScheme.getCost());
            info.setArrivalTime(sf.format(minTransferTimesScheme.getArrivalTime()));
            info.setTransferTimes(minTransferTimesScheme.getTransferTimes() - 1);
        }
        plan = new Plan(type, minTransferTimesScheme, info);
        planList.add(plan);
    }
}
