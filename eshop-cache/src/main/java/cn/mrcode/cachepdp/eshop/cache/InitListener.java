package cn.mrcode.cachepdp.eshop.cache;

import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import cn.mrcode.cachepdp.eshop.cache.prewarm.CachePrewarmThread;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/5/25 15:58
 */
@Component
public class InitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        SpringContextUtil.setWebApplicationContext(webApplicationContext);

        new CachePrewarmThread().start();
    }
}
