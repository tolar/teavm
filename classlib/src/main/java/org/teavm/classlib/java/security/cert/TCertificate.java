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
package org.teavm.classlib.java.security.cert;

import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TInvalidKeyException;
import org.teavm.classlib.java.security.TNoSuchAlgorithmException;
import org.teavm.classlib.java.security.TNoSuchProviderException;
import org.teavm.classlib.java.security.TProvider;
import org.teavm.classlib.java.security.TPublicKey;
import org.teavm.classlib.java.security.TSignatureException;
import org.teavm.classlib.java.util.TArrays;
import org.teavm.classlib.sun.security.cert.TCertificateEncodingException;
import org.teavm.classlib.sun.security.x509.TX509CertImpl;

public abstract class TCertificate {

    // the certificate type
    private final String type;

    /** Cache the hash code for the certiticate */
    private int hash = -1; // Default to -1

    /**
     * Creates a certificate of the specified type.
     *
     * @param type the standard name of the certificate type.
     * See the CertificateFactory section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard certificate types.
     */
    protected TCertificate(String type) {
        this.type = type;
    }

    /**
     * Returns the type of this certificate.
     *
     * @return the type of this certificate.
     */
    public final String getType() {
        return this.type;
    }

    /**
     * Compares this certificate for equality with the specified
     * object. If the {@code other} object is an
     * {@code instanceof} {@code Certificate}, then
     * its encoded form is retrieved and compared with the
     * encoded form of this certificate.
     *
     * @param other the object to test for equality with this certificate.
     * @return true iff the encoded forms of the two certificates
     * match, false otherwise.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TCertificate)) {
            return false;
        }
        try {
            byte[] thisCert = TX509CertImpl.getEncodedInternal(this);
            byte[] otherCert = TX509CertImpl.getEncodedInternal((TCertificate)other);

            return TArrays.equals(thisCert, otherCert);
        } catch (TCertificateException e) {
            return false;
        }
    }

    /**
     * Returns a hashcode value for this certificate from its
     * encoded form.
     *
     * @return the hashcode value.
     */
    public int hashCode() {
        int h = hash;
        if (h == -1) {
            try {
                h = TArrays.hashCode(TX509CertImpl.getEncodedInternal(this));
            } catch (TCertificateException e) {
                h = 0;
            }
            hash = h;
        }
        return h;
    }

    /**
     * Returns the encoded form of this certificate. It is
     * assumed that each certificate type would have only a single
     * form of encoding; for example, X.509 certificates would
     * be encoded as ASN.1 DER.
     *
     * @return the encoded form of this certificate
     *
     * @exception TCertificateEncodingException if an encoding error occurs.
     */
    public abstract byte[] getEncoded()
            throws TCertificateEncodingException;


    public abstract void verify(TPublicKey key)
            throws TCertificateException, TNoSuchAlgorithmException,
            TInvalidKeyException,TNoSuchProviderException,
            TSignatureException;


    public abstract void verify(TPublicKey key, TString sigProvider)
            throws TCertificateException, TNoSuchAlgorithmException,
            TInvalidKeyException, TNoSuchProviderException,
            TSignatureException;


    public void verify(TPublicKey key, TProvider sigProvider)
            throws TCertificateException, TNoSuchAlgorithmException,
            TInvalidKeyException, TSignatureException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a string representation of this certificate.
     *
     * @return a string representation of this certificate.
     */
    public abstract String toString();

    /**
     * Gets the public key from this certificate.
     *
     * @return the public key.
     */
    public abstract TPublicKey getPublicKey();

    /**
     * Alternate Certificate class for serialization.
     * @since 1.3
     */
    protected static class CertificateRep implements java.io.Serializable {

        private static final long serialVersionUID = -8563758940495660020L;

        private String type;
        private byte[] data;

        /**
         * Construct the alternate Certificate class with the Certificate
         * type and Certificate encoding bytes.
         *
         * <p>
         *
         * @param type the standard name of the Certificate type. <p>
         *
         * @param data the Certificate data.
         */
        protected CertificateRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        /**
         * Resolve the Certificate Object.
         *
         * <p>
         *
         * @return the resolved Certificate Object
         *
         * @throws java.io.ObjectStreamException if the Certificate
         *      could not be resolved
         */
        protected Object readResolve() throws java.io.ObjectStreamException {
        }
    }

    /**
     * Replace the Certificate to be serialized.
     *
     * @return the alternate Certificate object to be serialized
     *
     * @throws java.io.ObjectStreamException if a new object representing
     * this Certificate could not be created
     * @since 1.3
     */
    protected Object writeReplace() throws java.io.ObjectStreamException {
        try {
            return new TCertificate.CertificateRep(type, getEncoded());
        } catch (TCertificateException e) {
            throw new java.io.NotSerializableException
                    ("java.security.cert.Certificate: " +
                            type +
                            ": " +
                            e.getMessage());
        }
    }

}
