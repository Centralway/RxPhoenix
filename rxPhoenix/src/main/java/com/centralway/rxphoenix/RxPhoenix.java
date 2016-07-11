package com.centralway.rxphoenix;

import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Stateless controller that will act as a delegate of any Host View, Activity or Fragment, that wants to register to
 * a cached Observable.
 */
public final class RxPhoenix {

    private static final String METHOD_NOT_DECLARED_EXCEPTION = "Provide public method annotated with " +
            RxPhoenixSubscription.class.getSimpleName() + " that returns subscription for request id: %d";
    private static final String METHOD_ACCESS_EXCEPTION = "Method access modifier must be public for request id: %d";
    private static final String METHOD_INVOCATION_EXCEPTION = "Exception while invoking method for request id: %d";

    private static final String KEY_UUID = "KEY_RXPHOENIX_UUID";

    /**
     * Host to which events should be delivered
     */
    private final Object mHost;

    /**
     * UUID identifying this instance upon configuration changes.
     */
    private UUID mUUID = UUID.randomUUID();

    /**
     * Holder for all subscriptions of this host.
     */
    private CompositeSubscription mSubscription = new CompositeSubscription();

    private final Cache mCache;
    private final Set<Integer> mDispatchedObservablesHistory;
    private final Map<Integer, Method> mMethodHashMap;

    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public RxPhoenix(Observable<LifecycleEvent> lifecycleObservable, Object host) {
        mHost = host;
        mCache = Cache.getInstance();
        mDispatchedObservablesHistory = new LinkedHashSet<>();
        mMethodHashMap = new HashMap<>();

        extractSubscriptionMethods(host);

        lifecycleObservable.subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                if (lifecycleEvent instanceof LifecycleEvent.InitEvent) {
                    onInit(((LifecycleEvent.InitEvent) lifecycleEvent).state);
                } else if (lifecycleEvent instanceof LifecycleEvent.SaveStateEvent) {
                    onSaveState(((LifecycleEvent.SaveStateEvent) lifecycleEvent).state);
                } else if (lifecycleEvent instanceof LifecycleEvent.ResumeEvent) {
                    onResume();
                } else if (lifecycleEvent instanceof LifecycleEvent.PauseEvent) {
                    onPause();
                } else if (lifecycleEvent instanceof LifecycleEvent.DieEvent) {
                    onDying();
                }
            }
        });
    }

    private void extractSubscriptionMethods(Object host) {
        Method[] methods = host.getClass().getMethods();
        for (Method method : methods) {
            RxPhoenixSubscription result = method.getAnnotation(RxPhoenixSubscription.class);
            if (result != null) {
                int requestID = result.value();
                mMethodHashMap.put(requestID, method);
            }
        }
    }

    @NonNull
    @CheckResult
    public <T> Observable.Transformer<T, T> surviveConfigChanges(final int requestId) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> inputObservable) {
                if (!mMethodHashMap.containsKey(requestId)) {
                    throw new IllegalStateException(String.format(METHOD_NOT_DECLARED_EXCEPTION, requestId));

                }
                // We generate an observable combining the ID of the request and the event of input observable, and we
                // request a cache of it (To keep results during lifecycle changes)
                Observable<T> cachedObservable = inputObservable.cache();
                // We store it in the cache.
                mCache.storeObservable(mUUID, requestId, cachedObservable);
                // We subscribe to it.
                dispatchObservableToHost(requestId, cachedObservable);
                return cachedObservable;
            }
        };
    }

    /**
     * This method is called when the host is being initialised. This is either {@link
     * android.app.Activity#onCreate(Bundle)} or {@link android.support.v4.app.Fragment#onCreate(Bundle)}.
     */
    private void onInit(@Nullable Bundle savedInstanceState) {
        // If a state exists it means that we are restoring an instance and we can get back the UUID, else we create
        // a new UUID.
        boolean isStateRestored = savedInstanceState != null;
        mUUID = isStateRestored ? (UUID) savedInstanceState.getSerializable(KEY_UUID) : mUUID;
    }

    /**
     * Called when the host is being saved for serialisation. Takes the state from the host and puts what we need to
     * store.
     */
    private void onSaveState(Bundle outState) {
        // We need to store the UUID of this instance as this identifies the "host" Activity upon configuration changes.
        outState.putSerializable(KEY_UUID, mUUID);
    }

    /**
     * Called when the host is being resumed after going invisible, and anyway after any initialisation of the host.
     */
    private void onResume() {
        // We must create a new object as when you un-subscribe a CompositeSubscription it will automatically
        // un-subscribe all new subscriptions added.
        mSubscription = mSubscription.isUnsubscribed() ? new CompositeSubscription() : mSubscription;
        // We get all the observables from the Cache and subscribe to all of them.
        Map<Integer, Observable> observables = mCache.getObservables(mUUID);
        for (Map.Entry<Integer, Observable> entry : observables.entrySet()) {
            if (!mDispatchedObservablesHistory.contains(entry.getKey())) {
                dispatchObservableToHost(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Called when the host is going to be invisible for the user.
     */
    private void onPause() {
        // On pause the view is assumed no more visible, so we don't want to update it. We un-subscribe all
        // subscriptions.
        mSubscription.unsubscribe();
        mDispatchedObservablesHistory.clear();
    }

    /**
     * Called when the host is going to be destroyed completely and not to be recreated.
     */
    private void onDying() {
        // If we are not changing configuration on destroy, it means the host will not be created no more,
        // and we need to drop any observables stored in the cache.
        mCache.dropObservables(mUUID);
    }

    /**
     * Internal method to subscribe to an {@link Observable}.
     */
    private <T> void dispatchObservableToHost(int requestId, Observable<T> cachedObservable) {
        //noinspection TryWithIdenticalCatches
        try {
            Subscription subscription = (Subscription) mMethodHashMap.get(requestId).invoke(mHost, cachedObservable);
            mSubscription.add(subscription);
            mDispatchedObservablesHistory.add(requestId);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(String.format(METHOD_ACCESS_EXCEPTION, requestId));
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(String.format(METHOD_INVOCATION_EXCEPTION, requestId));
        }
    }

}
