package com.epicgames.ue4;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    private static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private ThreadManager() {
        throw new UnsupportedOperationException();
    }

    public static void execute(final Runnable runnable) {
        cachedThreadPool.execute(runnable);
    }
}
