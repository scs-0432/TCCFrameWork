package com.scs.services.impl;

import com.scs.common.PhaseEnum;
import com.scs.common.RoleEnum;
import com.scs.common.bean.TransactionBean;
import com.scs.common.bean.TransactionContext;
import com.scs.services.TransactionHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 大菠萝
 * @date 2023/02/19 21:26
 **/
@Service
@Slf4j
public class ProviderTransactionHandler implements TransactionHandler {
    private InitializationTransactionHandler initializationTransactionHandler;
    private final TransactionExecutor transactionExecutor;

    @Autowired
    public ProviderTransactionHandler(InitializationTransactionHandler initializationTransactionHandler) {
        this.initializationTransactionHandler = initializationTransactionHandler;
        this.transactionExecutor = initializationTransactionHandler.getTransactionExecutor();
    }

    @Override
    public Object handle(TransactionContext context, ProceedingJoinPoint pjp) throws Throwable {
        if (context == null) {
            return null;
        }
        switch (PhaseEnum.acquireByCode(context.getPhase())) {
            case  TRYING:
                    TransactionBean bean = null;
                try {
                    log.info("提供者事务开始");
                    bean = transactionExecutor.createAndSaveTransaction(context.getTransId(), pjp, PhaseEnum.TRYING.getCode(), RoleEnum.PROVIDER.getCode());
                    Object proceed = pjp.proceed();
                    return proceed;
                } catch (Throwable e) {
                    throw e;
                }finally {
                    if (bean != null) {
                        transactionExecutor.updateTransactionPhase(bean.getTransId(),PhaseEnum.TRYING.getCode());
                    }
                }
            case CONFIRMING:
                Object result = null;
                try {
                    TransactionBean transactionBean = transactionExecutor.queryTransaction(context.getTransId());
                    if (transactionBean != null) {
                        if (transactionBean.getPhase() == PhaseEnum.READY.getCode()) {
                            throw new Exception("try 阶段还没有执行完毕就收到confirm 指令");
                        }
                        result = initializationTransactionHandler.confirmPhase(transactionBean);
                    }
                } catch (Exception e) {
                    throw e;
                }
                return result;

            case CANCELING:
                Object result_cancel = null;
                try {
                    TransactionBean transactionBean = transactionExecutor.queryTransaction(context.getTransId());
                    if (transactionBean != null) {
                        if (transactionBean.getPhase() == PhaseEnum.READY.getCode()) {
                            throw new Exception("try 阶段还没有执行完毕就收到canceling 指令");
                        }
                        result_cancel = initializationTransactionHandler.cancelPhase(transactionBean);
                    }
                } catch (Exception e) {
                    throw e;
                }
                return result_cancel;

        }
        return null;
    }

}