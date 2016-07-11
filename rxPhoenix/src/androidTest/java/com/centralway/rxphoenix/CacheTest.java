package com.centralway.rxphoenix;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import rx.Observable;

@RunWith(AndroidJUnit4.class)
public class CacheTest {
    private static final UUID TEST_UUID = UUID.randomUUID();

    @Test
    public void testGetInstance() throws Exception {
        // Given two getInstance calls
        Cache first = Cache.getInstance();
        Cache second = Cache.getInstance();

        // The Object returned is the same
        Assert.assertTrue(first == second);
    }

    @Test
    public void testStoreObservable() throws Exception {
        // Given an Observable is stored in the cache
        Observable observable = Observable.just("test");
        Cache.getInstance().storeObservable(TEST_UUID, 0, observable);

        // The cache will store it.
        Assert.assertTrue(Cache.getInstance().getObservables(TEST_UUID).containsKey(0));
        Assert.assertTrue(Cache.getInstance().getObservables(TEST_UUID).get(0) == observable);
    }

    @Test
    public void testStoreObservableHasOnlyOne() throws Exception {
        // Given an Observable is stored in the cache
        Observable observable = Observable.just("test");
        Cache.getInstance().storeObservable(TEST_UUID, 1, observable);

        // The cache will contain only that.
        Assert.assertTrue(Cache.getInstance().getObservables(TEST_UUID).size() == 1);
    }

    @Test
    public void testDropObservable() throws Exception {
        // Given an Observable is stored in the cache
        Observable observable = Observable.just("test");
        Cache.getInstance().storeObservable(TEST_UUID, 2, observable);

        // When we drop it
        Cache.getInstance().dropObservable(TEST_UUID, 2);

        // The cache will be empty
        Assert.assertTrue(Cache.getInstance().getObservables(TEST_UUID).size() == 0);
    }

    @Test
    public void testDropObservables() throws Exception {
        // Given an Observable is stored in the cache
        Observable observable = Observable.just("test");
        Cache.getInstance().storeObservable(TEST_UUID, 3, observable);

        // When we drop all
        Cache.getInstance().dropObservables(TEST_UUID);

        // The cache will be empty
        Assert.assertTrue(Cache.getInstance().getObservables(TEST_UUID).size() == 0);
    }

    @Test
    public void testDropMultipleObservables() throws Exception {
        // Given two Observables are stored in the cache
        Observable observable = Observable.just("test");
        Cache.getInstance().storeObservable(TEST_UUID, 4, observable);
        Cache.getInstance().storeObservable(TEST_UUID, 5, observable);

        // When we drop all
        Cache.getInstance().dropObservables(TEST_UUID);

        // The cache will be empty
        Assert.assertTrue(Cache.getInstance().getObservables(TEST_UUID).size() == 0);
    }

    @After
    public void tearDown() throws Exception {
        Cache.getInstance().dropObservables(TEST_UUID);
    }
}