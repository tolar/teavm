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
package org.teavm.classlib.java.security;

import org.teavm.classlib.java.lang.TRuntimeException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.lang.TThrowable;

public class TProviderException extends TRuntimeException {

    private static final long serialVersionUID = 5256023526693665674L;

    /**
     * Constructs a TProviderException with no detail message. A
     * detail message is a String that describes this particular
     * exception.
     */
    public TProviderException() {
        super();
    }

    /**
     * Constructs a TProviderException with the specified detail
     * message. A detail message is a String that describes this
     * particular exception.
     *
     * @param s the detail message.
     */
    public TProviderException(TString s) {
        super(s);
    }

    /**
     * Creates a {@code TProviderException} with the specified
     * detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A {@code null} value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     * @since 1.5
     */
    public TProviderException(TString message, TThrowable cause) {
        super(message, cause);
    }

    /**
     * Creates a {@code TProviderException} with the specified cause
     * and a detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of
     * {@code cause}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A {@code null} value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     * @since 1.5
     */
    public TProviderException(TThrowable cause) {
        super(cause);
    }
}