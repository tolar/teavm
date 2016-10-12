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
package org.teavm.classlib.sun.security.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.x500.X500Principal;

import org.teavm.classlib.java.io.TBufferedInputStream;
import org.teavm.classlib.java.io.TBufferedReader;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.io.TInputStreamReader;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.security.TInvalidKeyException;
import org.teavm.classlib.java.security.TNoSuchAlgorithmException;
import org.teavm.classlib.java.security.TNoSuchProviderException;
import org.teavm.classlib.java.security.TPrincipal;
import org.teavm.classlib.java.security.TProvider;
import org.teavm.classlib.java.security.TPublicKey;
import org.teavm.classlib.java.security.TSignature;
import org.teavm.classlib.java.security.TSignatureException;
import org.teavm.classlib.java.security.cert.TCertificate;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.security.cert.TCertificateParsingException;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.java.util.TIterator;
import org.teavm.classlib.javax.auth.x500.TX500Principal;
import org.teavm.classlib.sun.misc.THexDumpEncoder;
import org.teavm.classlib.sun.security.cert.TCertificateEncodingException;
import org.teavm.classlib.sun.security.provider.TX509Factory;
import org.teavm.classlib.sun.security.util.TDerEncoder;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TX509CertImpl extends TX509Certificate implements TDerEncoder {

    private static final long serialVersionUID = -3457612960190864406L;
    private static final String DOT = ".";
    public static final String NAME = "x509";
    public static final String INFO = "info";
    public static final String ALG_ID = "algorithm";
    public static final String SIGNATURE = "signature";
    public static final String SIGNED_CERT = "signed_cert";
    public static final String SUBJECT_DN = "x509.info.subject.dname";
    public static final String ISSUER_DN = "x509.info.issuer.dname";
    public static final String SERIAL_ID = "x509.info.serialNumber.number";
    public static final String PUBLIC_KEY = "x509.info.key.value";
    public static final String VERSION = "x509.info.version.number";
    public static final String SIG_ALG = "x509.algorithm";
    public static final String SIG = "x509.signature";
    private boolean readOnly = false;
    private byte[] signedCert = null;
    protected TX509CertInfo info = null;
    protected TAlgorithmId algId = null;
    protected byte[] signature = null;
    private static final String KEY_USAGE_OID = "2.5.29.15";
    private static final String EXTENDED_KEY_USAGE_OID = "2.5.29.37";
    private static final String BASIC_CONSTRAINT_OID = "2.5.29.19";
    private static final String SUBJECT_ALT_NAME_OID = "2.5.29.17";
    private static final String ISSUER_ALT_NAME_OID = "2.5.29.18";
    private static final String AUTH_INFO_ACCESS_OID = "1.3.6.1.5.5.7.1.1";
    private static final int NUM_STANDARD_KEY_USAGE = 9;
    private Collection<List<?>> subjectAlternativeNames;
    private Collection<List<?>> issuerAlternativeNames;
    private List<String> extKeyUsage;
    private Set<TAccessDescription> authInfoAccess;
    private TPublicKey verifiedPublicKey;
    private TString verifiedProvider;
    private boolean verificationResult;
    private ConcurrentHashMap<String, String> fingerprints = new ConcurrentHashMap(2);

    public TX509CertImpl() {
    }

    public TX509CertImpl(byte[] var1) throws TCertificateException {
        try {
            this.parse(new TDerValue(var1));
        } catch (TIOException var3) {
            this.signedCert = null;
            throw new TCertificateException(TString.wrap("Unable to initialize, " + var3), var3);
        }
    }

    public TX509CertImpl(TInputStream var1) throws CertificateException {
        TDerValue var2 = null;
        TBufferedInputStream var3 = new TBufferedInputStream(var1);

        try {
            var3.mark(2147483647);
            var2 = this.readRFC1421Cert(var3);
        } catch (IOException var8) {
            try {
                var3.reset();
                var2 = new TDerValue(var3);
            } catch (TIOException var7) {
                throw new TCertificateException(TString.wrap("Input stream must be either DER-encoded bytes or RFC1421 hex-encoded DER-encoded bytes: " + var7.getMessage()), var7);
            }
        }

        try {
            this.parse(var2);
        } catch (TIOException var6) {
            this.signedCert = null;
            throw new TCertificateException(TString.wrap("Unable to parse DER value of certificate, " + var6), var6);
        }
    }

    private TDerValue readRFC1421Cert(TInputStream var1) throws IOException {
        TDerValue var2 = null;
        TString var3 = null;
        TBufferedReader var4 = new TBufferedReader(new TInputStreamReader(var1, TString.wrap("ASCII")));

        try {
            var3 = var4.readLine();
        } catch (TIOException var7) {
            throw new TIOException(TString.wrap("Unable to read InputStream: " + var7.getMessage()));
        }

        if(!var3.equals(TString.wrap("-----BEGIN CERTIFICATE-----"))) {
            throw new IOException("InputStream is not RFC1421 hex-encoded DER bytes");
        } else {
            ByteArrayOutputStream var5 = new ByteArrayOutputStream();

            try {
                while((var3 = var4.readLine()) != null) {
                    if(var3.equals(TString.wrap("-----END CERTIFICATE-----"))) {
                        var2 = new TDerValue(var5.toByteArray());
                        break;
                    }

                    var5.write(Base64.getMimeDecoder().decode(var3));
                }

                return var2;
            } catch (IOException var8) {
                throw new IOException("Unable to read InputStream: " + var8.getMessage());
            }
        }
    }

    public TX509CertImpl(TX509CertInfo var1) {
        this.info = var1;
    }

    public TX509CertImpl(TDerValue var1) throws TCertificateException {
        try {
            this.parse(var1);
        } catch (TIOException var3) {
            this.signedCert = null;
            throw new TCertificateException(TString.wrap("Unable to initialize, " + var3), var3);
        }
    }

    public void encode(OutputStream var1) throws CertificateEncodingException {
        if(this.signedCert == null) {
            throw new CertificateEncodingException("Null certificate to encode");
        } else {
            try {
                var1.write((byte[])this.signedCert.clone());
            } catch (IOException var3) {
                throw new CertificateEncodingException(var3.toString());
            }
        }
    }

    public void derEncode(TOutputStream var1) throws TIOException {
        if(this.signedCert == null) {
            throw new TIOException(TString.wrap("Null certificate to encode"));
        } else {
            var1.write((byte[])this.signedCert.clone());
        }
    }

    public byte[] getEncoded() throws TCertificateEncodingException {
        return (byte[])this.getEncodedInternal().clone();
    }

    public byte[] getEncodedInternal() throws TCertificateEncodingException {
        if(this.signedCert == null) {
            throw new TCertificateEncodingException(TString.wrap("Null certificate to encode"));
        } else {
            return this.signedCert;
        }
    }

    public void verify(TPublicKey var1) throws TCertificateException, TNoSuchAlgorithmException, TInvalidKeyException,
            TNoSuchProviderException, TSignatureException {
        this.verify(var1, "");
    }

    public synchronized void verify(TPublicKey var1, TString var2) throws TCertificateException, TNoSuchAlgorithmException, TInvalidKeyException, TNoSuchProviderException, TSignatureException {
        if(var2 == null) {
            var2 = TString.wrap("");
        }

        if(this.verifiedPublicKey != null && this.verifiedPublicKey.equals(var1) && var2.equals(this.verifiedProvider)) {
            if(!this.verificationResult) {
                throw new TSignatureException(TString.wrap("Signature does not match."));
            }
        } else if(this.signedCert == null) {
            throw new TCertificateEncodingException(TString.wrap("Uninitialized certificate"));
        } else {
            TSignature var3 = null;
            if(var2.length() == 0) {
                var3 = TSignature.getInstance(this.algId.getName());
            } else {
                var3 = TSignature.getInstance(this.algId.getName(), var2);
            }

            var3.initVerify(var1);
            byte[] var4 = this.info.getEncodedInfo();
            var3.update(var4, 0, var4.length);
            this.verificationResult = var3.verify(this.signature);
            this.verifiedPublicKey = var1;
            this.verifiedProvider = var2;
            if(!this.verificationResult) {
                throw new TSignatureException(TString.wrap("Signature does not match."));
            }
        }
    }

    public synchronized void verify(TPublicKey var1, TProvider var2) throws TCertificateException, TNoSuchAlgorithmException, TInvalidKeyException, TSignatureException {
        if(this.signedCert == null) {
            throw new TCertificateEncodingException(TString.wrap("Uninitialized certificate"));
        } else {
            TSignature var3 = null;
            if(var2 == null) {
                var3 = TSignature.getInstance(this.algId.getName());
            } else {
                var3 = TSignature.getInstance(this.algId.getName(), var2);
            }

            var3.initVerify(var1);
            byte[] var4 = this.info.getEncodedInfo();
            var3.update(var4, 0, var4.length);
            this.verificationResult = var3.verify(this.signature);
            this.verifiedPublicKey = var1;
            if(!this.verificationResult) {
                throw new SignatureException("Signature does not match.");
            }
        }
    }

    public static void verify(X509Certificate var0, PublicKey var1, Provider var2) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        var0.verify(var1, var2);
    }

    public void sign(PrivateKey var1, String var2) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.sign(var1, var2, (String)null);
    }

    public void sign(PrivateKey var1, String var2, String var3) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        try {
            if(this.readOnly) {
                throw new CertificateEncodingException("cannot over-write existing certificate");
            } else {
                Signature var4 = null;
                if(var3 != null && var3.length() != 0) {
                    var4 = Signature.getInstance(var2, var3);
                } else {
                    var4 = Signature.getInstance(var2);
                }

                var4.initSign(var1);
                this.algId = TAlgorithmId.get(var4.getAlgorithm());
                TDerOutputStream var5 = new TDerOutputStream();
                TDerOutputStream var6 = new TDerOutputStream();
                this.info.encode(var6);
                byte[] var7 = var6.toByteArray();
                this.algId.encode(var6);
                var4.update(var7, 0, var7.length);
                this.signature = var4.sign();
                var6.putBitString(this.signature);
                var5.write((byte)48, var6);
                this.signedCert = var5.toByteArray();
                this.readOnly = true;
            }
        } catch (IOException var8) {
            throw new CertificateEncodingException(var8.toString());
        }
    }

    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        Date var1 = new Date();
        this.checkValidity(var1);
    }

    public void checkValidity(Date var1) throws CertificateExpiredException, CertificateNotYetValidException {
        TCertificateValidity var2 = null;

        try {
            var2 = (TCertificateValidity)this.info.get(TString.wrap("validity"));
        } catch (Exception var4) {
            throw new CertificateNotYetValidException("Incorrect validity period");
        }

        if(var2 == null) {
            throw new CertificateNotYetValidException("Null validity period");
        } else {
            var2.valid(var1);
        }
    }

    public Object get(TString var1) throws CertificateParsingException {
        TX509AttributeName var2 = new TX509AttributeName(var1);
        TString var3 = var2.getPrefix();
        if(!var3.equalsIgnoreCase(TString.wrap("x509"))) {
            throw new CertificateParsingException("Invalid root of attribute name, expected [x509], received [" + var3 + "]");
        } else {
            var2 = new TX509AttributeName(var2.getSuffix());
            var3 = var2.getPrefix();
            if(var3.equalsIgnoreCase(TString.wrap("info"))) {
                if(this.info == null) {
                    return null;
                } else if(var2.getSuffix() != null) {
                    try {
                        return this.info.get(var2.getSuffix());
                    } catch (TIOException var5) {
                        throw new CertificateParsingException(var5.toString());
                    } catch (TCertificateException var6) {
                        throw new CertificateParsingException(var6.toString());
                    }
                } else {
                    return this.info;
                }
            } else if(var3.equalsIgnoreCase(TString.wrap("algorithm"))) {
                return this.algId;
            } else if(var3.equalsIgnoreCase(TString.wrap("signature"))) {
                return this.signature != null?this.signature.clone():null;
            } else if(var3.equalsIgnoreCase(TString.wrap("signed_cert"))) {
                return this.signedCert != null?this.signedCert.clone():null;
            } else {
                throw new CertificateParsingException("Attribute name not recognized or get() not allowed for the same: " + var3);
            }
        }
    }

    public void set(TString var1, Object var2) throws CertificateException, IOException {
        if(this.readOnly) {
            throw new CertificateException("cannot over-write existing certificate");
        } else {
            TX509AttributeName var3 = new TX509AttributeName(var1);
            TString var4 = var3.getPrefix();
            if(!var4.equalsIgnoreCase(TString.wrap("x509"))) {
                throw new CertificateException("Invalid root of attribute name, expected [x509], received " + var4);
            } else {
                var3 = new TX509AttributeName(var3.getSuffix());
                var4 = var3.getPrefix();
                if(var4.equalsIgnoreCase(TString.wrap("info"))) {
                    if(var3.getSuffix() == null) {
                        if(!(var2 instanceof TX509CertInfo)) {
                            throw new CertificateException("Attribute value should be of type TX509CertInfo.");
                        }

                        this.info = (TX509CertInfo)var2;
                        this.signedCert = null;
                    } else {
                        this.info.set(var3.getSuffix(), var2);
                        this.signedCert = null;
                    }

                } else {
                    throw new CertificateException("Attribute name not recognized or set() not allowed for the same: " + var4);
                }
            }
        }
    }

    public void delete(TString var1) throws CertificateException, IOException {
        if(this.readOnly) {
            throw new CertificateException("cannot over-write existing certificate");
        } else {
            TX509AttributeName var2 = new TX509AttributeName(var1);
            TString var3 = var2.getPrefix();
            if(!var3.equalsIgnoreCase(TString.wrap("x509"))) {
                throw new CertificateException("Invalid root of attribute name, expected [x509], received " + var3);
            } else {
                var2 = new TX509AttributeName(var2.getSuffix());
                var3 = var2.getPrefix();
                if(var3.equalsIgnoreCase(TString.wrap("info"))) {
                    if(var2.getSuffix() != null) {
                        this.info = null;
                    } else {
                        this.info.delete(var2.getSuffix());
                    }
                } else if(var3.equalsIgnoreCase(TString.wrap("algorithm"))) {
                    this.algId = null;
                } else if(var3.equalsIgnoreCase(TString.wrap("signature"))) {
                    this.signature = null;
                } else {
                    if(!var3.equalsIgnoreCase(TString.wrap("signed_cert"))) {
                        throw new CertificateException("Attribute name not recognized or delete() not allowed for the same: " + var3);
                    }

                    this.signedCert = null;
                }

            }
        }
    }

    public Enumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("x509.info");
        var1.addElement("x509.algorithm");
        var1.addElement("x509.signature");
        var1.addElement("x509.signed_cert");
        return var1.elements();
    }

    public String getName() {
        return "x509";
    }

    public String toString() {
        if(this.info != null && this.algId != null && this.signature != null) {
            StringBuilder var1 = new StringBuilder();
            var1.append("[\n");
            var1.append(this.info.toString() + "\n");
            var1.append("  Algorithm: [" + this.algId.toString() + "]\n");
            THexDumpEncoder var2 = new THexDumpEncoder();
            var1.append("  Signature:\n" + var2.encodeBuffer(this.signature));
            var1.append("\n]");
            return var1.toString();
        } else {
            return "";
        }
    }

    public TPublicKey getPublicKey() {
        if(this.info == null) {
            return null;
        } else {
            try {
                TPublicKey var1 = (TPublicKey)this.info.get(TString.wrap("key.value"));
                return var1;
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public int getVersion() {
        if(this.info == null) {
            return -1;
        } else {
            try {
                int var1 = ((Integer)this.info.get(TString.wrap("version.number"))).intValue();
                return var1 + 1;
            } catch (Exception var2) {
                return -1;
            }
        }
    }

    public TBigInteger getSerialNumber() {
        TSerialNumber var1 = this.getSerialNumberObject();
        return var1 != null?var1.getNumber():null;
    }

    public TSerialNumber getSerialNumberObject() {
        if(this.info == null) {
            return null;
        } else {
            try {
                TSerialNumber var1 = (TSerialNumber)this.info.get(TString.wrap("serialNumber.number"));
                return var1;
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public Principal getSubjectDN() {
        if(this.info == null) {
            return null;
        } else {
            try {
                Principal var1 = (Principal)this.info.get(TString.wrap("subject.dname"));
                return var1;
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public TX500Principal getSubjectX500Principal() {
        if(this.info == null) {
            return null;
        } else {
            try {
                TX500Principal var1 = (TX500Principal)this.info.get(TString.wrap("subject.x500principal"));
                return var1;
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public TPrincipal getIssuerDN() {
        if(this.info == null) {
            return null;
        } else {
            try {
                TPrincipal var1 = (TPrincipal)this.info.get(TString.wrap("issuer.dname"));
                return var1;
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public TX500Principal getIssuerX500Principal() {
        if(this.info == null) {
            return null;
        } else {
            try {
                TX500Principal var1 = (TX500Principal)this.info.get(TString.wrap("issuer.x500principal"));
                return var1;
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public Date getNotBefore() {
        if(this.info == null) {
            return null;
        } else {
            try {
                Date var1 = (Date)this.info.get(TString.wrap("validity.notBefore"));
                return var1;
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public Date getNotAfter() {
        if(this.info == null) {
            return null;
        } else {
            try {
                Date var1 = (Date)this.info.get(TString.wrap("validity.notAfter"));
                return var1;
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public byte[] getTBSCertificate() throws CertificateEncodingException {
        if(this.info != null) {
            return this.info.getEncodedInfo();
        } else {
            throw new CertificateEncodingException("Uninitialized certificate");
        }
    }

    public byte[] getSignature() {
        return this.signature == null?null:(byte[])this.signature.clone();
    }

    public String getSigAlgName() {
        return this.algId == null?null:this.algId.getName();
    }

    public String getSigAlgOID() {
        if(this.algId == null) {
            return null;
        } else {
            TObjectIdentifier var1 = this.algId.getOID();
            return var1.toString();
        }
    }

    public byte[] getSigAlgParams() {
        if(this.algId == null) {
            return null;
        } else {
            try {
                return this.algId.getEncodedParams();
            } catch (TIOException var2) {
                return null;
            }
        }
    }

    public boolean[] getIssuerUniqueID() {
        if(this.info == null) {
            return null;
        } else {
            try {
                TUniqueIdentity var1 = (TUniqueIdentity)this.info.get(TString.wrap("issuerID"));
                return var1 == null?null:var1.getId();
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public boolean[] getSubjectUniqueID() {
        if(this.info == null) {
            return null;
        } else {
            try {
                TUniqueIdentity var1 = (TUniqueIdentity)this.info.get(TString.wrap("subjectID"));
                return var1 == null?null:var1.getId();
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public TKeyIdentifier getAuthKeyId() {
        TAuthorityKeyIdentifierExtension var1 = this.getAuthorityKeyIdentifierExtension();
        if(var1 != null) {
            try {
                return (TKeyIdentifier)var1.get("key_id");
            } catch (TIOException var3) {
                ;
            }
        }

        return null;
    }

    public TKeyIdentifier getSubjectKeyId() {
        TSubjectKeyIdentifierExtension var1 = this.getSubjectKeyIdentifierExtension();
        if(var1 != null) {
            try {
                return var1.get(TString.wrap("key_id"));
            } catch (TIOException var3) {
                ;
            }
        }

        return null;
    }

    public TAuthorityKeyIdentifierExtension getAuthorityKeyIdentifierExtension() {
        return (TAuthorityKeyIdentifierExtension)this.getExtension(TPKIXExtensions.AuthorityKey_Id);
    }

    public TBasicConstraintsExtension getBasicConstraintsExtension() {
        return (TBasicConstraintsExtension)this.getExtension(TPKIXExtensions.BasicConstraints_Id);
    }

    public TCertificatePoliciesExtension getCertificatePoliciesExtension() {
        return (TCertificatePoliciesExtension)this.getExtension(TPKIXExtensions.CertificatePolicies_Id);
    }

    public TExtendedKeyUsageExtension getExtendedKeyUsageExtension() {
        return (TExtendedKeyUsageExtension)this.getExtension(TPKIXExtensions.ExtendedKeyUsage_Id);
    }

    public TIssuerAlternativeNameExtension getIssuerAlternativeNameExtension() {
        return (TIssuerAlternativeNameExtension)this.getExtension(TPKIXExtensions.IssuerAlternativeName_Id);
    }

    public TNameConstraintsExtension getNameConstraintsExtension() {
        return (TNameConstraintsExtension)this.getExtension(TPKIXExtensions.NameConstraints_Id);
    }

    public TPolicyConstraintsExtension getPolicyConstraintsExtension() {
        return (TPolicyConstraintsExtension)this.getExtension(TPKIXExtensions.PolicyConstraints_Id);
    }

    public TPolicyMappingsExtension getPolicyMappingsExtension() {
        return (TPolicyMappingsExtension)this.getExtension(TPKIXExtensions.PolicyMappings_Id);
    }

    public TPrivateKeyUsageExtension getPrivateKeyUsageExtension() {
        return (TPrivateKeyUsageExtension)this.getExtension(TPKIXExtensions.PrivateKeyUsage_Id);
    }

    public TSubjectAlternativeNameExtension getSubjectAlternativeNameExtension() {
        return (TSubjectAlternativeNameExtension)this.getExtension(TPKIXExtensions.SubjectAlternativeName_Id);
    }

    public TSubjectKeyIdentifierExtension getSubjectKeyIdentifierExtension() {
        return (TSubjectKeyIdentifierExtension)this.getExtension(TPKIXExtensions.SubjectKey_Id);
    }

    public TCRLDistributionPointsExtension getCRLDistributionPointsExtension() {
        return (TCRLDistributionPointsExtension)this.getExtension(TPKIXExtensions.CRLDistributionPoints_Id);
    }

    public boolean hasUnsupportedCriticalExtension() {
        if(this.info == null) {
            return false;
        } else {
            try {
                TCertificateExtensions var1 = (TCertificateExtensions)this.info.get(TString.wrap("extensions"));
                return var1 == null?false:var1.hasUnsupportedCriticalExtension();
            } catch (Exception var2) {
                return false;
            }
        }
    }

    public Set<String> getCriticalExtensionOIDs() {
        if(this.info == null) {
            return null;
        } else {
            try {
                TCertificateExtensions var1 = (TCertificateExtensions)this.info.get(TString.wrap("extensions"));
                if(var1 == null) {
                    return null;
                } else {
                    TreeSet var2 = new TreeSet();
                    TIterator var3 = var1.getAllExtensions().iterator();

                    while(var3.hasNext()) {
                        TExtension var4 = (TExtension)var3.next();
                        if(var4.isCritical()) {
                            var2.add(var4.getExtensionId().toString());
                        }
                    }

                    return var2;
                }
            } catch (Exception var5) {
                return null;
            }
        }
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        if(this.info == null) {
            return null;
        } else {
            try {
                TCertificateExtensions var1 = (TCertificateExtensions)this.info.get(TString.wrap("extensions"));
                if(var1 == null) {
                    return null;
                } else {
                    TreeSet var2 = new TreeSet();
                    TIterator var3 = var1.getAllExtensions().iterator();

                    while(var3.hasNext()) {
                        TExtension var4 = (TExtension)var3.next();
                        if(!var4.isCritical()) {
                            var2.add(var4.getExtensionId().toString());
                        }
                    }

                    var2.addAll(var1.getUnparseableExtensions().keySet());
                    return var2;
                }
            } catch (Exception var5) {
                return null;
            }
        }
    }

    public TExtension getExtension(TObjectIdentifier var1) {
        if(this.info == null) {
            return null;
        } else {
            try {
                TCertificateExtensions var2;
                try {
                    var2 = (TCertificateExtensions)this.info.get(TString.wrap("extensions"));
                } catch (TCertificateException var6) {
                    return null;
                }

                if(var2 == null) {
                    return null;
                } else {
                    TExtension var3 = var2.getExtension(var1.toString());
                    if(var3 != null) {
                        return var3;
                    } else {
                        TIterator var4 = var2.getAllExtensions().iterator();

                        TExtension var5;
                        do {
                            if(!var4.hasNext()) {
                                return null;
                            }

                            var5 = (TExtension)var4.next();
                        } while(!var5.getExtensionId().equals(var1));

                        return var5;
                    }
                }
            } catch (TIOException var7) {
                return null;
            }
        }
    }

    public TExtension getUnparseableExtension(TObjectIdentifier var1) {
        if(this.info == null) {
            return null;
        } else {
            try {
                TCertificateExtensions var2;
                try {
                    var2 = (TCertificateExtensions)this.info.get(TString.wrap("extensions"));
                } catch (TCertificateException var4) {
                    return null;
                }

                return var2 == null?null:(TExtension)var2.getUnparseableExtensions().get(var1.toString());
            } catch (TIOException var5) {
                return null;
            }
        }
    }

    public byte[] getExtensionValue(TString var1) {
        try {
            TObjectIdentifier var2 = new TObjectIdentifier(var1);
            TString var3 = TOIDMap.getName(var2);
            TExtension var4 = null;
            TCertificateExtensions var5 = (TCertificateExtensions)this.info.get(TString.wrap("extensions"));
            if(var3 == null) {
                if(var5 == null) {
                    return null;
                }

                TIterator var6 = var5.getAllExtensions().iterator();

                while(var6.hasNext()) {
                    TExtension var7 = (TExtension)var6.next();
                    TObjectIdentifier var8 = var7.getExtensionId();
                    if(var8.equals(var2)) {
                        var4 = var7;
                        break;
                    }
                }
            } else {
                try {
                    var4 = (TExtension)this.get(var3);
                } catch (CertificateException var9) {
                    ;
                }
            }

            if(var4 == null) {
                if(var5 != null) {
                    var4 = (TExtension)var5.getUnparseableExtensions().get(var1);
                }

                if(var4 == null) {
                    return null;
                }
            }

            byte[] var11 = var4.getExtensionValue();
            if(var11 == null) {
                return null;
            } else {
                TDerOutputStream var12 = new TDerOutputStream();
                var12.putOctetString(var11);
                return var12.toByteArray();
            }
        } catch (Exception var10) {
            return null;
        }
    }

    public boolean[] getKeyUsage() {
        try {
            TString var1 = TOIDMap.getName(TPKIXExtensions.KeyUsage_Id);
            if(var1 == null) {
                return null;
            } else {
                TKeyUsageExtension var2 = (TKeyUsageExtension)this.get(var1);
                if(var2 == null) {
                    return null;
                } else {
                    boolean[] var3 = var2.getBits();
                    if(var3.length < 9) {
                        boolean[] var4 = new boolean[9];
                        System.arraycopy(var3, 0, var4, 0, var3.length);
                        var3 = var4;
                    }

                    return var3;
                }
            }
        } catch (Exception var5) {
            return null;
        }
    }

    public synchronized List<String> getExtendedKeyUsage() throws CertificateParsingException {
        if(this.readOnly && this.extKeyUsage != null) {
            return this.extKeyUsage;
        } else {
            TExtendedKeyUsageExtension var1 = this.getExtendedKeyUsageExtension();
            if(var1 == null) {
                return null;
            } else {
                this.extKeyUsage = Collections.unmodifiableList(var1.getExtendedKeyUsage());
                return this.extKeyUsage;
            }
        }
    }

    public static List<String> getExtendedKeyUsage(TX509Certificate var0) throws CertificateParsingException {
        try {
            byte[] var1 = var0.getExtensionValue("2.5.29.37");
            if(var1 == null) {
                return null;
            } else {
                TDerValue var2 = new TDerValue(var1);
                byte[] var3 = var2.getOctetString();
                TExtendedKeyUsageExtension var4 = new TExtendedKeyUsageExtension(Boolean.FALSE, var3);
                return Collections.unmodifiableList(var4.getExtendedKeyUsage());
            }
        } catch (IOException var5) {
            throw new CertificateParsingException(var5);
        }
    }

    public int getBasicConstraints() {
        try {
            TString var1 = TOIDMap.getName(TPKIXExtensions.BasicConstraints_Id);
            if(var1 == null) {
                return -1;
            } else {
                TBasicConstraintsExtension var2 = (TBasicConstraintsExtension)this.get(var1);
                return var2 == null?-1:(((Boolean)var2.get("is_ca")).booleanValue()?((Integer)var2.get("path_len")).intValue():-1);
            }
        } catch (Exception var3) {
            return -1;
        }
    }

    private static Collection<List<?>> makeAltNames(TGeneralNames var0) {
        if(var0.isEmpty()) {
            return Collections.emptySet();
        } else {
            ArrayList var1 = new ArrayList();

            ArrayList var5;
            for(Iterator var2 = var0.names().iterator(); var2.hasNext(); var1.add(Collections.unmodifiableList(var5))) {
                TGeneralName var3 = (TGeneralName)var2.next();
                TGeneralNameInterface var4 = var3.getName();
                var5 = new ArrayList(2);
                var5.add(Integer.valueOf(var4.getType()));
                switch(var4.getType()) {
                    case 1:
                        var5.add(((TRFC822Name)var4).getName());
                        break;
                    case 2:
                        var5.add(((TDNSName)var4).getName());
                        break;
                    case 3:
                    case 5:
                    default:
                        TDerOutputStream var6 = new TDerOutputStream();

                        try {
                            var4.encode(var6);
                        } catch (TIOException var8) {
                            throw new RuntimeException("name cannot be encoded", var8);
                        }

                        var5.add(var6.toByteArray());
                        break;
                    case 4:
                        var5.add(((TX500Name)var4).getRFC2253Name());
                        break;
                    case 6:
                        var5.add(((TURIName)var4).getName());
                        break;
                    case 7:
                        try {
                            var5.add(((TIPAddressName)var4).getName());
                            break;
                        } catch (IOException var9) {
                            throw new RuntimeException("IPAddress cannot be parsed", var9);
                        }
                    case 8:
                        var5.add(((TOIDName)var4).getOID().toString());
                }
            }

            return Collections.unmodifiableCollection(var1);
        }
    }

    private static Collection<List<?>> cloneAltNames(Collection<List<?>> var0) {
        boolean var1 = false;
        Iterator var2 = var0.iterator();

        while(var2.hasNext()) {
            List var3 = (List)var2.next();
            if(var3.get(1) instanceof byte[]) {
                var1 = true;
            }
        }

        if(var1) {
            ArrayList var7 = new ArrayList();
            Iterator var8 = var0.iterator();

            while(var8.hasNext()) {
                List var4 = (List)var8.next();
                Object var5 = var4.get(1);
                if(var5 instanceof byte[]) {
                    ArrayList var6 = new ArrayList(var4);
                    var6.set(1, ((byte[])((byte[])var5)).clone());
                    var7.add(Collections.unmodifiableList(var6));
                } else {
                    var7.add(var4);
                }
            }

            return Collections.unmodifiableCollection(var7);
        } else {
            return var0;
        }
    }

    public synchronized Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        if(this.readOnly && this.subjectAlternativeNames != null) {
            return cloneAltNames(this.subjectAlternativeNames);
        } else {
            TSubjectAlternativeNameExtension var1 = this.getSubjectAlternativeNameExtension();
            if(var1 == null) {
                return null;
            } else {
                TGeneralNames var2;
                try {
                    var2 = var1.get("subject_name");
                } catch (TIOException var4) {
                    return Collections.emptySet();
                }

                this.subjectAlternativeNames = makeAltNames(var2);
                return this.subjectAlternativeNames;
            }
        }
    }

    public static Collection<List<?>> getSubjectAlternativeNames(TX509Certificate var0) throws CertificateParsingException {
        try {
            byte[] var1 = var0.getExtensionValue(TString.wrap("2.5.29.17"));
            if(var1 == null) {
                return null;
            } else {
                TDerValue var2 = new TDerValue(var1);
                byte[] var3 = var2.getOctetString();
                TSubjectAlternativeNameExtension var4 = new TSubjectAlternativeNameExtension(Boolean.FALSE, var3);

                TGeneralNames var5;
                try {
                    var5 = var4.get("subject_name");
                } catch (TIOException var7) {
                    return Collections.emptySet();
                }

                return makeAltNames(var5);
            }
        } catch (IOException var8) {
            throw new CertificateParsingException(var8);
        }
    }

    public synchronized Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        if(this.readOnly && this.issuerAlternativeNames != null) {
            return cloneAltNames(this.issuerAlternativeNames);
        } else {
            TIssuerAlternativeNameExtension var1 = this.getIssuerAlternativeNameExtension();
            if(var1 == null) {
                return null;
            } else {
                TGeneralNames var2;
                try {
                    var2 = var1.get("issuer_name");
                } catch (TIOException var4) {
                    return Collections.emptySet();
                }

                this.issuerAlternativeNames = makeAltNames(var2);
                return this.issuerAlternativeNames;
            }
        }
    }

    public static Collection<List<?>> getIssuerAlternativeNames(X509Certificate var0) throws CertificateParsingException {
        try {
            byte[] var1 = var0.getExtensionValue("2.5.29.18");
            if(var1 == null) {
                return null;
            } else {
                TDerValue var2 = new TDerValue(var1);
                byte[] var3 = var2.getOctetString();
                TIssuerAlternativeNameExtension var4 = new TIssuerAlternativeNameExtension(Boolean.FALSE, var3);

                TGeneralNames var5;
                try {
                    var5 = var4.get("issuer_name");
                } catch (TIOException var7) {
                    return Collections.emptySet();
                }

                return makeAltNames(var5);
            }
        } catch (IOException var8) {
            throw new CertificateParsingException(var8);
        }
    }

    public TAuthorityInfoAccessExtension getAuthorityInfoAccessExtension() {
        return (TAuthorityInfoAccessExtension)this.getExtension(TPKIXExtensions.AuthInfoAccess_Id);
    }

    private void parse(TDerValue var1) throws TCertificateException, TIOException {
        if(this.readOnly) {
            throw new TCertificateParsingException(TString.wrap("cannot over-write existing certificate"));
        } else if(var1.data != null && var1.tag == 48) {
            this.signedCert = var1.toByteArray();
            TDerValue[] var2 = new TDerValue[]{var1.data.getDerValue(), var1.data.getDerValue(), var1.data.getDerValue()};
            if(var1.data.available() != 0) {
                throw new TCertificateParsingException(TString.wrap("signed overrun, bytes = " + var1.data.available()));
            } else if(var2[0].tag != 48) {
                throw new TCertificateParsingException(TString.wrap("signed fields invalid"));
            } else {
                this.algId = TAlgorithmId.parse(var2[1]);
                this.signature = var2[2].getBitString();
                if(var2[1].data.available() != 0) {
                    throw new TCertificateParsingException(TString.wrap("algid field overrun"));
                } else if(var2[2].data.available() != 0) {
                    throw new TCertificateParsingException(TString.wrap("signed fields overrun"));
                } else {
                    this.info = new TX509CertInfo(var2[0]);
                    TAlgorithmId var3 = (TAlgorithmId)this.info.get(TString.wrap("algorithmID.algorithm"));
                    if(!this.algId.equals(var3)) {
                        throw new TCertificateParsingException(TString.wrap("Signature algorithm mismatch"));
                    } else {
                        this.readOnly = true;
                    }
                }
            }
        } else {
            throw new TCertificateParsingException(TString.wrap("invalid DER-encoded certificate data"));
        }
    }

    private static TX500Principal getX500Principal(TX509Certificate var0, boolean var1) throws Exception {
        byte[] var2 = var0.getEncoded();
        TDerInputStream var3 = new TDerInputStream(var2);
        TDerValue var4 = var3.getSequence(3)[0];
        TDerInputStream var5 = var4.data;
        TDerValue var6 = var5.getDerValue();
        if(var6.isContextSpecific((byte)0)) {
            var6 = var5.getDerValue();
        }

        var6 = var5.getDerValue();
        var6 = var5.getDerValue();
        if(!var1) {
            var6 = var5.getDerValue();
            var6 = var5.getDerValue();
        }

        byte[] var7 = var6.toByteArray();
        return new TX500Principal(var7);
    }

    public static TX500Principal getSubjectX500Principal(TX509Certificate var0) {
        try {
            return getX500Principal(var0, false);
        } catch (Exception var2) {
            throw new RuntimeException("Could not parse subject", var2);
        }
    }

    public static TX500Principal getIssuerX500Principal(TX509Certificate var0) {
        try {
            return getX500Principal(var0, true);
        } catch (Exception var2) {
            throw new RuntimeException("Could not parse issuer", var2);
        }
    }

    public static byte[] getEncodedInternal(TCertificate var0) throws TCertificateEncodingException {
        return var0 instanceof TX509CertImpl ?((TX509CertImpl)var0).getEncodedInternal():var0.getEncoded();
    }

    public static TX509CertImpl toImpl(TX509Certificate var0) throws TCertificateException {
        return var0 instanceof TX509CertImpl?(TX509CertImpl)var0: TX509Factory.intern(var0);
    }

    public static boolean isSelfIssued(X509Certificate var0) {
        X500Principal var1 = var0.getSubjectX500Principal();
        X500Principal var2 = var0.getIssuerX500Principal();
        return var1.equals(var2);
    }

    public static boolean isSelfSigned(X509Certificate var0, String var1) {
        if(isSelfIssued(var0)) {
            try {
                if(var1 == null) {
                    var0.verify(var0.getPublicKey());
                } else {
                    var0.verify(var0.getPublicKey(), var1);
                }

                return true;
            } catch (Exception var3) {
                ;
            }
        }

        return false;
    }

    public String getFingerprint(String var1) {
        return (String)this.fingerprints.computeIfAbsent(var1, (var1) -> {
            return this.getCertificateFingerPrint(var1);
        });
    }

    private String getCertificateFingerPrint(String var1) {
        String var2 = "";

        try {
            byte[] var3 = this.getEncoded();
            MessageDigest var4 = MessageDigest.getInstance(var1);
            byte[] var5 = var4.digest(var3);
            StringBuffer var6 = new StringBuffer();

            for(int var7 = 0; var7 < var5.length; ++var7) {
                byte2hex(var5[var7], var6);
            }

            var2 = var6.toString();
        } catch (TCertificateEncodingException | NoSuchAlgorithmException var8) {
            ;
        }

        return var2;
    }

    private static void byte2hex(byte var0, StringBuffer var1) {
        char[] var2 = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int var3 = (var0 & 240) >> 4;
        int var4 = var0 & 15;
        var1.append(var2[var3]);
        var1.append(var2[var4]);
    }
}
