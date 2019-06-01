package cn.mrcode.cachepdp.eshop.product.ha;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "cn.mrcode.cachepdp.eshop.product.ha.mappter")
public class ProductHaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductHaApplication.class, args);
    }
}
