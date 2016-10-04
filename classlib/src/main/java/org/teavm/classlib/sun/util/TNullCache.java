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


class TNullCache<K, V> extends TCache<K, V> {

    static final TCache<Object, Object> INSTANCE = new TNullCache();

    private TNullCache() {
    }

    public int size() {
        return 0;
    }

    public void clear() {
    }

    public void put(K var1, V var2) {
    }

    public V get(Object var1) {
        return null;
    }

    public void remove(Object var1) {
    }

    public void setCapacity(int var1) {
    }

    public void setTimeout(int var1) {
    }

    public void accept(TCache.CacheVisitor<K, V> var1) {
    }
}
