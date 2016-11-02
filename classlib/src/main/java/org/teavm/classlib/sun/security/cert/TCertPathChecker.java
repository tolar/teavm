/*
 *  Copyright 2016 vasek.
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
package org.teavm.classlib.sun.security.cert;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;

/**
 * Created by vasek on 29. 10. 2016.
 */
public interface TCertPathChecker {

    /**
     * Initializes the internal state of this {@code TCertPathChecker}.
     *
     * <p>The {@code forward} flag specifies the order that certificates will
     * be passed to the {@link #check check} method (forward or reverse).
     *
     * @param forward the order that certificates are presented to the
     *        {@code check} method. If {@code true}, certificates are
     *        presented from target to trust anchor (forward); if
     *        {@code false}, from trust anchor to target (reverse).
     * @throws CertPathValidatorException if this {@code TCertPathChecker} is
     *         unable to check certificates in the specified order
     */
    void init(boolean forward) throws CertPathValidatorException;

    /**
     * Indicates if forward checking is supported. Forward checking refers
     * to the ability of the {@code TCertPathChecker} to perform its checks
     * when certificates are presented to the {@code check} method in the
     * forward direction (from target to trust anchor).
     *
     * @return {@code true} if forward checking is supported, {@code false}
     *         otherwise
     */
    boolean isForwardCheckingSupported();

    /**
     * Performs the check(s) on the specified certificate using its internal
     * state. The certificates are presented in the order specified by the
     * {@code init} method.
     *
     * @param cert the {@code Certificate} to be checked
     * @throws CertPathValidatorException if the specified certificate does
     *         not pass the check
     */
    void check(Certificate cert) throws CertPathValidatorException;
}
