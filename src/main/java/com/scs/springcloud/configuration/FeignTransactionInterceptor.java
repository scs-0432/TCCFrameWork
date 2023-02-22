package com.scs.springcloud.configuration;

import com.scs.common.RoleEnum;
import com.scs.common.bean.TransactionContext;
import com.scs.common.threadLocal.TransactionThreadLocal;
import com.scs.common.bean.rpc.RpcMediator;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Objects;
import org.slf4j.Logger;

/**
 * @author 大菠萝
 * @date 2023/02/12 17:28
 **/
public class FeignTransactionInterceptor  implements RequestInterceptor {
    private Logger logger;

    public FeignTransactionInterceptor(Logger logger) {
        this.logger = logger;
    }
    @Override
    public void apply(RequestTemplate requestTemplate) {
        TransactionContext context = TransactionThreadLocal.getInstance().getContext();
        if (Objects.nonNull(context) && context.getRole() == RoleEnum.INITIATOR.getCode()) {
            logger.info("拦截当前线程的请求并且添加参数，当前线程的名字{}", Thread.currentThread().getName());
            RpcMediator rpcMediator = RpcMediator.getRpcMediator();
            rpcMediator.transfer(requestTemplate, context);

        }
    }
}