package com.rayn.ignite.lifecycbean;

import org.apache.ignite.IgniteException;
import org.apache.ignite.lifecycle.LifecycleBean;
import org.apache.ignite.lifecycle.LifecycleEventType;

/**
 * @author Rayn
 * @email liuwei412552703@163.com
 * Created by Rayn on 2016/11/3 18:07.
 */
public class MyLifecycleBean implements LifecycleBean {
    /**
     * This method is called when lifecycle event occurs.
     *
     * @param evt Lifecycle event.
     * @throws IgniteException Thrown in case of any errors.
     */
    @Override
    public void onLifecycleEvent(LifecycleEventType evt) throws IgniteException {
        if (evt == LifecycleEventType.BEFORE_NODE_START) {
            // Do something.

        } else if (evt == LifecycleEventType.AFTER_NODE_START) {
            // Do something.

        } else if (evt == LifecycleEventType.BEFORE_NODE_STOP) {
            // Do something.

        } else if (evt == LifecycleEventType.AFTER_NODE_STOP) {
            // Do something.

        }

    }
}
