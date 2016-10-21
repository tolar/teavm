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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.X509CRLEntry;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.security.TInvalidKeyException;
import org.teavm.classlib.java.security.TNoSuchAlgorithmException;
import org.teavm.classlib.java.security.TNoSuchProviderException;
import org.teavm.classlib.java.security.TPrincipal;
import org.teavm.classlib.java.security.TPrivateKey;
import org.teavm.classlib.java.security.TProvider;
import org.teavm.classlib.java.security.TPublicKey;
import org.teavm.classlib.java.security.TSignature;
import org.teavm.classlib.java.security.TSignatureException;
import org.teavm.classlib.java.security.cert.TCRLException;
import org.teavm.classlib.java.security.cert.TCertificate;
import org.teavm.classlib.java.security.cert.TX509CRL;
import org.teavm.classlib.java.security.cert.TX509CRLEntry;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.java.util.TDate;
import org.teavm.classlib.java.util.TIterator;
import org.teavm.classlib.java.util.TLinkedList;
import org.teavm.classlib.java.util.TList;
import org.teavm.classlib.java.util.TMap;
import org.teavm.classlib.java.util.TSet;
import org.teavm.classlib.java.util.TTreeMap;
import org.teavm.classlib.java.util.TTreeSet;
import org.teavm.classlib.javax.auth.x500.TX500Principal;
import org.teavm.classlib.sun.misc.THexDumpEncoder;
import org.teavm.classlib.sun.security.provider.TX509Factory;
import org.teavm.classlib.sun.security.util.TDerEncoder;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TX509CRLImpl extends TX509CRL implements TDerEncoder {
    private byte[] signedCRL;
    private byte[] signature;
    private byte[] tbsCertList;
    private TAlgorithmId sigAlgId;
    private int version;
    private TAlgorithmId infoSigAlgId;
    private TX500Name issuer;
    private TX500Principal issuerPrincipal;
    private TDate thisUpdate;
    private TDate nextUpdate;
    private TMap<TX509CRLImpl.X509IssuerSerial, TX509CRLEntry> revokedMap;
    private TList<TX509CRLEntry> revokedList;
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
        this.revokedMap = new TTreeMap();
        this.revokedList = new TLinkedList();
        this.extensions = null;
        this.readOnly = false;
    }

    public TX509CRLImpl(byte[] var1) throws TCRLException {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TTreeMap();
        this.revokedList = new TLinkedList();
        this.extensions = null;
        this.readOnly = false;

        try {
            this.parse(new TDerValue(var1));
        } catch (TIOException var3) {
            this.signedCRL = null;
            throw new TCRLException(TString.wrap("Parsing error: " + var3.getMessage()));
        }
    }

    public TX509CRLImpl(TDerValue var1) throws TCRLException {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TTreeMap();
        this.revokedList = new TLinkedList();
        this.extensions = null;
        this.readOnly = false;

        try {
            this.parse(var1);
        } catch (TIOException var3) {
            this.signedCRL = null;
            throw new TCRLException(TString.wrap("Parsing error: " + var3.getMessage()));
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
        this.revokedMap = new TTreeMap();
        this.revokedList = new TLinkedList();
        this.extensions = null;
        this.readOnly = false;

        try {
            this.parse(new TDerValue(var1));
        } catch (TIOException var3) {
            this.signedCRL = null;
            throw new TCRLException(TString.wrap("Parsing error: " + var3.getMessage()));
        }
    }

    public TX509CRLImpl(TX500Name var1, TDate var2, TDate var3) {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TTreeMap();
        this.revokedList = new TLinkedList();
        this.extensions = null;
        this.readOnly = false;
        this.issuer = var1;
        this.thisUpdate = var2;
        this.nextUpdate = var3;
    }

    public TX509CRLImpl(TX500Name var1, TDate var2, TDate var3, TX509CRLEntry[] var4) throws CRLException {
        this.signedCRL = null;
        this.signature = null;
        this.tbsCertList = null;
        this.sigAlgId = null;
        this.issuer = null;
        this.issuerPrincipal = null;
        this.thisUpdate = null;
        this.nextUpdate = null;
        this.revokedMap = new TTreeMap();
        this.revokedList = new TLinkedList();
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
                } catch (TIOException var10) {
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

    public TX509CRLImpl(TX500Name var1, TDate var2, TDate var3, TX509CRLEntry[] var4, TCRLExtensions var5) throws CRLException {
        this(var1, var2, var3, var4);
        if(var5 != null) {
            this.extensions = var5;
            this.version = 1;
        }

    }

    public byte[] getEncodedInternal() throws TCRLException {
        if(this.signedCRL == null) {
            throw new TCRLException(TString.wrap("Null CRL to encode"));
        } else {
            return this.signedCRL;
        }
    }

    public byte[] getEncoded() throws TCRLException {
        return (byte[])this.getEncodedInternal().clone();
    }

    public void encodeInfo(TOutputStream var1) throws CRLException {
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
                    TIterator var5 = this.revokedList.iterator();

                    while(var5.hasNext()) {
                        TX509CRLEntry var6 = (TX509CRLEntry)var5.next();
                        ((TX509CRLEntryImpl)var6).encode(var3);
                    }

                    var2.write((byte)48, var3);
                }

                if(this.extensions != null) {
                    this.extensions.encode(var2, true);
                }

                var4.write((byte)48, var2);
                this.tbsCertList = var4.toByteArray();
                var1.write(this.tbsCertList);
            }
        } catch (TIOException var7) {
            throw new TCRLException(TString.wrap("Encoding error: " + var7.getMessage()));
        }
    }

    public void verify(TPublicKey var1) throws TCRLException, TNoSuchAlgorithmException, TInvalidKeyException,
            TNoSuchProviderException, TSignatureException {
        this.verify(var1, TString.wrap(""));
    }

    public synchronized void verify(TPublicKey var1, TString var2) throws TCRLException, TNoSuchAlgorithmException, TInvalidKeyException, TNoSuchProviderException, TSignatureException {
        if(var2 == null) {
            var2 = TString.wrap("");
        }

        if(this.verifiedPublicKey == null || !this.verifiedPublicKey.equals(var1) || !var2.equals(this.verifiedProvider)) {
            if(this.signedCRL == null) {
                throw new TCRLException(TString.wrap("Uninitialized CRL"));
            } else {
                TSignature var3 = null;
                if(var2.length() == 0) {
                    var3 = TSignature.getInstance(this.sigAlgId.getName());
                } else {
                    var3 = TSignature.getInstance(this.sigAlgId.getName(), var2);
                }

                var3.initVerify(var1);
                if(this.tbsCertList == null) {
                    throw new TCRLException(TString.wrap("Uninitialized CRL"));
                } else {
                    var3.update(this.tbsCertList, 0, this.tbsCertList.length);
                    if(!var3.verify(this.signature)) {
                        throw new TSignatureException(TString.wrap("Signature does not match."));
                    } else {
                        this.verifiedPublicKey = var1;
                        this.verifiedProvider = var2;
                    }
                }
            }
        }
    }

    public synchronized void verify(TPublicKey var1, TProvider var2) throws TCRLException, TNoSuchAlgorithmException, TInvalidKeyException, TSignatureException {
        if(this.signedCRL == null) {
            throw new TCRLException(TString.wrap("Uninitialized CRL"));
        } else {
            TSignature var3 = null;
            if(var2 == null) {
                var3 = TSignature.getInstance(this.sigAlgId.getName());
            } else {
                var3 = TSignature.getInstance(this.sigAlgId.getName(), var2);
            }

            var3.initVerify(var1);
            if(this.tbsCertList == null) {
                throw new TCRLException(TString.wrap("Uninitialized CRL"));
            } else {
                var3.update(this.tbsCertList, 0, this.tbsCertList.length);
                if(!var3.verify(this.signature)) {
                    throw new TSignatureException(TString.wrap("Signature does not match."));
                } else {
                    this.verifiedPublicKey = var1;
                }
            }
        }
    }

    public static void verify(TX509CRL var0, TPublicKey var1, TProvider var2) throws TCRLException,
            TNoSuchAlgorithmException, TInvalidKeyException, TSignatureException {
        var0.verify(var1, var2);
    }

    public void sign(TPrivateKey var1, TString var2) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.sign(var1, var2, (TString)null);
    }

    public void sign(TPrivateKey var1, TString var2, TString var3) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        try {
            if(this.readOnly) {
                throw new CRLException("cannot over-write existing CRL");
            } else {
                TSignature var4 = null;
                if(var3 != null && var3.length() != 0) {
                    var4 = TSignature.getInstance(var2, var3);
                } else {
                    var4 = TSignature.getInstance(var2);
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
                var5.write((byte)48, var6);
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
            TIterator var3 = this.revokedList.iterator();

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

    public boolean isRevoked(TCertificate var1) {
        if(!this.revokedMap.isEmpty() && var1 instanceof TX509Certificate) {
            TX509Certificate var2 = (TX509Certificate)var1;
            TX509CRLImpl.X509IssuerSerial var3 = new TX509CRLImpl.X509IssuerSerial(var2);
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
    public TDate getThisUpdate() {
        return new TDate(this.thisUpdate.getTime());
    }

    public TDate getNextUpdate() {
        return this.nextUpdate == null?null:new TDate(this.nextUpdate.getTime());
    }

    public TX509CRLEntry getRevokedCertificate(TBigInteger var1) {
        if(this.revokedMap.isEmpty()) {
            return null;
        } else {
            TX509CRLImpl.X509IssuerSerial var2 = new TX509CRLImpl.X509IssuerSerial(this.getIssuerX500Principal(), var1);
            return (TX509CRLEntry)this.revokedMap.get(var2);
        }
    }

    public TX509CRLEntry getRevokedCertificate(TX509Certificate var1) {
        if(this.revokedMap.isEmpty()) {
            return null;
        } else {
            TX509CRLImpl.X509IssuerSerial var2 = new TX509CRLImpl.X509IssuerSerial(var1);
            return (TX509CRLEntry)this.revokedMap.get(var2);
        }
    }

    public TSet<TX509CRLEntry> getRevokedCertificates() {
        return this.revokedList.isEmpty()?null:new TTreeSet(this.revokedList);
    }

    public byte[] getTBSCertList() throws TCRLException {
        if(this.tbsCertList == null) {
            throw new TCRLException(TString.wrap("Uninitialized CRL"));
        } else {
            return (byte[])this.tbsCertList.clone();
        }
    }

    public byte[] getSignature() {
        return this.signature == null?null:(byte[])this.signature.clone();
    }

    public TString getSigAlgName() {
        return this.sigAlgId == null?null:this.sigAlgId.getName();
    }

    public TString getSigAlgOID() {
        if(this.sigAlgId == null) {
            return null;
        } else {
            TObjectIdentifier var1 = this.sigAlgId.getOID();
            return TString.wrap(var1.toString());
        }
    }

    public byte[] getSigAlgParams() {
        if(this.sigAlgId == null) {
            return null;
        } else {
            try {
                return this.sigAlgId.getEncodedParams();
            } catch (TIOException var2) {
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
            TKeyIdentifier var2 = (TKeyIdentifier)var1.get("key_id");
            return var2;
        } else {
            return null;
        }
    }

    public TAuthorityKeyIdentifierExtension getAuthKeyIdExtension() throws TIOException {
        Object var1 = this.getExtension(TPKIXExtensions.AuthorityKey_Id);
        return (TAuthorityKeyIdentifierExtension)var1;
    }

    public TCRLNumberExtension getCRLNumberExtension() throws TIOException {
        Object var1 = this.getExtension(TPKIXExtensions.CRLNumber_Id);
        return (TCRLNumberExtension)var1;
    }

    public TBigInteger getCRLNumber() throws TIOException {
        TCRLNumberExtension var1 = this.getCRLNumberExtension();
        if(var1 != null) {
            TBigInteger var2 = var1.get(TString.wrap("value"));
            return var2;
        } else {
            return null;
        }
    }

    public TDeltaCRLIndicatorExtension getDeltaCRLIndicatorExtension() throws IOException {
        Object var1 = this.getExtension(TPKIXExtensions.DeltaCRLIndicator_Id);
        return (TDeltaCRLIndicatorExtension)var1;
    }

    public TBigInteger getBaseCRLNumber() throws IOException {
        TDeltaCRLIndicatorExtension var1 = this.getDeltaCRLIndicatorExtension();
        if(var1 != null) {
            TBigInteger var2 = var1.get(TString.wrap("value"));
            return var2;
        } else {
            return null;
        }
    }

    public TIssuerAlternativeNameExtension getIssuerAltNameExtension() throws IOException {
        Object var1 = this.getExtension(TPKIXExtensions.IssuerAlternativeName_Id);
        return (TIssuerAlternativeNameExtension)var1;
    }

    public TIssuingDistributionPointExtension getIssuingDistributionPointExtension() throws IOException {
        Object var1 = this.getExtension(TPKIXExtensions.IssuingDistributionPoint_Id);
        return (TIssuingDistributionPointExtension)var1;
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
                TExtension var3 = (TExtension)var2.next();
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
                TExtension var3 = (TExtension)var2.next();
                if(!var3.isCritical()) {
                    var1.add(var3.getExtensionId().toString());
                }
            }

            return var1;
        }
    }

    public byte[] getExtensionValue(TString var1) {
        if(this.extensions == null) {
            return null;
        } else {
            try {
                TString var2 = TOIDMap.getName(new TObjectIdentifier(var1));
                TExtension var3 = null;
                if(var2 == null) {
                    TObjectIdentifier var4 = new TObjectIdentifier(var1);
                    TExtension var5 = null;
                    Enumeration var7 = this.extensions.getElements();

                    while(var7.hasMoreElements()) {
                        var5 = (TExtension)var7.nextElement();
                        TObjectIdentifier var6 = var5.getExtensionId();
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
                        TDerOutputStream var10 = new TDerOutputStream();
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

    private void parse(TDerValue var1) throws TCRLException, TIOException {
        if(this.readOnly) {
            throw new TCRLException(TString.wrap("cannot over-write existing CRL"));
        } else if(var1.getData() != null && var1.tag == 48) {
            this.signedCRL = var1.toByteArray();
            TDerValue[] var2 = new TDerValue[]{var1.data.getDerValue(), var1.data.getDerValue(), var1.data.getDerValue()};
            if(var1.data.available() != 0) {
                throw new TCRLException(TString.wrap("signed overrun, bytes = " + var1.data.available()));
            } else if(var2[0].tag != 48) {
                throw new TCRLException(TString.wrap("signed CRL fields invalid"));
            } else {
                this.sigAlgId = TAlgorithmId.parse(var2[1]);
                this.signature = var2[2].getBitString();
                if(var2[1].data.available() != 0) {
                    throw new TCRLException(TString.wrap("AlgorithmId field overrun"));
                } else if(var2[2].data.available() != 0) {
                    throw new TCRLException(TString.wrap("Signature field overrun"));
                } else {
                    this.tbsCertList = var2[0].toByteArray();
                    TDerInputStream var3 = var2[0].data;
                    this.version = 0;
                    byte var5 = (byte)var3.peekByte();
                    if(var5 == 2) {
                        this.version = var3.getInteger();
                        if(this.version != 1) {
                            throw new TCRLException(TString.wrap("Invalid version"));
                        }
                    }

                    TDerValue var4 = var3.getDerValue();
                    TAlgorithmId var6 = TAlgorithmId.parse(var4);
                    if(!var6.equals(this.sigAlgId)) {
                        throw new TCRLException(TString.wrap("Signature algorithm mismatch"));
                    } else {
                        this.infoSigAlgId = var6;
                        this.issuer = new TX500Name(var3);
                        if(this.issuer.isEmpty()) {
                            throw new TCRLException(TString.wrap("Empty issuer DN not allowed in X509CRLs"));
                        } else {
                            var5 = (byte)var3.peekByte();
                            if(var5 == 23) {
                                this.thisUpdate = var3.getUTCTime();
                            } else {
                                if(var5 != 24) {
                                    throw new TCRLException(TString.wrap("Invalid encoding for thisUpdate (tag=" + var5 + ")"));
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
                                        TDerValue[] var7 = var3.getSequence(4);
                                        TX500Principal var8 = this.getIssuerX500Principal();
                                        TX500Principal var9 = var8;

                                        for(int var10 = 0; var10 < var7.length; ++var10) {
                                            TX509CRLEntryImpl var11 = new TX509CRLEntryImpl(var7[var10]);
                                            var9 = this.getCertIssuer(var11, var9);
                                            var11.setCertificateIssuer(var8, var9);
                                            TX509CRLImpl.X509IssuerSerial var12 = new TX509CRLImpl.X509IssuerSerial(var9, var11.getSerialNumber());
                                            this.revokedMap.put(var12, var11);
                                            this.revokedList.add(var11);
                                        }
                                    }

                                    if(var3.available() != 0) {
                                        var4 = var3.getDerValue();
                                        if(var4.isConstructed() && var4.isContextSpecific((byte)0)) {
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
            throw new TCRLException(TString.wrap("Invalid DER-encoded CRL data"));
        }
    }

    public static TX500Principal getIssuerX500Principal(TX509CRL var0) {
        try {
            byte[] var1 = var0.getEncoded();
            TDerInputStream var2 = new TDerInputStream(var1);
            TDerValue var3 = var2.getSequence(3)[0];
            TDerInputStream var4 = var3.data;
            byte var6 = (byte)var4.peekByte();
            TDerValue var5;
            if(var6 == 2) {
                var5 = var4.getDerValue();
            }

            var5 = var4.getDerValue();
            var5 = var4.getDerValue();
            byte[] var7 = var5.toByteArray();
            return new TX500Principal(var7);
        } catch (Exception var8) {
            throw new RuntimeException("Could not parse issuer", var8);
        }
    }

    public static byte[] getEncodedInternal(TX509CRL var0) throws TCRLException {
        return var0 instanceof TX509CRLImpl ?((TX509CRLImpl)var0).getEncodedInternal():var0.getEncoded();
    }

    public static TX509CRLImpl toImpl(TX509CRL var0) throws CRLException {
        return var0 instanceof TX509CRLImpl ?(TX509CRLImpl)var0: TX509Factory.intern(var0);
    }

    private TX500Principal getCertIssuer(TX509CRLEntryImpl var1, TX500Principal var2) throws TIOException {
        TCertificateIssuerExtension var3 = var1.getCertificateIssuerExtension();
        if(var3 != null) {
            TGeneralNames var4 = var3.get(TString.wrap("issuer"));
            TX500Name var5 = (TX500Name)var4.get(0).getName();
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

    private static final class X509IssuerSerial implements Comparable<TX509CRLImpl.X509IssuerSerial> {
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
            } else if(!(var1 instanceof TX509CRLImpl.X509IssuerSerial)) {
                return false;
            } else {
                TX509CRLImpl.X509IssuerSerial var2 = (TX509CRLImpl.X509IssuerSerial)var1;
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

        public int compareTo(TX509CRLImpl.X509IssuerSerial var1) {
            int var2 = this.issuer.toString().compareTo(var1.issuer.toString());
            return var2 != 0?var2:this.serial.compareTo(var1.serial);
        }
    }
}