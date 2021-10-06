package com.gyr.trains.crawler;

import com.gyr.trains.crawler.bean.Train;
import com.gyr.trains.mapper.TrainsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class TrainsCrawlerTest {
    @Autowired
    TrainsMapper trainsMapper;

    @Test
    void crawlerTest() {
        TrainsCrawler trainsCrawler = new TrainsCrawler();
        List<Train> trains = trainsCrawler.start();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        trainsMapper.insertTrains("trains_" + dateFormat.format(new Date()), trains);
    }
}
