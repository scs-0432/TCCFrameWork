package com.scs.bootstrap;


import com.scs.common.config.FrameConfig;
import com.scs.common.utils.SpringBeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;



public class TransactionBootstrap implements ApplicationContextAware{

    private BootstrapService bootstrapService;

    private FrameConfig frameConfig;

    @Autowired
    public TransactionBootstrap(BootstrapService bootstrapService, FrameConfig frameConfig) {
        this.bootstrapService = bootstrapService;
        this.frameConfig = frameConfig;
    }




    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtils.getInstance().setCfgContext((ConfigurableApplicationContext)applicationContext);
        bootstrapService.initTccTransaction(frameConfig);
    }


}
