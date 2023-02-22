package com.scs.bootstrap;

import com.scs.common.config.FrameConfig;
import com.scs.dao.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 大菠萝
 * @date 2023/02/21 15:51
 **/
@Service
@Slf4j
public class BootstrapService {
    private LogService logService;
    @Autowired

    public BootstrapService(LogService logService) {
        this.logService = logService;
    }

    public void initTccTransaction(FrameConfig frameConfig) {
        try {
            log.info("TCC framework init start");
            //初始化连接池，创建数据库表
            logService.init(frameConfig);
            log.info("TCC framework init success");
        } catch (Exception e) {
            log.info("TCC framework init exception:{}",e);
        }
    }
}