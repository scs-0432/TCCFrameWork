package com.scs.aspect;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author 大菠萝
 * @date 2023/02/08 23:26
 **/
public interface TransactionAspectHandler {
    Object handleAspectPointcut(ProceedingJoinPoint proceedingJoinPoint) throws Throwable;
}