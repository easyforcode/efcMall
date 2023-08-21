package com.mall.global.timeout;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TimeoutHandler {

  private static final Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);
  private static final ExecutorService executorService = Executors
      .newCachedThreadPool(
          new ThreadFactoryBuilder().setNameFormat("timeoutExecute-pool-%d").build());

  @Autowired
  private TaskTimeoutHelper retrievers;

  private volatile boolean running = true;

  @PostConstruct
  public void handle() {
    executorService.submit(() -> {
      int count = 0;
      while (running) {
        try {
          String requestId = retrievers.retrieve();
          logger.info("任务【{}】超时，开始处理", requestId);
          //SncExecutors.addTask(new EndTaskTask("TimeoutHandler", dcpService, requestId));
          count = 0;
        } catch (InterruptedException ignore) {
          logger.error("任务超时处理任务已被中断", ignore);
        } catch (Exception e) {
          try {
            if (count <= 500) {
              count += 10;
            }
            Thread.sleep(count * 10);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
          }
          logger.error("任务超时处理任务出现异常，Go to sleep : {}", count * 10, e);
        }
      }
    });
  }

  public void stop() {
    running = false;

  }

}
