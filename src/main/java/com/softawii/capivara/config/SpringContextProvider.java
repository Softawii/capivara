package com.softawii.capivara.config;

import com.softawii.curupira.v2.integration.ContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringContextProvider implements ContextProvider {

    private final ApplicationContext context;

    public SpringContextProvider(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public <T> T getInstance(Class<T> aClass) {
        return context.getBean(aClass);
    }
}
