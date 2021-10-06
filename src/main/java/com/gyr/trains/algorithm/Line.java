package com.gyr.trains.algorithm;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Line {
    Station startStation;
    Station endStation;
    Date startTime;
    Date endTime;
    String seatType;
    double price;
    String id;

    public Line(Station startStation, Station endStation, Date startTime, Date endTime, String seatType, double price, String id) {
        this.startStation = startStation;
        this.endStation = endStation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.seatType = seatType;
        this.price = price;
        this.id = id;
    }

    @Override
    public String toString() {
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        return "Line{" +
                "startStation=" + startStation +
                ", endStation=" + endStation +
                ", startTime=" + sf.format(startTime) +
                ", endTime=" + sf.format(endTime) +
                ", seatType='" + seatType + '\'' +
                ", price=" + price +
                ", id='" + id + '\'' +
                '}';
    }

    public Station getStartStation() {
        return startStation;
    }

    public Station getEndStation() {
        return endStation;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getSeatType() {
        return seatType;
    }

    public double getPrice() {
        return price;
    }

    public String getId() {
        return id;
    }
}
