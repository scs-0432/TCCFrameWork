package com.scs.services.impl;

import com.scs.common.bean.TransactionContext;
import com.scs.common.utils.SpringBeanUtils;
import com.scs.services.TransactionFactory;
import com.scs.services.TransactionHandler;
import com.scs.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 大菠萝
 * @date 2023/02/12 18:23
 **/
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private TransactionFactory transactionFactory;
    @Autowired
    public TransactionServiceImpl(TransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
    }


    @Override
    public Object invoke(TransactionContext context, ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Class handlerClass = transactionFactory.factory(context);
        SpringBeanUtils instance = SpringBeanUtils.getInstance();
        TransactionHandler bean = (TransactionHandler) instance.getBean(handlerClass);
        log.info("获取到对应的事务处理器" + bean.toString());
        return bean.handle(context, proceedingJoinPoint);

    }
}