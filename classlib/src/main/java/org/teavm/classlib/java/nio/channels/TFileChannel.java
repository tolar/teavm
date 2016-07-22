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
package org.teavm.classlib.java.nio.channels;

import java.io.IOException;

public abstract class TFileChannel {

    public abstract long size() throws IOException;

    public final void close() throws IOException {
    }

    public final TFileLock lock() throws IOException {
        return lock(0L, Long.MAX_VALUE, false);
    }

    public abstract TFileLock lock(long position, long size, boolean shared)
            throws IOException;
}
