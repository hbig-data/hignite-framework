# Apache Ignite Framework
============================================================================

    ignite-spring：基于Spring的配置支持
    ignite-indexing：SQL查询和索引
    ignite-geospatial：地理位置索引
    ignite-hibernate：Hibernate集成
    ignite-web：Web Session集群化
    ignite-schedule：基于Cron的计划任务
    ignite-log4j：Log4j日志
    ignite-jcl：Apache Commons logging日志
    ignite-jta：XA集成
    ignite-hadoop2-integration：HDFS2.0集成
    ignite-rest-http：HTTP REST请求
    ignite-scalar：Ignite Scalar API
    ignite-slf4j：SLF4J日志
    ignite-ssh；SSH支持，远程机器上启动网格节点
    ignite-urideploy：基于URI的部署
    ignite-aws：AWS S3上的无缝集群发现
    ignite-aop：网格支持AOP
    ignite-visor-console：开源的命令行管理和监控工具
    
## 生命周期事件类型

当前支持如下生命周期事件类型：

    BEFORE_NODE_START：Ignite节点的启动程序初始化之前调用
    AFTER_NODE_START：Ignite节点启动之后调用
    BEFORE_NODE_STOP：Ignite节点的停止程序初始化之前调用
    AFTER_NODE_STOP：Ignite节点停止之后调用
    