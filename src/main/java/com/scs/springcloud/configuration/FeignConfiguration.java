package com.scs.springcloud.configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 大菠萝
 * @date 2023/02/12 19:38
 **/
@Slf4j
@Configuration
public class FeignConfiguration {

    /**
     * feign远程调用拦截器
     * @return
     */
    @Bean
    @Qualifier("FeignTransactionContextInterceptor")
    public FeignTransactionInterceptor FeignTransactionContextInterceptor(){
        return new FeignTransactionInterceptor(log);
    }



}
