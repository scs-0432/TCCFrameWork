package com.scs.services.impl;

import com.scs.common.PhaseEnum;
import com.scs.common.RoleEnum;
import com.scs.common.bean.InvocationBean;
import com.scs.common.bean.ParticipantBean;
import com.scs.common.bean.TransactionBean;
import com.scs.common.bean.TransactionContext;
import com.scs.common.threadLocal.TransactionThreadLocal;
import com.scs.common.utils.SpringBeanUtils;
import com.scs.services.TransactionScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author 大菠萝
 * @date 2023/02/21 16:51
 **/
@Slf4j
@Service
public class TransactionScheduleServiceImpl implements TransactionScheduleService {
    @Autowired
    TransactionExecutor transactionExecutor;

    @Override
    public void confirmPhase(TransactionBean transactionBean) {
        transactionBean.setRetryTimes(transactionBean.getRetryTimes() + 1);
        transactionExecutor.updateTransactionRetryTimes(transactionBean.getTransId(), transactionBean.getVersion(), transactionBean.getRetryTimes());
        List<ParticipantBean> participantBeanList = transactionBean.getParticipantBeanList();
        boolean confirmPhaseResult = true;
        if (!CollectionUtils.isEmpty(participantBeanList)) {
            List<ParticipantBean> failResult = new ArrayList<>();
            for (ParticipantBean participantBean : participantBeanList) {
                try {
                    reflectExecute(participantBean.getTransId(), participantBean,
                            PhaseEnum.CONFIRMING.getCode(), participantBean.getConfirmInvocation());
                } catch (Exception e) {
                    //执行失败的参与者
                    confirmPhaseResult = false;
                    failResult.add(participantBean);
                    log.info("执行confirmPhase参与者异常,e:{}", e);
                } finally {
                    TransactionThreadLocal.getInstance().removeContext();
                }
            }
            //处理confirm阶段结果：删除事务日志或者更新事务日志
            handleConfirmOrCancelPhaseResult(confirmPhaseResult, transactionBean, failResult);
        }
    }

    @Override
    public void cancelPhase(TransactionBean transactionBean) {

    }

    private void handleConfirmOrCancelPhaseResult(boolean confirmPhaseResult, TransactionBean transactionBean, List<ParticipantBean> failResult) {
        if (confirmPhaseResult) {
            transactionExecutor.removeTransaction(transactionBean.getTransId());
        } else {
            transactionBean.setParticipantBeanList(failResult);
            transactionExecutor.updateUpdateParticipantList(transactionBean);//更新重试次数
        }
    }

    private void reflectExecute(String transId, ParticipantBean participantBean, int phase, InvocationBean invocationBean) throws Exception {
        if (participantBean != null && invocationBean != null) {
            //构建事务上下文，如果confirm阶段执行的是本地confirm方法将不传递，
            // 如果是执行远程的目标方法（实际上是try方法），那么将传递事务上下文
            TransactionContext context = new TransactionContext();
            context.setTransId(transId);
            context.setPhase(phase);
            context.setRole(RoleEnum.CONSUMER.getCode());
            TransactionThreadLocal.getInstance().setContext(context);
            //执行目标方法
            final Class clazz = invocationBean.getTargetClass();
            final String method = invocationBean.getMethodName();
            final Object[] args = invocationBean.getArgs();
            final Class[] parameterTypes = invocationBean.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
        }
    }


}