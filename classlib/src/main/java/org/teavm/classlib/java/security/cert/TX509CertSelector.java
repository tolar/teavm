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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TPublicKey;
import org.teavm.classlib.java.util.TDate;
import org.teavm.classlib.javax.auth.x500.TX500Principal;
import org.teavm.classlib.sun.misc.THexDumpEncoder;
import org.teavm.classlib.sun.security.cert.TCertificatePolicySet;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;
import org.teavm.classlib.sun.security.x509.TAlgorithmId;
import org.teavm.classlib.sun.security.x509.TCertificatePoliciesExtension;
import org.teavm.classlib.sun.security.x509.TCertificatePolicyId;
import org.teavm.classlib.sun.security.x509.TDNSName;
import org.teavm.classlib.sun.security.x509.TEDIPartyName;
import org.teavm.classlib.sun.security.x509.TExtendedKeyUsageExtension;
import org.teavm.classlib.sun.security.x509.TGeneralName;
import org.teavm.classlib.sun.security.x509.TGeneralNameInterface;
import org.teavm.classlib.sun.security.x509.TGeneralNames;
import org.teavm.classlib.sun.security.x509.TGeneralSubtree;
import org.teavm.classlib.sun.security.x509.TGeneralSubtrees;
import org.teavm.classlib.sun.security.x509.TIPAddressName;
import org.teavm.classlib.sun.security.x509.TNameConstraintsExtension;
import org.teavm.classlib.sun.security.x509.TOIDName;
import org.teavm.classlib.sun.security.x509.TOtherName;
import org.teavm.classlib.sun.security.x509.TPolicyInformation;
import org.teavm.classlib.sun.security.x509.TPrivateKeyUsageExtension;
import org.teavm.classlib.sun.security.x509.TRFC822Name;
import org.teavm.classlib.sun.security.x509.TSubjectAlternativeNameExtension;
import org.teavm.classlib.sun.security.x509.TURIName;
import org.teavm.classlib.sun.security.x509.TX400Address;
import org.teavm.classlib.sun.security.x509.TX500Name;
import org.teavm.classlib.sun.security.x509.TX509CertImpl;
import org.teavm.classlib.sun.security.x509.TX509Key;

/**
 * Created by vasek on 26. 10. 2016.
 */
public class TX509CertSelector implements TCertSelector {

    private final static TObjectIdentifier ANY_EXTENDED_KEY_USAGE =
            TObjectIdentifier.newInternal(new int[] {2, 5, 29, 37, 0});

    static {
        TCertPathHelperImpl.initialize();
    }

    private BigInteger serialNumber;
    private TX500Principal issuer;
    private TX500Principal subject;
    private byte[] subjectKeyID;
    private byte[] authorityKeyID;
    private TDate certificateValid;
    private TDate privateKeyValid;
    private TObjectIdentifier subjectPublicKeyAlgID;
    private TPublicKey subjectPublicKey;
    private byte[] subjectPublicKeyBytes;
    private boolean[] keyUsage;
    private Set<TString> keyPurposeSet;
    private Set<TObjectIdentifier> keyPurposeOIDSet;
    private Set<List<?>> subjectAlternativeNames;
    private Set<TGeneralNameInterface> subjectAlternativeGeneralNames;
    private TCertificatePolicySet policy;
    private Set<String> policySet;
    private Set<List<?>> pathToNames;
    private Set<TGeneralNameInterface> pathToGeneralNames;
    private TNameConstraintsExtension nc;
    private byte[] ncBytes;
    private int basicConstraints = -1;
    private TX509Certificate x509Cert;
    private boolean matchAllSubjectAltNames = true;

    private static final Boolean FALSE = Boolean.FALSE;

    private static final int PRIVATE_KEY_USAGE_ID = 0;
    private static final int SUBJECT_ALT_NAME_ID = 1;
    private static final int NAME_CONSTRAINTS_ID = 2;
    private static final int CERT_POLICIES_ID = 3;
    private static final int EXTENDED_KEY_USAGE_ID = 4;
    private static final int NUM_OF_EXTENSIONS = 5;
    private static final TString[] EXTENSION_OIDS = new TString[NUM_OF_EXTENSIONS];

    static {
        EXTENSION_OIDS[PRIVATE_KEY_USAGE_ID]  = TString.wrap("2.5.29.16");
        EXTENSION_OIDS[SUBJECT_ALT_NAME_ID]   = TString.wrap("2.5.29.17");
        EXTENSION_OIDS[NAME_CONSTRAINTS_ID]   = TString.wrap("2.5.29.30");
        EXTENSION_OIDS[CERT_POLICIES_ID]      = TString.wrap("2.5.29.32");
        EXTENSION_OIDS[EXTENDED_KEY_USAGE_ID] = TString.wrap("2.5.29.37");
    };

    /* Constants representing the GeneralName types */
    static final int NAME_ANY = 0;
    static final int NAME_RFC822 = 1;
    static final int NAME_DNS = 2;
    static final int NAME_X400 = 3;
    static final int NAME_DIRECTORY = 4;
    static final int NAME_EDI = 5;
    static final int NAME_URI = 6;
    static final int NAME_IP = 7;
    static final int NAME_OID = 8;

    /**
     * Creates an {@code TX509CertSelector}. Initially, no criteria are set
     * so any {@code X509Certificate} will match.
     */
    public TX509CertSelector() {
        // empty
    }

    /**
     * Sets the certificateEquals criterion. The specified
     * {@code X509Certificate} must be equal to the
     * {@code X509Certificate} passed to the {@code match} method.
     * If {@code null}, then this check is not applied.
     *
     * <p>This method is particularly useful when it is necessary to
     * match a single certificate. Although other criteria can be specified
     * in conjunction with the certificateEquals criterion, it is usually not
     * practical or necessary.
     *
     * @param cert the {@code X509Certificate} to match (or
     * {@code null})
     * @see #getCertificate
     */
    public void setCertificate(TX509Certificate cert) {
        x509Cert = cert;
    }

    /**
     * Sets the serialNumber criterion. The specified serial number
     * must match the certificate serial number in the
     * {@code X509Certificate}. If {@code null}, any certificate
     * serial number will do.
     *
     * @param serial the certificate serial number to match
     *        (or {@code null})
     * @see #getSerialNumber
     */
    public void setSerialNumber(BigInteger serial) {
        serialNumber = serial;
    }

    /**
     * Sets the issuer criterion. The specified distinguished name
     * must match the issuer distinguished name in the
     * {@code X509Certificate}. If {@code null}, any issuer
     * distinguished name will do.
     *
     * @param issuer a distinguished name as TX500Principal
     *                 (or {@code null})
     * @since 1.5
     */
    public void setIssuer(TX500Principal issuer) {
        this.issuer = issuer;
    }

    /**
     * <strong>Denigrated</strong>, use {@linkplain #setIssuer(TX500Principal)}
     * or {@linkplain #setIssuer(byte[])} instead. This method should not be
     * relied on as it can fail to match some certificates because of a loss of
     * encoding information in the
     * <a href="http://www.ietf.org/rfc/rfc2253.txt">RFC 2253</a> String form
     * of some distinguished names.
     * <p>
     * Sets the issuer criterion. The specified distinguished name
     * must match the issuer distinguished name in the
     * {@code X509Certificate}. If {@code null}, any issuer
     * distinguished name will do.
     * <p>
     * If {@code issuerDN} is not {@code null}, it should contain a
     * distinguished name, in RFC 2253 format.
     *
     * @param issuerDN a distinguished name in RFC 2253 format
     *                 (or {@code null})
     * @throws IOException if a parsing error occurs (incorrect form for DN)
     */
    public void setIssuer(TString issuerDN) throws IOException {
        if (issuerDN == null) {
            issuer = null;
        } else {
            issuer = new TX500Name(issuerDN).asX500Principal();
        }
    }

