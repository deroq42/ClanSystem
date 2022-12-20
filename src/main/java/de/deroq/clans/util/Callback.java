package de.deroq.clans.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.function.Consumer;

/**
 * @author Miles
 * @since 10.12.2022
 */
public abstract class Callback {

    public static <V> void of(ListenableFuture<V> future, Consumer<V> consumer) {
        Futures.addCallback(future, new FutureCallback<V>() {
            @Override
            public void onSuccess(V v) {
                consumer.accept(v);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        }, Runnable::run);
    }
}
