package cn.mrcode.cachepdp.eshop.cache;

import org.springframework.web.context.WebApplicationContext;

public class SpringContextUtil {
    private static WebApplicationContext context;

    public static WebApplicationContext getWebApplicationContext() {
        return context;
    }

    public static void setWebApplicationContext(WebApplicationContext webApplicationContext) {
        context = webApplicationContext;
    }
}
