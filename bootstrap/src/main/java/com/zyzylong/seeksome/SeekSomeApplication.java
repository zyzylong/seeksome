package com.zyzylong.seeksome;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zyzylong.seeksome.admin.mapper")
public class SeekSomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeekSomeApplication.class, args);
    }
}
