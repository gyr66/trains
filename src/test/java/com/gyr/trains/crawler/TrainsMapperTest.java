package com.gyr.trains.crawler;

import com.gyr.trains.crawler.bean.Train;
import com.gyr.trains.mapper.TrainsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TrainsMapperTest {
    @Autowired
    TrainsMapper trainsMapper;

    @Test
    void mapperTest() {
        List<Train> trainList = new ArrayList<>();
        Train train = new Train("1", "2", "3", "4", "5", 6);
        trainList.add(train);
        train = new Train("2", "3", "4", "5", "6", 7);
        trainList.add(train);
        trainsMapper.insertTrains("trains_20211007", trainList);


    }
}
