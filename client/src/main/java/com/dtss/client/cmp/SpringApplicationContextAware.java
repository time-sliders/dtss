package com.dtss.client.cmp;

import com.dtss.client.core.zk.ZooKeeperComponent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringApplicationContextAware implements ApplicationContextAware {

    private static ApplicationContext springAc;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringApplicationContextAware.springAc = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return springAc;
    }

    private static final String ZK_CMP_BEAN_NAME = "zooKeeperComponent";

    public static ZooKeeperComponent getZkCmp() {
        return springAc.getBean(ZK_CMP_BEAN_NAME, ZooKeeperComponent.class);
    }
}
