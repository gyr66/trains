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

    @Autowired
    HttpClientDownloader httpClientDownloader;

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
            logger.error("爬取到错误页面: " + content);
            logger.error("错误页面的请求地址为: " + resultItem.getRequest().getUrl());
        }
        return trainList;
    }

    public List<Train> start() {
        set.clear(); // 每次启动前先清空set
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        String fd = sf.format(new Date());
        String baseUrl = "https://search.12306.cn/search/v1/train/search?date=" + fd + "&keyword=";
        List<String> urlList = new ArrayList<>();
        for (int i = 1; i <= 5; i++)
            urlList.add(baseUrl + i);
        String[] urls = new String[urlList.size()];
        urlList.toArray(urls);
        ResultItemsCollectorPipeline resultItemsCollectorPipeline = new ResultItemsCollectorPipeline();
        Spider.create(new Processor())
                .setDownloader(httpClientDownloader)
                .addUrl(urls)
                .addPipeline(new TrainPipLine())
                .addPipeline(resultItemsCollectorPipeline)
                .thread(1000)
                .run();
        List<ResultItems> resultItems = resultItemsCollectorPipeline.getCollected();
        logger.info("共爬取到" + resultItems.size() + "页");
        set.clear(); // 在生成要返回的train集合时要将之前写入文件时留下的set记录清空
        for (ResultItems resultItem : resultItems) {
            List<Train> trainList = dealResultItem(resultItem, logger);
            if (trainList != null) trains.addAll(trainList);
        }
        return trains;
    }
}

class TrainPipLine implements Pipeline {
    static PrintWriter printWriter;
    static LongAdder longAdder = new LongAdder();

    static {
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
            printWriter = new PrintWriter("trains_" + sf.format(new Date()) + ".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(ResultItems resultItems, Task task) {
        longAdder.increment();
        long longValue = longAdder.longValue();
        logger.info("已爬取" + longValue + "条");

        List<Train> trainList = TrainsCrawler.dealResultItem(resultItems, logger);

        if (trainList == null) return;
        for (Train train : trainList) {
            String res = train.getFrom_station() + " " + train.getTo_station() + " " + train.getStation_train_code() + " " + train.getTrain_no() + " " + train.getDate() + " " + train.getTotal_num();
            printWriter.println(res);
        }
        printWriter.flush();
    }
}




