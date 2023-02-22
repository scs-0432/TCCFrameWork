package com.scs.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public abstract class AbstractMiloTransactionAspect {

    private TransactionAspectHandler transactionAspectHandler;


    /**
     * 设置拦截后的处理器
     * @param
     */
    protected void setMiloTransactionAspect(final  TransactionAspectHandler transactionAspectHandler) {
        this.transactionAspectHandler = transactionAspectHandler;
    }

    /**
     * 拦截MiloTCC注解 {@linkplain }
     */
    @Pointcut("@annotation(com.scs.annotation.TCC)")
    public void interceptPointcut(){
    }

    /**
     * 拦截器处理方法
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("interceptPointcut()")
    public Object interceptPointcutHandleMethod(final ProceedingJoinPoint pjp) throws Throwable {
        return transactionAspectHandler.handleAspectPointcut(pjp);
    }

}
