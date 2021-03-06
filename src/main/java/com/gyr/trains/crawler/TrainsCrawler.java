package com.gyr.trains.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gyr.trains.crawler.bean.Train;
import com.gyr.trains.crawler.webmagic.ResultItems;
import com.gyr.trains.crawler.webmagic.Spider;
import com.gyr.trains.crawler.webmagic.Task;
import com.gyr.trains.crawler.webmagic.downloader.HttpClientDownloader;
import com.gyr.trains.crawler.webmagic.pipeline.Pipeline;
import com.gyr.trains.crawler.webmagic.pipeline.ResultItemsCollectorPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;

@Component
public class TrainsCrawler {
    static Set<String> set = new HashSet<>();
    List<Train> trains = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(getClass());
    Date date = new Date();

    @Autowired
    HttpClientDownloader httpClientDownloader;

    public void setDate(Date date) {
        this.date = date;
    }

    public static List<Train> dealResultItem(ResultItems resultItem, Logger logger) {
        List<Train> trainList = new ArrayList<>();
        String content = resultItem.get("content");
        try {
            JSONObject jsonObject = JSON.parseObject(content);
            JSONArray data = jsonObject.getJSONArray("data");
            if (data.isEmpty()) return null;
            for (Object item : data) {
                JSONObject train = (JSONObject) item;
                String train_no = train.getString("train_no");
                if (!set.contains(train_no)) {
                    set.add(train_no);
                    Train pojo = train.toJavaObject(Train.class);
                    trainList.add(pojo);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("?????????????????????: " + content);
            logger.error("??????????????????????????????: " + resultItem.getRequest().getUrl());
        }
        return trainList;
    }

    public List<Train> start() {
        set.clear(); // ????????????????????????set
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        String fd = sf.format(date);
        String baseUrl = "https://search.12306.cn/search/v1/train/search?date=" + fd + "&keyword=";
        List<String> urlList = new ArrayList<>();
        for (int i = 1; i <= 10000; i++)
            urlList.add(baseUrl + i);
        String[] urls = new String[urlList.size()];
        urlList.toArray(urls);
        ResultItemsCollectorPipeline resultItemsCollectorPipeline = new ResultItemsCollectorPipeline();
        Spider.create(new Processor())
                .setDownloader(httpClientDownloader)
                .addUrl(urls)
                .addPipeline(new TrainPipLine(date))
                .addPipeline(resultItemsCollectorPipeline)
                .thread(1000)
                .run();
        List<ResultItems> resultItems = resultItemsCollectorPipeline.getCollected();
        logger.info("????????????" + resultItems.size() + "???");
        set.clear(); // ?????????????????????train?????????????????????????????????????????????set????????????
        for (ResultItems resultItem : resultItems) {
            List<Train> trainList = dealResultItem(resultItem, logger);
            if (trainList != null) trains.addAll(trainList);
        }
        return trains;
    }
}

class TrainPipLine implements Pipeline {
    PrintWriter printWriter;
    LongAdder longAdder = new LongAdder();
    Logger logger = LoggerFactory.getLogger(getClass());
    Date date;

    public TrainPipLine(Date date) {
        this.date = date;
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
            printWriter = new PrintWriter("trains_" + sf.format(date) + ".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        longAdder.increment();
        long longValue = longAdder.longValue();
        logger.info("?????????" + longValue + "???");

        List<Train> trainList = TrainsCrawler.dealResultItem(resultItems, logger);

        if (trainList == null) return;
        for (Train train : trainList) {
            String res = train.getFrom_station() + " " + train.getTo_station() + " " + train.getStation_train_code() + " " + train.getTrain_no() + " " + train.getDate() + " " + train.getTotal_num();
            printWriter.println(res);
        }
        printWriter.flush();
    }
}




