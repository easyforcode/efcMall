package com.mall.global.threadPool;

public abstract class EfcExrcutorsTask implements Runnable {

  /**
   * 全局唯一，用于标识是那里请求
   */
  protected String requestType;

  /**
   * 任务执行入口
   */
  public void run() {
    before();
    boolean success = execute();
    after(success);
  }

  /**
   * 在执行任务前执行
   */
  protected void before() {
  }

  /**
   * 执行任务
   *
   * @return
   */
  protected abstract boolean execute();

  /**
   * 在执行任务后执行
   *
   * @param success
   */
  protected void after(boolean success) {

  }

  public EfcExrcutorsTask(String requestId) {
    this.requestType = requestType;
  }
}
