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

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.security.auth.x500.X500Principal;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.security.TPrincipal;
import org.teavm.classlib.java.security.TPublicKey;
import org.teavm.classlib.java.security.cert.TCRLException;
import org.teavm.classlib.java.security.cert.TX509CRL;
import org.teavm.classlib.java.security.cert.TX509CRLEntry;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.javax.auth.x500.TX500Principal;
import org.teavm.classlib.sun.misc.THexDumpEncoder;
import org.teavm.classlib.sun.security.util.TDerEncoder;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TX509CRLImpl extends TX509CRL implements TDerEncoder {
    private byte[] signedCRL;
    private byte[] signature;
    private byte[] tbsCertList;
    private TAlgorithmId sigAlgId;
    private int version;
    private TAlgorithmId infoSigAlgId;
    private TX500Name issuer;
    private X500Principal issuerPrincipal;
    private Date thisUpdate;
    private Date nextUpdate;
    private Map<org.teavm.classlib.sun.security.x509.TX509CRLImpl.X509IssuerSerial, X509CRLEntry> revokedMap;
    private List<X509CRLEntry> revokedList;
    private TCRLExtensions extensions;
    private static final boolean isExplicit = true;
    private static final long YR_2050 = 2524636800000L;
    private boolean readOnly;
    private TPublicKey verifiedPublicKey;
    private TString verifiedProvider;

    private TX509CRLImpl() {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TreeMap();
        this.revokedList = new LinkedList();
        this.extensions = null;
        this.readOnly = false;
    }

    public TX509CRLImpl(byte[] var1) throws CRLException {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TreeMap();
        this.revokedList = new LinkedList();
        this.extensions = null;
        this.readOnly = false;

        try {
            this.parse(new TDerValue(var1));
        } catch (IOException var3) {
            this.signedCRL = null;
            throw new CRLException("Parsing error: " + var3.getMessage());
        }
    }

    public TX509CRLImpl(TDerValue var1) throws CRLException {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TreeMap();
        this.revokedList = new LinkedList();
        this.extensions = null;
        this.readOnly = false;

        try {
            this.parse(var1);
        } catch (IOException var3) {
            this.signedCRL = null;
            throw new CRLException("Parsing error: " + var3.getMessage());
        }
    }

    public TX509CRLImpl(TInputStream var1) throws CRLException {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TreeMap();
        this.revokedList = new LinkedList();
        this.extensions = null;
        this.readOnly = false;

        try {
            this.parse(new TDerValue(var1));
        } catch (IOException var3) {
            this.signedCRL = null;
            throw new CRLException("Parsing error: " + var3.getMessage());
        }
    }

    public TX509CRLImpl(TX500Name var1, Date var2, Date var3) {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TreeMap();
        this.revokedList = new LinkedList();
        this.extensions = null;
        this.readOnly = false;
        this.issuer = var1;
        this.thisUpdate = var2;
        this.nextUpdate = var3;
    }

    public TX509CRLImpl(TX500Name var1, Date var2, Date var3, TX509CRLEntry[] var4) throws CRLException {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TreeMap();
        this.revokedList = new LinkedList();
        this.extensions = null;
        this.readOnly = false;
        this.issuer = var1;
        this.thisUpdate = var2;
        this.nextUpdate = var3;
        if(var4 != null) {
            TX500Principal var5 = this.getIssuerX500Principal();
            TX500Principal var6 = var5;

            for(int var7 = 0; var7 < var4.length; ++var7) {
                TX509CRLEntryImpl var8 = (TX509CRLEntryImpl)var4[var7];

                try {
                    var6 = this.getCertIssuer(var8, var6);
                } catch (IOException var10) {
                    throw new CRLException(var10);
                }

                var8.setCertificateIssuer(var5, var6);
                org.teavm.classlib.sun.security.x509.TX509CRLImpl.X509IssuerSerial var9 = new org.teavm.classlib.sun.security.x509.TX509CRLImpl.X509IssuerSerial(var6, var8.getSerialNumber());
                this.revokedMap.put(var9, var8);
                this.revokedList.add(var8);
                if(var8.hasExtensions()) {
                    this.version = 1;
                }
            }
        }

    }

    public TX509CRLImpl(TX500Name var1, Date var2, Date var3, X509CRLEntry[] var4, TCRLExtensions var5) throws CRLException {
        this(var1, var2, var3, var4);
        if(var5 != null) {
            this.extensions = var5;
            this.version = 1;
        }

    }

    public byte[] getEncodedInternal() throws CRLException {
        if(this.signedCRL == null) {
            throw new CRLException("Null CRL to encode");
        } else {
            return this.signedCRL;
        }
    }

    public byte[] getEncoded() throws TCRLException {
        return (byte[])this.getEncodedInternal().clone();
    }

    public void encodeInfo(OutputStream var1) throws CRLException {
        try {
            TDerOutputStream var2 = new TDerOutputStream();
            TDerOutputStream var3 = new TDerOutputStream();
            TDerOutputStream var4 = new TDerOutputStream();
            if(this.version != 0) {
                var2.putInteger(this.version);
            }

            this.infoSigAlgId.encode(var2);
            if(this.version == 0 && this.issuer.toString() == null) {
                throw new CRLException("Null Issuer DN not allowed in v1 CRL");
            } else {
                this.issuer.encode(var2);
                if(this.thisUpdate.getTime() < 2524636800000L) {
                    var2.putUTCTime(this.thisUpdate);
                } else {
                    var2.putGeneralizedTime(this.thisUpdate);
                }

                if(this.nextUpdate != null) {
                    if(this.nextUpdate.getTime() < 2524636800000L) {
                        var2.putUTCTime(this.nextUpdate);
                    } else {
                        var2.putGeneralizedTime(this.nextUpdate);
                    }
                }

                if(!this.revokedList.isEmpty()) {
                    Iterator var5 = this.revokedList.iterator();

                    while(var5.hasNext()) {
                       TX509CRLEntry var6 = (TX509CRLEntry)var5.next();
                        ((TX509CRLEntryImpl)var6).encode(var3);
                    }

                    var2.write(48, var3);
                }

                if(this.extensions != null) {
                    this.extensions.encode(var2, true);
                }

                var4.write(48, var2);
                this.tbsCertList = var4.toByteArray();
                var1.write(this.tbsCertList);
            }
        } catch (IOException var7) {
            throw new CRLException("Encoding error: " + var7.getMessage());
        }
    }

    public void verify(PublicKey var1) throws CRLException, NoSuchAlgorithmException, InvalidKeyException,
            NoSuchProviderException, SignatureException {
        this.verify(var1, "");
    }

    public synchronized void verify(PublicKey var1, String var2) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        if(var2 == null) {
            var2 = "";
        }

        if(this.verifiedPublicKey == null || !this.verifiedPublicKey.equals(var1) || !var2.equals(this.verifiedProvider)) {
            if(this.signedCRL == null) {
                throw new CRLException("Uninitialized CRL");
            } else {
                Signature var3 = null;
                if(var2.length() == 0) {
                    var3 = Signature.getInstance(this.sigAlgId.getName());
                } else {
                    var3 = Signature.getInstance(this.sigAlgId.getName(), var2);
                }

                var3.initVerify(var1);
                if(this.tbsCertList == null) {
                    throw new CRLException("Uninitialized CRL");
                } else {
                    var3.update(this.tbsCertList, 0, this.tbsCertList.length);
                    if(!var3.verify(this.signature)) {
                        throw new SignatureException("Signature does not match.");
                    } else {
                        this.verifiedPublicKey = var1;
                        this.verifiedProvider = var2;
                    }
                }
            }
        }
    }

    public synchronized void verify(PublicKey var1, Provider var2) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if(this.signedCRL == null) {
            throw new CRLException("Uninitialized CRL");
        } else {
            Signature var3 = null;
            if(var2 == null) {
                var3 = Signature.getInstance(this.sigAlgId.getName());
            } else {
                var3 = Signature.getInstance(this.sigAlgId.getName(), var2);
            }

            var3.initVerify(var1);
            if(this.tbsCertList == null) {
                throw new CRLException("Uninitialized CRL");
            } else {
                var3.update(this.tbsCertList, 0, this.tbsCertList.length);
                if(!var3.verify(this.signature)) {
                    throw new SignatureException("Signature does not match.");
                } else {
                    this.verifiedPublicKey = var1;
                }
            }
        }
    }

    public static void verify(X509CRL var0, PublicKey var1, Provider var2) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        var0.verify(var1, var2);
    }

    public void sign(PrivateKey var1, String var2) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.sign(var1, var2, (String)null);
    }

    public void sign(PrivateKey var1, String var2, String var3) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        try {
            if(this.readOnly) {
                throw new CRLException("cannot over-write existing CRL");
            } else {
                Signature var4 = null;
                if(var3 != null && var3.length() != 0) {
                    var4 = Signature.getInstance(var2, var3);
                } else {
                    var4 = Signature.getInstance(var2);
                }

                var4.initSign(var1);
                this.sigAlgId = TAlgorithmId.get(var4.getAlgorithm());
                this.infoSigAlgId = this.sigAlgId;
                TDerOutputStream var5 = new TDerOutputStream();
                TDerOutputStream var6 = new TDerOutputStream();
                this.encodeInfo(var6);
                this.sigAlgId.encode(var6);
                var4.update(this.tbsCertList, 0, this.tbsCertList.length);
                this.signature = var4.sign();
                var6.putBitString(this.signature);
                var5.write(48, var6);
                this.signedCRL = var5.toByteArray();
                this.readOnly = true;
            }
        } catch (IOException var7) {
            throw new CRLException("Error while encoding data: " + var7.getMessage());
        }
    }

    public String toString() {
        StringBuffer var1 = new StringBuffer();
        var1.append("X.509 CRL v" + (this.version + 1) + "\n");
        if(this.sigAlgId != null) {
            var1.append("Signature Algorithm: " + this.sigAlgId.toString() + ", OID=" + this.sigAlgId.getOID().toString() + "\n");
        }

        if(this.issuer != null) {
            var1.append("Issuer: " + this.issuer.toString() + "\n");
        }

        if(this.thisUpdate != null) {
            var1.append("\nThis Update: " + this.thisUpdate.toString() + "\n");
        }

        if(this.nextUpdate != null) {
            var1.append("Next Update: " + this.nextUpdate.toString() + "\n");
        }

        if(this.revokedList.isEmpty()) {
            var1.append("\nNO certificates have been revoked\n");
        } else {
            var1.append("\nRevoked Certificates: " + this.revokedList.size());
            int var2 = 1;
            Iterator var3 = this.revokedList.iterator();

            while(var3.hasNext()) {
                X509CRLEntry var4 = (X509CRLEntry)var3.next();
                var1.append("\n[" + var2++ + "] " + var4.toString());
            }
        }

        if(this.extensions != null) {
            Collection var10 = this.extensions.getAllExtensions();
            Object[] var12 = var10.toArray();
            var1.append("\nCRL Extensions: " + var12.length);

            for(int var13 = 0; var13 < var12.length; ++var13) {
                var1.append("\n[" + (var13 + 1) + "]: ");
                TExtension var5 = (TExtension)var12[var13];

                try {
                    if(TOIDMap.getClass(var5.getExtensionId()) == null) {
                        var1.append(var5.toString());
                        byte[] var6 = var5.getExtensionValue();
                        if(var6 != null) {
                            TDerOutputStream var7 = new TDerOutputStream();
                            var7.putOctetString(var6);
                            var6 = var7.toByteArray();
                            THexDumpEncoder var8 = new THexDumpEncoder();
                            var1.append("Extension unknown: DER encoded OCTET string =\n" + var8.encodeBuffer(var6) + "\n");
                        }
                    } else {
                        var1.append(var5.toString());
                    }
                } catch (Exception var9) {
                    var1.append(", Error parsing this extension");
                }
            }
        }

        if(this.signature != null) {
            THexDumpEncoder var11 = new THexDumpEncoder();
            var1.append("\nSignature:\n" + var11.encodeBuffer(this.signature) + "\n");
        } else {
            var1.append("NOT signed yet\n");
        }

        return var1.toString();
    }

    public boolean isRevoked(Certificate var1) {
        if(!this.revokedMap.isEmpty() && var1 instanceof X509Certificate) {
            X509Certificate var2 = (X509Certificate)var1;
            org.teavm.classlib.sun.security.x509.TX509CRLImpl.X509IssuerSerial var3 = new org.teavm.classlib.sun.security.x509.TX509CRLImpl.X509IssuerSerial(var2);
            return this.revokedMap.containsKey(var3);
        } else {
            return false;
        }
    }

    public int getVersion() {
        return this.version + 1;
    }

    public TPrincipal getIssuerDN() {
        return this.issuer;
    }

    public TX500Principal getIssuerX500Principal() {
        if(this.issuerPrincipal == null) {
            this.issuerPrincipal = this.issuer.asX500Principal();
        }

        return this.issuerPrincipal;
    }

    public Date getThisUpdate() {
        return new Date(this.thisUpdate.getTime());
    }

    public Date getNextUpdate() {
        return this.nextUpdate == null?null:new Date(this.nextUpdate.getTime());
    }

    public X509CRLEntry getRevokedCertificate(TBigInteger var1) {
        if(this.revokedMap.isEmpty()) {
            return null;
        } else {
            sun.security.x509.X509CRLImpl.X509IssuerSerial var2 = new sun.security.x509.X509CRLImpl.X509IssuerSerial(this.getIssuerX500Principal(), var1);
            return (X509CRLEntry)this.revokedMap.get(var2);
        }
    }

    public X509CRLEntry getRevokedCertificate(X509Certificate var1) {
        if(this.revokedMap.isEmpty()) {
            return null;
        } else {
            sun.security.x509.X509CRLImpl.X509IssuerSerial var2 = new sun.security.x509.X509CRLImpl.X509IssuerSerial(var1);
            return (X509CRLEntry)this.revokedMap.get(var2);
        }
    }

    public Set<X509CRLEntry> getRevokedCertificates() {
        return this.revokedList.isEmpty()?null:new TreeSet(this.revokedList);
    }

    public byte[] getTBSCertList() throws CRLException {
        if(this.tbsCertList == null) {
            throw new CRLException("Uninitialized CRL");
        } else {
            return (byte[])this.tbsCertList.clone();
        }
    }

    public byte[] getSignature() {
        return this.signature == null?null:(byte[])this.signature.clone();
    }

    public String getSigAlgName() {
        return this.sigAlgId == null?null:this.sigAlgId.getName();
    }

    public String getSigAlgOID() {
        if(this.sigAlgId == null) {
            return null;
        } else {
            ObjectIdentifier var1 = this.sigAlgId.getOID();
            return var1.toString();
        }
    }

    public byte[] getSigAlgParams() {
        if(this.sigAlgId == null) {
            return null;
        } else {
            try {
                return this.sigAlgId.getEncodedParams();
            } catch (IOException var2) {
                return null;
            }
        }
    }

    public TAlgorithmId getSigAlgId() {
        return this.sigAlgId;
    }

    public TKeyIdentifier getAuthKeyId() throws IOException {
        TAuthorityKeyIdentifierExtension var1 = this.getAuthKeyIdExtension();
        if(var1 != null) {
            KeyIdentifier var2 = (KeyIdentifier)var1.get("key_id");
            return var2;
        } else {
            return null;
        }
    }

    public TAuthorityKeyIdentifierExtension getAuthKeyIdExtension() throws IOException {
        Object var1 = this.getExtension(TPKIXExtensions.AuthorityKey_Id);
        return (TAuthorityKeyIdentifierExtension)var1;
    }

    public TCRLNumberExtension getCRLNumberExtension() throws IOException {
        Object var1 = this.getExtension(PKIXExtensions.CRLNumber_Id);
        return (TCRLNumberExtension)var1;
    }

    public BigInteger getCRLNumber() throws IOException {
        TCRLNumberExtension var1 = this.getCRLNumberExtension();
        if(var1 != null) {
            BigInteger var2 = var1.get("value");
            return var2;
        } else {
            return null;
        }
    }

    public DeltaCRLIndicatorExtension getDeltaCRLIndicatorExtension() throws IOException {
        Object var1 = this.getExtension(PKIXExtensions.DeltaCRLIndicator_Id);
        return (DeltaCRLIndicatorExtension)var1;
    }

    public BigInteger getBaseCRLNumber() throws IOException {
        DeltaCRLIndicatorExtension var1 = this.getDeltaCRLIndicatorExtension();
        if(var1 != null) {
            BigInteger var2 = var1.get("value");
            return var2;
        } else {
            return null;
        }
    }

    public IssuerAlternativeNameExtension getIssuerAltNameExtension() throws IOException {
        Object var1 = this.getExtension(PKIXExtensions.IssuerAlternativeName_Id);
        return (IssuerAlternativeNameExtension)var1;
    }

    public IssuingDistributionPointExtension getIssuingDistributionPointExtension() throws IOException {
        Object var1 = this.getExtension(PKIXExtensions.IssuingDistributionPoint_Id);
        return (IssuingDistributionPointExtension)var1;
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.extensions == null?false:this.extensions.hasUnsupportedCriticalExtension();
    }

    public Set<String> getCriticalExtensionOIDs() {
        if(this.extensions == null) {
            return null;
        } else {
            TreeSet var1 = new TreeSet();
            Iterator var2 = this.extensions.getAllExtensions().iterator();

            while(var2.hasNext()) {
                Extension var3 = (Extension)var2.next();
                if(var3.isCritical()) {
                    var1.add(var3.getExtensionId().toString());
                }
            }

            return var1;
        }
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        if(this.extensions == null) {
            return null;
        } else {
            TreeSet var1 = new TreeSet();
            Iterator var2 = this.extensions.getAllExtensions().iterator();

            while(var2.hasNext()) {
                Extension var3 = (Extension)var2.next();
                if(!var3.isCritical()) {
                    var1.add(var3.getExtensionId().toString());
                }
            }

            return var1;
        }
    }

    public byte[] getExtensionValue(String var1) {
        if(this.extensions == null) {
            return null;
        } else {
            try {
                String var2 = OIDMap.getName(new ObjectIdentifier(var1));
                Extension var3 = null;
                if(var2 == null) {
                    ObjectIdentifier var4 = new ObjectIdentifier(var1);
                    Extension var5 = null;
                    Enumeration var7 = this.extensions.getElements();

                    while(var7.hasMoreElements()) {
                        var5 = (Extension)var7.nextElement();
                        ObjectIdentifier var6 = var5.getExtensionId();
                        if(var6.equals(var4)) {
                            var3 = var5;
                            break;
                        }
                    }
                } else {
                    var3 = this.extensions.get(var2);
                }

                if(var3 == null) {
                    return null;
                } else {
                    byte[] var9 = var3.getExtensionValue();
                    if(var9 == null) {
                        return null;
                    } else {
                        DerOutputStream var10 = new DerOutputStream();
                        var10.putOctetString(var9);
                        return var10.toByteArray();
                    }
                }
            } catch (Exception var8) {
                return null;
            }
        }
    }

    public Object getExtension(TObjectIdentifier var1) {
        return this.extensions == null?null:this.extensions.get(TOIDMap.getName(var1));
    }

    private void parse(TDerValue var1) throws CRLException, IOException {
        if(this.readOnly) {
            throw new CRLException("cannot over-write existing CRL");
        } else if(var1.getData() != null && var1.tag == 48) {
            this.signedCRL = var1.toByteArray();
            DerValue[] var2 = new DerValue[]{var1.data.getDerValue(), var1.data.getDerValue(), var1.data.getDerValue()};
            if(var1.data.available() != 0) {
                throw new CRLException("signed overrun, bytes = " + var1.data.available());
            } else if(var2[0].tag != 48) {
                throw new CRLException("signed CRL fields invalid");
            } else {
                this.sigAlgId = AlgorithmId.parse(var2[1]);
                this.signature = var2[2].getBitString();
                if(var2[1].data.available() != 0) {
                    throw new CRLException("AlgorithmId field overrun");
                } else if(var2[2].data.available() != 0) {
                    throw new CRLException("Signature field overrun");
                } else {
                    this.tbsCertList = var2[0].toByteArray();
                    DerInputStream var3 = var2[0].data;
                    this.version = 0;
                    byte var5 = (byte)var3.peekByte();
                    if(var5 == 2) {
                        this.version = var3.getInteger();
                        if(this.version != 1) {
                            throw new CRLException("Invalid version");
                        }
                    }

                    DerValue var4 = var3.getDerValue();
                    AlgorithmId var6 = AlgorithmId.parse(var4);
                    if(!var6.equals(this.sigAlgId)) {
                        throw new CRLException("Signature algorithm mismatch");
                    } else {
                        this.infoSigAlgId = var6;
                        this.issuer = new X500Name(var3);
                        if(this.issuer.isEmpty()) {
                            throw new CRLException("Empty issuer DN not allowed in X509CRLs");
                        } else {
                            var5 = (byte)var3.peekByte();
                            if(var5 == 23) {
                                this.thisUpdate = var3.getUTCTime();
                            } else {
                                if(var5 != 24) {
                                    throw new CRLException("Invalid encoding for thisUpdate (tag=" + var5 + ")");
                                }

                                this.thisUpdate = var3.getGeneralizedTime();
                            }

                            if(var3.available() != 0) {
                                var5 = (byte)var3.peekByte();
                                if(var5 == 23) {
                                    this.nextUpdate = var3.getUTCTime();
                                } else if(var5 == 24) {
                                    this.nextUpdate = var3.getGeneralizedTime();
                                }

                                if(var3.available() != 0) {
                                    var5 = (byte)var3.peekByte();
                                    if(var5 == 48 && (var5 & 192) != 128) {
                                        DerValue[] var7 = var3.getSequence(4);
                                        X500Principal var8 = this.getIssuerX500Principal();
                                        X500Principal var9 = var8;

                                        for(int var10 = 0; var10 < var7.length; ++var10) {
                                            X509CRLEntryImpl var11 = new X509CRLEntryImpl(var7[var10]);
                                            var9 = this.getCertIssuer(var11, var9);
                                            var11.setCertificateIssuer(var8, var9);
                                            sun.security.x509.X509CRLImpl.X509IssuerSerial var12 = new sun.security.x509.X509CRLImpl.X509IssuerSerial(var9, var11.getSerialNumber());
                                            this.revokedMap.put(var12, var11);
                                            this.revokedList.add(var11);
                                        }
                                    }

                                    if(var3.available() != 0) {
                                        var4 = var3.getDerValue();
                                        if(var4.isConstructed() && var4.isContextSpecific(0)) {
                                            this.extensions = new TCRLExtensions(var4.data);
                                        }

                                        this.readOnly = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new CRLException("Invalid DER-encoded CRL data");
        }
    }

    public static X500Principal getIssuerX500Principal(X509CRL var0) {
        try {
            byte[] var1 = var0.getEncoded();
            DerInputStream var2 = new DerInputStream(var1);
            DerValue var3 = var2.getSequence(3)[0];
            DerInputStream var4 = var3.data;
            byte var6 = (byte)var4.peekByte();
            DerValue var5;
            if(var6 == 2) {
                var5 = var4.getDerValue();
            }

            var5 = var4.getDerValue();
            var5 = var4.getDerValue();
            byte[] var7 = var5.toByteArray();
            return new X500Principal(var7);
        } catch (Exception var8) {
            throw new RuntimeException("Could not parse issuer", var8);
        }
    }

    public static byte[] getEncodedInternal(TX509CRL var0) throws CRLException {
        return var0 instanceof sun.security.x509.X509CRLImpl ?((sun.security.x509.X509CRLImpl)var0).getEncodedInternal():var0.getEncoded();
    }

    public static sun.security.x509.X509CRLImpl toImpl(X509CRL var0) throws CRLException {
        return var0 instanceof sun.security.x509.X509CRLImpl ?(sun.security.x509.X509CRLImpl)var0: X509Factory.intern(var0);
    }

    private TX500Principal getCertIssuer(TX509CRLEntryImpl var1, TX500Principal var2) throws IOException {
        TCertificateIssuerExtension var3 = var1.getCertificateIssuerExtension();
        if(var3 != null) {
            GeneralNames var4 = var3.get("issuer");
            X500Name var5 = (X500Name)var4.get(0).getName();
            return var5.asX500Principal();
        } else {
            return var2;
        }
    }

    public void derEncode(TOutputStream var1) throws TIOException {
        if(this.signedCRL == null) {
            throw new TIOException(TString.wrap("Null CRL to encode"));
        } else {
            var1.write((byte[])this.signedCRL.clone());
        }
    }

    private static final class X509IssuerSerial implements Comparable<org.teavm.classlib.sun.security.x509.TX509CRLImpl.X509IssuerSerial> {
        final TX500Principal issuer;
        final TBigInteger serial;
        volatile int hashcode;

        X509IssuerSerial(TX500Principal var1, TBigInteger var2) {
            this.hashcode = 0;
            this.issuer = var1;
            this.serial = var2;
        }

        X509IssuerSerial(TX509Certificate var1) {
            this(var1.getIssuerX500Principal(), var1.getSerialNumber());
        }

        TX500Principal getIssuer() {
            return this.issuer;
        }

        TBigInteger getSerial() {
            return this.serial;
        }

        public boolean equals(Object var1) {
            if(var1 == this) {
                return true;
            } else if(!(var1 instanceof sun.security.x509.X509CRLImpl.X509IssuerSerial)) {
                return false;
            } else {
                sun.security.x509.X509CRLImpl.X509IssuerSerial var2 = (sun.security.x509.X509CRLImpl.X509IssuerSerial)var1;
                return this.serial.equals(var2.getSerial()) && this.issuer.equals(var2.getIssuer());
            }
        }

        public int hashCode() {
            if(this.hashcode == 0) {
                byte var1 = 17;
                int var2 = 37 * var1 + this.issuer.hashCode();
                var2 = 37 * var2 + this.serial.hashCode();
                this.hashcode = var2;
            }

            return this.hashcode;
        }

        public int compareTo(org.teavm.classlib.sun.security.x509.TX509CRLImpl.X509IssuerSerial var1) {
            int var2 = this.issuer.toString().compareTo(var1.issuer.toString());
            return var2 != 0?var2:this.serial.compareTo(var1.serial);
        }
    }
}