    /**
     * Sets the issuer criterion. The specified distinguished name
     * must match the issuer distinguished name in the
     * {@code X509Certificate}. If {@code null} is specified,
     * the issuer criterion is disabled and any issuer distinguished name will
     * do.
     * <p>
     * If {@code issuerDN} is not {@code null}, it should contain a
     * single DER encoded distinguished name, as defined in X.501. The ASN.1
     * notation for this structure is as follows.
     * <pre>{@code
     * Name ::= CHOICE {
     *   RDNSequence }
     *
     * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     *
     * RelativeDistinguishedName ::=
     *   SET SIZE (1 .. MAX) OF AttributeTypeAndValue
     *
     * AttributeTypeAndValue ::= SEQUENCE {
     *   type     AttributeType,
     *   value    AttributeValue }
     *
     * AttributeType ::= OBJECT IDENTIFIER
     *
     * AttributeValue ::= ANY DEFINED BY AttributeType
     * ....
     * DirectoryString ::= CHOICE {
     *       teletexString           TeletexString (SIZE (1..MAX)),
     *       printableString         PrintableString (SIZE (1..MAX)),
     *       universalString         UniversalString (SIZE (1..MAX)),
     *       utf8String              UTF8String (SIZE (1.. MAX)),
     *       bmpString               BMPString (SIZE (1..MAX)) }
     * }</pre>
     * <p>
     * Note that the byte array specified here is cloned to protect against
     * subsequent modifications.
     *
     * @param issuerDN a byte array containing the distinguished name
     *                 in ASN.1 DER encoded form (or {@code null})
     * @throws IOException if an encoding error occurs (incorrect form for DN)
     */
    public void setIssuer(byte[] issuerDN) throws IOException {
        try {
            issuer = (issuerDN == null ? null : new TX500Principal(issuerDN));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid name", e);
        }
    }

    /**
     * Sets the subject criterion. The specified distinguished name
     * must match the subject distinguished name in the
     * {@code X509Certificate}. If {@code null}, any subject
     * distinguished name will do.
     *
     * @param subject a distinguished name as TX500Principal
     *                  (or {@code null})
     * @since 1.5
     */
    public void setSubject(TX500Principal subject) {
        this.subject = subject;
    }

    /**
     * <strong>Denigrated</strong>, use {@linkplain #setSubject(TX500Principal)}
     * or {@linkplain #setSubject(byte[])} instead. This method should not be
     * relied on as it can fail to match some certificates because of a loss of
     * encoding information in the RFC 2253 String form of some distinguished
     * names.
     * <p>
     * Sets the subject criterion. The specified distinguished name
     * must match the subject distinguished name in the
     * {@code X509Certificate}. If {@code null}, any subject
     * distinguished name will do.
     * <p>
     * If {@code subjectDN} is not {@code null}, it should contain a
     * distinguished name, in RFC 2253 format.
     *
     * @param subjectDN a distinguished name in RFC 2253 format
     *                  (or {@code null})
     * @throws IOException if a parsing error occurs (incorrect form for DN)
     */
    public void setSubject(TString subjectDN) throws IOException {
        if (subjectDN == null) {
            subject = null;
        } else {
            subject = new TX500Name(subjectDN).asX500Principal();
        }
    }

    public void setSubject(byte[] subjectDN) throws IOException {
        try {
            subject = (subjectDN == null ? null : new TX500Principal(subjectDN));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid name", e);
        }
    }

    /**
     * Sets the subjectKeyIdentifier criterion. The
     * {@code X509Certificate} must contain a SubjectKeyIdentifier
     * extension for which the contents of the extension
     * matches the specified criterion value.
     * If the criterion value is {@code null}, no
     * subjectKeyIdentifier check will be done.
     * <p>
     * If {@code subjectKeyID} is not {@code null}, it
     * should contain a single DER encoded value corresponding to the contents
     * of the extension value (not including the object identifier,
     * criticality setting, and encapsulating OCTET STRING)
     * for a SubjectKeyIdentifier extension.
     * The ASN.1 notation for this structure follows.
     *
     * <pre>{@code
     * SubjectKeyIdentifier ::= KeyIdentifier
     *
     * KeyIdentifier ::= OCTET STRING
     * }</pre>
     * <p>
     * Since the format of subject key identifiers is not mandated by
     * any standard, subject key identifiers are not parsed by the
     * {@code TX509CertSelector}. Instead, the values are compared using
     * a byte-by-byte comparison.
     * <p>
     * Note that the byte array supplied here is cloned to protect against
     * subsequent modifications.
     *
     * @param subjectKeyID the subject key identifier (or {@code null})
     * @see #getSubjectKeyIdentifier
     */
    public void setSubjectKeyIdentifier(byte[] subjectKeyID) {
        if (subjectKeyID == null) {
            this.subjectKeyID = null;
        } else {
            this.subjectKeyID = subjectKeyID.clone();
        }
    }

    /**
     * Sets the authorityKeyIdentifier criterion. The
     * {@code X509Certificate} must contain an
     * AuthorityKeyIdentifier extension for which the contents of the
     * extension value matches the specified criterion value.
     * If the criterion value is {@code null}, no
     * authorityKeyIdentifier check will be done.
     * <p>
     * If {@code authorityKeyID} is not {@code null}, it
     * should contain a single DER encoded value corresponding to the contents
     * of the extension value (not including the object identifier,
     * criticality setting, and encapsulating OCTET STRING)
     * for an AuthorityKeyIdentifier extension.
     * The ASN.1 notation for this structure follows.
     *
     * <pre>{@code
     * AuthorityKeyIdentifier ::= SEQUENCE {
     *    keyIdentifier             [0] KeyIdentifier           OPTIONAL,
     *    authorityCertIssuer       [1] GeneralNames            OPTIONAL,
     *    authorityCertSerialNumber [2] CertificateSerialNumber OPTIONAL  }
     *
     * KeyIdentifier ::= OCTET STRING
     * }</pre>
     * <p>
     * Authority key identifiers are not parsed by the
     * {@code TX509CertSelector}.  Instead, the values are
     * compared using a byte-by-byte comparison.
     * <p>
     * When the {@code keyIdentifier} field of
     * {@code AuthorityKeyIdentifier} is populated, the value is
     * usually taken from the {@code SubjectKeyIdentifier} extension
     * in the issuer's certificate.  Note, however, that the result of
     * {@code X509Certificate.getExtensionValue(<SubjectKeyIdentifier Object
     * Identifier>)} on the issuer's certificate may NOT be used
     * directly as the input to {@code setAuthorityKeyIdentifier}.
     * This is because the SubjectKeyIdentifier contains
     * only a KeyIdentifier OCTET STRING, and not a SEQUENCE of
     * KeyIdentifier, GeneralNames, and CertificateSerialNumber.
     * In order to use the extension value of the issuer certificate's
     * {@code SubjectKeyIdentifier}
     * extension, it will be necessary to extract the value of the embedded
     * {@code KeyIdentifier} OCTET STRING, then DER encode this OCTET
     * STRING inside a SEQUENCE.
     * For more details on SubjectKeyIdentifier, see
     * {@link #setSubjectKeyIdentifier(byte[] subjectKeyID)}.
     * <p>
     * Note also that the byte array supplied here is cloned to protect against
     * subsequent modifications.
     *
     * @param authorityKeyID the authority key identifier
     *        (or {@code null})
     * @see #getAuthorityKeyIdentifier
     */
    public void setAuthorityKeyIdentifier(byte[] authorityKeyID) {
        if (authorityKeyID == null) {
            this.authorityKeyID = null;
        } else {
            this.authorityKeyID = authorityKeyID.clone();
        }
    }

    /**
     * Sets the certificateValid criterion. The specified date must fall
     * within the certificate validity period for the
     * {@code X509Certificate}. If {@code null}, no certificateValid
     * check will be done.
     * <p>
     * Note that the {@code Date} supplied here is cloned to protect
     * against subsequent modifications.
     *
     * @param certValid the {@code Date} to check (or {@code null})
     * @see #getCertificateValid
     */
    public void setCertificateValid(Date certValid) {
        if (certValid == null) {
            certificateValid = null;
        } else {
            certificateValid = (TDate)certValid.clone();
        }
    }

    /**
     * Sets the privateKeyValid criterion. The specified date must fall
     * within the private key validity period for the
     * {@code X509Certificate}. If {@code null}, no privateKeyValid
     * check will be done.
     * <p>
     * Note that the {@code Date} supplied here is cloned to protect
     * against subsequent modifications.
     *
     * @param privateKeyValid the {@code Date} to check (or
     *                        {@code null})
     * @see #getPrivateKeyValid
     */
    public void setPrivateKeyValid(Date privateKeyValid) {
        if (privateKeyValid == null) {
            this.privateKeyValid = null;
        } else {
            this.privateKeyValid = (TDate)privateKeyValid.clone();
        }
    }

