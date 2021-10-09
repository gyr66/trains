package com.gyr.trains.crawler;

import com.gyr.trains.crawler.task.CrawlerJob;
import com.gyr.trains.crawler.webmagic.ResultItems;
import com.gyr.trains.crawler.webmagic.Spider;
import com.gyr.trains.crawler.webmagic.Task;
import com.gyr.trains.crawler.webmagic.downloader.HttpClientDownloader;
import com.gyr.trains.crawler.webmagic.pipeline.Pipeline;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

@SpringBootTest
public class ProxyTest {
    @Autowired
    HttpClientDownloader httpClientDownloader;

    @Autowired
    CrawlerJob crawlerJob;

    Logger logger = LoggerFactory.getLogger(getClass());


    @Test
    void proxyTest() throws InterruptedException {
        crawlerJob.startFresh();
        System.out.println("tttt");
        Thread.sleep(5000);
        Spider.create(new Processor())
                .setDownloader(httpClientDownloader)
                .addUrl("http://icanhazip.com/")
                .addPipeline(new proxyTestPipLine())
                .thread(1)
                .run();
    }

    class proxyTestPipLine implements Pipeline {

        @Override
        public void process(ResultItems resultItems, Task task) {
            String s = resultItems.get("content");
            logger.info(s);

        }
    }
}
