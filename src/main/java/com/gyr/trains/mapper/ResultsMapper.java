package com.gyr.trains.mapper;

import com.gyr.trains.crawler.bean.Result;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ResultsMapper {
    List<Result> getAllResults(String tableName);
}
