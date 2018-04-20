package com.dtss.server.service;

import com.dtss.client.model.JobConfig;
import com.dtss.client.model.query.JobConfigQuery;

import java.util.List;

/**
 * @author luyun
 * @since 2018.02.10 16:12
 */
public interface JobQueryService {

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<JobConfig> query(JobConfigQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer count(JobConfigQuery query);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    JobConfig findById(String id);
}
