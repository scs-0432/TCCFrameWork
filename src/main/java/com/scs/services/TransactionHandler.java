package com.scs.services;

import com.scs.common.bean.TransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author 大菠萝
 * @date 2023/02/12 19:11
 **/
public interface TransactionHandler {
    Object handle(TransactionContext context, ProceedingJoinPoint pjp) throws Throwable;

}