package com.gyr.trains.Web.bean;

import lombok.Data;

@Data
public class Query {
    String startStation;
    String endStation;

    String earliestDepartureTime;
    String latestArrivalTime;

    int maxTransferTimes;
    int minMinutesForSameStationTransfer;
}
