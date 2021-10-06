package com.gyr.trains.crawler;

import com.gyr.trains.crawler.task.CrawlerJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;

@SpringBootTest
public class TaskTest {
    @Autowired
    CrawlerJob crawlerJob;

    @Test
    void taskTest() throws ParseException {
        crawlerJob.run();
    }

}
