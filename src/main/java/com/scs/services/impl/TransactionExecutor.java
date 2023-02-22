package com.scs.services.impl;

import com.scs.annotation.TCC;
import com.scs.common.bean.InvocationBean;
import com.scs.common.bean.ParticipantBean;
import com.scs.common.bean.TransactionBean;
import com.scs.common.config.FrameConfig;
import com.scs.dao.LogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author 大菠萝
 * @date 2023/02/12 19:18
 **/
@Service
@Slf4j
public class TransactionExecutor {
    final LogService logService;

    @Autowired
    public TransactionExecutor(LogService logService) {
        this.logService = logService;
    }

    public TransactionBean createAndSaveTransaction(String transId, ProceedingJoinPoint pjp, int phase, int role) throws Exception {
        if (Objects.nonNull(transId)) {
            transId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        TransactionBean bean = new TransactionBean(transId);
        bean.setPhase(phase);
        bean.setRole(role);
        bean.setRetryTimes(0);
        bean.setVersion(0);

        Class<?> clazz = pjp.getTarget().getClass();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object[] args = pjp.getArgs();
        TCC tcc = method.getAnnotation(TCC.class);
        bean.setTargetClass(clazz.getName());
        bean.setTargetMethod(method.getName());
        String confirmMethodName = tcc.confirmMethod();
        String cancelMethodName = tcc.cancelMethod();
        /*反射获取confirm和cancel方法*/
        InvocationBean confirmInvocationBean = getInvocationBean(bean, clazz, method, args, confirmMethodName);
        InvocationBean cancelInvocationBean = getInvocationBean(bean, clazz, method, args, cancelMethodName);
        ParticipantBean participantBean = ParticipantBean.builder().transId(bean.getTransId())
                .cancelInvocation(confirmInvocationBean)
                .confirmInvocation(cancelInvocationBean).build();
        //添加到线程变量里面
        bean.addParticipant(participantBean);
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());

        //存储事务日志
        int saveResult = logService.save(bean);
        if (saveResult != 0) {
            return bean;
        }
        throw new Exception("保存事务的时候dao层出现了异常");
    }

    public void updateTransactionPhase(String transId, int code) {
        logService.updatePhase(transId, code);
    }

    public void removeTransaction(String transId) {
        logService.remove(transId);
    }

    public void updateUpdateParticipantList(TransactionBean transactionBean) {
        logService.updateParticipantList(transactionBean);

    }

    public TransactionBean queryTransaction(String transId) {
        return logService.query(transId);
    }

    public List<TransactionBean> queryAllByDelay(Date delayDate) {
        return logService.queryAllByDelay(delayDate);

    }

    private InvocationBean getInvocationBean(TransactionBean bean, Class<?> clazz, Method method, Object[] args, String MethodName) {
        InvocationBean invoCationBean = null;
        if (!StringUtils.isEmpty(MethodName)) {
            bean.setConfirmMethod(MethodName);
            invoCationBean = new InvocationBean(clazz, MethodName, method.getParameterTypes(), args);
        }
        return invoCationBean;
    }

    public int updateTransactionRetryTimes(String transId, Integer version, Integer retryTimes) {
        return logService.updateTransactionRetryTimes(transId, version, retryTimes);
    }

}