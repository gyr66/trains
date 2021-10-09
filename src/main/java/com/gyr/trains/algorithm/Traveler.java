package com.gyr.trains.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Traveler {
    SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
    Logger logger = LoggerFactory.getLogger(getClass());
    FileWriter fileWriter = new FileWriter("res.txt", true);

    Graph graph; // 所要旅行的图
    Station startStation; // 起点
    Stack<Line> path = new Stack<>(); // 记录走的路径
    Map<Station, List<Scheme>> notes = new HashMap<>(); // 记录某个站点的历史到达记录

    // 下面是一些限制条件
    int minMinutesForSameStationTransfer = 20; // 同站换乘最少允许时间
    int maxTransferTimes = 3; // 最大允许的换乘次数
    Date earliestDepartureTime = sf.parse("05:00"); // 最早出发时间
    Date latestArrivalTime = sf.parse("24:00"); // 最晚到达时间

    public Traveler(Graph graph) throws ParseException, IOException {
        this.graph = graph;
    }

    public Map<Station, List<Scheme>> start(Station startStation) throws IOException, ParseException {
        this.startStation = startStation;
        travel(startStation, earliestDepartureTime, 0, 0);
//        writeDown();
        return notes;
    }

    public Map<Station, List<Scheme>> start(Station startStation, Date earliestDepartureTime, Date latestArrivalTime, int maxTransferTimes, int minMinutesForSameStationTransfer) throws IOException, ParseException {
        this.maxTransferTimes = maxTransferTimes;
        this.earliestDepartureTime = earliestDepartureTime;
        this.latestArrivalTime = latestArrivalTime;
        this.minMinutesForSameStationTransfer = minMinutesForSameStationTransfer;
        return start(startStation);
    }

    void travel(Station currentStation, Date currentDate, double currentCost, int transferTimes) throws ParseException {
        // 剪枝
        if (currentDate.compareTo(latestArrivalTime) > 0) return;

        if (!path.empty()) {
            // 将当前路径构造为一个scheme
            Scheme scheme = new Scheme(path);

            // 与历史到达记录进行比较，移除某些历史到达记录和决定是否继续在当前情况的基础上继续尝试
            List<Scheme> schemeList = notes.get(currentStation);
            if (schemeList == null) schemeList = new ArrayList<>();
            List<Scheme> temp = new ArrayList<>();
            boolean essential = true;
            for (Scheme historyScheme : schemeList) {
                if (!scheme.betterInAllAspects(historyScheme))
                    temp.add(historyScheme); // 如果当前scheme比之前的某个到达记录各方面都好，则移除该到达记录
                if (!scheme.atLeastBetterInOneAspect(historyScheme)) {
                    essential = false;
                    break;
                } // 如果当前scheme比之前的某个到达记录各方面都差，则剪枝
            }
            if (!essential) return;
            temp.add(scheme);
            notes.put(currentStation, temp);
        }

        // 尝试从当前站乘车
        for (Line line : graph.map.get(currentStation.id)) {
            if (!path.empty() && !path.peek().id.equals(line.id) && (line.startTime.getTime() - currentDate.getTime()) < (long) minMinutesForSameStationTransfer * 60 * 1000)
                continue;
            if (line.startTime.compareTo(currentDate) < 0) continue; // 防止跨天
            // 更新换乘次数
            int newTransferTimes = transferTimes;
            if (!path.isEmpty() && !(path.peek().id).equals(line.id)) newTransferTimes++;
            if (newTransferTimes > maxTransferTimes) continue;
            path.push(line);
            travel(line.endStation, line.endTime, currentCost + line.price, newTransferTimes);
            path.pop();
        }
    }

    public void writeDown() throws IOException {
        fileWriter.write("起点站: " + startStation + "\n");
        notes.forEach((station, result) -> {
            try {
                fileWriter.write(station.name + ":\n");
                int cnt = 0;
                for (Scheme scheme : result) {
                    if (scheme.cost == 0) continue;
                    fileWriter.write("方案" + ++cnt + ": 价格:" + scheme.cost + " 到达时间:" + sf.format(scheme.arrivalTime) + "\n");
                    for (Line line : scheme.route) {
                        fileWriter.write(line.toString() + "\n");
                    }
                }
                fileWriter.write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        fileWriter.flush();
    }
}
