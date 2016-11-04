package com.rayn.ignite;

import com.rayn.ignite.lifecycbean.MyLifecycleBean;
import com.rayn.ignite.listener.DataGridIgniteInClosure;
import com.rayn.ignite.listener.MyIgniteInClosure;
import org.apache.ignite.*;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheException;

/**
 * @author Rayn
 * @email liuwei412552703@163.com
 * Created by Rayn on 2016/11/3 17:50.
 */
public class HelloIgnite {
    private static final Logger LOG = LoggerFactory.getLogger(HelloIgnite.class);
    private static final String cache_name = "mycache";

    private Ignite ignite = null;

    @Before
    public void setUp() throws Exception {

        // Create new configuration.
        IgniteConfiguration cfg = new IgniteConfiguration();

        //client mode.
        cfg.setClientMode(true);

        // Provide lifecycle bean to configuration.
        cfg.setLifecycleBeans(new MyLifecycleBean());


        /**
         * 管理慢客户端
         */
        TcpCommunicationSpi commSpi = new TcpCommunicationSpi();
        commSpi.setSlowClientQueueLimit(1000);
        cfg.setCommunicationSpi(commSpi);

        /**
         * 客户端自动重连可以通过TcpDiscoverySpi的clientReconnectDisabled属性禁用
         */
        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
        discoverySpi.setClientReconnectDisabled(true);

        //客户端可以请求网络中的存活服务端节点来启动。
        //如果不管服务端节点是否存活都要启动客户端节点非常必要，可以以如下的方式在客户端强制服务端模式发现：
        discoverySpi.setForceServerMode(true);

        cfg.setDiscoverySpi(discoverySpi);

        ignite = Ignition.start(cfg);

        /**
         * 创建分布式缓存
         */
        CacheConfiguration ccfg = new CacheConfiguration(cache_name);
        // Set required cache configuration properties.
        ccfg.setCacheMode(CacheMode.LOCAL);
        ccfg.setCopyOnRead(false);

        // Create cache on all the existing and future server nodes.
        // Note that since the local node is a client, it will not
        // be caching any data.
        IgniteCache<String, String> cache = ignite.getOrCreateCache(ccfg);


    }

    /**
     * 客户端计算重连
     *
     * @throws Exception
     */
    @Test
    public void testReconnCompute() throws Exception {
        IgniteCompute compute = ignite.compute();

        try {
            while (true) {
                compute.run(new IgniteRunnable() {
                    @Override
                    public void run() {


                    }
                });
            }
        } catch (IgniteClientDisconnectedException e) {
            // Wait for reconnect.
            e.reconnectFuture().get();
            // Can proceed and use the same IgniteCompute instance.
        }

    }

    /**
     * 客户端缓存重链
     *
     * @throws Exception
     */
    @Test
    public void testReconnCache() throws Exception {
        IgniteCache cache = ignite.getOrCreateCache(new CacheConfiguration<>());
        while (true) {
            try {
                cache.put("testKey", "testValue");
            } catch (CacheException e) {
                if (e.getCause() instanceof IgniteClientDisconnectedException) {
                    IgniteClientDisconnectedException cause = (IgniteClientDisconnectedException) e.getCause();
                    cause.reconnectFuture().get(); // Wait for reconnect.
                    // Can proceed and use the same IgniteCache instance.
                }
            }
        }

    }

    /**
     * 同步调用
     *
     * @throws Exception
     */
    @Test
    public void testComputeGridSync() throws Exception {
        IgniteCompute compute = ignite.compute();

        // Execute a job and wait for the result.
        // Execute computation on the server nodes.
        String res = compute.call(new IgniteCallable<String>() {
            @Override
            public String call() throws Exception {
                // Print hello world on some cluster node.
                System.out.println("Hello World");
                return "Hello World";
            }
        });

        /**
         * 服务端计算
         */
        // Execute computation on the server nodes (default behavior).
        compute.broadcast(new IgniteRunnable() {
            @Override
            public void run() {
                System.out.println("Hello Server");
            }
        });

        /**
         * 客户端计算
         */
        ClusterGroup clientGroup = ignite.cluster().forClients();
        IgniteCompute clientCompute = ignite.compute(clientGroup);

        // Execute computation on the client nodes.
        clientCompute.broadcast(new IgniteRunnable() {
            @Override
            public void run() {
                System.out.println("Hello Client");
            }
        });


    }

    /**
     * 计算网格异步调用
     *
     * @throws Exception
     */
    @Test
    public void testComputeGridAsync() throws Exception {
        // Enable asynchronous mode.
        IgniteCompute asyncCompute = ignite.compute().withAsync();

        // Asynchronously execute a job.
        asyncCompute.call(new IgniteCallable<String>() {
            @Override
            public String call() throws Exception {
                // Print hello world on some cluster node and wait for completion.
                System.out.println("Hello World");
                return "Hello World";
            }
        });

        // Get the future for the above invocation.
        IgniteFuture<String> fut = asyncCompute.future();

        // Asynchronously listen for completion and print out the result.
        fut.listen(new MyIgniteInClosure());

    }

    /**
     * 数据网格同步
     *
     * @throws Exception
     */
    @Test
    public void testDataGridSync() throws Exception {
        IgniteCache<String, Integer> cache = ignite.cache(cache_name);

        // Synchronously store value in cache and get previous value.
        Integer val = cache.getAndPut("1", 1);

    }

    /**
     * 数据网格异步
     *
     * @throws Exception
     */
    @Test
    public void testDataGridAsync() throws Exception {
        // Enable asynchronous mode.
        IgniteCache<Object, Object> asyncCache = ignite.cache(cache_name).withAsync();

        // Asynchronously store value in cache.
        asyncCache.getAndPut("1", 1);

        // Get future for the above invocation.
        IgniteFuture<Integer> fut = asyncCache.future();

        // Asynchronously listen for the operation to complete.
        fut.listen(new DataGridIgniteInClosure());

    }
}
