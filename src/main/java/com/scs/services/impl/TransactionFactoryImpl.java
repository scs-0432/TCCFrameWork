package com.scs.services.impl;

import com.scs.common.RoleEnum;
import com.scs.common.bean.TransactionContext;
import com.scs.services.TransactionFactory;

import java.util.Objects;

/**
 * @author 大菠萝
 * @date 2023/02/12 18:40
 **/
public class TransactionFactoryImpl implements TransactionFactory {


    @Override
    public Class factory(TransactionContext context) throws Exception {
        if (Objects.isNull(context)) {
            return InitializationTransactionHandler.class;
        } else {
            if (context.getRole() == RoleEnum.CONSUMER.getCode()) {
                return ConsumerTransactionHandler.class;
            }
            if (context.getRole() == RoleEnum.PROVIDER.getCode()) {
                return ProviderTransactionHandler.class;
            }
        }
        throw new Exception("找不到对应的事务处理器");


    }
}