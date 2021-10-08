package com.gyr.trains.mapper;

import com.gyr.trains.crawler.bean.Route;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoutesMapper {
    void insertRoutes(@Param("tableName") String tableName, @Param("routeList") List<Route> routeList);

    List<Route> getAllRoutes(String tableName);
}
