package com.dtss.commons.concurrent;

import java.util.concurrent.Callable;

/**
 * 抽象的任务提供者,负责为{@link EnhanceCompletionService}提供处理任务
 *
 * @author luyun
 * @version ZERO 2.0
 * @see EnhanceCompletionService
 * @see AbstractResultConsumer
 * @since 2017.09.04
 */
public abstract class AbstractTaskProvider<V, S> {

    protected EnhanceCompletionService<V, S> ecs;

    public AbstractTaskProvider(EnhanceCompletionService<V, S> ecs) {
        this.ecs = ecs;
    }

    /**
     * 提交所有的任务到{@link AbstractTaskProvider#ecs}中
     * <p>
     * 提交单个任务时需要使用 {@link EnhanceCompletionService#submit(Callable)} 方法<br/>
     * 该方法允许分页查询,并不影响消费线程消费结果
     */
    public abstract void offerTasks();
}
