package me.steffenjacobs.greetingmod.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private int cacheSize;

    public LruCache(int cacheSize) {
        super(16, 0.75f, true);
        this.cacheSize = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= cacheSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LruCache<?, ?> lruCache = (LruCache<?, ?>) o;
        return cacheSize == lruCache.cacheSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cacheSize);
    }
}
