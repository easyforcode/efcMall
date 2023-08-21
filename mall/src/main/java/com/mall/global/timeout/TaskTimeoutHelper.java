package com.mall.global.timeout;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskTimeoutHelper implements TimeoutRetriever {

  private TimeoutDelayQueue queue;

  public TaskTimeoutHelper(StringRedisTemplate stringRedisTemplate) {
    this.queue = new TimeoutDelayQueue("SCRIPT:TASK:TIMEOUT", stringRedisTemplate);
  }

  public boolean setTimeout(String taskId, long timeout, long startTime) {
    return queue.setTimeout(buildKey(taskId), timeout, startTime);
  }

  @Override
  public String retrieve() throws InterruptedException {
    return queue.take();
  }

  @Override
  public boolean createExecutionResult(String retrieveKey) {
    String[] taskIdAndStepId = retrieveKey.split("_");
    long taskId = Long.parseLong(taskIdAndStepId[0]);
    long stepId = Long.parseLong(taskIdAndStepId[1]);
    return true;
  }

  public void remove(String taskId) {
    queue.remove(buildKey(taskId));
  }


  private String buildKey(String taskId) {
    return taskId;
  }
}
