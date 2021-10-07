package com.gyr.trains.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gyr.trains.crawler.bean.Route;
import com.gyr.trains.crawler.utils.ParseUrlUtil;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

@Component
public class TrainRoutesCrawler {
    List<String> train_nos;
    List<Route> trainRoutes = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    HttpClientDownloader httpClientDownloader;

    public TrainRoutesCrawler(List<String> train_nos) {
        this.train_nos = train_nos;
    }

    public TrainRoutesCrawler() {

    }

    public static List<Route> dealResultItem(ResultItems resultItem, Logger logger) {
        List<Route> routeList = new ArrayList<>();
        String content = resultItem.get("content");
        String url = resultItem.get("url");
        String train_no = ParseUrlUtil.parse(url).get("train_no");
        Route route = null;
        try {
            JSONObject jsonObject = JSON.parseObject(content).getJSONObject("data");
            JSONArray data = jsonObject.getJSONArray("data");
            if (data.isEmpty()) return null;
            String station_train_code = data.getJSONObject(0).getString("station_train_code");
            for (int i = 0; i < data.size() - 1; i++) {
                JSONObject begin = data.getJSONObject(i);
                JSONObject end = data.getJSONObject(i + 1);
                String start_station_name = begin.getString("station_name");
                String start_time = begin.getString("start_time");
                String end_station_name = end.getString("station_name");
                String arrive_time = end.getString("arrive_time");
                String from_station_no = begin.getString("station_no");
                String to_station_no = end.getString("station_no");
                route = new Route(start_station_name, end_station_name, start_time, arrive_time, station_train_code, from_station_no, to_station_no, train_no);
                routeList.add(route);
            }
        } catch (Exception e) {
            logger.error("爬取到错误页面: " + content);
            logger.error("错误页面的请求地址为: " + resultItem.getRequest().getUrl());
        }
        return routeList;
    }

    String buildUrl(String train_no, Date date) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        String fd = sf.format(new Date());
        return "https://kyfw.12306.cn/otn/czxx/queryByTrainNo?train_no=" + train_no + "&from_station_telecode=WAR&to_station_telecode=WAR&depart_date=" + fd;
    }

    public void setTrain_nos(List<String> train_nos) {
        this.train_nos = train_nos;
    }

    public List<Route> start() {
        List<String> urlList = new ArrayList<>();
        for (String train_no : train_nos) {
            urlList.add(buildUrl(train_no, new Date()));
        }
        String[] urls = new String[urlList.size()];
        urlList.toArray(urls);
        ResultItemsCollectorPipeline resultItemsCollectorPipeline = new ResultItemsCollectorPipeline();
        Spider.create(new Processor())
                .setDownloader(httpClientDownloader)
                .addPipeline(new RoutePipLine())
                .addUrl(urls)
                .addPipeline(resultItemsCollectorPipeline)
                .thread(1000)
                .run();
        List<ResultItems> resultItems = resultItemsCollectorPipeline.getCollected();
        logger.info("共爬取到" + resultItems.size() + "页");
        for (ResultItems resultItem : resultItems) {
            List<Route> routeList = dealResultItem(resultItem, logger);
            if (routeList != null) trainRoutes.addAll(routeList);
        }
        return trainRoutes;
    }
}

class RoutePipLine implements Pipeline {
    static PrintWriter printWriter;
    static LongAdder longAdder = new LongAdder();

    static {
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
            printWriter = new PrintWriter("routes_" + sf.format(new Date()) + ".txt");
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

        List<Route> routeList = TrainRoutesCrawler.dealResultItem(resultItems, logger);
        if (routeList == null) {
            logger.warn("找到了为空的route: " + resultItems);
            return;
        }
        for (Route route : routeList) {
            String res = route.getStart_station_name() + " " + route.getEnd_station_name() + " " + route.getStart_time() + " " + route.getArrive_time() + " " + route.getStation_train_code() + " " + route.getFrom_station_no() + " " + route.getTo_station_no() + " " + route.getTrain_no();
            printWriter.println(res);
        }
        printWriter.flush();
    }
}


