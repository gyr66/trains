package com.gyr.trains.Web.controller;

import com.gyr.trains.Web.bean.Message;
import com.gyr.trains.Web.bean.Plan;
import com.gyr.trains.Web.bean.Query;
import com.gyr.trains.Web.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@RestController
public class SearchController {
    @Autowired
    SearchService searchService;

    Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/search")
    public Message search(Query query) throws ParseException, IOException {
        logger.info(query.toString());
        List<Plan> planList = searchService.searchPlans(query);
        Message message = new Message();
        message.setCode("200");
        message.setData(planList);
        return message;
    }
}
