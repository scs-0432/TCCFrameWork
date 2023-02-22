package com.scs.springcloud.start.configuration;

import com.scs.bootstrap.BootstrapService;
import com.scs.bootstrap.TransactionBootstrap;
import com.scs.springcloud.start.config.ConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = {"com.scs"})
@EnableTransactionManagement
public class AutoConfiguration {


    private final ConfigProperties configProperties;

    @Autowired
    public AutoConfiguration(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    

    @Bean
    @Qualifier("TransactionBootstrap")
    public TransactionBootstrap TransactionBootstrap(BootstrapService bootstrapService){
        log.info("ConfigProperties:{}",configProperties);
        TransactionBootstrap TransactionBootstrap = new TransactionBootstrap(bootstrapService,configProperties);
        return TransactionBootstrap;
    }


}
