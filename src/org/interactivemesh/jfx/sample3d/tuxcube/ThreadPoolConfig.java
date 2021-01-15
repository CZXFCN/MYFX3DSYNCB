package org.interactivemesh.jfx.sample3d.tuxcube;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author 【Device】（【张辉】 【Device.zhang】@tuya.com）
 * @since 2021/1/3 10:20 AM
 */
public class ThreadPoolConfig {
    private volatile static ThreadPoolExecutor threadPoolExecutor = null;

    public static ThreadPoolExecutor getThreadPool() {
        if (threadPoolExecutor == null) {
            synchronized (ThreadPoolConfig.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(50, 50, 10000,
                            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
                }
            }
        }
        return threadPoolExecutor;
    }
}
