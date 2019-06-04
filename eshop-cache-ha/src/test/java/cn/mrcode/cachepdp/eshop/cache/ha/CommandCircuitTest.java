package cn.mrcode.cachepdp.eshop.cache.ha;

import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/4 22:35
 */
public class CommandCircuitTest {
    @Test
    public void test() throws InterruptedException {
        Date start = new Date();
        for (int i = 0; i < 11; i++) {
            boolean flag = false;
            if (i > 4) {
                flag = true;
            }
            CommandCircuit commandCircuit = new CommandCircuit(flag);
            TimeUnit.MILLISECONDS.toMillis(500);
            System.out.println(i + " - " + commandCircuit.execute());
        }
        System.out.println("流量 10 个，异常 50 % 达标：start=" + start + ";end=" + new Date());
        TimeUnit.SECONDS.sleep(4);
        System.out.println("尝试请求：" + new Date());
        for (int i = 0; i < 4; i++) {
            CommandCircuit c = new CommandCircuit(false);
            System.out.println(c.execute());
        }
        TimeUnit.SECONDS.sleep(3);
        System.out.println("3 秒之后，断路器变成半开状态，一个请求通过");
        CommandCircuit commandCircuit = new CommandCircuit(false);
        System.out.println(commandCircuit.execute());
        System.out.println("断路器关闭，尝试访问");
        for (int i = 0; i < 3; i++) {
            CommandCircuit c = new CommandCircuit(false);
            System.out.println(c.execute());
        }
    }
}