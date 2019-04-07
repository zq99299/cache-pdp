package cn.mrcode.cachepdp.eshop.cache.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import cn.mrcode.cachepdp.eshop.cache.service.CacheService;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/7 16:30
 */
@Component
public class KafkaInit implements ApplicationRunner {
    @Autowired
    private CacheService cacheService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(new KafkaConcusmer("eshop-message", cacheService)).start();
    }
}
