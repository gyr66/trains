package com.gyr.trains.Web.bean;

import com.gyr.trains.algorithm.Line;
import lombok.Data;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.text.SimpleDateFormat;

@Data
public class Item {
    String actionUrl = "";
    String type = "line1";
    String trainNumber;
    String startTime;
    String start;
    String destinationTime;
    String destination;
    String duration;
    double cost;

    public Item(Line line) {
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        start = line.getStartStation().getName();
        startTime = sf.format(line.getStartTime());
        destination = line.getEndStation().getName();
        destinationTime = sf.format(line.getEndTime());
        cost = line.getPrice();
        trainNumber = line.getId();
        String temp = DurationFormatUtils.formatPeriod(line.getStartTime().getTime(), line.getEndTime().getTime(), "H");
        int hours = Integer.parseInt(temp);
        temp = DurationFormatUtils.formatPeriod(line.getStartTime().getTime(), line.getEndTime().getTime(), "m");
        int minutes = Integer.parseInt(temp) - hours * 60;
        duration = hours + "小时" + minutes + "分";
    }
}
