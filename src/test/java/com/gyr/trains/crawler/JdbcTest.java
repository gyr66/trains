package com.gyr.trains.crawler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class JdbcTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void jdbcTest() {
        jdbcTemplate.execute("" +
                "CREATE TABLE `12306`.`table_test`  (\n" +
                "  `id` int NOT NULL AUTO_INCREMENT,\n" +
                "  `name` varchar(255) NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ");");
    };

}
