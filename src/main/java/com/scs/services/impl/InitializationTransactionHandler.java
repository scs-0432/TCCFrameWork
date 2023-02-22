package com.scs.services.impl;

import com.scs.common.PhaseEnum;
import com.scs.common.ResultEnum;
import com.scs.common.RoleEnum;
import com.scs.common.bean.InvocationBean;
import com.scs.common.bean.ParticipantBean;
import com.scs.common.bean.TransactionBean;
import com.scs.common.bean.TransactionContext;
import com.scs.common.threadLocal.TransactionThreadLocal;
import com.scs.common.utils.SpringBeanUtils;
import com.scs.services.TransactionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author 大菠萝
 * @date 2023/02/12 19:12
 **/
@Slf4j
@Service
public class InitializationTransactionHandler implements TransactionHandler {
    @Getter
    private TransactionExecutor transactionExecutor;


    @Autowired
    public InitializationTransactionHandler(TransactionExecutor transactionExecutor) {
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public Object handle(TransactionContext context, ProceedingJoinPoint pjp) throws Throwable {
        Object val = null;
        try {
            try {
                val = tryPhase(context, pjp);
                TransactionBean bean = TransactionThreadLocal.getInstance().getBean();
                confirmPhase(bean);
            } catch (Throwable e) {
                TransactionBean bean = TransactionThreadLocal.getInstance().getBean();
                cancelPhase(bean);
                throw e;
            }
        } finally {
            TransactionThreadLocal.getInstance().removeContext();
            TransactionThreadLocal.getInstance().removeBean();

        }
        return val;
    }

    public Object cancelPhase(TransactionBean bean) {
        Object cancelResult = null;
        bean.setPhase(PhaseEnum.CANCELING.getCode());
        transactionExecutor.updateTransactionPhase(bean.getTransId(), PhaseEnum.CANCELING.getCode());
        List<ParticipantBean> participantBeanList = bean.getParticipantBeanList();
        boolean cancelPhaseResult = true;
        if (!CollectionUtils.isEmpty(participantBeanList)) {
            List<ParticipantBean> successful = new ArrayList<>();
            List<ParticipantBean> failure = new ArrayList<>();
            for (ParticipantBean participantBean : participantBeanList) {
                try {
                    reflectAndHandle(participantBean.getTransId(), participantBean, PhaseEnum.CANCELING.getCode(), participantBean.getConfirmInvocation());
                    successful.add(participantBean);
                } catch (Exception e) {
                    cancelPhaseResult = false;
                    failure.add(participantBean);
                    e.printStackTrace();
                } finally {
                    TransactionThreadLocal.getInstance().removeContext();
                }
            }
            handleConfirmOrCancelPhaseResult(cancelPhaseResult, bean, failure);
            if (successful.size() > 0) {
                cancelResult = successful.get(0);
            }

        }
        return cancelPhaseResult;


    }


    private void handleConfirmOrCancelPhaseResult(boolean confirmPhaseResult, TransactionBean transactionBean, List<ParticipantBean> failResult) {
        if (confirmPhaseResult) {
            transactionExecutor.removeTransaction(transactionBean.getTransId());
        } else {
            transactionBean.setParticipantBeanList(failResult);
            transactionExecutor.updateUpdateParticipantList(transactionBean);
        }
    }

    private Object tryPhase(TransactionContext context, ProceedingJoinPoint pjp) throws Throwable {
        Object value = null;
        TransactionBean transactionBean = transactionExecutor.createAndSaveTransaction(null, pjp, PhaseEnum.READY.getCode(), RoleEnum.INITIATOR.getCode());
        TransactionThreadLocal instance = TransactionThreadLocal.getInstance();
        TransactionContext contextInstance = TransactionContext.builder()
                .transId(transactionBean.getTransId())
                .phase(PhaseEnum.TRYING.getCode())
                .role(RoleEnum.INITIATOR.getCode())
                .build();
        instance.setBean(transactionBean);
        instance.setContext(contextInstance);
        value = pjp.proceed();
        //在执行完真正的业务后 trying 阶段后 开始更新事务日志表
        transactionBean.setPhase(PhaseEnum.TRYING.getCode());
        transactionExecutor.updateTransactionPhase(transactionBean.getTransId(), PhaseEnum.TRYING.getCode());
        return value;
    }

    public Object confirmPhase(TransactionBean bean) throws Exception {
        log.info("开始事务的confirm 阶段，当前的线程{}", Thread.currentThread().getName());
        Object returnVal = null;
        boolean confirmResult = true;
        if (Objects.nonNull(bean)) {
            bean.setPhase(PhaseEnum.CONFIRMING.getCode());
            transactionExecutor.updateTransactionPhase(bean.getTransId(), PhaseEnum.CONFIRMING.getCode());
            List<ParticipantBean> successful = new ArrayList<>();
            List<ParticipantBean> failure = new ArrayList<>();
            if (!CollectionUtils.isEmpty(bean.getParticipantBeanList())) {
                for (ParticipantBean participantBean : bean.getParticipantBeanList()) {
                    try {
                        reflectAndHandle(participantBean.getTransId(), participantBean, PhaseEnum.CONFIRMING.getCode(), participantBean.getConfirmInvocation());
                        successful.add(participantBean);
                    } catch (Exception e) {
                        confirmResult = false;
                        failure.add(participantBean);
                        e.printStackTrace();
                    } finally {
                        TransactionThreadLocal.getInstance().removeContext();
                    }
                }
                handleConfirmOrCancelPhaseResult(confirmResult, bean, failure);
                if (successful.size() > 0) {
                    returnVal = successful.get(0);
                }
                return returnVal;
            }
        }
        return returnVal;

    }

    private Object reflectAndHandle(String transId, ParticipantBean participantBean, int phase, InvocationBean invocationBean) throws Exception {
        if (Objects.nonNull(participantBean) && Objects.nonNull(transId)) {
            TransactionBean transactionBean = new TransactionBean();
            transactionBean.setTransId(transId);
            transactionBean.setPhase(phase);
            transactionBean.setRole(RoleEnum.CONSUMER.getCode());
            final Class clazz = invocationBean.getTargetClass();
            final String method = invocationBean.getMethodName();
            final Object[] args = invocationBean.getArgs();
            final Class[] parameterTypes = invocationBean.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            return MethodUtils.invokeMethod(bean, method, args, parameterTypes);
        }
        return null;
    }
}