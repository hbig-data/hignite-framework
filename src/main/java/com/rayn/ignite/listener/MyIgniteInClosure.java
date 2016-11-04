package com.rayn.ignite.listener;

import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.lang.IgniteInClosure;

/**
 * @author Rayn
 * @email liuwei412552703@163.com
 * Created by Rayn on 2016/11/3 18:16.
 */
public class MyIgniteInClosure implements IgniteInClosure<IgniteFuture<String>> {
    /**
     * Closure body.
     *
     * @param f Closure argument.
     */
    @Override
    public void apply(IgniteFuture<String> f) {
        System.out.println("Job result: " + f.get());
    }
}
