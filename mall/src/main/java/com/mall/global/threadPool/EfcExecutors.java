package com.mall.global.threadPool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EfcExecutors {
  private static final Logger logger = LoggerFactory.getLogger(EfcExecutors.class);
  private static ExecutorService executor = Executors.newFixedThreadPool(50);

  public static ExecutorService getExecutor() {
    return executor;
  };

  public static void addTask(EfcExrcutorsTask task) {
    getExecutor().execute(task);
  }
}
