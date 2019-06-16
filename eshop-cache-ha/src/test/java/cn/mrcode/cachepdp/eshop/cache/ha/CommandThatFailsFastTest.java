package cn.mrcode.cachepdp.eshop.cache.ha;

import com.netflix.hystrix.exception.HystrixRuntimeException;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void testSuccess() {
        assertEquals("success", new CommandThatFailsFast(false).execute());
    }

    @Test
    public void testFailure2() {
        try {
            new CommandThatFailsFast(true).execute();
//            fail("we should have thrown an exception");
        } catch (HystrixRuntimeException e) {
            System.out.println("xxx");
            assertEquals("failure from CommandThatFailsFast", e.getCause().getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void fun4() {
        CommandThatFailsFast2 commandThatFailsFast2 = new CommandThatFailsFast2();
        Iterator<Integer> iterator = commandThatFailsFast2.observe().toBlocking().getIterator();
        while (iterator.hasNext()) {
            System.out.println("响应结果：" + iterator.next());
        }
    }
}