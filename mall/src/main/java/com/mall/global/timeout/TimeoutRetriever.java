package com.mall.global.timeout;

public interface TimeoutRetriever {

  String retrieve() throws InterruptedException;

  boolean createExecutionResult(String retrieveKey);
}
