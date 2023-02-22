package com.scs.springcloud;

import com.scs.aspect.AbstractMiloTransactionAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author 大菠萝
 * @date 2023/02/08 23:29
 **/
@Component
public class SpringCloudTransactionAspect extends AbstractMiloTransactionAspect implements Ordered {

    @Autowired
    public SpringCloudTransactionAspect(SpringCloudTransactionAspectHandler springCloudTransactionAspectHandler) {
        super.setMiloTransactionAspect(springCloudTransactionAspectHandler);

    }
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}