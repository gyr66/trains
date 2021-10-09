package com.gyr.trains.crawler.task;

import com.gyr.trains.Web.service.MailService;
import com.gyr.trains.Web.service.SearchService;
import com.gyr.trains.crawler.TrainPricesCrawler;
import com.gyr.trains.crawler.TrainRoutesCrawler;
import com.gyr.trains.crawler.TrainsCrawler;
import com.gyr.trains.crawler.bean.Price;
import com.gyr.trains.crawler.bean.Route;
import com.gyr.trains.crawler.bean.Train;
import com.gyr.trains.crawler.webmagic.downloader.HttpClientDownloader;
import com.gyr.trains.crawler.webmagic.proxy.Proxy;
import com.gyr.trains.crawler.webmagic.proxy.SimpleProxyProvider;
import com.gyr.trains.mapper.PricesMapper;
import com.gyr.trains.mapper.RoutesMapper;
import com.gyr.trains.mapper.TrainsMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

@Configuration
@EnableScheduling
public class CrawlerJob {
    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    TrainsMapper trainsMapper;

    @Autowired
    RoutesMapper routesMapper;

    @Autowired
    PricesMapper pricesMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    SearchService searchService;

    @Autowired
    HttpClientDownloader httpClientDownloader;

    @Autowired
    TrainRoutesCrawler trainRoutesCrawler;

    @Autowired
    TrainPricesCrawler trainPricesCrawler;

    @Autowired
    MailService mailService;
    @Autowired
    TrainsCrawler trainsCrawler;
    Logger logger = LoggerFactory.getLogger(getClass());

    ScheduledFuture<?> future;

