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

import java.io.IOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TPublicKey;
import org.teavm.classlib.javax.auth.x500.TX500Principal;
import org.teavm.classlib.sun.security.x509.TNameConstraintsExtension;

/**
 * Created by vasek on 25. 10. 2016.
 */
public class TTrustAnchor {

    private final TPublicKey pubKey;
    private final TString caName;
    private final TX500Principal caPrincipal;
    private final TX509Certificate trustedCert;
    private byte[] ncBytes;
    private TNameConstraintsExtension nc;

    /**
     * Creates an instance of {@code TTrustAnchor} with the specified
     * {@code X509Certificate} and optional name constraints, which
     * are intended to be used as additional constraints when validating
     * an X.509 certification path.
     * <p>
     * The name constraints are specified as a byte array. This byte array
     * should contain the DER encoded form of the name constraints, as they
     * would appear in the NameConstraints structure defined in
     * <a href="http://www.ietf.org/rfc/rfc3280">RFC 3280</a>
     * and X.509. The ASN.1 definition of this structure appears below.
     *
     * <pre>{@code
     *  NameConstraints ::= SEQUENCE {
     *       permittedSubtrees       [0]     GeneralSubtrees OPTIONAL,
     *       excludedSubtrees        [1]     GeneralSubtrees OPTIONAL }
     *
     *  GeneralSubtrees ::= SEQUENCE SIZE (1..MAX) OF GeneralSubtree
     *
     *  GeneralSubtree ::= SEQUENCE {
     *       base                    GeneralName,
     *       minimum         [0]     BaseDistance DEFAULT 0,
     *       maximum         [1]     BaseDistance OPTIONAL }
     *
     *  BaseDistance ::= INTEGER (0..MAX)
     *
     *  GeneralName ::= CHOICE {
     *       otherName                       [0]     OtherName,
     *       rfc822Name                      [1]     IA5String,
     *       dNSName                         [2]     IA5String,
     *       x400Address                     [3]     ORAddress,
     *       directoryName                   [4]     Name,
     *       ediPartyName                    [5]     EDIPartyName,
     *       uniformResourceIdentifier       [6]     IA5String,
     *       iPAddress                       [7]     OCTET STRING,
     *       registeredID                    [8]     OBJECT IDENTIFIER}
     * }</pre>
     * <p>
     * Note that the name constraints byte array supplied is cloned to protect
     * against subsequent modifications.
     *
     * @param trustedCert a trusted {@code X509Certificate}
     * @param nameConstraints a byte array containing the ASN.1 DER encoding of
     * a NameConstraints extension to be used for checking name constraints.
     * Only the value of the extension is included, not the OID or criticality
     * flag. Specify {@code null} to omit the parameter.
     * @throws IllegalArgumentException if the name constraints cannot be
     * decoded
     * @throws NullPointerException if the specified
     * {@code X509Certificate} is {@code null}
     */
    public TTrustAnchor(TX509Certificate trustedCert, byte[] nameConstraints)
    {
        if (trustedCert == null)
            throw new NullPointerException("the trustedCert parameter must " +
                    "be non-null");
        this.trustedCert = trustedCert;
        this.pubKey = null;
        this.caName = null;
        this.caPrincipal = null;
        setNameConstraints(nameConstraints);
    }

    public TTrustAnchor(TX500Principal caPrincipal, TPublicKey pubKey,
            byte[] nameConstraints) {
        if ((caPrincipal == null) || (pubKey == null)) {
            throw new NullPointerException();
        }
        this.trustedCert = null;
        this.caPrincipal = caPrincipal;
        this.caName = TString.wrap(caPrincipal.getName());
        this.pubKey = pubKey;
        setNameConstraints(nameConstraints);
    }

    public TTrustAnchor(TString caName, TPublicKey pubKey, byte[] nameConstraints)
    {
        if (pubKey == null)
            throw new NullPointerException("the pubKey parameter must be " +
                    "non-null");
        if (caName == null)
            throw new NullPointerException("the caName parameter must be " +
                    "non-null");
        if (caName.length() == 0)
            throw new IllegalArgumentException("the caName " +
                    "parameter must be a non-empty String");
        // check if caName is formatted correctly
        this.caPrincipal = new TX500Principal(caName);
        this.pubKey = pubKey;
        this.caName = caName;
        this.trustedCert = null;
        setNameConstraints(nameConstraints);
    }

    /**
     * Returns the most-trusted CA certificate.
     *
     * @return a trusted {@code X509Certificate} or {@code null}
     * if the trust anchor was not specified as a trusted certificate
     */
    public final TX509Certificate getTrustedCert() {
        return this.trustedCert;
    }

    /**
     * Returns the name of the most-trusted CA as an X500Principal.
     *
     * @return the X.500 distinguished name of the most-trusted CA, or
     * {@code null} if the trust anchor was not specified as a trusted
     * public key and name or X500Principal pair
     * @since 1.5
     */
    public final TX500Principal getCA() {
        return this.caPrincipal;
    }

    /**
     * Returns the name of the most-trusted CA in RFC 2253 {@code String}
     * format.
     *
     * @return the X.500 distinguished name of the most-trusted CA, or
     * {@code null} if the trust anchor was not specified as a trusted
     * public key and name or X500Principal pair
     */
    public final TString getCAName() {
        return this.caName;
    }

    /**
     * Returns the public key of the most-trusted CA.
     *
     * @return the public key of the most-trusted CA, or {@code null}
     * if the trust anchor was not specified as a trusted public key and name
     * or X500Principal pair
     */
    public final TPublicKey getCAPublicKey() {
        return this.pubKey;
    }

    /**
     * Decode the name constraints and clone them if not null.
     */
    private void setNameConstraints(byte[] bytes) {
        if (bytes == null) {
            ncBytes = null;
            nc = null;
        } else {
            ncBytes = bytes.clone();
            // validate DER encoding
            try {
                nc = new TNameConstraintsExtension(Boolean.FALSE, bytes);
            } catch (IOException ioe) {
                IllegalArgumentException iae =
                        new IllegalArgumentException(ioe.getMessage());
                iae.initCause(ioe);
                throw iae;
            }
        }
    }

    public final byte [] getNameConstraints() {
        return ncBytes == null ? null : ncBytes.clone();
    }

    /**
     * Returns a formatted string describing the {@code TTrustAnchor}.
     *
     * @return a formatted string describing the {@code TTrustAnchor}
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        if (pubKey != null) {
            sb.append("  Trusted CA Public Key: " + pubKey.toString() + "\n");
            sb.append("  Trusted CA Issuer Name: "
                    + String.valueOf(caName) + "\n");
        } else {
            sb.append("  Trusted CA cert: " + trustedCert.toString() + "\n");
        }
        if (nc != null)
            sb.append("  Name Constraints: " + nc.toString() + "\n");
        return sb.toString();
    }
}
