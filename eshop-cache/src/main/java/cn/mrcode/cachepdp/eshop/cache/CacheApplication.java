package cn.mrcode.cachepdp.eshop.cache;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

@SpringBootApplication
@MapperScan(value = "cn.mrcode.cachepdp.eshop.cache.mapper")
@EnableCaching
public class CacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheApplication.class, args);
    }

    @Bean
    public JedisCluster jedisCluster() {
        // 这里使用 redis-trib.rb check 192.168.99.170:7001 找到 3 个 master 节点，添加进来
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        jedisClusterNodes.add(new HostAndPort("192.168.99.170", 7001));
        jedisClusterNodes.add(new HostAndPort("192.168.99.172", 7005));
        jedisClusterNodes.add(new HostAndPort("192.168.99.171", 7003));
        JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes);
        return jedisCluster;
    }
}
