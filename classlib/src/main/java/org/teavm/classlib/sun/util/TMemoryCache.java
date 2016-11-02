/*
 *  Copyright 2016 vtolar.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.sun.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.teavm.classlib.java.lang.TObject;
import org.teavm.classlib.java.util.TLinkedHashMap;
import org.teavm.classlib.java.util.TMap;

class TMemoryCache<K, V> extends TCache<K, V> {
    private static final float LOAD_FACTOR = 0.75F;
    private static final boolean DEBUG = false;
    private final TMap<K, CacheEntry<K, V>> cacheMap;
    private int maxSize;
    private long lifetime;
    private final ReferenceQueue<V> queue;

    public TMemoryCache(boolean var1, int var2) {
        this(var1, var2, 0);
    }

    public TMemoryCache(boolean var1, int var2, int var3) {
        this.maxSize = var2;
        this.lifetime = (long)(var3 * 1000);
        if(var1) {
            this.queue = new ReferenceQueue();
        } else {
            this.queue = null;
        }

        int var4 = (int)((float)var2 / 0.75F) + 1;
        this.cacheMap = new TLinkedHashMap(var4, 0.75F, true);
    }

    private void emptyQueue() {
        if(this.queue != null) {
            int var1 = this.cacheMap.size();

            while(true) {
                TMemoryCache.CacheEntry var2 = (TMemoryCache.CacheEntry)this.queue.poll();
                if(var2 == null) {
                    return;
                }

                TObject var3 = var2.getKey();
                if(var3 != null) {
                    TMemoryCache.CacheEntry var4 = (TMemoryCache.CacheEntry)this.cacheMap.remove(var3);
                    if(var4 != null && var2 != var4) {
                        this.cacheMap.put(var3, var4);
                    }
                }
            }
        }
    }

    private void expungeExpiredEntries() {
        this.emptyQueue();
        if(this.lifetime != 0L) {
            int var1 = 0;
            long var2 = System.currentTimeMillis();
            Iterator var4 = this.cacheMap.values().iterator();

            while(var4.hasNext()) {
                TMemoryCache.CacheEntry var5 = (TMemoryCache.CacheEntry)var4.next();
                if(!var5.isValid(var2)) {
                    var4.remove();
                    ++var1;
                }
            }

        }
    }

    public synchronized int size() {
        this.expungeExpiredEntries();
        return this.cacheMap.size();
    }

    public synchronized void clear() {
        if(this.queue != null) {
            Iterator var1 = this.cacheMap.values().iterator();

            while(var1.hasNext()) {
                TMemoryCache.CacheEntry var2 = (TMemoryCache.CacheEntry)var1.next();
                var2.invalidate();
            }

            while(true) {
                if(this.queue.poll() != null) {
                    continue;
                }
            }
        }

        this.cacheMap.clear();
    }

    public synchronized void put(K var1, V var2) {
        this.emptyQueue();
        long var3 = this.lifetime == 0L?0L:System.currentTimeMillis() + this.lifetime;
        TMemoryCache.CacheEntry var5 = this.newEntry(var1, var2, var3, this.queue);
        TMemoryCache.CacheEntry var6 = (TMemoryCache.CacheEntry)this.cacheMap.put(var1, var5);
        if(var6 != null) {
            var6.invalidate();
        } else {
            if(this.maxSize > 0 && this.cacheMap.size() > this.maxSize) {
                this.expungeExpiredEntries();
                if(this.cacheMap.size() > this.maxSize) {
                    Iterator var7 = this.cacheMap.values().iterator();
                    TMemoryCache.CacheEntry var8 = (TMemoryCache.CacheEntry)var7.next();
                    var7.remove();
                    var8.invalidate();
                }
            }

        }
    }

    public synchronized V get(Object var1) {
        this.emptyQueue();
        TMemoryCache.CacheEntry var2 = (TMemoryCache.CacheEntry)this.cacheMap.get(var1);
        if(var2 == null) {
            return null;
        } else {
            long var3 = this.lifetime == 0L?0L:System.currentTimeMillis();
            if(!var2.isValid(var3)) {
                this.cacheMap.remove(var1);
                return null;
            } else {
                return  var2.getValue();
            }
        }
    }

    public synchronized void remove(Object var1) {
        this.emptyQueue();
        TMemoryCache.CacheEntry var2 = (TMemoryCache.CacheEntry)this.cacheMap.remove(var1);
        if(var2 != null) {
            var2.invalidate();
        }

    }

    public synchronized void setCapacity(int var1) {
        this.expungeExpiredEntries();
        if(var1 > 0 && this.cacheMap.size() > var1) {
            Iterator var2 = this.cacheMap.values().iterator();

            for(int var3 = this.cacheMap.size() - var1; var3 > 0; --var3) {
                TMemoryCache.CacheEntry var4 = (TMemoryCache.CacheEntry)var2.next();
                var2.remove();
                var4.invalidate();
            }
        }

        this.maxSize = var1 > 0?var1:0;
    }

    public synchronized void setTimeout(int var1) {
        this.emptyQueue();
        this.lifetime = var1 > 0?(long)var1 * 1000L:0L;
    }

    public synchronized void accept(CacheVisitor<K, V> var1) {
        this.expungeExpiredEntries();
        Map var2 = this.getCachedEntries();
        var1.visit(var2);
    }

    private Map<K, V> getCachedEntries() {
        HashMap var1 = new HashMap(this.cacheMap.size());
        Iterator var2 = this.cacheMap.values().iterator();

        while(var2.hasNext()) {
            TMemoryCache.CacheEntry var3 = (TMemoryCache.CacheEntry)var2.next();
            var1.put(var3.getKey(), var3.getValue());
        }

        return var1;
    }

    protected TMemoryCache.CacheEntry<K, V> newEntry(K var1, V var2, long var3, ReferenceQueue<V> var5) {
        return (TMemoryCache.CacheEntry)(var5 != null?new TMemoryCache.SoftCacheEntry(var1, var2, var3, var5):new TMemoryCache.HardCacheEntry(var1, var2, var3));
    }

    private interface CacheEntry<K, V> {
        boolean isValid(long var1);

        void invalidate();

        K getKey();

        V getValue();
    }

    private static class HardCacheEntry<K, V> implements TMemoryCache.CacheEntry<K, V> {
        private K key;
        private V value;
        private long expirationTime;

        HardCacheEntry(K var1, V var2, long var3) {
            this.key = var1;
            this.value = var2;
            this.expirationTime = var3;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public boolean isValid(long var1) {
            boolean var3 = var1 <= this.expirationTime;
            if(!var3) {
                this.invalidate();
            }

            return var3;
        }

        public void invalidate() {
            this.key = null;
            this.value = null;
            this.expirationTime = -1L;
        }
    }

    private static class SoftCacheEntry<K, V> extends SoftReference<V> implements TMemoryCache.CacheEntry<K, V> {
        private K key;
        private long expirationTime;

        SoftCacheEntry(K var1, V var2, long var3, ReferenceQueue<V> var5) {
            super(var2, var5);
            this.key = var1;
            this.expirationTime = var3;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.get();
        }

        public boolean isValid(long var1) {
            boolean var3 = var1 <= this.expirationTime && this.get() != null;
            if(!var3) {
                this.invalidate();
            }

            return var3;
        }

        public void invalidate() {
            this.clear();
            this.key = null;
            this.expirationTime = -1L;
        }
    }
}
