package com.gyr.trains.Web.bean;

import lombok.Data;

import java.util.List;

@Data
public class Message {
    String actionUrl = "";
    String code;
    List<Plan> data;
}
