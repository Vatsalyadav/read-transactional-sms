package com.vatsalyadav.apps.readsms;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {

    // For Singleton instantiation
    private static AppExecutors sInstance;
    private final Executor networkIO;

    private AppExecutors(Executor networkIO) {
        this.networkIO = networkIO;
    }

    public static AppExecutors getInstance() {
        if (sInstance == null) {
            sInstance = new AppExecutors(Executors.newFixedThreadPool(3));
        }
        return sInstance;
    }

    public Executor networkIO() {
        return networkIO;
    }

}
