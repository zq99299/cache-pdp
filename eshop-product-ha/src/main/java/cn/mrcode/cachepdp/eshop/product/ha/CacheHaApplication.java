package cn.mrcode.cachepdp.eshop.product.ha;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "cn.mrcode.cachepdp.eshop.product.ha.mappter")
public class CacheHaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheHaApplication.class, args);
    }
}
