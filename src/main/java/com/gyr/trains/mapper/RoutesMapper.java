package com.gyr.trains.mapper;

import com.gyr.trains.crawler.bean.Route;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoutesMapper {
    void insertRoutes(String tableName, List<Route> routeList);

    List<Route> getAllRoutes();
}
