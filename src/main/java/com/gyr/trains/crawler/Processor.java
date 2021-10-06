package com.gyr.trains.crawler;

import com.gyr.trains.crawler.webmagic.Page;
import com.gyr.trains.crawler.webmagic.Site;
import com.gyr.trains.crawler.webmagic.processor.PageProcessor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Processor implements PageProcessor {
    Logger logger = LoggerFactory.getLogger(getClass());

    private final Site site = Site.me()
            .setTimeOut(1000)
            .setCycleRetryTimes(500000)
            .setRetryTimes(10)
            .setSleepTime(500)
            .setCharset("utf-8")
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Safari/537.36")
            .addCookie("JSESSIONID", "5065C3572EA3E654EEA599868726AA29");

    @SneakyThrows
    @Override
    public void process(Page page) {
       page.putField("content", page.getRawText());
       page.putField("url", page.getRequest().getUrl());
    }

    @Override
    public Site getSite() {
        return site;
    }
}