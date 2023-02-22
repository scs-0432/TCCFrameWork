package com.scs.springcloud.start.config;

import com.scs.common.config.FrameConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component("config")
@ConfigurationProperties(prefix = "tcc",
        ignoreInvalidFields = true)
public class ConfigProperties extends FrameConfig {

}
