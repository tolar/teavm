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
package org.teavm.classlib.java.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertSelector;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.util.Set;

/**
 * Created by vasek on 22. 10. 2016.
 */
public class TPKIXBuilderParameters extends PKIXParameters {

    private int maxPathLength = 5;

    /**
     * Creates an instance of {@code TPKIXBuilderParameters} with
     * the specified {@code Set} of most-trusted CAs.
     * Each element of the set is a {@link TrustAnchor TTrustAnchor}.
     *
     * <p>Note that the {@code Set} is copied to protect against
     * subsequent modifications.
     *
     * @param trustAnchors a {@code Set} of {@code TTrustAnchor}s
     * @param targetConstraints a {@code CertSelector} specifying the
     * constraints on the target certificate
     * @throws InvalidAlgorithmParameterException if {@code trustAnchors}
     * is empty {@code (trustAnchors.isEmpty() == true)}
     * @throws NullPointerException if {@code trustAnchors} is
     * {@code null}
     * @throws ClassCastException if any of the elements of
     * {@code trustAnchors} are not of type
     * {@code java.security.cert.TTrustAnchor}
     */
    public TPKIXBuilderParameters(Set<TrustAnchor> trustAnchors, CertSelector
            targetConstraints) throws InvalidAlgorithmParameterException
    {
        super(trustAnchors);
        setTargetCertConstraints(targetConstraints);
    }

    /**
     * Creates an instance of {@code TPKIXBuilderParameters} that
     * populates the set of most-trusted CAs from the trusted
     * certificate entries contained in the specified {@code KeyStore}.
     * Only keystore entries that contain trusted {@code X509Certificate}s
     * are considered; all other certificate types are ignored.
     *
     * @param keystore a {@code KeyStore} from which the set of
     * most-trusted CAs will be populated
     * @param targetConstraints a {@code CertSelector} specifying the
     * constraints on the target certificate
     * @throws KeyStoreException if {@code keystore} has not been
     * initialized
     * @throws InvalidAlgorithmParameterException if {@code keystore} does
     * not contain at least one trusted certificate entry
     * @throws NullPointerException if {@code keystore} is
     * {@code null}
     */
    public TPKIXBuilderParameters(KeyStore keystore,
            CertSelector targetConstraints)
            throws KeyStoreException, InvalidAlgorithmParameterException
    {
        super(keystore);
        setTargetCertConstraints(targetConstraints);
    }

    /**
     * Sets the value of the maximum number of non-self-issued intermediate
     * certificates that may exist in a certification path. A certificate
     * is self-issued if the DNs that appear in the subject and issuer
     * fields are identical and are not empty. Note that the last certificate
     * in a certification path is not an intermediate certificate, and is not
     * included in this limit. Usually the last certificate is an end entity
     * certificate, but it can be a CA certificate. A PKIX
     * {@code CertPathBuilder} instance must not build
     * paths longer than the length specified.
     *
     * <p> A value of 0 implies that the path can only contain
     * a single certificate. A value of -1 implies that the
     * path length is unconstrained (i.e. there is no maximum).
     * The default maximum path length, if not specified, is 5.
     * Setting a value less than -1 will cause an exception to be thrown.
     *
     * <p> If any of the CA certificates contain the
     * {@code BasicConstraintsExtension}, the value of the
     * {@code pathLenConstraint} field of the extension overrides
     * the maximum path length parameter whenever the result is a
     * certification path of smaller length.
     *
     * @param maxPathLength the maximum number of non-self-issued intermediate
     *  certificates that may exist in a certification path
     * @throws InvalidParameterException if {@code maxPathLength} is set
     *  to a value less than -1
     *
     * @see #getMaxPathLength
     */
    public void setMaxPathLength(int maxPathLength) {
        if (maxPathLength < -1) {
            throw new InvalidParameterException("the maximum path "
                    + "length parameter can not be less than -1");
        }
        this.maxPathLength = maxPathLength;
    }

    /**
     * Returns the value of the maximum number of intermediate non-self-issued
     * certificates that may exist in a certification path. See
     * the {@link #setMaxPathLength} method for more details.
     *
     * @return the maximum number of non-self-issued intermediate certificates
     *  that may exist in a certification path, or -1 if there is no limit
     *
     * @see #setMaxPathLength
     */
    public int getMaxPathLength() {
        return maxPathLength;
    }

    /**
     * Returns a formatted string describing the parameters.
     *
     * @return a formatted string describing the parameters
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        sb.append(super.toString());
        sb.append("  Maximum Path TLength: " + maxPathLength + "\n");
        sb.append("]\n");
        return sb.toString();
    }
}
