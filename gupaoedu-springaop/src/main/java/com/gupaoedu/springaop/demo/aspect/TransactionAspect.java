package com.gupaoedu.springaop.demo.aspect;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionAspect {

    public void startTransaction(){
        log.info("方法调用之前开启事务!!!");
    }


    public void commitTransaction(){
        log.info("方法调用之后提交事务!!!");
    }

    public void rollbackTransaction(){
        log.info("出现异常时回滚事务!!!");
    }
}
