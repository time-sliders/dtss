package com.dtss.client.core.handle;

import com.alibaba.fastjson.JSON;
import com.dtss.client.core.zk.ZooKeeperComponent;
import com.dtss.client.model.ZkCmd;
import com.dtss.commons.Millisecond;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * job处理器
 *
 * @author luyun
 * @since 2018.01.28 16:55
 */
@Component
public class AssignedTaskHandler implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AssignedTaskHandler.class);

    @Autowired
    private ZooKeeperComponent zooKeeperComponent;

    @Autowired
    private CmdDispatcher cmdDispatcher;

    private static ThreadPoolExecutor tpe;

    private static final String THREAD_NAME = "任务处理线程";

    public void handle(String zkJobNodePath, String app) {
        zooKeeperComponent.getZooKeeper()
                .getData(zkJobNodePath, false, getTaskDataCallback, app);
    }

    /**
     * 获取到任务节点数据之后的回调函数
     */
    private AsyncCallback.DataCallback getTaskDataCallback = new AsyncCallback.DataCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            switch (code) {
                case NONODE:
                    logger.info("[DTSS]ZooKeeper获取已分配任务时发现任务节点［" + path + "］被移除");
                    break;
                case OK:
                    deleteTaskNode(path, stat, data);
                    break;
                case CONNECTIONLOSS:
                    handle(path, (String) ctx);
                    break;
                default:
            }
        }
    };

    /**
     * 从ZooKeeper上删除任务节点
     *
     * @param path 任务节点路径
     * @param stat 节点状态
     */
    private void deleteTaskNode(String path, Stat stat, byte[] data) {
        zooKeeperComponent.getZooKeeper()
                .delete(path, stat.getVersion(), taskNodeDeleteCallback, data);
    }

    private AsyncCallback.VoidCallback taskNodeDeleteCallback = new AsyncCallback.VoidCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx) {
            KeeperException.Code code = KeeperException.Code.get(rc);
            switch (code) {
                case OK:
                    dealTaskNodeData((byte[]) ctx);
                    break;
                default:
            }
        }
    };

    private void dealTaskNodeData(byte[] data) {
        tpe.submit(new JobDataConsumer(data));
    }


    private class JobDataConsumer implements Runnable {

        private byte[] data;

        JobDataConsumer(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {

            try {
                String jobDataJSONString = new String(data);
                ZkCmd cmd = JSON.parseObject(jobDataJSONString, ZkCmd.class);
                Integer optType = cmd.getOptType();

                logger.info("接收到任务:" + jobDataJSONString);
                JobHandler jobHandler = cmdDispatcher.getExecutor(optType);
                if (jobHandler == null) {
                    logger.error("[DTSS]未知的命令类型:" + optType);
                    return;
                }

                jobHandler.handle(cmd);

            } catch (Throwable e) {
                logger.error("[DTSS]客户端执行任务异常", e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        tpe = new ThreadPoolExecutor(20, 20,
                Millisecond.TEN_SECONDS, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName(THREAD_NAME);
                        return t;
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        JobDataConsumer task = (JobDataConsumer) r;
                        String jobDataJSONString = new String(task.data);
                        logger.info("[DTSS]ZooKeeper由于待处理任务线程池已满，执行任务取消!CmdJSON:" + jobDataJSONString);
                    }
                });
        tpe.allowCoreThreadTimeOut(true);
    }
}
