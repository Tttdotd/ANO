package com.tdotd.ano;

import com.tdotd.ano.config.AnoProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.tdotd.ano.mapper")
@EnableConfigurationProperties(AnoProperties.class)
public class AnoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnoApplication.class, args);
    }

}
