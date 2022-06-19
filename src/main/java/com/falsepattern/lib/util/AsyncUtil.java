package com.falsepattern.lib.util;

import com.falsepattern.lib.internal.Tags;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asynchronous utilities. Notice: you should not abuse this for anything performance-sensitive.
 * The asynchronous worker exists primarily to run code independently of the main thread during loading.
 * You must make sure to solve any potential deadlocks yourself!
 *
 * //Deprecated since 0.9.0, removal in 0.10+: Use java-provided async tools instead. CompletableFuture is a nice one.
 */
@Deprecated
public class AsyncUtil {
    public static final ExecutorService asyncWorker = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName(Tags.MODNAME + " Async Worker");
        return thread;
    });
}
