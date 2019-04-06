package cn.mrcode.cachepdp.eshop.inventory.request;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程与队列初始化
 *
 * @author : zhuqiang
 * @date : 2019/4/3 22:44
 */
@Component
public class RequestQueue implements ApplicationRunner {
    private List<ArrayBlockingQueue<Request>> queues = new ArrayList<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int workThread = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(workThread);
        for (int i = 0; i < workThread; i++) {
            ArrayBlockingQueue<Request> queue = new ArrayBlockingQueue<>(100);
            executorService.submit(new RequestProcessorThread(queue));
            queues.add(queue);
        }
    }

    public ArrayBlockingQueue<Request> getQueue(int index) {
        return queues.get(index);
    }

    public int queueSize() {
        return queues.size();
    }
}
