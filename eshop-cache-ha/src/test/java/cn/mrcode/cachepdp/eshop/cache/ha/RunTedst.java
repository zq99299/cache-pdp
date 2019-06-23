package cn.mrcode.cachepdp.eshop.cache.ha;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command.GetProductCommand2;

/**
 * <pre>
 * ${desc}
 * </pre>
 *
 * @author zhuqiang
 * @date 2019/6/19 10:12
 */
public class RunTedst {
    //test.java
    volatile int finishState = 0;


    @Test
    public void test4() throws InterruptedException, ExecutionException {
        BlockingQueue queue = new SynchronousQueue();
        queue = new LinkedBlockingDeque<>(20);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                10,
                50,
                60,
                TimeUnit.SECONDS,
                queue);
        CountDownLatch c = new CountDownLatch(40);
        IntStream.range(0, 40)
                .parallel()
                .mapToObj(item -> (Runnable) () -> {
                    System.out.println(Thread.currentThread().getName());
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    c.countDown();

                })
                .forEach(item -> threadPoolExecutor.submit(item));
        c.await();
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 50; i++) {
//                    String name = "name_" + i;
//                    RunTedst testCallable = new RunTedst(name);
//                    try {
//                        executorCompletionService.submit(testCallable);
//
//                        synchronized (lock) {
//                            System.out.print("+++添加任务 name: " + name);
//                            System.out.print(" ActiveCount: " + threadPoolExecutor.getActiveCount());
//                            System.out.print(" poolSize: " + threadPoolExecutor.getPoolSize());
//                            System.out.print(" queueSize: " + threadPoolExecutor.getQueue().size());
//                            System.out.println(" taskCount: " + threadPoolExecutor.getTaskCount());
//                        }
//                    } catch (RejectedExecutionException e) {
//                        synchronized (lock) {
//                            System.out.println("拒绝：" + name);
//                        }
//                    }
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                finishState = 1;
//            }
//        };
//
//        Thread addThread = new Thread(runnable);
//        addThread.start();
//
//        //System.out.println(" taskCount: " + threadPoolExecutor.getTaskCount());
//
//        //添加的任务有被抛弃的。taskCount不一定等于添加的任务。
//        int completeCount = 0;
//        while (!(completeCount == threadPoolExecutor.getTaskCount() && finishState == 1)) {
//            Future<String> take = executorCompletionService.take();
//            String taskName = take.get();
//            synchronized (lock) {
//                System.out.print("---完成任务 name: " + taskName);
//                System.out.print(" ActiveCount: " + threadPoolExecutor.getActiveCount());
//                System.out.print(" poolSize: " + threadPoolExecutor.getPoolSize());
//                System.out.print(" queueSize: " + threadPoolExecutor.getQueue().size());
//                System.out.print(" taskCount: " + threadPoolExecutor.getTaskCount());
//                System.out.println(" finishTask：" + (++completeCount));
//
//            }
//        }
//
//        addThread.join();
//
//
//        while (threadPoolExecutor.getPoolSize() > 0) {
//            Thread.sleep(1000);
//            synchronized (lock) {
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
//                System.out.print(simpleDateFormat.format(new Date()));
//                //System.out.print("name: " + taskName);
//                System.out.print(" ActiveCount: " + threadPoolExecutor.getActiveCount());
//                System.out.print(" poolSize: " + threadPoolExecutor.getPoolSize());
//                System.out.print(" queueSize: " + threadPoolExecutor.getQueue().size());
//                System.out.println(" taskCount: " + threadPoolExecutor.getTaskCount());
//            }
//        }
//
//        // Tell threads to finish off.
//        threadPoolExecutor.shutdown();
//        // Wait for everything to finish.
//        while (!threadPoolExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
//            System.out.println("complete");
//        }

    }
}
