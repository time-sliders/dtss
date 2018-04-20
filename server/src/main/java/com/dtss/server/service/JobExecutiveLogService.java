package com.dtss.server.service;

import com.dtss.client.model.JobExecutiveLog;
import com.dtss.client.model.query.JobExecutiveLogQuery;

import java.util.List;

public interface JobExecutiveLogService {

    /**
     * 批量查询
     *
     * @param query 查询参数
     */
    List<JobExecutiveLog> list(JobExecutiveLogQuery query);

    /**
     * 查询总量
     *
     * @param query 查询参数
     */
    Integer count(JobExecutiveLogQuery query);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    JobExecutiveLog findById(Long id);

    /**
     * 根据id更新一调数据
     *
     * @param updateParam 更新参数
     */
    int updateById(JobExecutiveLog updateParam);

    /**
     * 保存数据
     */
    int insert(JobExecutiveLog jobExecutiveLog);

    /**
     * 移动数据到历史表
     */
    int migrateToHis(JobExecutiveLog jobExecutiveLog);
}