package com.example.stage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 */
@MapperScan(basePackages = {"com.example.**.mapper"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class StageApplication {

    public static void main(String[] args) {
        SpringApplication.run(StageApplication.class, args);
    }

}
