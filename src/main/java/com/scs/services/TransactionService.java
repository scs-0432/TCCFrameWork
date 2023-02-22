package com.scs.services;

import com.scs.common.bean.TransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author 大菠萝
 * @date 2023/02/12 15:52
 **/
public interface TransactionService {

    Object invoke(TransactionContext context, ProceedingJoinPoint proceedingJoinPoint) throws Throwable;
}