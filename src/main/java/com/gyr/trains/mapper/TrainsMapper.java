package com.gyr.trains.mapper;

import com.gyr.trains.crawler.bean.Train;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TrainsMapper {
    void insertTrains(String tableName, List<Train> trainList);
    List<String> getAllTrain_nos(String tableName);
}
