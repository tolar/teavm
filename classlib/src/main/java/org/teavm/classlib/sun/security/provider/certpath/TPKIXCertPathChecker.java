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
package org.teavm.classlib.sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Set;
import org.teavm.classlib.sun.security.cert.TCertPathChecker;

/**
 * Created by vasek on 29. 10. 2016.
 */
public abstract class TPKIXCertPathChecker
        implements TCertPathChecker, Cloneable {

    /**
     * Default constructor.
     */
    protected TPKIXCertPathChecker() {}

    /**
     * Initializes the internal state of this {@code TPKIXCertPathChecker}.
     * <p>
     * The {@code forward} flag specifies the order that
     * certificates will be passed to the {@link #check check} method
     * (forward or reverse). A {@code TPKIXCertPathChecker} <b>must</b>
     * support reverse checking and <b>may</b> support forward checking.
     *
     * @param forward the order that certificates are presented to
     * the {@code check} method. If {@code true}, certificates
     * are presented from target to most-trusted CA (forward); if
     * {@code false}, from most-trusted CA to target (reverse).
     * @throws CertPathValidatorException if this
     * {@code TPKIXCertPathChecker} is unable to check certificates in
     * the specified order; it should never be thrown if the forward flag
     * is false since reverse checking must be supported
     */
    @Override
    public abstract void init(boolean forward)
            throws CertPathValidatorException;

    /**
     * Indicates if forward checking is supported. Forward checking refers
     * to the ability of the {@code TPKIXCertPathChecker} to perform
     * its checks when certificates are presented to the {@code check}
     * method in the forward direction (from target to most-trusted CA).
     *
     * @return {@code true} if forward checking is supported,
     * {@code false} otherwise
     */
    @Override
    public abstract boolean isForwardCheckingSupported();

    /**
     * Returns an immutable {@code Set} of X.509 certificate extensions
     * that this {@code TPKIXCertPathChecker} supports (i.e. recognizes, is
     * able to process), or {@code null} if no extensions are supported.
     * <p>
     * Each element of the set is a {@code String} representing the
     * Object Identifier (OID) of the X.509 extension that is supported.
     * The OID is represented by a set of nonnegative integers separated by
     * periods.
     * <p>
     * All X.509 certificate extensions that a {@code TPKIXCertPathChecker}
     * might possibly be able to process should be included in the set.
     *
     * @return an immutable {@code Set} of X.509 extension OIDs (in
     * {@code String} format) supported by this
     * {@code TPKIXCertPathChecker}, or {@code null} if no
     * extensions are supported
     */
    public abstract Set<String> getSupportedExtensions();

    /**
     * Performs the check(s) on the specified certificate using its internal
     * state and removes any critical extensions that it processes from the
     * specified collection of OID strings that represent the unresolved
     * critical extensions. The certificates are presented in the order
     * specified by the {@code init} method.
     *
     * @param cert the {@code Certificate} to be checked
     * @param unresolvedCritExts a {@code Collection} of OID strings
     * representing the current set of unresolved critical extensions
     * @exception CertPathValidatorException if the specified certificate does
     * not pass the check
     */
    public abstract void check(Certificate cert,
            Collection<String> unresolvedCritExts)
            throws CertPathValidatorException;

    /**
     * {@inheritDoc}
     *
     * <p>This implementation calls
     * {@code check(cert, java.util.Collections.<String>emptySet())}.
     */
    @Override
    public void check(Certificate cert) throws CertPathValidatorException {
        check(cert, java.util.Collections.<String>emptySet());
    }

    /**
     * Returns a clone of this object. Calls the {@code Object.clone()}
     * method.
     * All subclasses which maintain state must support and
     * override this method, if necessary.
     *
     * @return a copy of this {@code TPKIXCertPathChecker}
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            /* Cannot happen */
            throw new InternalError(e.toString(), e);
        }
    }
}
