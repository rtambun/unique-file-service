package com.rtambun.minio.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
@PropertySource("classpath:application.properties")
public class ApplicationProperties {

    private Environment env;

    public ApplicationProperties(Environment env) {
        this.env = env;
    }

    public String getConfigValue(String configKey){
        return env.getProperty(configKey);
    }
}
