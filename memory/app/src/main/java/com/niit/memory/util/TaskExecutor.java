package com.niit.memory.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskExecutor {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TaskExecutor() {}

    public static void execute(Runnable task) {
        executor.execute(task);
    }

    public static void runOnUiThread(Runnable task) {
        mainHandler.post(task);
    }
}
