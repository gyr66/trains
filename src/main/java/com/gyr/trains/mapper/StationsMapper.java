package com.gyr.trains.mapper;

import com.gyr.trains.algorithm.Station;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StationsMapper {
    List<Station> getAllStations();

    Station getStationByName(String name);
}
