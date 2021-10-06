package com.gyr.trains.crawler;

import com.gyr.trains.crawler.bean.Route;
import com.gyr.trains.mapper.RoutesMapper;
import com.gyr.trains.mapper.TrainsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class RoutesCrawlerTest {
    @Autowired
    RoutesMapper routesMapper;
    @Autowired
    TrainsMapper trainsMapper;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    @Test
    void crawlerTest() {

        List<String> tran_nos = trainsMapper.getAllTrain_nos("trains_" + dateFormat);
        TrainRoutesCrawler trainRoutesCrawler = new TrainRoutesCrawler(tran_nos);
        List<Route> routes = trainRoutesCrawler.start();
        routesMapper.insertRoutes("routes", routes);
    }

    @Test
    void crawlerShortTest() {
        List<String> tran_nos = trainsMapper.getAllTrain_nos("trains_" + dateFormat);
        List<String> test = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            test.add(tran_nos.get(i));
        }
        TrainRoutesCrawler trainRoutesCrawler = new TrainRoutesCrawler(test);
        List<Route> routes = trainRoutesCrawler.start();
        routesMapper.insertRoutes("routes", routes);
    }
}
