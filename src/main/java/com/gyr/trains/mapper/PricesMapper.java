package com.gyr.trains.mapper;

import com.gyr.trains.crawler.bean.Price;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PricesMapper {
    void insertPrices(String tableName, List<Price> priceList);
}
