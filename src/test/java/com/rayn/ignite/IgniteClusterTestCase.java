package com.rayn.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.cluster.ClusterMetrics;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.zk.TcpDiscoveryZookeeperIpFinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Rayn
 * @email liuwei412552703@163.com
 * Created by Rayn on 2016/11/4 10:10.
 */
public class IgniteClusterTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteClusterTestCase.class);

    private Ignite ignite = null;
    private IgniteCluster cluster = null;

    @Before
    public void setUp() throws Exception {
        // Create new configuration.
        IgniteConfiguration cfg = new IgniteConfiguration();

        Map<String, String> attrs = Collections.singletonMap("ROLE", "worker");
        cfg.setUserAttributes(attrs);


        ignite = Ignition.ignite(UUID.randomUUID().toString().replaceAll("-", ""));
        cluster = ignite.cluster();

        /**
         * 基于多播发现
         */
        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setMulticastGroup("228.10.10.157");
        spi.setIpFinder(ipFinder);


        /**
         * 基于静态 IP 发现
         */
        TcpDiscoveryVmIpFinder vmIpFinder = new TcpDiscoveryVmIpFinder();
        // Set initial IP addresses.
        // Note that you can optionally specify a port or a port range.
        vmIpFinder.setAddresses(Arrays.asList("1.2.3.4", "1.2.3.5:47500..47509"));

        /**
         * 基于多播和静态IP的发现
         */
        TcpDiscoveryMulticastIpFinder ipFinderComplex = new TcpDiscoveryMulticastIpFinder();
        // Set Multicast group.
        ipFinderComplex.setMulticastGroup("228.10.10.157");
        // Set initial IP addresses.
        // Note that you can optionally specify a port or a port range.
        ipFinderComplex.setAddresses(Arrays.asList("1.2.3.4", "1.2.3.5:47500..47509"));
        spi.setIpFinder(ipFinderComplex);


        /**
         * 基于 Zookeeper 服务发现
         */
        TcpDiscoveryZookeeperIpFinder ipFinderZookeeper = new TcpDiscoveryZookeeperIpFinder();
        // Specify ZooKeeper connection string.
        ipFinderZookeeper.setZkConnectionString("127.0.0.1:2181");
        spi.setIpFinder(ipFinderZookeeper);




        // Override default discovery SPI.
        cfg.setDiscoverySpi(spi);
        // Start Ignite node.
//        Ignition.start(cfg);

    }

    @After
    public void tearDown() throws Exception {


    }


    /**
     * 集群节点属性
     *
     * @throws Exception
     */
    @Test
    public void testClusterNodeAttr() throws Exception {
        ClusterGroup workers = ignite.cluster().forAttribute("ROLE", "worker");

        Collection<ClusterNode> nodes = workers.nodes();

        for (ClusterNode node : nodes) {
            Map<String, Object> attributes = node.attributes();
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                LOG.info("Node Key : {}, Node Value : {}", entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * 集群节点指标数据
     *
     * @throws Exception
     */
    @Test
    public void testMetricsInfo() throws Exception {

        /**
         * 当前的Ignite节点
         * 本地集群节点
         */
        ClusterNode localClusterNode = ignite.cluster().localNode();


        // Local Ignite node.
        ClusterNode localNode = cluster.localNode();

        // Node metrics.
        ClusterMetrics metrics = localNode.metrics();

        // Get some metric values.
        double cpuLoad = metrics.getCurrentCpuLoad();
        long usedHeap = metrics.getHeapMemoryUsed();
        int numberOfCores = metrics.getTotalCpus();
        int activeJobs = metrics.getCurrentActiveJobs();

        LOG.info("CPU Load : {}, useHeap : {}, numOfCore : {}, activeJobs : {}", cpuLoad, usedHeap, numberOfCores, activeJobs);
    }

    /**
     * 集群组
     * 表示集群内节点的一个逻辑组
     * <p>
     * 从设计上讲，所有集群节点都是平等的，所以没有必要以一个特定的顺序启动任何节点，或者给他们赋予特定的规则。然而，
     * Ignite可以因为一些应用的特殊需求而创建集群节点的逻辑组，比如，可能希望只在远程节点上部署一个服务，或者给部分worker
     * 节点赋予一个叫做‘worker’的规则来做作业的执行。
     *
     * @throws Exception
     */
    @Test
    public void testClusterGroup() throws Exception {
        final Ignite ignite = Ignition.ignite();
        IgniteCluster cluster = ignite.cluster();

        // Get compute instance which will only execute
        // over remote nodes, i.e. not this node.
        IgniteCompute compute = ignite.compute(cluster.forRemotes());

        // Broadcast to all remote nodes and print the ID of the node
        // on which this closure is executing.
        compute.broadcast(new IgniteRunnable() {
            @Override
            public void run() {
                System.out.println("Hello Node: " + ignite.cluster().localNode().id());
            }
        });


    }

    /**
     * 预定义集群组
     */
    @Test
    public void testName() throws Exception {

        IgniteCluster cluster = ignite.cluster();

        // Cluster group with remote nodes, i.e. other than this node.
        ClusterGroup remoteGroup = cluster.forRemotes();

        /**
         *获得赋予了‘worker’属性值的节点
         */
        ClusterGroup workerGroup = cluster.forAttribute("ROLE", "worker");
        Collection<ClusterNode> nodes = workerGroup.nodes();


        /**
         * Nodes with less than 50% CPU load.
         * 集群组只会包括CPU利用率小于50%的节点，注意这个组里面的节点会随着CPU负载的变化而改变
         */
        ClusterGroup readyNodes = cluster.forPredicate(new IgnitePredicate<ClusterNode>() {
            @Override
            public boolean apply(ClusterNode clusterNode) {
                return clusterNode.metrics().getCurrentCpuLoad() < 0.5;
            }
        });


        /**
         * Group containing oldest node out of remote nodes.
         * 集群组组合
         * 可以通过彼此之间的嵌套来组合集群组，比如，下面的代码片段显示了如何通过组合最老组和远程组来获得最老的远程节点：
         */
        ClusterGroup oldestGroup = cluster.forRemotes().forOldest();
        ClusterNode oldestNode = oldestGroup.node();


        /**
         * 从集群组中获得节点
         */
        // All cluster nodes in the group.
        Collection<ClusterNode> grpNodes = remoteGroup.nodes();

        // First node in the group (useful for groups with one node).
        ClusterNode node = remoteGroup.node();

        // And if you know a node ID, get node by ID.
        UUID myID = UUID.fromString("test");
        node = remoteGroup.node(myID);


        /**
         * 集群组获得元数据
         */
        // Cluster group metrics.
        ClusterMetrics metrics = remoteGroup.metrics();

        // Get some metric values.
        double cpuLoad = metrics.getCurrentCpuLoad();
        long usedHeap = metrics.getHeapMemoryUsed();
        int numberOfCores = metrics.getTotalCpus();
        int activeJobs = metrics.getCurrentActiveJobs();


    }


    /**
     * 领导选举
     *
     * @throws Exception
     */
    @Test
    public void testLeaderSelect() throws Exception {
        /**
         * 最老的节点
         *
         * 一旦获得了集群组，就可以用它执行任务、部署服务、发送消息等。
         */
        IgniteCluster cluster = ignite.cluster();
        // Dynamic cluster group representing the oldest cluster node.
        // Will automatically shift to the next oldest, if the oldest
        // node crashes.
        ClusterGroup oldestNode = cluster.forOldest();

        /**
         * 最新的节点
         */
        // Dynamic cluster group representing the youngest cluster node.
        // Will automatically shift to the next youngest, if the youngest
        // node crashes.
        ClusterGroup clusterGroup = cluster.forYoungest();


    }
}
