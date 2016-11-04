package com.rayn.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rayn
 * @email liuwei412552703@163.com
 * Created by Rayn on 2016/11/3 15:33.
 */
public class IgniteBase {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteBase.class);


    protected Ignite ignite = null;
    protected IgniteCache<String, Integer> cache = null;



    @Before
    public void setUp() throws Exception {

        ignite = Ignition.start("examples/config/example-ignite.xml");
        cache = ignite.getOrCreateCache("myCacheName");

    }

    @After
    public void tearDown() throws Exception {
        if(null != cache) {
            cache.close();
        }
        if(null != ignite){
            ignite.close();
        }
    }
}
