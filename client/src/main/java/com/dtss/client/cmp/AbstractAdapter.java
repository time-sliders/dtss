package com.dtss.client.cmp;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * 抽象适配器
 * <p/>
 * 适用于当业务上需要根据某一个字段 决定由不同的处理器来处理业务逻辑时.<br/>
 * demo:当请求类型购买时,由购买引擎处理购买请求
 *
 * @author luyun
 */
public class AbstractAdapter<K/*获取执行者的参数类型,一般为String*/, E/*定义的执行者父类*/>
        implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext context;

    private Map<K, String/*Spring Bean Name*/> executorMapping = new HashMap<K, String>();

    // 默认的执行者
    private String defaultExecutor;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    protected void registerDefaultExecutor(String defaultExecutorBeanName) {
        defaultExecutor = defaultExecutorBeanName;
    }

    protected void registerExecutor(K k, String executorBeanName) {
        executorMapping.put(k, executorBeanName);
    }

    /**
     * 根据不同的字段类型,获取不同的执行者
     */
    @SuppressWarnings("unchecked")
    public E getExecutor(K k) {

        String executor;

        if (k == null) {
            executor = defaultExecutor;
        } else {
            executor = executorMapping.get(k);
            if (executor == null) {
                executor = defaultExecutor;
            }
        }

        if (executor == null) {
            return null;
        }

        return (E) context.getBean(executor);
    }

    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(defaultExecutor) && MapUtils.isEmpty(executorMapping)) {
            throw new RuntimeException(getClass() + "尚未注册执行者");
        }
    }
}