    /**
     * Sets the subjectPublicKeyAlgID criterion. The
     * {@code X509Certificate} must contain a subject public key
     * with the specified algorithm. If {@code null}, no
     * subjectPublicKeyAlgID check will be done.
     *
     * @param oid The object identifier (OID) of the algorithm to check
     *            for (or {@code null}). An OID is represented by a
     *            set of nonnegative integers separated by periods.
     * @throws IOException if the OID is invalid, such as
     * the first component being not 0, 1 or 2 or the second component
     * being greater than 39.
     *
     * @see #getSubjectPublicKeyAlgID
     */
    public void setSubjectPublicKeyAlgID(TString oid) throws IOException {
        if (oid == null) {
            subjectPublicKeyAlgID = null;
        } else {
            subjectPublicKeyAlgID = new TObjectIdentifier(oid);
        }
    }

    /**
     * Sets the subjectPublicKey criterion. The
     * {@code X509Certificate} must contain the specified subject public
     * key. If {@code null}, no subjectPublicKey check will be done.
     *
     * @param key the subject public key to check for (or {@code null})
     * @see #getSubjectPublicKey
     */
    public void setSubjectPublicKey(TPublicKey key) {
        if (key == null) {
            subjectPublicKey = null;
            subjectPublicKeyBytes = null;
        } else {
            subjectPublicKey = key;
            subjectPublicKeyBytes = key.getEncoded();
        }
    }

    /**
     * Sets the subjectPublicKey criterion. The {@code X509Certificate}
     * must contain the specified subject public key. If {@code null},
     * no subjectPublicKey check will be done.
     * <p>
     * Because this method allows the public key to be specified as a byte
     * array, it may be used for unknown key types.
     * <p>
     * If {@code key} is not {@code null}, it should contain a
     * single DER encoded SubjectPublicKeyInfo structure, as defined in X.509.
     * The ASN.1 notation for this structure is as follows.
     * <pre>{@code
     * SubjectPublicKeyInfo  ::=  SEQUENCE  {
     *   algorithm            AlgorithmIdentifier,
     *   subjectPublicKey     BIT STRING  }
     *
     * AlgorithmIdentifier  ::=  SEQUENCE  {
     *   algorithm               OBJECT IDENTIFIER,
     *   parameters              ANY DEFINED BY algorithm OPTIONAL  }
     *                              -- contains a value of the type
     *                              -- registered for use with the
     *                              -- algorithm object identifier value
     * }</pre>
     * <p>
     * Note that the byte array supplied here is cloned to protect against
     * subsequent modifications.
     *
     * @param key a byte array containing the subject public key in ASN.1 DER
     *            form (or {@code null})
     * @throws IOException if an encoding error occurs (incorrect form for
     * subject public key)
     * @see #getSubjectPublicKey
     */
    public void setSubjectPublicKey(byte[] key) throws IOException {
        if (key == null) {
            subjectPublicKey = null;
            subjectPublicKeyBytes = null;
        } else {
            subjectPublicKeyBytes = key.clone();
            subjectPublicKey = TX509Key.parse(new TDerValue(subjectPublicKeyBytes));
        }
    }

    public void setKeyUsage(boolean[] keyUsage) {
        if (keyUsage == null) {
            this.keyUsage = null;
        } else {
            this.keyUsage = keyUsage.clone();
        }
    }

    /**
     * Sets the extendedKeyUsage criterion. The {@code X509Certificate}
     * must allow the specified key purposes in its extended key usage
     * extension. If {@code keyPurposeSet} is empty or {@code null},
     * no extendedKeyUsage check will be done. Note that an
     * {@code X509Certificate} that has no extendedKeyUsage extension
     * implicitly allows all key purposes.
     * <p>
     * Note that the {@code Set} is cloned to protect against
     * subsequent modifications.
     *
     * @param keyPurposeSet a {@code Set} of key purpose OIDs in string
     * format (or {@code null}). Each OID is represented by a set of
     * nonnegative integers separated by periods.
     * @throws IOException if the OID is invalid, such as
     * the first component being not 0, 1 or 2 or the second component
     * being greater than 39.
     * @see #getExtendedKeyUsage
     */
    public void setExtendedKeyUsage(Set<TString> keyPurposeSet) throws IOException {
        if ((keyPurposeSet == null) || keyPurposeSet.isEmpty()) {
            this.keyPurposeSet = null;
            keyPurposeOIDSet = null;
        } else {
            this.keyPurposeSet =
                    Collections.unmodifiableSet(new HashSet<TString>(keyPurposeSet));
            keyPurposeOIDSet = new HashSet<TObjectIdentifier>();
            for (TString s : this.keyPurposeSet) {
                keyPurposeOIDSet.add(new TObjectIdentifier(s));
            }
        }
    }

    /**
     * Enables/disables matching all of the subjectAlternativeNames
     * specified in the {@link #setSubjectAlternativeNames
     * setSubjectAlternativeNames} or {@link #addSubjectAlternativeName
     * addSubjectAlternativeName} methods. If enabled,
     * the {@code X509Certificate} must contain all of the
     * specified subject alternative names. If disabled, the
     * {@code X509Certificate} must contain at least one of the
     * specified subject alternative names.
     *
     * <p>The matchAllNames flag is {@code true} by default.
     *
     * @param matchAllNames if {@code true}, the flag is enabled;
     * if {@code false}, the flag is disabled.
     * @see #getMatchAllSubjectAltNames
     */
    public void setMatchAllSubjectAltNames(boolean matchAllNames) {
        this.matchAllSubjectAltNames = matchAllNames;
    }

    public void setSubjectAlternativeNames(Collection<List<?>> names)
            throws IOException {
        if (names == null) {
            subjectAlternativeNames = null;
            subjectAlternativeGeneralNames = null;
        } else {
            if (names.isEmpty()) {
                subjectAlternativeNames = null;
                subjectAlternativeGeneralNames = null;
                return;
            }
            Set<List<?>> tempNames = cloneAndCheckNames(names);
            // Ensure that we either set both of these or neither
            subjectAlternativeGeneralNames = parseNames(tempNames);
            subjectAlternativeNames = tempNames;
        }
    }

    public void addSubjectAlternativeName(int type, String name)
            throws IOException {
        addSubjectAlternativeNameInternal(type, name);
    }

    /**
     * Adds a name to the subjectAlternativeNames criterion. The
     * {@code X509Certificate} must contain all or at least one
     * of the specified subjectAlternativeNames, depending on the value of
     * the matchAllNames flag (see {@link #setMatchAllSubjectAltNames
     * setMatchAllSubjectAltNames}).
     * <p>
     * This method allows the caller to add a name to the set of subject
     * alternative names.
     * The specified name is added to any previous value for the
     * subjectAlternativeNames criterion. If the specified name is a
     * duplicate, it may be ignored.
     * <p>
     * The name is provided as a byte array. This byte array should contain
     * the DER encoded name, as it would appear in the GeneralName structure
     * defined in RFC 3280 and X.509. The encoded byte array should only contain
     * the encoded value of the name, and should not include the tag associated
     * with the name in the GeneralName structure. The ASN.1 definition of this
     * structure appears below.
     * <pre>{@code
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
     * Note that the byte array supplied here is cloned to protect against
     * subsequent modifications.
     *
     * @param type the name type (0-8, as listed above)
     * @param name a byte array containing the name in ASN.1 DER encoded form
     * @throws IOException if a parsing error occurs
     */
    public void addSubjectAlternativeName(int type, byte[] name)
            throws IOException {
        // clone because byte arrays are modifiable
        addSubjectAlternativeNameInternal(type, name.clone());
    }

    /**
     * A private method that adds a name (String or byte array) to the
     * subjectAlternativeNames criterion. The {@code X509Certificate}
     * must contain the specified subjectAlternativeName.
     *
     * @param type the name type (0-8, as specified in
     *             RFC 3280, section 4.2.1.7)
     * @param name the name in string or byte array form
     * @throws IOException if a parsing error occurs
     */
    private void addSubjectAlternativeNameInternal(int type, Object name)
            throws IOException {
        // First, ensure that the name parses
        TGeneralNameInterface tempName = makeGeneralNameInterface(type, name);
        if (subjectAlternativeNames == null) {
            subjectAlternativeNames = new HashSet<List<?>>();
        }
        if (subjectAlternativeGeneralNames == null) {
            subjectAlternativeGeneralNames = new HashSet<TGeneralNameInterface>();
        }
        List<Object> list = new ArrayList<Object>(2);
        list.add(Integer.valueOf(type));
        list.add(name);
        subjectAlternativeNames.add(list);
        subjectAlternativeGeneralNames.add(tempName);
    }

