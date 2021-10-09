package com.gyr.trains.algorithm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

public class Scheme {
    SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
    Stack<Line> route = new Stack<>();
    double cost;
    Date arrivalTime = sf.parse("00:00");
    int transferTimes;
    long totalTime;
    long differenceFromExpectedArrivalTime;
    Date expectedArrivalTime = sf.parse("19:00");

    public void setExpectedArrivalTime(Date expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public Scheme(Stack<Line> path) throws ParseException {
        for (Line line : path) {
            if (route.empty() || !route.peek().id.equals(line.id) || !route.peek().seatType.equals(line.seatType)) {
                route.push(line);
                transferTimes++;
            } else {
                Line lastLine = route.pop();
                route.push(new Line(lastLine.startStation, line.endStation, lastLine.startTime, line.endTime, line.seatType, lastLine.price + line.price, line.id));
            }
        }
        long startTime = Long.MAX_VALUE, endTime = Long.MIN_VALUE;
        for (Line line : route) {
            this.cost += line.price;
            this.arrivalTime = line.endTime;
            startTime = Math.min(startTime, line.startTime.getTime());
            endTime = Math.max(endTime, line.getStartTime().getTime());
        }
        totalTime = endTime - startTime;
        differenceFromExpectedArrivalTime = Math.abs(arrivalTime.getTime() - expectedArrivalTime.getTime());
    }

    public Scheme(Stack<Line> route, double cost, Date arrivalTime, int transferTimes) throws ParseException {
        this.route = route;
        this.cost = cost;
        this.arrivalTime = arrivalTime;
        this.transferTimes = transferTimes;
    }

    public Stack<Line> getRoute() {
        return route;
    }

    public double getCost() {
        return cost;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public int getTransferTimes() {
        return transferTimes;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getDifferenceFromExpectedArrivalTime() {
        return differenceFromExpectedArrivalTime;
    }

    boolean betterInAllAspects(Scheme scheme) {
        if (cost < scheme.cost && arrivalTime.compareTo(scheme.arrivalTime) <= 0 && transferTimes <= scheme.transferTimes && totalTime <= scheme.totalTime && differenceFromExpectedArrivalTime <= scheme.differenceFromExpectedArrivalTime)
            return true;
        if (cost <= scheme.cost && arrivalTime.compareTo(scheme.arrivalTime) < 0 && transferTimes <= scheme.transferTimes && totalTime <= scheme.totalTime && differenceFromExpectedArrivalTime <= scheme.differenceFromExpectedArrivalTime)
            return true;
        if (cost <= scheme.cost && arrivalTime.compareTo(scheme.arrivalTime) <= 0 && transferTimes < scheme.transferTimes && totalTime <= scheme.totalTime && differenceFromExpectedArrivalTime <= scheme.differenceFromExpectedArrivalTime)
            return true;
        if (cost <= scheme.cost && arrivalTime.compareTo(scheme.arrivalTime) <= 0 && transferTimes <= scheme.transferTimes && totalTime < scheme.totalTime && differenceFromExpectedArrivalTime <= scheme.differenceFromExpectedArrivalTime)
            return true;
        if (cost <= scheme.cost && arrivalTime.compareTo(scheme.arrivalTime) <= 0 && transferTimes <= scheme.transferTimes && totalTime <= scheme.totalTime && differenceFromExpectedArrivalTime < scheme.differenceFromExpectedArrivalTime)
            return true;
        return false;
    }

    boolean atLeastBetterInOneAspect(Scheme scheme) {
        return cost < scheme.cost || arrivalTime.compareTo(scheme.arrivalTime) < 0 || transferTimes < scheme.transferTimes || totalTime < scheme.totalTime;
    }
}
