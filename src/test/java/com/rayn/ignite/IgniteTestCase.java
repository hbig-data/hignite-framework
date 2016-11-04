package com.rayn.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.transactions.Transaction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.Lock;

/**
 * @author Rayn
 * @email liuwei412552703@163.com
 * Created by Rayn on 2016/11/3 15:33.
 */
public class IgniteTestCase extends IgniteBase {


    /**
     * @throws Exception
     */
    @Test
    public void testIgnite() throws Exception {
        try (Ignite ignite = Ignition.start("example-ignite.xml")) {

            Collection<IgniteCallable<Integer>> calls = new ArrayList<>();

            // Iterate through all the words in the sentence and create Callable jobs.
            for (final String word : "Count characters using callable".split(" ")) {
                calls.add(new IgniteCallable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return word.length();
                    }
                });
            }
            // Execute collection of Callables on the grid.
            Collection<Integer> res = ignite.compute().call(calls);

            int sum = 0;

            // Add up individual word lengths received from remote nodes.
            for (int len : res) {
                sum += len;
            }
            System.out.println(">>> Total number of characters in the phrase is '" + sum + "'.");
        }

    }

    /**
     * 原子性测试
     *
     * @throws Exception
     */
    @Test
    public void testAtomic() throws Exception {
        // Put-if-absent which returns previous value.
        Integer oldVal = cache.getAndPutIfAbsent("Hello", 11);

        // Put-if-absent which returns boolean success flag.
        boolean success = cache.putIfAbsent("World", 22);

        // Replace-if-exists operation (opposite of getAndPutIfAbsent), returns previous value.
        oldVal = cache.getAndReplace("Hello", 11);

        // Replace-if-exists operation (opposite of putIfAbsent), returns boolean success flag.
        success = cache.replace("World", 22);

        // Replace-if-matches operation.
        success = cache.replace("World", 2, 22);

        // Remove-if-matches operation.
        success = cache.remove("Hello", 1);

    }

    /**
     * 事务
     *
     * @throws Exception
     */
    @Test
    public void testTransaction() throws Exception {
        try (Transaction tx = ignite.transactions().txStart()) {
            Integer hello = cache.get("Hello");
            if (hello == 1) {
                cache.put("Hello", 11);
            }
            cache.put("World", 22);

            tx.commit();
        }

    }

    /**
     * 分布式锁
     *
     * @throws Exception
     */
    @Test
    public void testDiscLock() throws Exception {
        // Lock cache key "Hello".
        Lock lock = cache.lock("Hello");
        lock.lock();
        try {
            cache.put("Hello", 11);
            cache.put("World", 22);
        } finally {
            lock.unlock();
        }

    }
}
