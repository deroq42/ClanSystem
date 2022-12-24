package de.deroq.clans.api.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Miles
 * @since 23.12.2022
 */
public abstract class Executors {

    private static final ListeningExecutorService asyncExecutor = MoreExecutors.listeningDecorator(new ScheduledThreadPoolExecutor(1));

    public static ListeningExecutorService asyncExecutor() {
        return asyncExecutor;
    }
}
