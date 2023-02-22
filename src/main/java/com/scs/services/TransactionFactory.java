package com.scs.services;

import com.scs.common.bean.TransactionContext;

public interface TransactionFactory {

    Class factory(TransactionContext transactionContext) throws Throwable;
}



