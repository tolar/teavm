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
package org.teavm.classlib.sun.security.jca;

import org.teavm.classlib.java.security.TSecureRandom;

public final class TJCAUtil {
    private static final int ARRAY_SIZE = 4096;

    private TJCAUtil() {
    }

    public static int getTempArraySize(int var0) {
        return Math.min(4096, var0);
    }

    public static TSecureRandom getSecureRandom() {
        return TJCAUtil.CachedSecureRandomHolder.instance;
    }

    private static class CachedSecureRandomHolder {
        public static TSecureRandom instance = new TSecureRandom();

        private CachedSecureRandomHolder() {
        }
    }
}
