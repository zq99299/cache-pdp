package cn.mrcode.cachepdp.eshop.cache.ha;

import org.junit.Test;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/3 23:08
 */
public class CommandThatFailsFastTest {
    @Test
    public void testFailure() {
        String execute = new CommandThatFailsFast(true).execute();
        System.out.println(execute);
    }
}