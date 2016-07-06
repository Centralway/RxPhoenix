package com.centralway.blade;

import android.support.annotation.NonNull;
import android.util.LruCache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import rx.Observable;

/**
 * Internal package-protected Singleton class that hosts an {@link LruCache} to keep Observables during configuration
 * changes.
 * all methods are synchronised and package protected.
 */
final class Cache {

    /**
     * Maximum number of events to be kept.
     */
    public static final int MAX_CACHE_SIZE = 16;

    private static final Cache INSTANCE = new Cache();
    private final LruCache<CacheKey, Observable> mCache = new LruCache<>(MAX_CACHE_SIZE);

    private Cache() {
    }

    /**
     * Gets the singleton instance of this cache.
     */
    static Cache getInstance() {
        return INSTANCE;
    }

    /**
     * Stores an observable in the cache, using as key a combination of the host UUID and the ID of the request.
     *
     * @param host       The host's UUID
     * @param id         The ID of the request
     * @param observable The observable
     */
    synchronized <T> void storeObservable(UUID host, int id, Observable<T> observable) {
        mCache.put(new CacheKey(host, id), observable);
    }

    /**
     * Returns all observables registered for an host (Identified by its UUID)
     *
     * @param host UUID of the host
     * @return List of Observables
     */
    synchronized Map<Integer, Observable> getObservables(UUID host) {
        Map<CacheKey, Observable> map = mCache.snapshot();
        Map<Integer, Observable> result = new HashMap<>(map.size());

        for (Map.Entry<CacheKey, Observable> entry : map.entrySet()) {
            if (entry.getKey().uuid == host) {
                result.put(entry.getKey().id, entry.getValue());
            }
        }

        return result;
    }

    /**
     * Removes a specific Observable from the cache.
     */
    synchronized void dropObservable(UUID host, int id) {
        mCache.remove(new CacheKey(host, id));
    }

    /**
     * Removes all Observables registered to a host from this cache.
     */
    synchronized void dropObservables(UUID host) {
        Map<CacheKey, Observable> map = mCache.snapshot();
        for (CacheKey cacheKey : map.keySet()) {
            if (cacheKey.uuid == host) {
                mCache.remove(cacheKey);
            }
        }
    }

    /**
     * Simple key that combines the UUID of the Host to the ID of the request.
     */
    private static final class CacheKey {

        private final UUID uuid;
        private final int id;

        private CacheKey(@NonNull UUID uuid, int id) {
            this.uuid = uuid;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;

            return id == cacheKey.id && uuid.equals(cacheKey.uuid);

        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + id;
            return result;
        }
    }

}
