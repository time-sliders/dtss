package com.dtss.commons.concurrent;

/**
 * 抽象完成服务的结果消费者
 *
 * @param <V> 单条数据处理结果类型
 * @param <S> 所有V的汇总结果
 * @author luyun
 * @version 2017.7.31 TB to HQB
 * @since 2017.07.27
 */
public abstract class AbstractResultConsumer<V, S> {

    protected S s;

    public AbstractResultConsumer(S s) {
        this.s = s;
    }

    /**
     * 消费处理单条结果，将单条任务的结果汇总到总结果对象上
     *
     * @param v 单个任务的执行结果
     */
    public abstract void consume(V v);

    /**
     * 获取汇总结果
     */
    public S getResult() {
        return s;
    }

}
