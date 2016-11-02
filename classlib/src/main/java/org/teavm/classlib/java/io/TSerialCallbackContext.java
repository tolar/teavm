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
package org.teavm.classlib.java.io;

import java.io.NotActiveException;

import org.teavm.classlib.java.lang.TObject;
import org.teavm.classlib.java.lang.TString;

final class TSerialCallbackContext {
    private final TObject obj;
    private final TObjectStreamClass desc;
    /**
     * Thread this context is in use by.
     * As this only works in one thread, we do not need to worry about thread-safety.
     */
    private Thread thread;

    public TSerialCallbackContext(TObject obj, TObjectStreamClass desc) {
        this.obj = obj;
        this.desc = desc;
        this.thread = Thread.currentThread();
    }

    public TObject getObj() throws TNotActiveException {
        checkAndSetUsed();
        return obj;
    }

    public TObjectStreamClass getDesc() {
        return desc;
    }

    public void check() throws NotActiveException {
        if (thread != null && thread != Thread.currentThread()) {
            throw new NotActiveException(
                    "expected thread: " + thread + ", but got: " + Thread.currentThread());
        }
    }

    private void checkAndSetUsed() throws TNotActiveException {
        if (thread != Thread.currentThread()) {
            throw new TNotActiveException(TString.wrap(
                    "not in readObject invocation or fields already read"));
        }
        thread = null;
    }

    public void setUsed() {
        thread = null;
    }
}
