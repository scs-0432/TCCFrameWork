package com.scs.services.impl;

import com.scs.common.PhaseEnum;
import com.scs.common.bean.InvocationBean;
import com.scs.common.bean.ParticipantBean;
import com.scs.common.bean.TransactionBean;
import com.scs.common.bean.TransactionContext;
import com.scs.common.threadLocal.TransactionThreadLocal;
import com.scs.services.TransactionHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * @author 大菠萝
 * @date 2023/02/18 14:57
 **/
@Service
@Slf4j
public class ConsumerTransactionHandler implements TransactionHandler {
    private TransactionExecutor transactionExecutor;

    @Autowired
    public ConsumerTransactionHandler(TransactionExecutor transactionExecutor) {
        this.transactionExecutor = transactionExecutor;
    }


    @Override
    public Object handle(TransactionContext context, ProceedingJoinPoint pjp) throws Throwable {
        if (context.getPhase() == PhaseEnum.CONFIRMING.getCode()) {
            log.info("调用参与者的feign接口去间接执行confirm方法");
            return pjp.proceed();
        }else if(context.getPhase() == PhaseEnum.CANCELING.getCode()){
            log.info("调用参与者的feign接口去间接执行cancel方法");
            return pjp.proceed();
        }else{
            //接口有tcc被调用
            TransactionBean transactionBean = TransactionThreadLocal.getInstance().getBean();
            ParticipantBean participantBean = createParticipant(context,pjp);
            transactionBean.addParticipant(participantBean);
            //更新发起者事务日志的参与者信息
            transactionExecutor.updateUpdateParticipantList(transactionBean);
            return pjp.proceed();
        }

    }
    private ParticipantBean createParticipant(TransactionContext context,ProceedingJoinPoint pjp){
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object[] args = pjp.getArgs();
        final Class<?> declaringClass = method.getDeclaringClass();
        InvocationBean confirmInvocationBean = new InvocationBean(declaringClass,method.getName(),method.getParameterTypes(),args);
        InvocationBean cancelInvocationBean = new InvocationBean(declaringClass,method.getName(),method.getParameterTypes(),args);
        ParticipantBean participantBean = new ParticipantBean(context.getTransId(),confirmInvocationBean,cancelInvocationBean);
        return participantBean;
    }
   
}