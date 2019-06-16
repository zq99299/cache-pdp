package cn.mrcode.cachepdp.eshop.cache.ha;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan(value = "cn.mrcode.cachepdp.eshop.cache.ha.mappter")
public class CacheHaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheHaApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean bean = new FilterRegistrationBean<>();
        // 在 jdk8 中 Filter 接口 除了 javax.servlet.Filter.doFilter 方法外，其他两个方法都是默认方法了
        // 所以这里使用了拉姆达表达式
        bean.setFilter((request, response, chain) -> {
            HystrixRequestContext context = HystrixRequestContext.initializeContext();
            try {
                chain.doFilter(request, response);
            } finally {
                context.shutdown();
            }
        });
        bean.addUrlPatterns("/*");
        return bean;
    }

    @Bean
    public ServletRegistrationBean indexServletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new HystrixMetricsStreamServlet());
        registration.addUrlMappings("/hystrix.stream");
        return registration;
    }
}
