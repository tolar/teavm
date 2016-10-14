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

import java.util.Arrays;
import java.util.Map;

public abstract class TCache<K, V> {
    protected TCache() {
    }

    public abstract int size();

    public abstract void clear();

    public abstract void put(K var1, V var2);

    public abstract V get(Object var1);

    public abstract void remove(Object var1);

    public abstract void setCapacity(int var1);

    public abstract void setTimeout(int var1);

    public abstract void accept(TCache.CacheVisitor<K, V> var1);

    public static <K, V> TCache<K, V> newSoftMemoryCache(int var0) {
        return new TMemoryCache(true, var0);
    }

    public static <K, V> TCache<K, V> newSoftMemoryCache(int var0, int var1) {
        return new TMemoryCache(true, var0, var1);
    }

    public static <K, V> TCache<K, V> newHardMemoryCache(int var0) {
        return new TMemoryCache(false, var0);
    }

    public static <K, V> TCache<K, V> newNullCache() {
        return (TCache<K, V>) TNullCache.INSTANCE;
    }

    public static <K, V> TCache<K, V> newHardMemoryCache(int var0, int var1) {
        return new TMemoryCache(false, var0, var1);
    }

    public interface CacheVisitor<K, V> {
        void visit(Map<K, V> var1);
    }

    public static class EqualByteArray {
        private final byte[] b;
        private volatile int hash;

        public EqualByteArray(byte[] var1) {
            this.b = var1;
        }

        public int hashCode() {
            int var1 = this.hash;
            if(var1 == 0) {
                var1 = this.b.length + 1;

                for(int var2 = 0; var2 < this.b.length; ++var2) {
                    var1 += (this.b[var2] & 255) * 37;
                }

                this.hash = var1;
            }

            return var1;
        }

        public boolean equals(Object var1) {
            if(this == var1) {
                return true;
            } else if(!(var1 instanceof TCache.EqualByteArray)) {
                return false;
            } else {
                TCache.EqualByteArray var2 = (TCache.EqualByteArray)var1;
                return Arrays.equals(this.b, var2.b);
            }
        }
    }
}
