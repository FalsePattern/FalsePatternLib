package com.falsepattern.lib.util;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.Tags;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asynchronous utilities. Notice: you should not abuse this for anything performance-sensitive.
 * The asynchronous worker exists primarily to run code independently of the main thread during loading.
 * You must make sure to solve any potential deadlocks yourself!
 */
@StableAPI(since = "0.8.0")
public class AsyncUtil {
    public static final ExecutorService asyncWorker = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName(Tags.MODNAME + " Async Worker");
        return thread;
    });
}
