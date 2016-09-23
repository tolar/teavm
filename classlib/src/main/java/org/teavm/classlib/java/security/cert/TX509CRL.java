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


import org.teavm.classlib.java.util.TArrays;
import org.teavm.classlib.javax.auth.x500.TX500Principal;
import org.teavm.classlib.sun.security.x509.TX509CRLImpl;

public abstract class TX509CRL extends TCRL implements TX509Extension {

    private transient TX500Principal issuerPrincipal;

    /**
     * Constructor for X.509 CRLs.
     */
    protected TX509CRL() {
        super("X.509");
    }

    /**
     * Compares this CRL for equality with the given
     * object. If the {@code other} object is an
     * {@code instanceof} {@code X509CRL}, then
     * its encoded form is retrieved and compared with the
     * encoded form of this CRL.
     *
     * @param other the object to test for equality with this CRL.
     *
     * @return true iff the encoded forms of the two CRLs
     * match, false otherwise.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof java.security.cert.X509CRL)) {
            return false;
        }
        try {
            byte[] thisCRL = TX509CRLImpl.getEncodedInternal(this);
            byte[] otherCRL = TX509CRLImpl.getEncodedInternal((java.security.cert.X509CRL)other);

            return TArrays.equals(thisCRL, otherCRL);
        } catch (TCRLException e) {
            return false;
        }
    }

    /**
     * Returns a hashcode value for this CRL from its
     * encoded form.
     *
     * @return the hashcode value.
     */
    public int hashCode() {
        int retval = 0;
        try {
            byte[] crlData = X509CRLImpl.getEncodedInternal(this);
            for (int i = 1; i < crlData.length; i++) {
                retval += crlData[i] * i;
            }
            return retval;
        } catch (TCRLException e) {
            return retval;
        }
    }

    /**
     * Returns the ASN.1 DER-encoded form of this CRL.
     *
     * @return the encoded form of this certificate
     * @exception TCRLException if an encoding error occurs.
     */
    public abstract byte[] getEncoded()
            throws TCRLException;

    /**
     * Verifies that this CRL was signed using the
     * private key that corresponds to the given public key.
     *
     * @param key the PublicKey used to carry out the verification.
     *
     * @exception NoSuchAlgorithmException on unsupported signature
     * algorithms.
     * @exception InvalidKeyException on incorrect key.
     * @exception NoSuchProviderException if there's no default provider.
     * @exception SignatureException on signature errors.
     * @exception TCRLException on encoding errors.
     */
    public abstract void verify(PublicKey key)
            throws TCRLException,  NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException,
            SignatureException;

    /**
     * Verifies that this CRL was signed using the
     * private key that corresponds to the given public key.
     * This method uses the signature verification engine
     * supplied by the given provider.
     *
     * @param key the PublicKey used to carry out the verification.
     * @param sigProvider the name of the signature provider.
     *
     * @exception NoSuchAlgorithmException on unsupported signature
     * algorithms.
     * @exception InvalidKeyException on incorrect key.
     * @exception NoSuchProviderException on incorrect provider.
     * @exception SignatureException on signature errors.
     * @exception TCRLException on encoding errors.
     */
    public abstract void verify(PublicKey key, String sigProvider)
            throws TCRLException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException,
            SignatureException;

