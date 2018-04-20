package com.dtss.server.core.job.async;

import com.dtss.client.model.JobConfig;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.01
 */
@Component
public class DeleteJobHandler extends AbstractAsyncJobChangeHandler<JobConfig> {

    private static final Logger logger = LoggerFactory.getLogger(DeleteJobHandler.class);

    @Override
    public void execute(JobConfig jobConfig) {
        zooKeeperComponent.getZooKeeper()
                .getData(getJobNodePath(jobConfig), false, zkJobsNodeDeleteQueryCallback, jobConfig);
    }

    private AsyncCallback.DataCallback zkJobsNodeDeleteQueryCallback = new AsyncCallback.DataCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            JobConfig jobConfig = (JobConfig) ctx;
            switch (code) {
                case SESSIONEXPIRED:
                case CONNECTIONLOSS:
                    execute(jobConfig);
                    break;
                case OK:
                    deleteNode(path, stat, (JobConfig) ctx);
                    break;
                case NONODE:
                    break;
                default:
            }
        }
    };

    private void deleteNode(String path, Stat stat, JobConfig ctx) {
        zooKeeperComponent.getZooKeeper()
                .delete(path, stat.getVersion(), nodeDeleteCallback, ctx);
    }

    private AsyncCallback.VoidCallback nodeDeleteCallback = new AsyncCallback.VoidCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            JobConfig jobConfig = (JobConfig) ctx;
            switch (code) {
                case SESSIONEXPIRED:
                case CONNECTIONLOSS:
                    execute(jobConfig);
                    break;
                case OK:
                    logger.info("[DTSS]ZooKeeper任务节点 [" + path + "] 删除成功.");
                    break;
                case NONODE:
                    break;
                default:
            }
        }
    };
}
