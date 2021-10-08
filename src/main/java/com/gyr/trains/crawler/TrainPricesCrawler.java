package com.gyr.trains.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gyr.trains.crawler.bean.Price;
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

/*
    A: "高级动卧", I: "一等卧", J: "二等卧", P: "特等座", M: "一等座", O: "二等座",
    F: "动卧", "9": "商务座", "6": "高级软卧", "4": "软卧", "3": "硬卧", "2": "软座", "1": "硬座", H: "其他", WZ: "无座", W: "无座"
 */

@Component
public class TrainPricesCrawler {
    Logger logger = LoggerFactory.getLogger(getClass());
    List<Route> routes;
    List<String> seatCode = new ArrayList<>();

    @Autowired
    HttpClientDownloader httpClientDownloader;

    public TrainPricesCrawler() {
        seatCode.add("O");
        seatCode.add("1");
    }

    public TrainPricesCrawler(List<Route> routes) {
        super();
        setRoutes(routes);
    }

    public static Price dealResultItem(ResultItems resultItem, Logger logger) {
        String content = resultItem.get("content");
        String url = resultItem.get("url");
        String seatType = ParseUrlUtil.parse(url).get("seat_types");
        String from_station_no = ParseUrlUtil.parse(url).get("from_station_no");
        String to_station_no = ParseUrlUtil.parse(url).get("to_station_no");
        Price price = null;
        try {
            JSONObject jsonObject = JSON.parseObject(content).getJSONObject("data");
            String cost = jsonObject.getString(seatType);
            if (cost == null) cost = "无";
            else if (cost.contains("￥") || cost.contains("¥")) {
                cost = cost.substring(1);
                double v = Double.parseDouble(cost);
                v *= 10;
                int actual = (int) v;
                cost = String.valueOf(actual);
            }
            String no = jsonObject.getString("train_no");
            price = new Price(no, from_station_no, to_station_no, seatType + "/" + cost);
        } catch (Exception e) {
            logger.error("爬取到错误页面: " + content);
            logger.error("错误页面的请求地址为: " + resultItem.getRequest().getUrl());
        }
        return price;
    }

    String buildUrl(String train_no, Date date, String from_station_no, String to_station_no, String seatType) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        String fd = sf.format(date);
        return "https://kyfw.12306.cn/otn/leftTicket/queryTicketPrice?train_no=" + train_no + "&from_station_no=" + from_station_no + "&to_station_no=" + to_station_no + "&seat_types=" + seatType + "&train_date=" + fd;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public List<Price> start() {
        List<Price> priceList = new ArrayList<>();
        List<String> urlList = new ArrayList<>();
        for (Route route : routes) {
            String train_no = route.getTrain_no();
            String from_station_no = route.getFrom_station_no();
            String to_station_no = route.getTo_station_no();
            for (String code : seatCode)
                urlList.add(buildUrl(train_no, new Date(), from_station_no, to_station_no, code));
        }
        String[] urls = new String[urlList.size()];
        urlList.toArray(urls);
        ResultItemsCollectorPipeline resultItemsCollectorPipeline = new ResultItemsCollectorPipeline();
        Spider.create(new Processor())
                .setDownloader(httpClientDownloader)
                .addPipeline(new PricePipLine())
                .addPipeline(resultItemsCollectorPipeline)
                .addUrl(urls)
                .thread(1000)
                .run();
        List<ResultItems> resultItems = resultItemsCollectorPipeline.getCollected();
        logger.info("共爬取到" + resultItems.size() + "页");

        for (ResultItems resultItem : resultItems) {
            Price price = dealResultItem(resultItem, logger);
            if (price != null) priceList.add(price);
        }
        return priceList;
    }
}

class PricePipLine implements Pipeline {
    static PrintWriter printWriter;
    static LongAdder longAdder = new LongAdder();

    static {
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
            printWriter = new PrintWriter("prices_" + sf.format(new Date()) + ".txt");
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

        Price price = TrainPricesCrawler.dealResultItem(resultItems, logger);
        String res = price.getTran_no() + " " + price.getFrom_station_no() + " " + price.getTo_station_no() + " " + price.getPrice();
        printWriter.println(res);
        printWriter.flush();
    }
}