    /**
     * Verifies that this CRL was signed using the
     * private key that corresponds to the given public key.
     * This method uses the signature verification engine
     * supplied by the given provider. Note that the specified Provider object
     * does not have to be registered in the provider list.
     *
     * This method was added to version 1.8 of the Java Platform Standard
     * Edition. In order to maintain backwards compatibility with existing
     * service providers, this method is not {@code abstract}
     * and it provides a default implementation.
     *
     * @param key the PublicKey used to carry out the verification.
     * @param sigProvider the signature provider.
     *
     * @exception NoSuchAlgorithmException on unsupported signature
     * algorithms.
     * @exception InvalidKeyException on incorrect key.
     * @exception SignatureException on signature errors.
     * @exception TCRLException on encoding errors.
     * @since 1.8
     */
    public void verify(PublicKey key, Provider sigProvider)
            throws TCRLException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        X509CRLImpl.verify(this, key, sigProvider);
    }

    /**
     * Gets the {@code version} (version number) value from the CRL.
     * The ASN.1 definition for this is:
     * <pre>
     * version    Version OPTIONAL,
     *             -- if present, must be v2
     *
     * Version  ::=  INTEGER  {  v1(0), v2(1), v3(2)  }
     *             -- v3 does not apply to CRLs but appears for consistency
     *             -- with definition of Version for certs
     * </pre>
     *
     * @return the version number, i.e. 1 or 2.
     */
    public abstract int getVersion();

    /**
     * <strong>Denigrated</strong>, replaced by {@linkplain
     * #getIssuerX500Principal()}. This method returns the {@code issuer}
     * as an implementation specific Principal object, which should not be
     * relied upon by portable code.
     *
     * <p>
     * Gets the {@code issuer} (issuer distinguished name) value from
     * the CRL. The issuer name identifies the entity that signed (and
     * issued) the CRL.
     *
     * <p>The issuer name field contains an
     * X.500 distinguished name (DN).
     * The ASN.1 definition for this is:
     * <pre>
     * issuer    Name
     *
     * Name ::= CHOICE { RDNSequence }
     * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     * RelativeDistinguishedName ::=
     *     SET OF AttributeValueAssertion
     *
     * AttributeValueAssertion ::= SEQUENCE {
     *                               AttributeType,
     *                               AttributeValue }
     * AttributeType ::= OBJECT IDENTIFIER
     * AttributeValue ::= ANY
     * </pre>
     * The {@code Name} describes a hierarchical name composed of
     * attributes,
     * such as country name, and corresponding values, such as US.
     * The type of the {@code AttributeValue} component is determined by
     * the {@code AttributeType}; in general it will be a
     * {@code directoryString}. A {@code directoryString} is usually
     * one of {@code PrintableString},
     * {@code TeletexString} or {@code UniversalString}.
     *
     * @return a Principal whose name is the issuer distinguished name.
     */
    public abstract Principal getIssuerDN();

    /**
     * Returns the issuer (issuer distinguished name) value from the
     * CRL as an {@code X500Principal}.
     * <p>
     * It is recommended that subclasses override this method.
     *
     * @return an {@code X500Principal} representing the issuer
     *          distinguished name
     * @since 1.4
     */
    public X500Principal getIssuerX500Principal() {
        if (issuerPrincipal == null) {
            issuerPrincipal = X509CRLImpl.getIssuerX500Principal(this);
        }
        return issuerPrincipal;
    }

    /**
     * Gets the {@code thisUpdate} date from the CRL.
     * The ASN.1 definition for this is:
     * <pre>
     * thisUpdate   ChoiceOfTime
     * ChoiceOfTime ::= CHOICE {
     *     utcTime        UTCTime,
     *     generalTime    GeneralizedTime }
     * </pre>
     *
     * @return the {@code thisUpdate} date from the CRL.
     */
    public abstract Date getThisUpdate();

    /**
     * Gets the {@code nextUpdate} date from the CRL.
     *
     * @return the {@code nextUpdate} date from the CRL, or null if
     * not present.
     */
    public abstract Date getNextUpdate();

    /**
     * Gets the CRL entry, if any, with the given certificate serialNumber.
     *
     * @param serialNumber the serial number of the certificate for which a CRL entry
     * is to be looked up
     * @return the entry with the given serial number, or null if no such entry
     * exists in this CRL.
     * @see TX509CRLEntry
     */
    public abstract TX509CRLEntry
    getRevokedCertificate(BigInteger serialNumber);

    /**
     * Get the CRL entry, if any, for the given certificate.
     *
     * <p>This method can be used to lookup CRL entries in indirect CRLs,
     * that means CRLs that contain entries from issuers other than the CRL
     * issuer. The default implementation will only return entries for
     * certificates issued by the CRL issuer. Subclasses that wish to
     * support indirect CRLs should override this method.
     *
     * @param certificate the certificate for which a CRL entry is to be looked
     *   up
     * @return the entry for the given certificate, or null if no such entry
     *   exists in this CRL.
     * @exception NullPointerException if certificate is null
     *
     * @since 1.5
     */
    public TX509CRLEntry getRevokedCertificate(X509Certificate certificate) {
        X500Principal certIssuer = certificate.getIssuerX500Principal();
        X500Principal crlIssuer = getIssuerX500Principal();
        if (certIssuer.equals(crlIssuer) == false) {
            return null;
        }
        return getRevokedCertificate(certificate.getSerialNumber());
    }

    /**
     * Gets all the entries from this CRL.
     * This returns a Set of TX509CRLEntry objects.
     *
     * @return all the entries or null if there are none present.
     * @see TX509CRLEntry
     */
    public abstract Set<? extends TX509CRLEntry> getRevokedCertificates();

    /**
     * Gets the DER-encoded CRL information, the
     * {@code tbsCertList} from this CRL.
     * This can be used to verify the signature independently.
     *
     * @return the DER-encoded CRL information.
     * @exception TCRLException if an encoding error occurs.
     */
    public abstract byte[] getTBSCertList() throws TCRLException;

    /**
     * Gets the {@code signature} value (the raw signature bits) from
     * the CRL.
     * The ASN.1 definition for this is:
     * <pre>
     * signature     BIT STRING
     * </pre>
     *
     * @return the signature.
     */
    public abstract byte[] getSignature();

    /**
     * Gets the signature algorithm name for the CRL
     * signature algorithm. An example is the string "SHA256withRSA".
     * The ASN.1 definition for this is:
     * <pre>
     * signatureAlgorithm   AlgorithmIdentifier
     *
     * AlgorithmIdentifier  ::=  SEQUENCE  {
     *     algorithm               OBJECT IDENTIFIER,
     *     parameters              ANY DEFINED BY algorithm OPTIONAL  }
     *                             -- contains a value of the type
     *                             -- registered for use with the
     *                             -- algorithm object identifier value
     * </pre>
     *
     * <p>The algorithm name is determined from the {@code algorithm}
     * OID string.
     *
     * @return the signature algorithm name.
     */
    public abstract String getSigAlgName();

    /**
     * Gets the signature algorithm OID string from the CRL.
     * An OID is represented by a set of nonnegative whole numbers separated
     * by periods.
     * For example, the string "1.2.840.10040.4.3" identifies the SHA-1
     * with DSA signature algorithm defined in
     * <a href="http://www.ietf.org/rfc/rfc3279.txt">RFC 3279: Algorithms and
     * Identifiers for the Internet X.509 Public Key Infrastructure Certificate
     * and CRL Profile</a>.
     *
     * <p>See {@link #getSigAlgName() getSigAlgName} for
     * relevant ASN.1 definitions.
     *
     * @return the signature algorithm OID string.
     */
    public abstract String getSigAlgOID();

    /**
     * Gets the DER-encoded signature algorithm parameters from this
     * CRL's signature algorithm. In most cases, the signature
     * algorithm parameters are null; the parameters are usually
     * supplied with the public key.
     * If access to individual parameter values is needed then use
     * {@link java.security.AlgorithmParameters AlgorithmParameters}
     * and instantiate with the name returned by
     * {@link #getSigAlgName() getSigAlgName}.
     *
     * <p>See {@link #getSigAlgName() getSigAlgName} for
     * relevant ASN.1 definitions.
     *
     * @return the DER-encoded signature algorithm parameters, or
     *         null if no parameters are present.
     */
    public abstract byte[] getSigAlgParams();
}
