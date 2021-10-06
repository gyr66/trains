package com.gyr.trains.crawler;

import com.gyr.trains.crawler.bean.Price;
import com.gyr.trains.crawler.bean.Route;
import com.gyr.trains.mapper.PricesMapper;
import com.gyr.trains.mapper.RoutesMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class PricesCrawlerTest {
    @Autowired
    RoutesMapper routesMapper;
    @Autowired
    PricesMapper pricesMapper;

    @Autowired
    TrainPricesCrawler crawler;

    @Test
    void crawlTest() {
        List<Route> routeList = routesMapper.getAllRoutes();
        crawler.setRoutes(routeList);
        List<Price> priceList = crawler.start();
        pricesMapper.insertPrices("price", priceList);
    }

    @Test
    void shortTest() {
        List<Route> routeList = routesMapper.getAllRoutes();
        List<Route> test = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            test.add(routeList.get(i));
        }
        crawler.setRoutes(test);
        List<Price> priceList = crawler.start();
//        pricesMapper.insertPrices(priceList);
    }
}
