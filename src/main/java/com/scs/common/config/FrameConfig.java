package com.scs.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 大菠萝
 * @date 2023/02/12 19:32
 **/
@Getter
public class FrameConfig {

    private int retryMax = 3;

    @Setter
    private String modelName;

    @Setter
    private DbConfig dbConfig;

    private Long scheduleInitDelay = 180L;

    private Long scheduleDelay = 90L;
}