    Date date = new Date();

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        return threadPoolTaskScheduler;
    }

    public void startFresh() {
        logger.info("定时更换ip任务开启");
        future = threadPoolTaskScheduler.schedule(new Fresh(), new CronTrigger("0/15 * * * * *"));
    }

    void stopFresh() {
        if (future != null) {
            future.cancel(true);
        }
        logger.info("定时更换ip任务关闭");
    }

    @Scheduled(cron = "0 50 18 * * ?")
    public void run() throws ParseException {
        long startTime = System.currentTimeMillis();

        // 开启从线程池里更换ip的任务
        startFresh();

        mailService.send("开始定时爬虫任务...");

        // 爬取当天所有火车车次
        logger.info("正在爬取所有火车车次...");
        String trainTableName = "trains_" + sf.format(date);
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + trainTableName + "`");
        jdbcTemplate.execute("CREATE TABLE `" + trainTableName + "`  (\n" +
                "  `from_station` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `to_station` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `station_train_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `train_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `date` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `total_num` int(0) NULL DEFAULT NULL\n" +
                ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;");
        List<Train> trainList = null;
        try {
            trainsCrawler.setDate(date);
            trainList = trainsCrawler.start();
        } catch (Exception e) {
            logger.error("爬取火车车次失败: " + e.getMessage());
            mailService.send("爬取火车车次失败: " + e.getMessage());
            return;
        } finally {
            stopFresh();
        }
        long endTime = System.currentTimeMillis();
        long minutes = (endTime - startTime) / 1000 / 60;
        mailService.send("火车车次爬取完成, 总共爬取" + trainList.size() + "个火车车次, 用时" + minutes + "分钟");
        try {
            trainsMapper.insertTrains(trainTableName, trainList);
        } catch (Exception e) {
            logger.error("trains插入数据库失败: " + e.getMessage());
            mailService.send("trains插入数据库失败: " + e.getMessage());
        }

        // 爬取当天的所有路线
        logger.info("正在爬取所有路线...");
        // 建routes表
        String routesTableName = "routes_" + sf.format(date);
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + routesTableName + "`");
        jdbcTemplate.execute("CREATE TABLE `" + routesTableName + "`  (\n" +
                "  `start_station_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `end_station_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `start_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `arrive_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `station_train_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `from_station_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `to_station_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `train_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL\n" +
                ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;\n");
        List<String> tran_nos = new ArrayList<>();
        for (Train train : trainList) tran_nos.add(train.getTrain_no());
        trainRoutesCrawler.setTrain_nos(tran_nos);
        List<Route> routeList = null;
        try {
            trainRoutesCrawler.setDate(date);
            routeList = trainRoutesCrawler.start();
        } catch (Exception e) {
            logger.error("爬取routes失败: " + e.getMessage());
            mailService.send("爬取routes失败: " + e.getMessage());
            return;
        } finally {
            stopFresh();
        }
        endTime = System.currentTimeMillis();
        minutes = (endTime - startTime) / 1000 / 60;
        mailService.send("路线爬取完成, 总共爬取" + routeList.size() + "条路线, 用时" + minutes + "分钟");
        try {
            routesMapper.insertRoutes(routesTableName, routeList);
        } catch (Exception e) {
            logger.error("routes插入数据库失败: " + e.getMessage());
            mailService.send("routes插入数据库失败: " + e.getMessage());
        }

        // 爬取当天的价格
        logger.info("正在爬取所有价格...");
        // 建prices表
        String pricesTableName = "prices_" + sf.format(date);
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + pricesTableName + "`");
        jdbcTemplate.execute("CREATE TABLE `" + pricesTableName + "`  (\n" +
                "  `train_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,\n" +
                "  `from_station_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `to_station_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,\n" +
                "  `price` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL\n" +
                ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;");
        trainPricesCrawler.setRoutes(routeList);
        List<Price> priceList = null;
        try {
            trainPricesCrawler.setDate(date);
            priceList = trainPricesCrawler.start();
        } catch (Exception e) {
            logger.error("爬取prices失败: " + e.getMessage());
            mailService.send("爬取prices失败" + e.getMessage());
            return;
        } finally {
            stopFresh();
        }
        endTime = System.currentTimeMillis();
        minutes = (endTime - startTime) / 1000 / 60;
        mailService.send("价格爬取完成, 总共爬取" + priceList.size() + "条价格, 用时" + minutes + "分钟");
        try {
            pricesMapper.insertPrices(pricesTableName, priceList);
        } catch (Exception e) {
            logger.error("prices插入数据库失败: " + e.getMessage());
            mailService.send("prices插入数据库失败: " + e.getMessage());
        }

        // 关闭从线程池里更换ip的任务
        stopFresh();

        // 联表
        logger.info("正在联表...");
        String table_name = "results_" + sf.format(date);
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + table_name + "`");
        jdbcTemplate.execute("CREATE TABLE " + table_name + "(\n" +
                "\tSELECT \n" +
                "\tstation_train_code, \n" +
                "\t(SELECT id FROM stations WHERE stations.`name` = start_station_name) AS start_station_id, \n" +
                "\tstart_station_name, \n" +
                "\t(SELECT id FROM stations WHERE stations.`name` = end_station_name) AS end_station_id, \n" +
                "\tend_station_name, \n" +
                "\tstart_time, \n" +
                "\tarrive_time, \n" +
                "\tprice\n" +
                "\tFROM " + routesTableName + ", " + pricesTableName + " \n" +
                "\tWHERE " + routesTableName + ".train_no = " + pricesTableName + ".train_no AND " + routesTableName + ".from_station_no = " + pricesTableName + ".from_station_no AND " + routesTableName + ".to_station_no = " + pricesTableName + ".to_station_no\n" +
                ")");

        // 更新LineList
        logger.info("正在更新lineList...");
        searchService.initialize();

        endTime = System.currentTimeMillis();
        minutes = (endTime - startTime) / 1000 / 60;

        logger.info("定时任务执行完毕!");
        mailService.send("定时任务执行完毕, 总共用时" + minutes + "分钟!");
    }

    class Fresh implements Runnable {
        List<Proxy> proxyList = new ArrayList<>();
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        Proxy proxy;

        @SneakyThrows
        @Override
        public void run() {
            proxyList.clear();
            Set<String> proxies = jedis.hkeys("use_proxy");
            for (String proxyString : proxies) {
                String host = proxyString.substring(0, proxyString.indexOf(":"));
                int port = Integer.parseInt(proxyString.substring(proxyString.indexOf(":") + 1));
                proxy = new Proxy(host, port);
                proxyList.add(proxy);
            }
            httpClientDownloader.setProxyProvider(new SimpleProxyProvider(proxyList));
            logger.info("更换了一批代理");
        }
    }

}