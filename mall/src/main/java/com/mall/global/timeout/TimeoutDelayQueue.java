package com.mall.global.timeout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TimeoutDelayQueue {

  private static final Logger logger = LoggerFactory.getLogger(TimeoutDelayQueue.class);

  /**
   * 队列唯一标识，同时也是作为Redis的队列的key
   */
  private final String id;
  private final ReentrantLock lock = new ReentrantLock();
  private final Condition available = lock.newCondition();
  private Thread leader = null;

  private ZSetOperations<String, String> operations;

  public TimeoutDelayQueue(String id, StringRedisTemplate stringRedisTemplate) {
    this.id = id;
    this.operations = stringRedisTemplate.opsForZSet();
  }


  public boolean setTimeout(String key, long timeout, long startTime) {
    final ReentrantLock thisLock = this.lock;
    thisLock.lock();

    try {
      long expectedTime = startTime + timeout;
      logger.debug("设置超时时间，key:{}, 超时时间：{}ms, 当前时间戳: {}, 期望过期时间戳: {}", key, timeout, startTime,
          expectedTime);
      operations.add(id, key, expectedTime);
      String first = doPeek();
      if (key.equals(first)) {
        leader = null;
        available.signal();
      }
      return true;
    } finally {
      thisLock.unlock();
    }
  }

  public String poll() {
    final ReentrantLock thisLock = this.lock;
    thisLock.lock();
    try {
      String first = doPeek();
      if (first == null) {
        return null;
      } else {
        long delay = getDelay(first);
        if (delay > 0) {
          return null;
        }
        return doPoll(first);
      }
    } finally {
      thisLock.unlock();
    }
  }

  public String poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    final ReentrantLock thisLock = this.lock;
    thisLock.lockInterruptibly();
    try {
      for (; ; ) {
        String first = doPeek();
        if (first == null) {
          if (nanos <= 0L) {
            return null;
          } else {
            nanos = available.awaitNanos(nanos);
          }
        } else {
          long delay = getDelay(first);
          if (delay <= 0L) {
            return doPoll(first);
          }
          if (nanos <= 0L) {
            return null;
          }
          // don't retain ref while
          first = null;
          if (nanos < delay || leader != null) {
            nanos = available.awaitNanos(nanos);
          } else {
            Thread thisThread = Thread.currentThread();
            leader = thisThread;
            try {
              long timeLeft = available.awaitNanos(delay);
              nanos -= delay - timeLeft;
            } finally {
              if (leader == thisThread) {
                leader = null;
              }
            }
          }
        }
      }
    } finally {
      if (leader == null) {
        available.signal();
      }
    }
  }

  public String take() throws InterruptedException {
    final ReentrantLock thisLock = this.lock;
    thisLock.lockInterruptibly();
    try {
      for (; ; ) {
        String first = doPeek();
        if (first == null) {
          available.await();
        } else {
          long delay = getDelay(first);
          if (delay <= 0) {
            logger.debug("超时时间已到，task:{}, 当前时间戳 ：{}", first, System.currentTimeMillis());
            String result = doPoll(first);
            if (result != null) {
              return result;
            }
          }
          // don't retain ref while wai
          first = null;
          if (delay <= 0) {
            logger.debug("poll失败，...");
            continue;
          }
          if (leader != null) {
            available.await();
          } else {
            Thread thisThread = Thread.currentThread();
            leader = thisThread;
            try {
              available.awaitNanos(delay);
            } finally {
              if (leader == thisThread) {
                leader = null;
              }
            }
          }
        }
      }
    } finally {
      if (leader == null) {
        available.signal();
      }
      thisLock.unlock();
    }
  }

  private String doPoll(String expected) {
    Long remove = operations.remove(id, expected);
    // remove success
    if (remove == 1L) {
      return expected;
    }
    logger.debug("task：{} 不存在", expected);
    return null;
  }

  private String doPeek() {
    Set<String> range = operations.range(id, 0, 0);
    if (!range.isEmpty()) {
      return range.iterator().next();
    }
    return null;
  }

  private String doPeek(String expected) {
    Set<String> range = operations.range(id, 0, 0);
    String result = null;
    if (!range.isEmpty()) {
      result = range.iterator().next();
      if (result.equals(expected)) {
        return result;
      }
    }
    if (result != null) {
      logger.debug("Peek元素失败，期望task:{}, 实际task:{}", expected, result);
    }
    return null;
  }

  /**
   * 返回步骤剩余ns数
   *
   * @param v task_step
   * @return 剩余ns
   */
  private long getDelay(String v) {
    Double score = operations.score(id, v);
    if (score != null) {
      // 毫秒转纳秒
      return (score.longValue() - System.currentTimeMillis()) * 1000000L;
    }
    // must return greater than 0
    return 1000;
  }

  public void remove(String key) {
    operations.remove(id, key);
  }
}
