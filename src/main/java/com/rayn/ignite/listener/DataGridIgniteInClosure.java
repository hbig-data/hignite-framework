package com.rayn.ignite.listener;

import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.lang.IgniteInClosure;

/**
 * @author Rayn
 * @email liuwei412552703@163.com
 * Created by Rayn on 2016/11/4 9:39.
 */
public class DataGridIgniteInClosure implements IgniteInClosure<IgniteFuture<Integer>> {
    /**
     * Closure body.
     *
     * @param future Closure argument.
     */
    @Override
    public void apply(IgniteFuture<Integer> future) {
        System.out.println("信息：" + future.get());
    }
}
