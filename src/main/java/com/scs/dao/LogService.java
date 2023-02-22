package com.scs.dao;

import com.scs.common.bean.TransactionBean;
import com.scs.common.config.FrameConfig;

import java.util.Date;
import java.util.List;

/**
 * @author 大菠萝
 * @date 2023/02/12 19:27
 **/
public interface LogService {

    void init(FrameConfig config);

    int save(TransactionBean bean);

    int updatePhase(String transId, Integer phase);

    int updateParticipantList(TransactionBean bean);

    int remove(String transId);

    TransactionBean query(String transId);

    List<TransactionBean> queryAllByDelay(Date delayTime);

    int updateTransactionRetryTimes(String transId, Integer version, Integer retryTimes);

}