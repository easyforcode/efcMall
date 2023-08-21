package com.mall.global.threadPool.task;

import com.mall.global.threadPool.EfcExrcutorsTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisTask extends EfcExrcutorsTask {

  private static final Logger logger = LoggerFactory.getLogger(RedisTask.class);

  public RedisTask(String requestId) {
    super(requestId);
  }

  @Override
  protected boolean execute() {
    //System.out.println(testTableService.increment("hello"));
    return true;
  }

}
