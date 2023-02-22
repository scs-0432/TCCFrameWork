package com.scs.services.schedule;


import com.scs.common.PhaseEnum;
import com.scs.common.RoleEnum;
import com.scs.common.bean.TransactionBean;
import com.scs.common.config.FrameConfig;
import com.scs.services.TransactionScheduleService;
import com.scs.services.impl.TransactionExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@SuppressWarnings("all")
public class TransactionLogHandleScheduled implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private FrameConfig Config;

    @Autowired
    private TransactionExecutor TransactionExecutor;

    @Autowired
    private TransactionScheduleService TransactionScheduleService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        log.info("==============初始化Schedule start==================");

        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, (Runnable runable) -> {
            Thread thread = new Thread(runable, "Scheduled-Thread");
            thread.setDaemon(true);
            return thread;
        });
        startSchedule(scheduledThreadPoolExecutor);
        log.info("==============初始化Schedule end==================");
    }

        /**
     * 处理事务日志
     * 第180秒开始执行 处理第90秒前的事务日志（未超最大处理次数，超过人工介入）
     * 第270秒执行 处理第180秒前的事务日志（未超最大处理次数，超过人工介入）
     * @param scheduledExecutorService
     */
    private void startSchedule(ScheduledExecutorService scheduledExecutorService){
        scheduledExecutorService.scheduleWithFixedDelay(()->{
            log.info("==============Schedule start====================");
            try {
                Date delayDate = new Date(System.currentTimeMillis() - Config.getScheduleDelay() * 1000);
                List<TransactionBean> TransactionList = TransactionExecutor.queryAllByDelay(delayDate);
                for(TransactionBean transactionBean : TransactionList){
                    if (transactionBean.getRetryTimes() > Config.getRetryMax()) {
                        log.error("重试次数过多，需要人工介入处理，transactionBean:{}",transactionBean);
                        continue;
                    }
                    Integer role = transactionBean.getRole();
                    Integer phase = transactionBean.getPhase();
                    if(role == RoleEnum.INITIATOR.getCode()){//发起者
                        if(phase == PhaseEnum.READY.getCode() || phase == PhaseEnum.TRYING.getCode() || phase == PhaseEnum.CANCELING.getCode()){
                            //执行cancel流程
                            TransactionScheduleService.cancelPhase(transactionBean);
                        }else if(phase == PhaseEnum.CONFIRMING.getCode()){
                            //执行confirm流程
                            TransactionScheduleService.confirmPhase(transactionBean);
                        }else{
                            log.error("未知事务日志，transactionBean：{}",transactionBean);
                        }
                    }else if(role == RoleEnum.CONSUMER.getCode()){//消费者
                        log.error("未知事务日志，transactionBean：{}",transactionBean);
                    }else if(role == RoleEnum.PROVIDER.getCode()){//提供者
                        if(phase == PhaseEnum.READY.getCode() || phase == PhaseEnum.TRYING.getCode() || phase == PhaseEnum.CANCELING.getCode()){
                            //执行cancel流程
                            TransactionScheduleService.cancelPhase(transactionBean);
                        }else if(phase == PhaseEnum.CONFIRMING.getCode()){
                            //执行confirm流程
                            TransactionScheduleService.confirmPhase(transactionBean);
                        }else{
                            log.error("未知事务日志，transactionBean：{}",transactionBean);
                        }
                    }else{
                        log.error("未知事务日志，transactionBean：{}",transactionBean);
                    }
                }
            } catch (Exception e) {
                log.error("Schedule error:{}",e);
            }
            log.info("==============Schedule end====================");
        },Config.getScheduleInitDelay(),Config.getScheduleDelay(), TimeUnit.SECONDS);

    }

}
