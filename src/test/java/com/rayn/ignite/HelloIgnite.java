package com.rayn.ignite;

import com.rayn.ignite.lifecycbean.MyLifecycleBean;
import com.rayn.ignite.listener.MyIgniteInClosure;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteFuture;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rayn
 * @email liuwei412552703@163.com
 * Created by Rayn on 2016/11/3 17:50.
 */
public class HelloIgnite {

    private Ignite ignite = null;

    @Before
    public void setUp() throws Exception {

        // Create new configuration.
        IgniteConfiguration cfg = new IgniteConfiguration();

        // Provide lifecycle bean to configuration.
        cfg.setLifecycleBeans(new MyLifecycleBean());


        ignite = Ignition.start();

    }

    /**
     * 同步调用
     *
     * @throws Exception
     */
    @Test
    public void testSync() throws Exception {
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
     * 异步调用
     *
     * @throws Exception
     */
    @Test
    public void testAsync() throws Exception {
        IgniteCompute compute = ignite.compute();

        // Execute a job and wait for the result.
        String res = compute.call(new IgniteCallable<String>() {
            @Override
            public String call() throws Exception {
                // Print hello world on some cluster node.
                System.out.println("Hello World");
                return "Hello World";
            }
        });

    }
}
