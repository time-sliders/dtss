# 分布式任务调度系统
* 一款采用Java语言，基于分布式中间件Zookeeper开发的分布式任务调度系统。
* 以Zookeeper实现Leader选举与任务分发功能。
* 任务触发基于Quartz框架。
* Zk与DB的数据一致性以内置的数据补偿的方式保障。
* 单秒任务触发峰值为800（这取决于不同的机器性能）

## 功能简介

### 如何实现分布式
* 多台Server同时存在时，通过创建ZooKeeper临时节点的方式选举出一个Leader，由Leader来做任务发送。
* 当Leader死亡时，其他存活Server尝试获取Leader权限。
* 同一时间会只有一台Leader存活（极端情况下会存在瞬时的脑裂现象，此时通过数据库唯一主键保障任务不会重复发送）

### 服务器端-添加任务

可以添加一个远程任务：
* 指定一个 Cron 表达式循环的执行任务。
* 添加任务时，测试这个任务是否配置成功。
* 任务执行时，支持友好的中断。


需要指定这个远程任务的*所属应用，SpringBean，Cron表达式*等等。
![img](https://github.com/time-sliders/dtss/blob/master/_readme/addJob.png)


### 服务器端-任务列表
查看配置了哪些任务，可以编辑修改这些任务，比如：*执行时间、参数*等等。
![img](https://github.com/time-sliders/dtss/blob/master/_readme/jobView.png)



### 服务器端-任务执行记录
查看所有的任务执行记录，包括：*发送任务与执行任务的机器，执行结果、任务耗时、返回信息*等等。
![img](https://github.com/time-sliders/dtss/blob/master/_readme/jobLogList.png)
![img](https://github.com/time-sliders/dtss/blob/master/_readme/jobLogDetail.png)


### Zookeeper节点情况
![img](https://github.com/time-sliders/dtss/blob/master/_readme/zkNode.png)

### 其他
其他文档请参考 _manualDoc目录下文件介绍
