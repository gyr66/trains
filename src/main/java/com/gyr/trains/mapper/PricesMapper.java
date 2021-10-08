package com.gyr.trains.mapper;

import com.gyr.trains.crawler.bean.Price;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PricesMapper {
    void insertPrices(@Param("tableName") String tableName, @Param("priceList") List<Price> priceList);
}