    /**
     * Parse an argument of the form passed to setSubjectAlternativeNames,
     * returning a {@code Collection} of
     * {@code GeneralNameInterface}s.
     * Throw an IllegalArgumentException or a ClassCastException
     * if the argument is malformed.
     *
     * @param names a Collection with one entry per name.
     *              Each entry is a {@code List} whose first entry
     *              is an Integer (the name type, 0-8) and whose second
     *              entry is a String or a byte array (the name, in
     *              string or ASN.1 DER encoded form, respectively).
     *              There can be multiple names of the same type. Null is
     *              not an acceptable value.
     * @return a Set of {@code GeneralNameInterface}s
     * @throws IOException if a parsing error occurs
     */
    private static Set<TGeneralNameInterface> parseNames(Collection<List<?>> names) throws IOException {
        Set<TGeneralNameInterface> genNames = new HashSet<TGeneralNameInterface>();
        for (List<?> nameList : names) {
            if (nameList.size() != 2) {
                throw new IOException("name list size not 2");
            }
            Object o =  nameList.get(0);
            if (!(o instanceof Integer)) {
                throw new IOException("expected an Integer");
            }
            int nameType = ((Integer)o).intValue();
            o = nameList.get(1);
            genNames.add(makeGeneralNameInterface(nameType, o));
        }

        return genNames;
    }

    /**
     * Compare for equality two objects of the form passed to
     * setSubjectAlternativeNames (or X509CRLSelector.setIssuerNames).
     * Throw an {@code IllegalArgumentException} or a
     * {@code ClassCastException} if one of the objects is malformed.
     *
     * @param object1 a Collection containing the first object to compare
     * @param object2 a Collection containing the second object to compare
     * @return true if the objects are equal, false otherwise
     */
    static boolean equalNames(Collection<?> object1, Collection<?> object2) {
        if ((object1 == null) || (object2 == null)) {
            return object1 == object2;
        }
        return object1.equals(object2);
    }

    /**
     * Make a {@code GeneralNameInterface} out of a name type (0-8) and an
     * Object that may be a byte array holding the ASN.1 DER encoded
     * name or a String form of the name.  Except for X.509
     * Distinguished Names, the String form of the name must not be the
     * result from calling toString on an existing GeneralNameInterface
     * implementing class.  The output of toString is not compatible
     * with the String constructors for names other than Distinguished
     * Names.
     *
     * @param type name type (0-8)
     * @param name name as ASN.1 Der-encoded byte array or String
     * @return a GeneralNameInterface name
     * @throws IOException if a parsing error occurs
     */
    static TGeneralNameInterface makeGeneralNameInterface(int type, Object name)
            throws IOException {
        TGeneralNameInterface result;

        if (name instanceof String) {
            switch (type) {
                case NAME_RFC822:
                    result = new TRFC822Name((TString)name);
                    break;
                case NAME_DNS:
                    result = new TDNSName((TString)name);
                    break;
                case NAME_DIRECTORY:
                    result = new TX500Name((TString)name);
                    break;
                case NAME_URI:
                    result = new TURIName((TString)name);
                    break;
                case NAME_IP:
                    result = new TIPAddressName((TString)name);
                    break;
                case NAME_OID:
                    result = new TOIDName((TString)name);
                    break;
                default:
                    throw new IOException("unable to parse String names of type "
                            + type);
            }
        } else if (name instanceof byte[]) {
            TDerValue val = new TDerValue((byte[]) name);

            switch (type) {
                case NAME_ANY:
                    result = new TOtherName(val);
                    break;
                case NAME_RFC822:
                    result = new TRFC822Name(val);
                    break;
                case NAME_DNS:
                    result = new TDNSName(val);
                    break;
                case NAME_X400:
                    result = new TX400Address(val);
                    break;
                case NAME_DIRECTORY:
                    result = new TX500Name(val);
                    break;
                case NAME_EDI:
                    result = new TEDIPartyName(val);
                    break;
                case NAME_URI:
                    result = new TURIName(val);
                    break;
                case NAME_IP:
                    result = new TIPAddressName(val);
                    break;
                case NAME_OID:
                    result = new TOIDName(val);
                    break;
                default:
                    throw new IOException("unable to parse byte array names of "
                            + "type " + type);
            }
        } else {
            throw new IOException("name not String or byte array");
        }
        return result;
    }


    /**
     * Sets the name constraints criterion. The {@code X509Certificate}
     * must have subject and subject alternative names that
     * meet the specified name constraints.
     * <p>
     * The name constraints are specified as a byte array. This byte array
     * should contain the DER encoded form of the name constraints, as they
     * would appear in the NameConstraints structure defined in RFC 3280
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
     * Note that the byte array supplied here is cloned to protect against
     * subsequent modifications.
     *
     * @param bytes a byte array containing the ASN.1 DER encoding of
     *              a NameConstraints extension to be used for checking
     *              name constraints. Only the value of the extension is
     *              included, not the OID or criticality flag. Can be
     *              {@code null},
     *              in which case no name constraints check will be performed.
     * @throws IOException if a parsing error occurs
     * @see #getNameConstraints
     */
    public void setNameConstraints(byte[] bytes) throws IOException {
        if (bytes == null) {
            ncBytes = null;
            nc = null;
        } else {
            ncBytes = bytes.clone();
            nc = new TNameConstraintsExtension(FALSE, bytes);
        }
    }

    /**
     * Sets the basic constraints constraint. If the value is greater than or
     * equal to zero, {@code X509Certificates} must include a
     * basicConstraints extension with
     * a pathLen of at least this value. If the value is -2, only end-entity
     * certificates are accepted. If the value is -1, no check is done.
     * <p>
     * This constraint is useful when building a certification path forward
     * (from the target toward the trust anchor. If a partial path has been
     * built, any candidate certificate must have a maxPathLen value greater
     * than or equal to the number of certificates in the partial path.
     *
     * @param minMaxPathLen the value for the basic constraints constraint
     * @throws IllegalArgumentException if the value is less than -2
     * @see #getBasicConstraints
     */
    public void setBasicConstraints(int minMaxPathLen) {
        if (minMaxPathLen < -2) {
            throw new IllegalArgumentException("basic constraints less than -2");
        }
        basicConstraints = minMaxPathLen;
    }

