package com.dtss.server.core.job.sender.stop;

import com.alibaba.fastjson.JSON;
import com.dtss.server.core.job.watch.ClientSystemNodeWatcher;
import com.dtss.client.consts.CmdConst;
import com.dtss.client.consts.ZookeeperPathConst;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.enums.JobExeStatusEnum;
import com.dtss.client.enums.OptType;
import com.dtss.client.model.JobExecutiveLog;
import com.dtss.client.model.ZkCmd;
import com.dtss.server.core.job.model.JobStopResult;
import com.dtss.server.core.job.sender.util.JobPathUtil;
import com.dtss.server.service.JobExecutiveLogService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 任务中断处理器
 *
 * @author LuYun
 * @since 2018.04.16
 */
@Component
public class JobStopExecutor implements ZookeeperPathConst, CmdConst {

    @Autowired
    private ZooKeeperComponent zooKeeperComponent;

    @Autowired
    private ClientSystemNodeWatcher clientSystemNodeWatcher;

    @Autowired
    private JobExecutiveLogService jobExecutiveLogService;

    /**
     * 尝试中止一个正在执行的任务
     * <p>
     * 1.任务是否只是发送到ZK,还未被处理
     * 1.1 是=>删除任务节点
     * 1.2 否=>确认当前服务器是否存活
     * 1.2.1 否=>结束
     * 1.2.2 是=>创建任务中止请求节点
     * 1.2.2.1 创建成功=>结束
     */
    public JobStopResult stop(JobExecutiveLog log) {

        if (log == null) {
            return JobStopResult.buildFail("任务执行记录不能为空");
        }

        if (log.getExecuteClientIp() == null) {
            JobExecutiveLog updateParam = new JobExecutiveLog();
            updateParam.setId(log.getId());
            updateParam.setStatus(JobExeStatusEnum.FAIL.getCode());
            updateParam.setInnerMsg("执行失败");
            jobExecutiveLogService.updateById(updateParam);

            return JobStopResult.buildSucc("任务删除成功");
        } else {
            checkTaskAssignNodeExists(log);
            return JobStopResult.buildSucc("已尝试中止任务[" + log.getExecuteClientIp() + "]");
        }
    }

    private void checkTaskAssignNodeExists(JobExecutiveLog log) {

        String taskAssignPath = JobPathUtil.buildTaskAssignNode(log.getApp(), log.getJobId(),
                log.getScheduleTime().toString(), log.getExecuteClientIp());

        zooKeeperComponent.getZooKeeper()
                .exists(taskAssignPath, false, taskAssignNodeExistsCallback, log);
    }

    private AsyncCallback.StatCallback taskAssignNodeExistsCallback = new AsyncCallback.StatCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            JobExecutiveLog log = (JobExecutiveLog) ctx;
            switch (KeeperException.Code.get(rc)) {
                case OK:
                    deleteTaskAssignNode(stat, path, log);
                    break;
                case NONODE:
                    sendTaskStopCmd(log);
                    break;
                default:
            }
        }
    };

    private void deleteTaskAssignNode(Stat stat, String path, JobExecutiveLog log) {
        zooKeeperComponent.getZooKeeper()
                .delete(path, stat.getVersion(), taskAssignNodeDeleteCallback, log);
    }

    private AsyncCallback.VoidCallback taskAssignNodeDeleteCallback = new AsyncCallback.VoidCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx) {
            JobExecutiveLog log = (JobExecutiveLog) ctx;
            switch (KeeperException.Code.get(rc)) {
                case OK:
                    break;
                case NONODE:
                    sendTaskStopCmd(log);
                    break;
                default:
            }
        }
    };

    private void sendTaskStopCmd(JobExecutiveLog log) {

        String clientIp = log.getExecuteClientIp();
        String app = log.getApp();

        List<String> clientList = clientSystemNodeWatcher.getAllClientList(log.getApp());
        if (CollectionUtils.isEmpty(clientList) || !clientList.contains(clientIp)) {
            System.out.println(log.getExecuteClientIp() + "未存活，操作取消");
            return;
        }

        String taskAssignPath = JobPathUtil.getTaskAssignPath(app, clientIp);
        String cmdKey = STOP_CMD_PREFIX + log.getId().toString();
        String taskStopCmdNode = taskAssignPath + I + cmdKey;

        ZkCmd zkCmd = new ZkCmd();
        zkCmd.setOptType(OptType.STOP.getCode());
        zkCmd.setLogId(log.getId());
        byte[] cmdData = JSON.toJSON(zkCmd).toString().getBytes();

        zooKeeperComponent.getZooKeeper()
                .create(taskStopCmdNode, cmdData, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT,
                        createTaskStopNodeCallBack, zkCmd);
    }

    private AsyncCallback.StringCallback createTaskStopNodeCallBack = new AsyncCallback.StringCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)) {
                case OK:
                    break;
                case NODEEXISTS:
                    break;
                default:
            }
        }
    };
}