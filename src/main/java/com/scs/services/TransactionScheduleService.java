package com.scs.services;

import com.scs.common.bean.TransactionBean;

public interface TransactionScheduleService {
    void confirmPhase(TransactionBean transactionBean);

    void cancelPhase(TransactionBean transactionBean);
}
