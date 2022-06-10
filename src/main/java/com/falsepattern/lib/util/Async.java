package com.falsepattern.lib.util;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.Tags;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@StableAPI(since = "0.8.0")
public class Async {
    public static final ExecutorService asyncWorker = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName(Tags.MODNAME + " Async Worker");
        return thread;
    });
}
