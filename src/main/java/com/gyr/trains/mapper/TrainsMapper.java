package com.gyr.trains.mapper;

import com.gyr.trains.crawler.bean.Train;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainsMapper {
    void insertTrains(@Param("tableName") String tableName, @Param("trainList") List<Train> trainList);

    List<String> getAllTrain_nos(String tableName);
}
