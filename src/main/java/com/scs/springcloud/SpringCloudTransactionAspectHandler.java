package com.scs.springcloud;

import com.scs.aspect.TransactionAspectHandler;
import com.scs.common.RoleEnum;
import com.scs.common.bean.TransactionContext;
import com.scs.common.threadLocal.TransactionThreadLocal;
import com.scs.common.bean.rpc.RpcMediator;
import com.scs.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Objects;

/**
 * @author 大菠萝
 * @date 2023/02/12 15:49
 **/
@Component
@Slf4j
public class SpringCloudTransactionAspectHandler implements TransactionAspectHandler {
    private TransactionService transactionService;
    private final String TRANSACTION_CONTEXT = "transaction_context";

    @Autowired
    public SpringCloudTransactionAspectHandler(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Object handleAspectPointcut(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("执行事务当前线程的名字是{}", Thread.currentThread().getName());
        TransactionContext context = TransactionThreadLocal.getInstance().getContext();
        if (Objects.nonNull(context)) {
            if (RoleEnum.INITIATOR.getCode() == context.getRole()) {
                context.setRole(RoleEnum.CONSUMER.getCode());
            }
        } else {

           final RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            TransactionContext transactionContext = RpcMediator.getRpcMediator().getTransactionContext(requestAttributes);
            if (transactionContext != null) {
                log.info("拿到别的服务的context对象", transactionContext);
                transactionContext.setRole(RoleEnum.PROVIDER.getCode());
            }
        }
        return transactionService.invoke(context, proceedingJoinPoint);

    }


}
