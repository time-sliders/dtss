package com.dtss.commons;

import java.io.Serializable;

/**
 * 查询条件<br/>
 * 作为Facade层暴露给外部的通用Query对象父类
 * 定义一些子类共用属性
 *
 * @author luyun
 * @since 2017.01.16 (fund portfolio pre)
 */
public class Query implements Serializable {

    private static final long serialVersionUID = 1877004675323533451L;

    /**
     * 默认的最大查询条数
     */
    private static final int DEFAULT_LIMIT = 1000;

    /**
     * 最大查询条数
     */
    private static final int MAX_LIMIT = 5000;

    /**
     * 默认的偏移量
     */
    private static final int DEFAULT_OFFSET = 0;

    /**
     * 查询条数
     */
    private Integer limit;

    /**
     * 偏移量
     */
    private Integer offset;

    /**
     * 排序字段
     */
    private OrderBy orderBy;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * 针对公共查询做修饰处理，防止参数错误
     */
    public void decorate() {
        /*
         * offset 与 limit 都不存在时，查询所有数据
         * 如果有一个有值，则另外一个必须同时有值，否则不会生效
         */
        if (offset != null) {
            offset = offset < 0 ? DEFAULT_OFFSET : offset;
            if (limit == null) {
                limit = DEFAULT_LIMIT;
            }
        }
        if (limit != null) {
            limit = limit <= 0 || limit > MAX_LIMIT ? DEFAULT_LIMIT : limit;
            if (offset == null) {
                offset = DEFAULT_OFFSET;
            }
        }
    }

}
