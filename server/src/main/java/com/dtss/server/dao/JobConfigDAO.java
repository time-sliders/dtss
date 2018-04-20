package com.dtss.server.dao;

import com.dtss.client.model.JobConfig;
import com.dtss.client.model.query.JobConfigQuery;

import java.util.List;

public interface JobConfigDAO {

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

    /**
     * 根据id更新一调数据
     *
     * @param updateParam 更新参数
     */
    int updateById(JobConfig updateParam);

    /**
     * 新增一条记录
     */
    int insert(JobConfig jobConfig);

    int deleteById(String id);
}