package com.gyr.trains.Web.bean;

import com.gyr.trains.algorithm.Line;
import com.gyr.trains.algorithm.Scheme;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Data
public class Plan {
    String actionUrl = "";
    List<Item> lines = new ArrayList<>();
    String title;
    String type = "groupLine1";
    Info info;

    public Plan(String type, Scheme scheme, Info info) {
        title = type;
        if (scheme != null) {
            List<Item> itemList = new ArrayList<>();
            Stack<Line> route = scheme.getRoute();
            for (Line line : route) {
                Item item = new Item(line);
                itemList.add(item);
            }
            lines = itemList;
        }
        this.info = info;
    }

}