    /**
     * Sets the policy constraint. The {@code X509Certificate} must
     * include at least one of the specified policies in its certificate
     * policies extension. If {@code certPolicySet} is empty, then the
     * {@code X509Certificate} must include at least some specified policy
     * in its certificate policies extension. If {@code certPolicySet} is
     * {@code null}, no policy check will be performed.
     * <p>
     * Note that the {@code Set} is cloned to protect against
     * subsequent modifications.
     *
     * @param certPolicySet a {@code Set} of certificate policy OIDs in
     *                      string format (or {@code null}). Each OID is
     *                      represented by a set of nonnegative integers
     *                    separated by periods.
     * @throws IOException if a parsing error occurs on the OID such as
     * the first component is not 0, 1 or 2 or the second component is
     * greater than 39.
     * @see #getPolicy
     */
    public void setPolicy(Set<String> certPolicySet) throws IOException {
        if (certPolicySet == null) {
            policySet = null;
            policy = null;
        } else {
            // Snapshot set and parse it
            Set<String> tempSet = Collections.unmodifiableSet
                    (new HashSet<String>(certPolicySet));
            /* Convert to Vector of TObjectIdentifiers */
            Iterator<String> i = tempSet.iterator();
            Vector<TCertificatePolicyId> polIdVector = new Vector<TCertificatePolicyId>();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof String)) {
                    throw new IOException("non String in certPolicySet");
                }
                polIdVector.add(new TCertificatePolicyId(new TObjectIdentifier(
                        (TString)o)));
            }
            // If everything went OK, make the changes
            policySet = tempSet;
            policy = new TCertificatePolicySet(polIdVector);
        }
    }

    public void setPathToNames(Collection<List<?>> names) throws IOException {
        if ((names == null) || names.isEmpty()) {
            pathToNames = null;
            pathToGeneralNames = null;
        } else {
            Set<List<?>> tempNames = cloneAndCheckNames(names);
            pathToGeneralNames = parseNames(tempNames);
            // Ensure that we either set both of these or neither
            pathToNames = tempNames;
        }
    }

    // called from CertPathHelper
    void setPathToNamesInternal(Set<TGeneralNameInterface> names) {
        // set names to non-null dummy value
        // this breaks getPathToNames()
        pathToNames = Collections.<List<?>>emptySet();
        pathToGeneralNames = names;
    }

    public void addPathToName(int type, String name) throws IOException {
        addPathToNameInternal(type, name);
    }

    public void addPathToName(int type, byte [] name) throws IOException {
        // clone because byte arrays are modifiable
        addPathToNameInternal(type, name.clone());
    }

    /**
     * A private method that adds a name (String or byte array) to the
     * pathToNames criterion. The {@code X509Certificate} must contain
     * the specified pathToName.
     *
     * @param type the name type (0-8, as specified in
     *             RFC 3280, section 4.2.1.7)
     * @param name the name in string or byte array form
     * @throws IOException if an encoding error occurs (incorrect form for DN)
     */
    private void addPathToNameInternal(int type, Object name)
            throws IOException {
        // First, ensure that the name parses
        TGeneralNameInterface tempName = makeGeneralNameInterface(type, name);
        if (pathToGeneralNames == null) {
            pathToNames = new HashSet<List<?>>();
            pathToGeneralNames = new HashSet<TGeneralNameInterface>();
        }
        List<Object> list = new ArrayList<Object>(2);
        list.add(Integer.valueOf(type));
        list.add(name);
        pathToNames.add(list);
        pathToGeneralNames.add(tempName);
    }

    /**
     * Returns the certificateEquals criterion. The specified
     * {@code X509Certificate} must be equal to the
     * {@code X509Certificate} passed to the {@code match} method.
     * If {@code null}, this check is not applied.
     *
     * @return the {@code X509Certificate} to match (or {@code null})
     * @see #setCertificate
     */
    public TX509Certificate getCertificate() {
        return x509Cert;
    }

    /**
     * Returns the serialNumber criterion. The specified serial number
     * must match the certificate serial number in the
     * {@code X509Certificate}. If {@code null}, any certificate
     * serial number will do.
     *
     * @return the certificate serial number to match
     *                (or {@code null})
     * @see #setSerialNumber
     */
    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    /**
     * Returns the issuer criterion as an {@code X500Principal}. This
     * distinguished name must match the issuer distinguished name in the
     * {@code X509Certificate}. If {@code null}, the issuer criterion
     * is disabled and any issuer distinguished name will do.
     *
     * @return the required issuer distinguished name as X500Principal
     *         (or {@code null})
     * @since 1.5
     */
    public TX500Principal getIssuer() {
        return issuer;
    }

    /**
     * <strong>Denigrated</strong>, use {@linkplain #getIssuer()} or
     * {@linkplain #getIssuerAsBytes()} instead. This method should not be
     * relied on as it can fail to match some certificates because of a loss of
     * encoding information in the RFC 2253 String form of some distinguished
     * names.
     * <p>
     * Returns the issuer criterion as a {@code String}. This
     * distinguished name must match the issuer distinguished name in the
     * {@code X509Certificate}. If {@code null}, the issuer criterion
     * is disabled and any issuer distinguished name will do.
     * <p>
     * If the value returned is not {@code null}, it is a
     * distinguished name, in RFC 2253 format.
     *
     * @return the required issuer distinguished name in RFC 2253 format
     *         (or {@code null})
     */
    public String getIssuerAsString() {
        return (issuer == null ? null : issuer.getName());
    }

    public byte[] getIssuerAsBytes() throws IOException {
        return (issuer == null ? null: issuer.getEncoded());
    }

    /**
     * Returns the subject criterion as an {@code X500Principal}. This
     * distinguished name must match the subject distinguished name in the
     * {@code X509Certificate}. If {@code null}, the subject criterion
     * is disabled and any subject distinguished name will do.
     *
     * @return the required subject distinguished name as X500Principal
     *         (or {@code null})
     * @since 1.5
     */
    public TX500Principal getSubject() {
        return subject;
    }

    /**
     * <strong>Denigrated</strong>, use {@linkplain #getSubject()} or
     * {@linkplain #getSubjectAsBytes()} instead. This method should not be
     * relied on as it can fail to match some certificates because of a loss of
     * encoding information in the RFC 2253 String form of some distinguished
     * names.
     * <p>
     * Returns the subject criterion as a {@code String}. This
     * distinguished name must match the subject distinguished name in the
     * {@code X509Certificate}. If {@code null}, the subject criterion
     * is disabled and any subject distinguished name will do.
     * <p>
     * If the value returned is not {@code null}, it is a
     * distinguished name, in RFC 2253 format.
     *
     * @return the required subject distinguished name in RFC 2253 format
     *         (or {@code null})
     */
    public String getSubjectAsString() {
        return (subject == null ? null : subject.getName());
    }

    public byte[] getSubjectAsBytes() throws IOException {
        return (subject == null ? null : subject.getEncoded());
    }

    /**
     * Returns the subjectKeyIdentifier criterion. The
     * {@code X509Certificate} must contain a SubjectKeyIdentifier
     * extension with the specified value. If {@code null}, no
     * subjectKeyIdentifier check will be done.
     * <p>
     * Note that the byte array returned is cloned to protect against
     * subsequent modifications.
     *
     * @return the key identifier (or {@code null})
     * @see #setSubjectKeyIdentifier
     */
    public byte[] getSubjectKeyIdentifier() {
        if (subjectKeyID == null) {
            return null;
        }
        return subjectKeyID.clone();
    }

    /**
     * Returns the authorityKeyIdentifier criterion. The
     * {@code X509Certificate} must contain a AuthorityKeyIdentifier
     * extension with the specified value. If {@code null}, no
     * authorityKeyIdentifier check will be done.
     * <p>
     * Note that the byte array returned is cloned to protect against
     * subsequent modifications.
     *
     * @return the key identifier (or {@code null})
     * @see #setAuthorityKeyIdentifier
     */
    public byte[] getAuthorityKeyIdentifier() {
        if (authorityKeyID == null) {
            return null;
        }
        return authorityKeyID.clone();
    }

    /**
     * Returns the certificateValid criterion. The specified date must fall
     * within the certificate validity period for the
     * {@code X509Certificate}. If {@code null}, no certificateValid
     * check will be done.
     * <p>
     * Note that the {@code Date} returned is cloned to protect against
     * subsequent modifications.
     *
     * @return the {@code Date} to check (or {@code null})
     * @see #setCertificateValid
     */
    public Date getCertificateValid() {
        if (certificateValid == null) {
            return null;
        }
        return (Date)certificateValid.clone();
    }

    /**
     * Returns the privateKeyValid criterion. The specified date must fall
     * within the private key validity period for the
     * {@code X509Certificate}. If {@code null}, no privateKeyValid
     * check will be done.
     * <p>
     * Note that the {@code Date} returned is cloned to protect against
     * subsequent modifications.
     *
     * @return the {@code Date} to check (or {@code null})
     * @see #setPrivateKeyValid
     */
    public Date getPrivateKeyValid() {
        if (privateKeyValid == null) {
            return null;
        }
        return (Date)privateKeyValid.clone();
    }

    /**
     * Returns the subjectPublicKeyAlgID criterion. The
     * {@code X509Certificate} must contain a subject public key
     * with the specified algorithm. If {@code null}, no
     * subjectPublicKeyAlgID check will be done.
     *
     * @return the object identifier (OID) of the signature algorithm to check
     *         for (or {@code null}). An OID is represented by a set of
     *         nonnegative integers separated by periods.
     * @see #setSubjectPublicKeyAlgID
     */
    public String getSubjectPublicKeyAlgID() {
        if (subjectPublicKeyAlgID == null) {
            return null;
        }
        return subjectPublicKeyAlgID.toString();
    }

    /**
     * Returns the subjectPublicKey criterion. The
     * {@code X509Certificate} must contain the specified subject
     * public key. If {@code null}, no subjectPublicKey check will be done.
     *
     * @return the subject public key to check for (or {@code null})
     * @see #setSubjectPublicKey
     */
    public TPublicKey getSubjectPublicKey() {
        return subjectPublicKey;
    }

    public boolean[] getKeyUsage() {
        if (keyUsage == null) {
            return null;
        }
        return keyUsage.clone();
    }

    /**
     * Returns the extendedKeyUsage criterion. The {@code X509Certificate}
     * must allow the specified key purposes in its extended key usage
     * extension. If the {@code keyPurposeSet} returned is empty or
     * {@code null}, no extendedKeyUsage check will be done. Note that an
     * {@code X509Certificate} that has no extendedKeyUsage extension
     * implicitly allows all key purposes.
     *
     * @return an immutable {@code Set} of key purpose OIDs in string
     * format (or {@code null})
     * @see #setExtendedKeyUsage
     */
    public Set<TString> getExtendedKeyUsage() {
        return keyPurposeSet;
    }

    /**
     * Indicates if the {@code X509Certificate} must contain all
     * or at least one of the subjectAlternativeNames
     * specified in the {@link #setSubjectAlternativeNames
     * setSubjectAlternativeNames} or {@link #addSubjectAlternativeName
     * addSubjectAlternativeName} methods. If {@code true},
     * the {@code X509Certificate} must contain all of the
     * specified subject alternative names. If {@code false}, the
     * {@code X509Certificate} must contain at least one of the
     * specified subject alternative names.
     *
     * @return {@code true} if the flag is enabled;
     * {@code false} if the flag is disabled. The flag is
     * {@code true} by default.
     * @see #setMatchAllSubjectAltNames
     */
    public boolean getMatchAllSubjectAltNames() {
        return matchAllSubjectAltNames;
    }

    public Collection<List<?>> getSubjectAlternativeNames() {
        if (subjectAlternativeNames == null) {
            return null;
        }
        return cloneNames(subjectAlternativeNames);
    }

    /**
     * Clone an object of the form passed to
     * setSubjectAlternativeNames and setPathToNames.
     * Throw a {@code RuntimeException} if the argument is malformed.
     * <p>
     * This method wraps cloneAndCheckNames, changing any
     * {@code IOException} into a {@code RuntimeException}. This
     * method should be used when the object being
     * cloned has already been checked, so there should never be any exceptions.
     *
     * @param names a {@code Collection} with one entry per name.
     *              Each entry is a {@code List} whose first entry
     *              is an Integer (the name type, 0-8) and whose second
     *              entry is a String or a byte array (the name, in
     *              string or ASN.1 DER encoded form, respectively).
     *              There can be multiple names of the same type. Null
     *              is not an acceptable value.
     * @return a deep copy of the specified {@code Collection}
     * @throws RuntimeException if a parsing error occurs
     */
    private static Set<List<?>> cloneNames(Collection<List<?>> names) {
        try {
            return cloneAndCheckNames(names);
        } catch (IOException e) {
            throw new RuntimeException("cloneNames encountered IOException: " +
                    e.getMessage());
        }
    }

    /**
     * Clone and check an argument of the form passed to
     * setSubjectAlternativeNames and setPathToNames.
     * Throw an {@code IOException} if the argument is malformed.
     *
     * @param names a {@code Collection} with one entry per name.
     *              Each entry is a {@code List} whose first entry
     *              is an Integer (the name type, 0-8) and whose second
     *              entry is a String or a byte array (the name, in
     *              string or ASN.1 DER encoded form, respectively).
     *              There can be multiple names of the same type.
     *              {@code null} is not an acceptable value.
     * @return a deep copy of the specified {@code Collection}
     * @throws IOException if a parsing error occurs
     */
    private static Set<List<?>> cloneAndCheckNames(Collection<List<?>> names) throws IOException {
        // Copy the Lists and Collection
        Set<List<?>> namesCopy = new HashSet<List<?>>();
        for (List<?> o : names)
        {
            namesCopy.add(new ArrayList<Object>(o));
        }

        // Check the contents of the Lists and clone any byte arrays
        for (List<?> list : namesCopy) {
            @SuppressWarnings("unchecked") // See javadoc for parameter "names".
                    List<Object> nameList = (List<Object>)list;
            if (nameList.size() != 2) {
                throw new IOException("name list size not 2");
            }
            Object o = nameList.get(0);
            if (!(o instanceof Integer)) {
                throw new IOException("expected an Integer");
            }
            int nameType = ((Integer)o).intValue();
            if ((nameType < 0) || (nameType > 8)) {
                throw new IOException("name type not 0-8");
            }
            Object nameObject = nameList.get(1);
            if (!(nameObject instanceof byte[]) &&
                    !(nameObject instanceof String)) {
                throw new IOException("name not byte array or String");
            }
            if (nameObject instanceof byte[]) {
                nameList.set(1, ((byte[]) nameObject).clone());
            }
        }
        return namesCopy;
    }

    public byte[] getNameConstraints() {
        if (ncBytes == null) {
            return null;
        } else {
            return ncBytes.clone();
        }
    }

    /**
     * Returns the basic constraints constraint. If the value is greater than
     * or equal to zero, the {@code X509Certificates} must include a
     * basicConstraints extension with a pathLen of at least this value.
     * If the value is -2, only end-entity certificates are accepted. If
     * the value is -1, no basicConstraints check is done.
     *
     * @return the value for the basic constraints constraint
     * @see #setBasicConstraints
     */
    public int getBasicConstraints() {
        return basicConstraints;
    }

    /**
     * Returns the policy criterion. The {@code X509Certificate} must
     * include at least one of the specified policies in its certificate policies
     * extension. If the {@code Set} returned is empty, then the
     * {@code X509Certificate} must include at least some specified policy
     * in its certificate policies extension. If the {@code Set} returned is
     * {@code null}, no policy check will be performed.
     *
     * @return an immutable {@code Set} of certificate policy OIDs in
     *         string format (or {@code null})
     * @see #setPolicy
     */
    public Set<String> getPolicy() {
        return policySet;
    }

    public Collection<List<?>> getPathToNames() {
        if (pathToNames == null) {
            return null;
        }
        return cloneNames(pathToNames);
    }

    /**
     * Return a printable representation of the {@code TCertSelector}.
     *
     * @return a {@code String} describing the contents of the
     *         {@code TCertSelector}
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("TX509CertSelector: [\n");
        if (x509Cert != null) {
            sb.append("  Certificate: " + x509Cert.toString() + "\n");
        }
        if (serialNumber != null) {
            sb.append("  Serial Number: " + serialNumber.toString() + "\n");
        }
        if (issuer != null) {
            sb.append("  Issuer: " + getIssuerAsString() + "\n");
        }
        if (subject != null) {
            sb.append("  Subject: " + getSubjectAsString() + "\n");
        }
        sb.append("  matchAllSubjectAltNames flag: "
                + String.valueOf(matchAllSubjectAltNames) + "\n");
        if (subjectAlternativeNames != null) {
            sb.append("  SubjectAlternativeNames:\n");
            Iterator<List<?>> i = subjectAlternativeNames.iterator();
            while (i.hasNext()) {
                List<?> list = i.next();
                sb.append("    type " + list.get(0) +
                        ", name " + list.get(1) + "\n");
            }
        }
        if (subjectKeyID != null) {
            THexDumpEncoder enc = new THexDumpEncoder();
            sb.append("  Subject Key Identifier: " +
                    enc.encodeBuffer(subjectKeyID) + "\n");
        }
        if (authorityKeyID != null) {
            THexDumpEncoder enc = new THexDumpEncoder();
            sb.append("  Authority Key Identifier: " +
                    enc.encodeBuffer(authorityKeyID) + "\n");
        }
        if (certificateValid != null) {
            sb.append("  Certificate Valid: " +
                    certificateValid.toString() + "\n");
        }
        if (privateKeyValid != null) {
            sb.append("  Private Key Valid: " +
                    privateKeyValid.toString() + "\n");
        }
        if (subjectPublicKeyAlgID != null) {
            sb.append("  Subject Public Key AlgID: " +
                    subjectPublicKeyAlgID.toString() + "\n");
        }
        if (subjectPublicKey != null) {
            sb.append("  Subject Public Key: " +
                    subjectPublicKey.toString() + "\n");
        }
        if (keyUsage != null) {
            sb.append("  Key Usage: " + keyUsageToString(keyUsage) + "\n");
        }
        if (keyPurposeSet != null) {
            sb.append("  Extended Key Usage: " +
                    keyPurposeSet.toString() + "\n");
        }
        if (policy != null) {
            sb.append("  Policy: " + policy.toString() + "\n");
        }
        if (pathToGeneralNames != null) {
            sb.append("  Path to names:\n");
            Iterator<TGeneralNameInterface> i = pathToGeneralNames.iterator();
            while (i.hasNext()) {
                sb.append("    " + i.next() + "\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // Copied from sun.security.x509.KeyUsageExtension
    // (without calling the superclass)
    /**
     * Returns a printable representation of the KeyUsage.
     */
    private static String keyUsageToString(boolean[] k) {
        String s = "KeyUsage [\n";
        try {
            if (k[0]) {
                s += "  DigitalSignature\n";
            }
            if (k[1]) {
                s += "  Non_repudiation\n";
            }
            if (k[2]) {
                s += "  Key_Encipherment\n";
            }
            if (k[3]) {
                s += "  Data_Encipherment\n";
            }
            if (k[4]) {
                s += "  Key_Agreement\n";
            }
            if (k[5]) {
                s += "  Key_CertSign\n";
            }
            if (k[6]) {
                s += "  Crl_Sign\n";
            }
            if (k[7]) {
                s += "  Encipher_Only\n";
            }
            if (k[8]) {
                s += "  Decipher_Only\n";
            }
        } catch (ArrayIndexOutOfBoundsException ex) {}

        s += "]\n";

        return (s);
    }

    /**
     * Returns an Extension object given any X509Certificate and extension oid.
     * Throw an {@code IOException} if the extension byte value is
     * malformed.
     *
     * @param cert a {@code X509Certificate}
     * @param extId an {@code integer} which specifies the extension index.
     * Currently, the supported extensions are as follows:
     * index 0 - PrivateKeyUsageExtension
     * index 1 - SubjectAlternativeNameExtension
     * index 2 - NameConstraintsExtension
     * index 3 - CertificatePoliciesExtension
     * index 4 - ExtendedKeyUsageExtension
     * @return an {@code Extension} object whose real type is as specified
     * by the extension oid.
     * @throws IOException if cannot construct the {@code Extension}
     * object with the extension encoding retrieved from the passed in
     * {@code X509Certificate}.
     */
    private static TExtension getExtensionObject(TX509Certificate cert, int extId)
            throws IOException {
        if (cert instanceof TX509CertImpl) {
            TX509CertImpl impl = (TX509CertImpl)cert;
            switch (extId) {
                case PRIVATE_KEY_USAGE_ID:
                    return impl.getPrivateKeyUsageExtension();
                case SUBJECT_ALT_NAME_ID:
                    return impl.getSubjectAlternativeNameExtension();
                case NAME_CONSTRAINTS_ID:
                    return impl.getNameConstraintsExtension();
                case CERT_POLICIES_ID:
                    return impl.getCertificatePoliciesExtension();
                case EXTENDED_KEY_USAGE_ID:
                    return impl.getExtendedKeyUsageExtension();
                default:
                    return null;
            }
        }
        byte[] rawExtVal = cert.getExtensionValue(EXTENSION_OIDS[extId]);
        if (rawExtVal == null) {
            return null;
        }
        TDerInputStream in = new TDerInputStream(rawExtVal);
        byte[] encoded = in.getOctetString();
        switch (extId) {
            case PRIVATE_KEY_USAGE_ID:
                try {
                    return new TPrivateKeyUsageExtension(FALSE, encoded);
                } catch (TCertificateException ex) {
                    throw new IOException(ex.getMessage());
                }
            case SUBJECT_ALT_NAME_ID:
                return new TSubjectAlternativeNameExtension(FALSE, encoded);
            case NAME_CONSTRAINTS_ID:
                return new TNameConstraintsExtension(FALSE, encoded);
            case CERT_POLICIES_ID:
                return new TCertificatePoliciesExtension(FALSE, encoded);
            case EXTENDED_KEY_USAGE_ID:
                return new TExtendedKeyUsageExtension(FALSE, encoded);
            default:
                return null;
        }
    }

    /**
     * Decides whether a {@code Certificate} should be selected.
     *
     * @param cert the {@code Certificate} to be checked
     * @return {@code true} if the {@code Certificate} should be
     *         selected, {@code false} otherwise
     */
    public boolean match(TCertificate cert) {
        if (!(cert instanceof TX509Certificate)) {
            return false;
        }
        TX509Certificate xcert = (TX509Certificate)cert;

        /* match on X509Certificate */
        if (x509Cert != null) {
            if (!x509Cert.equals(xcert)) {
                return false;
            }
        }

        /* match on serial number */
        if (serialNumber != null) {
            if (!serialNumber.equals(xcert.getSerialNumber())) {
                return false;
            }
        }

        /* match on issuer name */
        if (issuer != null) {
            if (!issuer.equals(xcert.getIssuerX500Principal())) {
                return false;
            }
        }

        /* match on subject name */
        if (subject != null) {
            if (!subject.equals(xcert.getSubjectX500Principal())) {
                return false;
            }
        }

        /* match on certificate validity range */
        if (certificateValid != null) {
            try {
                xcert.checkValidity(certificateValid);
            } catch (TCertificateException e) {
                return false;
            }
        }

        /* match on subject public key */
        if (subjectPublicKeyBytes != null) {
            byte[] certKey = xcert.getPublicKey().getEncoded();
            if (!Arrays.equals(subjectPublicKeyBytes, certKey)) {
                return false;
            }
        }

        boolean result = matchBasicConstraints(xcert)
                && matchKeyUsage(xcert)
                && matchExtendedKeyUsage(xcert)
                && matchSubjectKeyID(xcert)
                && matchAuthorityKeyID(xcert)
                && matchPrivateKeyValid(xcert)
                && matchSubjectPublicKeyAlgID(xcert)
                && matchPolicy(xcert)
                && matchSubjectAlternativeNames(xcert)
                && matchPathToNames(xcert)
                && matchNameConstraints(xcert);

        return result;
    }

    /* match on subject key identifier extension value */
    private boolean matchSubjectKeyID(TX509Certificate xcert) {
        if (subjectKeyID == null) {
            return true;
        }
        try {
            byte[] extVal = xcert.getExtensionValue(TString.wrap("2.5.29.14"));
            if (extVal == null) {
                return false;
            }
            TDerInputStream in = new TDerInputStream(extVal);
            byte[] certSubjectKeyID = in.getOctetString();
            if (certSubjectKeyID == null ||
                    !Arrays.equals(subjectKeyID, certSubjectKeyID)) {
                return false;
            }
        } catch (TIOException ex) {
            return false;
        }
        return true;
    }

    /* match on authority key identifier extension value */
    private boolean matchAuthorityKeyID(TX509Certificate xcert) {
        if (authorityKeyID == null) {
            return true;
        }
        try {
            byte[] extVal = xcert.getExtensionValue(TString.wrap("2.5.29.35"));
            if (extVal == null) {
                return false;
            }
            TDerInputStream in = new TDerInputStream(extVal);
            byte[] certAuthKeyID = in.getOctetString();
            if (certAuthKeyID == null ||
                    !Arrays.equals(authorityKeyID, certAuthKeyID)) {
                return false;
            }
        } catch (TIOException ex) {
            return false;
        }
        return true;
    }

    /* match on private key usage range */
    private boolean matchPrivateKeyValid(TX509Certificate xcert) {
        if (privateKeyValid == null) {
            return true;
        }
        TPrivateKeyUsageExtension ext = null;
        try {
            ext = (TPrivateKeyUsageExtension)
                    getExtensionObject(xcert, PRIVATE_KEY_USAGE_ID);
            if (ext != null) {
                ext.valid(privateKeyValid);
            }
        } catch (TCertificateExpiredException e1) {
            return false;
        } catch (TCertificateNotYetValidException e2) {
            return false;
        } catch (IOException e4) {
            return false;
        }
        return true;
    }

    /* match on subject public key algorithm OID */
    private boolean matchSubjectPublicKeyAlgID(TX509Certificate xcert) {
        if (subjectPublicKeyAlgID == null) {
            return true;
        }
        try {
            byte[] encodedKey = xcert.getPublicKey().getEncoded();
            TDerValue val = new TDerValue(encodedKey);

            TAlgorithmId algID = TAlgorithmId.parse(val.data.getDerValue());
            if (!subjectPublicKeyAlgID.equals((Object)algID.getOID())) {
                return false;
            }
        } catch (TIOException e5) {
            return false;
        }
        return true;
    }

    /* match on key usage extension value */
    private boolean matchKeyUsage(TX509Certificate xcert) {
        if (keyUsage == null) {
            return true;
        }
        boolean[] certKeyUsage = xcert.getKeyUsage();
        if (certKeyUsage != null) {
            for (int keyBit = 0; keyBit < keyUsage.length; keyBit++) {
                if (keyUsage[keyBit] &&
                        ((keyBit >= certKeyUsage.length) || !certKeyUsage[keyBit])) {
                    return false;
                }
            }
        }
        return true;
    }

    /* match on extended key usage purpose OIDs */
    private boolean matchExtendedKeyUsage(TX509Certificate xcert) {
        if ((keyPurposeSet == null) || keyPurposeSet.isEmpty()) {
            return true;
        }
        try {
            TExtendedKeyUsageExtension ext =
                    (TExtendedKeyUsageExtension)getExtensionObject(xcert,
                            EXTENDED_KEY_USAGE_ID);
            if (ext != null) {
                Vector<TObjectIdentifier> certKeyPurposeVector =
                        ext.get(TString.wrap(TExtendedKeyUsageExtension.USAGES));
                if (!certKeyPurposeVector.contains(ANY_EXTENDED_KEY_USAGE)
                        && !certKeyPurposeVector.containsAll(keyPurposeOIDSet)) {
                    return false;
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    /* match on subject alternative name extension names */
    private boolean matchSubjectAlternativeNames(TX509Certificate xcert) {
        if ((subjectAlternativeNames == null) || subjectAlternativeNames.isEmpty()) {
            return true;
        }
        try {
            TSubjectAlternativeNameExtension sanExt =
                    (TSubjectAlternativeNameExtension) getExtensionObject(xcert,
                            SUBJECT_ALT_NAME_ID);
            if (sanExt == null) {
                return false;
            }
            TGeneralNames certNames =
                    sanExt.get(TSubjectAlternativeNameExtension.SUBJECT_NAME);
            Iterator<TGeneralNameInterface> i =
                    subjectAlternativeGeneralNames.iterator();
            while (i.hasNext()) {
                TGeneralNameInterface matchName = i.next();
                boolean found = false;
                for (Iterator<TGeneralName> t = certNames.iterator();
                     t.hasNext() && !found; ) {
                    TGeneralNameInterface certName = (t.next()).getName();
                    found = certName.equals(matchName);
                }
                if (!found && (matchAllSubjectAltNames || !i.hasNext())) {
                    return false;
                } else if (found && !matchAllSubjectAltNames) {
                    break;
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    /* match on name constraints */
    private boolean matchNameConstraints(TX509Certificate xcert) {
        if (nc == null) {
            return true;
        }
        try {
            if (!nc.verify(xcert)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /* match on policy OIDs */
    private boolean matchPolicy(TX509Certificate xcert) {
        if (policy == null) {
            return true;
        }
        try {
            TCertificatePoliciesExtension ext = (TCertificatePoliciesExtension)
                    getExtensionObject(xcert, CERT_POLICIES_ID);
            if (ext == null) {
                return false;
            }
            List<TPolicyInformation> policies = ext.get(TCertificatePoliciesExtension.POLICIES);
            /*
             * Convert the Vector of PolicyInformation to a Vector
             * of CertificatePolicyIds for easier comparison.
             */
            List<TCertificatePolicyId> policyIDs = new ArrayList<TCertificatePolicyId>(policies.size());
            for (TPolicyInformation info : policies) {
                policyIDs.add(info.getPolicyIdentifier());
            }
            if (policy != null) {
                boolean foundOne = false;
                /*
                 * if the user passes in an empty policy Set, then
                 * we just want to make sure that the candidate certificate
                 * has some policy OID in its CertPoliciesExtension
                 */
                if (policy.getCertPolicyIds().isEmpty()) {
                    if (policyIDs.isEmpty()) {
                        return false;
                    }
                } else {
                    for (TCertificatePolicyId id : policy.getCertPolicyIds()) {
                        if (policyIDs.contains(id)) {
                            foundOne = true;
                            break;
                        }
                    }
                    if (!foundOne) {
                        return false;
                    }
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    /* match on pathToNames */
    private boolean matchPathToNames(TX509Certificate xcert) {
        if (pathToGeneralNames == null) {
            return true;
        }
        try {
            TNameConstraintsExtension ext = (TNameConstraintsExtension)
                    getExtensionObject(xcert, NAME_CONSTRAINTS_ID);
            if (ext == null) {
                return true;
            }

            TGeneralSubtrees permitted =
                    ext.get(TString.wrap(TNameConstraintsExtension.PERMITTED_SUBTREES));
            TGeneralSubtrees excluded =
                    ext.get(TString.wrap(TNameConstraintsExtension.EXCLUDED_SUBTREES));
            if (excluded != null) {
                if (matchExcluded(excluded) == false) {
                    return false;
                }
            }
            if (permitted != null) {
                if (matchPermitted(permitted) == false) {
                    return false;
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    private boolean matchExcluded(TGeneralSubtrees excluded) {
        /*
         * Enumerate through excluded and compare each entry
         * to all pathToNames. If any pathToName is within any of the
         * subtrees listed in excluded, return false.
         */
        for (Iterator<TGeneralSubtree> t = excluded.iterator(); t.hasNext(); ) {
            TGeneralSubtree tree = t.next();
            TGeneralNameInterface excludedName = tree.getName().getName();
            Iterator<TGeneralNameInterface> i = pathToGeneralNames.iterator();
            while (i.hasNext()) {
                TGeneralNameInterface pathToName = i.next();
                if (excludedName.getType() == pathToName.getType()) {
                    switch (pathToName.constrains(excludedName)) {
                        case TGeneralNameInterface.NAME_WIDENS:
                        case TGeneralNameInterface.NAME_MATCH:
                            return false;
                        default:
                    }
                }
            }
        }
        return true;
    }

    private boolean matchPermitted(TGeneralSubtrees permitted) {
        /*
         * Enumerate through pathToNames, checking that each pathToName
         * is in at least one of the subtrees listed in permitted.
         * If not, return false. However, if no subtrees of a given type
         * are listed, all names of that type are permitted.
         */
        Iterator<TGeneralNameInterface> i = pathToGeneralNames.iterator();
        while (i.hasNext()) {
            TGeneralNameInterface pathToName = i.next();
            Iterator<TGeneralSubtree> t = permitted.iterator();
            boolean permittedNameFound = false;
            boolean nameTypeFound = false;
            String names = "";
            while (t.hasNext() && !permittedNameFound) {
                TGeneralSubtree tree = t.next();
                TGeneralNameInterface permittedName = tree.getName().getName();
                if (permittedName.getType() == pathToName.getType()) {
                    nameTypeFound = true;
                    names = names + "  " + permittedName;
                    switch (pathToName.constrains(permittedName)) {
                        case TGeneralNameInterface.NAME_WIDENS:
                        case TGeneralNameInterface.NAME_MATCH:
                            permittedNameFound = true;
                            break;
                        default:
                    }
                }
            }
            if (!permittedNameFound && nameTypeFound) {
                return false;
            }
        }
        return true;
    }

    /* match on basic constraints */
    private boolean matchBasicConstraints(TX509Certificate xcert) {
        if (basicConstraints == -1) {
            return true;
        }
        int maxPathLen = xcert.getBasicConstraints();
        if (basicConstraints == -2) {
            if (maxPathLen != -1) {
                return false;
            }
        } else {
            if (maxPathLen < basicConstraints) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked") // Safe casts assuming clone() works correctly
    private static <T> Set<T> cloneSet(Set<T> set) {
        if (set instanceof HashSet) {
            Object clone = ((HashSet<T>)set).clone();
            return (Set<T>)clone;
        } else {
            return new HashSet<T>(set);
        }
    }

    /**
     * Returns a copy of this object.
     *
     * @return the copy
     */
    public Object clone() {
        try {
            TX509CertSelector copy = (TX509CertSelector)super.clone();
            // Must clone these because addPathToName et al. modify them
            if (subjectAlternativeNames != null) {
                copy.subjectAlternativeNames =
                        cloneSet(subjectAlternativeNames);
                copy.subjectAlternativeGeneralNames =
                        cloneSet(subjectAlternativeGeneralNames);
            }
            if (pathToGeneralNames != null) {
                copy.pathToNames = cloneSet(pathToNames);
                copy.pathToGeneralNames = cloneSet(pathToGeneralNames);
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            /* Cannot happen */
            throw new InternalError(e.toString(), e);
        }
    }
}
