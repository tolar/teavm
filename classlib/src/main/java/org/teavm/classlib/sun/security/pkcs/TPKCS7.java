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
package org.teavm.classlib.sun.security.pkcs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import org.teavm.classlib.java.io.TByteArrayInputStream;
import org.teavm.classlib.java.io.TDataInputStream;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.security.TPrincipal;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.security.cert.TCertificateFactory;
import org.teavm.classlib.java.security.cert.TX509CRL;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.sun.security.timestamp.TTSRequest;
import org.teavm.classlib.sun.security.timestamp.TTSResponse;
import org.teavm.classlib.sun.security.timestamp.TTimestampToken;
import org.teavm.classlib.sun.security.timestamp.TTimestamper;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;
import org.teavm.classlib.sun.security.x509.TAlgorithmId;
import org.teavm.classlib.sun.security.x509.TX500Name;
import org.teavm.classlib.sun.security.x509.TX509CertImpl;
import org.teavm.classlib.sun.security.x509.TX509CertInfo;

public class TPKCS7 {
    private TObjectIdentifier contentType;
    private TBigInteger version;
    private TAlgorithmId[] digestAlgorithmIds;
    private TContentInfo contentInfo;
    private TX509Certificate[] certificates;
    private TX509CRL[] crls;
    private TSignerInfo[] signerInfos;
    private boolean oldStyle;
    private TPrincipal[] certIssuerNames;
    private static final String KP_TIMESTAMPING_OID = "1.3.6.1.5.5.7.3.8";
    private static final String EXTENDED_KEY_USAGE_OID = "2.5.29.37";

    public TPKCS7(TInputStream var1) throws TParsingException, TIOException {
        this.version = null;
        this.digestAlgorithmIds = null;
        this.contentInfo = null;
        this.certificates = null;
        this.crls = null;
        this.signerInfos = null;
        this.oldStyle = false;
        TDataInputStream var2 = new TDataInputStream(var1);
        byte[] var3 = new byte[var2.available()];
        var2.readFully(var3);
        this.parse(new TDerInputStream(var3));
    }

    public TPKCS7(TDerInputStream var1) throws TParsingException {
        this.version = null;
        this.digestAlgorithmIds = null;
        this.contentInfo = null;
        this.certificates = null;
        this.crls = null;
        this.signerInfos = null;
        this.oldStyle = false;
        this.parse(var1);
    }

    public TPKCS7(byte[] var1) throws TParsingException {
        this.version = null;
        this.digestAlgorithmIds = null;
        this.contentInfo = null;
        this.certificates = null;
        this.crls = null;
        this.signerInfos = null;
        this.oldStyle = false;

        try {
            TDerInputStream var2 = new TDerInputStream(var1);
            this.parse(var2);
        } catch (TIOException var4) {
            TParsingException var3 = new TParsingException(TString.wrap("Unable to parse the encoded bytes"));
            var3.initCause(var4);
            throw var3;
        }
    }

    private void parse(TDerInputStream var1) throws TParsingException {
        try {
            var1.mark(var1.available());
            this.parse(var1, false);
        } catch (TIOException var6) {
            try {
                var1.reset();
                this.parse(var1, true);
                this.oldStyle = true;
            } catch (TIOException var5) {
                TParsingException var4 = new TParsingException(TString.wrap(var5.getMessage()));
                var4.initCause(var6);
                var4.addSuppressed(var5);
                throw var4;
            }
        }

    }

    private void parse(TDerInputStream var1, boolean var2) throws TIOException {
        this.contentInfo = new TContentInfo(var1, var2);
        this.contentType = this.contentInfo.contentType;
        TDerValue var3 = this.contentInfo.getContent();
        if(this.contentType.equals(TContentInfo.SIGNED_DATA_OID)) {
            this.parseSignedData(var3);
        } else if(this.contentType.equals(TContentInfo.OLD_SIGNED_DATA_OID)) {
            this.parseOldSignedData(var3);
        } else {
            if(!this.contentType.equals(TContentInfo.NETSCAPE_CERT_SEQUENCE_OID)) {
                throw new TParsingException(TString.wrap("content type " + this.contentType + " not supported."));
            }

            this.parseNetscapeCertChain(var3);
        }

    }

    public TPKCS7(TAlgorithmId[] var1, TContentInfo var2, TX509Certificate[] var3, TX509CRL[] var4, TSignerInfo[] var5) {
        this.version = null;
        this.digestAlgorithmIds = null;
        this.contentInfo = null;
        this.certificates = null;
        this.crls = null;
        this.signerInfos = null;
        this.oldStyle = false;
        this.version = TBigInteger.ONE;
        this.digestAlgorithmIds = var1;
        this.contentInfo = var2;
        this.certificates = var3;
        this.crls = var4;
        this.signerInfos = var5;
    }

    public TPKCS7(TAlgorithmId[] var1, TContentInfo var2, TX509Certificate[] var3, TSignerInfo[] var4) {
        this(var1, var2, var3, (TX509CRL[])null, var4);
    }

    private void parseNetscapeCertChain(TDerValue var1) throws TParsingException, TIOException {
        TDerInputStream var2 = new TDerInputStream(var1.toByteArray());
        TDerValue[] var3 = var2.getSequence(2);
        this.certificates = new TX509Certificate[var3.length];
        TCertificateFactory var4 = null;

        try {
            var4 = TCertificateFactory.getInstance("X.509");
        } catch (CertificateException var16) {
            ;
        }

        for(int var5 = 0; var5 < var3.length; ++var5) {
            TByteArrayInputStream var6 = null;

            try {
                TParsingException var8;
                try {
                    if(var4 == null) {
                        this.certificates[var5] = new TX509CertImpl(var3[var5]);
                    } else {
                        byte[] var7 = var3[var5].toByteArray();
                        var6 = new TByteArrayInputStream(var7);
                        this.certificates[var5] = (TX509Certificate)var4.generateCertificate(var6);
                        var6.close();
                        var6 = null;
                    }
                } catch (TCertificateException var14) {
                    var8 = new TParsingException(TString.wrap(var14.getMessage()));
                    var8.initCause(var14);
                    throw var8;
                } catch (TIOException var15) {
                    var8 = new TParsingException(TString.wrap(var15.getMessage()));
                    var8.initCause(var15);
                    throw var8;
                }
            } finally {
                if(var6 != null) {
                    var6.close();
                }

            }
        }

    }

    private void parseSignedData(TDerValue var1) throws TParsingException, TIOException {
        TDerInputStream var2 = var1.toDerInputStream();
        this.version = var2.getBigInteger();
        TDerValue[] var3 = var2.getSet(1);
        int var4 = var3.length;
        this.digestAlgorithmIds = new TAlgorithmId[var4];

        try {
            for(int var5 = 0; var5 < var4; ++var5) {
                TDerValue var36 = var3[var5];
                this.digestAlgorithmIds[var5] = TAlgorithmId.parse(var36);
            }
        } catch (TIOException var34) {
            TParsingException var6 = new TParsingException(TString.wrap("Error parsing digest AlgorithmId IDs: " + var34.getMessage()));
            var6.initCause(var34);
            throw var6;
        }

        this.contentInfo = new TContentInfo(var2);
        TCertificateFactory var35 = null;

        try {
            var35 = TCertificateFactory.getInstance("X.509");
        } catch (CertificateException var29) {
            ;
        }

        TDerValue[] var37;
        int var7;
        if((byte)var2.peekByte() == -96) {
            var37 = var2.getSet(2, true);
            var4 = var37.length;
            this.certificates = new TX509Certificate[var4];
            var7 = 0;

            for(int var8 = 0; var8 < var4; ++var8) {
                ByteArrayInputStream var9 = null;

                try {
                    TParsingException var11;
                    try {
                        byte var10 = var37[var8].getTag();
                        if(var10 == 48) {
                            if(var35 == null) {
                                this.certificates[var7] = new TX509CertImpl(var37[var8]);
                            } else {
                                byte[] var42 = var37[var8].toByteArray();
                                var9 = new ByteArrayInputStream(var42);
                                this.certificates[var7] = (TX509Certificate)var35.generateCertificate(var9);
                                var9.close();
                                var9 = null;
                            }

                            ++var7;
                        }
                    } catch (TCertificateException var31) {
                        var11 = new TParsingException(TString.wrap(var31.getMessage()));
                        var11.initCause(var31);
                        throw var11;
                    } catch (TIOException var32) {
                        var11 = new TParsingException(TString.wrap(var32.getMessage()));
                        var11.initCause(var32);
                        throw var11;
                    }
                } finally {
                    if(var9 != null) {
                        var9.close();
                    }

                }
            }

            if(var7 != var4) {
                this.certificates = (TX509Certificate[]) Arrays.copyOf(this.certificates, var7);
            }
        }

        if((byte)var2.peekByte() == -95) {
            var37 = var2.getSet(1, true);
            var4 = var37.length;
            this.crls = new TX509CRL[var4];

            for(var7 = 0; var7 < var4; ++var7) {
                ByteArrayInputStream var38 = null;

                try {
                    if(var35 == null) {
                        this.crls[var7] = new X509CRLImpl(var37[var7]);
                    } else {
                        byte[] var41 = var37[var7].toByteArray();
                        var38 = new ByteArrayInputStream(var41);
                        this.crls[var7] = (X509CRL)var35.generateCRL(var38);
                        var38.close();
                        var38 = null;
                    }
                } catch (CRLException var28) {
                    ParsingException var40 = new ParsingException(var28.getMessage());
                    var40.initCause(var28);
                    throw var40;
                } finally {
                    if(var38 != null) {
                        var38.close();
                    }

                }
            }
        }

        var37 = var2.getSet(1);
        var4 = var37.length;
        this.signerInfos = new TSignerInfo[var4];

        for(var7 = 0; var7 < var4; ++var7) {
            TDerInputStream var39 = var37[var7].toDerInputStream();
            this.signerInfos[var7] = new TSignerInfo(var39);
        }

    }

    private void parseOldSignedData(TDerValue var1) throws TParsingException, TIOException {
        TDerInputStream var2 = var1.toDerInputStream();
        this.version = var2.getBigInteger();
        TDerValue[] var3 = var2.getSet(1);
        int var4 = var3.length;
        this.digestAlgorithmIds = new TAlgorithmId[var4];

        try {
            for(int var5 = 0; var5 < var4; ++var5) {
                TDerValue var6 = var3[var5];
                this.digestAlgorithmIds[var5] = TAlgorithmId.parse(var6);
            }
        } catch (TIOException var21) {
            throw new TParsingException(TString.wrap("Error parsing digest AlgorithmId IDs"));
        }

        this.contentInfo = new TContentInfo(var2, true);
        TCertificateFactory var22 = null;

        try {
            var22 = TCertificateFactory.getInstance("X.509");
        } catch (CertificateException var19) {
            ;
        }

        TDerValue[] var23 = var2.getSet(2);
        var4 = var23.length;
        this.certificates = new TX509Certificate[var4];

        for(int var7 = 0; var7 < var4; ++var7) {
            ByteArrayInputStream var8 = null;

            try {
                TParsingException var10;
                try {
                    if(var22 == null) {
                        this.certificates[var7] = new TX509CertImpl(var23[var7]);
                    } else {
                        byte[] var9 = var23[var7].toByteArray();
                        var8 = new ByteArrayInputStream(var9);
                        this.certificates[var7] = (TX509Certificate)var22.generateCertificate(var8);
                        var8.close();
                        var8 = null;
                    }
                } catch (CertificateException var17) {
                    var10 = new TParsingException(TString.wrap(var17.getMessage()));
                    var10.initCause(var17);
                    throw var10;
                } catch (IOException var18) {
                    var10 = new TParsingException(TString.wrap(var18.getMessage()));
                    var10.initCause(var18);
                    throw var10;
                }
            } finally {
                if(var8 != null) {
                    var8.close();
                }

            }
        }

        var2.getSet(0);
        TDerValue[] var24 = var2.getSet(1);
        var4 = var24.length;
        this.signerInfos = new TSignerInfo[var4];

        for(int var25 = 0; var25 < var4; ++var25) {
            TDerInputStream var26 = var24[var25].toDerInputStream();
            this.signerInfos[var25] = new TSignerInfo(var26, true);
        }

    }

    public void encodeSignedData(TOutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        this.encodeSignedData(var2);
        var1.write(var2.toByteArray());
    }

    public void encodeSignedData(TDerOutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        var2.putInteger(this.version);
        var2.putOrderedSetOf(49, this.digestAlgorithmIds);
        this.contentInfo.encode(var2);
        if(this.certificates != null && this.certificates.length != 0) {
            TX509CertImpl[] var3 = new TX509CertImpl[this.certificates.length];

            for(int var4 = 0; var4 < this.certificates.length; ++var4) {
                if(this.certificates[var4] instanceof TX509CertImpl) {
                    var3[var4] = (TX509CertImpl)this.certificates[var4];
                } else {
                    try {
                        byte[] var5 = this.certificates[var4].getEncoded();
                        var3[var4] = new TX509CertImpl(var5);
                    } catch (TCertificateException var10) {
                        throw new IOException(var10);
                    }
                }
            }

            var2.putOrderedSetOf(-96, var3);
        }

        if(this.crls != null && this.crls.length != 0) {
            HashSet var11 = new HashSet(this.crls.length);
            X509CRL[] var13 = this.crls;
            int var15 = var13.length;

            for(int var6 = 0; var6 < var15; ++var6) {
                X509CRL var7 = var13[var6];
                if(var7 instanceof X509CRLImpl) {
                    var11.add((X509CRLImpl)var7);
                } else {
                    try {
                        byte[] var8 = var7.getEncoded();
                        var11.add(new X509CRLImpl(var8));
                    } catch (CRLException var9) {
                        throw new IOException(var9);
                    }
                }
            }

            var2.putOrderedSetOf(-95, (DerEncoder[])var11.toArray(new X509CRLImpl[var11.size()]));
        }

        var2.putOrderedSetOf(49, this.signerInfos);
        DerValue var12 = new DerValue(48, var2.toByteArray());
        ContentInfo var14 = new ContentInfo(ContentInfo.SIGNED_DATA_OID, var12);
        var14.encode(var1);
    }

    public TSignerInfo verify(TSignerInfo var1, byte[] var2) throws NoSuchAlgorithmException, SignatureException {
        return var1.verify(this, var2);
    }

    public TSignerInfo[] verify(byte[] var1) throws NoSuchAlgorithmException, SignatureException {
        Vector var2 = new Vector();

        for(int var3 = 0; var3 < this.signerInfos.length; ++var3) {
            TSignerInfo var4 = this.verify(this.signerInfos[var3], var1);
            if(var4 != null) {
                var2.addElement(var4);
            }
        }

        if(!var2.isEmpty()) {
            TSignerInfo[] var5 = new TSignerInfo[var2.size()];
            var2.copyInto(var5);
            return var5;
        } else {
            return null;
        }
    }

    public TSignerInfo[] verify() throws NoSuchAlgorithmException, SignatureException {
        return this.verify((byte[])null);
    }

    public TBigInteger getVersion() {
        return this.version;
    }

    public TAlgorithmId[] getDigestAlgorithmIds() {
        return this.digestAlgorithmIds;
    }

    public TContentInfo getContentInfo() {
        return this.contentInfo;
    }

    public TX509Certificate[] getCertificates() {
        return this.certificates != null?(TX509Certificate[])this.certificates.clone():null;
    }

    public TX509CRL[] getCRLs() {
        return this.crls != null?(TX509CRL[])this.crls.clone():null;
    }

    public TSignerInfo[] getSignerInfos() {
        return this.signerInfos;
    }

    public TX509Certificate getCertificate(BigInteger var1, TX500Name var2) {
        if(this.certificates != null) {
            if(this.certIssuerNames == null) {
                this.populateCertIssuerNames();
            }

            for(int var3 = 0; var3 < this.certificates.length; ++var3) {
                TX509Certificate var4 = this.certificates[var3];
                TBigInteger var5 = var4.getSerialNumber();
                if(var1.equals(var5) && var2.equals(this.certIssuerNames[var3])) {
                    return var4;
                }
            }
        }

        return null;
    }

    private void populateCertIssuerNames() {
        if(this.certificates != null) {
            this.certIssuerNames = new TPrincipal[this.certificates.length];

            for(int var1 = 0; var1 < this.certificates.length; ++var1) {
                TX509Certificate var2 = this.certificates[var1];
                TPrincipal var3 = var2.getIssuerDN();
                if(!(var3 instanceof TX500Name)) {
                    try {
                        TX509CertInfo var4 = new TX509CertInfo(var2.getTBSCertificate());
                        var3 = (TPrincipal)var4.get(TString.wrap("issuer.dname"));
                    } catch (Exception var5) {
                        ;
                    }
                }

                this.certIssuerNames[var1] = var3;
            }

        }
    }

    public String toString() {
        String var1 = "";
        var1 = var1 + this.contentInfo + "\n";
        if(this.version != null) {
            var1 = var1 + "PKCS7 :: version: " + Debug.toHexString(this.version) + "\n";
        }

        int var2;
        if(this.digestAlgorithmIds != null) {
            var1 = var1 + "PKCS7 :: digest AlgorithmIds: \n";

            for(var2 = 0; var2 < this.digestAlgorithmIds.length; ++var2) {
                var1 = var1 + "\t" + this.digestAlgorithmIds[var2] + "\n";
            }
        }

        if(this.certificates != null) {
            var1 = var1 + "PKCS7 :: certificates: \n";

            for(var2 = 0; var2 < this.certificates.length; ++var2) {
                var1 = var1 + "\t" + var2 + ".   " + this.certificates[var2] + "\n";
            }
        }

        if(this.crls != null) {
            var1 = var1 + "PKCS7 :: crls: \n";

            for(var2 = 0; var2 < this.crls.length; ++var2) {
                var1 = var1 + "\t" + var2 + ".   " + this.crls[var2] + "\n";
            }
        }

        if(this.signerInfos != null) {
            var1 = var1 + "PKCS7 :: signer infos: \n";

            for(var2 = 0; var2 < this.signerInfos.length; ++var2) {
                var1 = var1 + "\t" + var2 + ".  " + this.signerInfos[var2] + "\n";
            }
        }

        return var1;
    }

    public boolean isOldStyle() {
        return this.oldStyle;
    }

    public static byte[] generateSignedData(byte[] var0, X509Certificate[] var1, byte[] var2, String var3, URI var4, String var5) throws CertificateException, IOException, NoSuchAlgorithmException {
        PKCS9Attributes var6 = null;
        if(var4 != null) {
            HttpTimestamper var7 = new HttpTimestamper(var4);
            byte[] var8 = generateTimestampToken(var7, var5, var0);
            var6 = new PKCS9Attributes(new PKCS9Attribute[]{new PKCS9Attribute("SignatureTimestampToken", var8)});
        }

        X500Name var17 = X500Name.asX500Name(var1[0].getIssuerX500Principal());
        BigInteger var18 = var1[0].getSerialNumber();
        String var9 = AlgorithmId.getEncAlgFromSigAlg(var3);
        String var10 = AlgorithmId.getDigAlgFromSigAlg(var3);
        SignerInfo var11 = new SignerInfo(var17, var18, AlgorithmId.get(var10), (PKCS9Attributes)null, AlgorithmId.get(var9), var0, var6);
        SignerInfo[] var12 = new SignerInfo[]{var11};
        AlgorithmId[] var13 = new AlgorithmId[]{var11.getDigestAlgorithmId()};
        ContentInfo var14 = var2 == null?new ContentInfo(ContentInfo.DATA_OID, (DerValue)null):new ContentInfo(var2);
        sun.security.pkcs.PKCS7 var15 = new sun.security.pkcs.PKCS7(var13, var14, var1, var12);
        ByteArrayOutputStream var16 = new ByteArrayOutputStream();
        var15.encodeSignedData((OutputStream)var16);
        return var16.toByteArray();
    }

    private static byte[] generateTimestampToken(TTimestamper var0, String var1, byte[] var2) throws IOException, CertificateException {
        MessageDigest var3 = null;
        TTSRequest var4 = null;

        try {
            var3 = MessageDigest.getInstance("SHA-1");
            var4 = new TSRequest(var1, var2, var3);
        } catch (NoSuchAlgorithmException var17) {
            ;
        }

        BigInteger var5 = null;
        if(TPKCS7.SecureRandomHolder.RANDOM != null) {
            var5 = new BigInteger(64, TPKCS7.SecureRandomHolder.RANDOM);
            var4.setNonce(var5);
        }

        var4.requestCertificate(true);
        TTSResponse var6 = var0.generateTimestamp(var4);
        int var7 = var6.getStatusCode();
        if(var7 != 0 && var7 != 1) {
            throw new IOException("Error generating timestamp: " + var6.getStatusCodeAsText() + " " + var6.getFailureCodeAsText());
        } else if(var1 != null && !var1.equals(var6.getTimestampToken().getPolicyID())) {
            throw new IOException("TSAPolicyID changed in timestamp token");
        } else {
            TPKCS7 var8 = var6.getToken();
            TTimestampToken var9 = var6.getTimestampToken();
            if(!var9.getHashAlgorithm().getName().equals("SHA-1")) {
                throw new IOException("Digest algorithm not SHA-1 in timestamp token");
            } else if(!MessageDigest.isEqual(var9.getHashedMessage(), var4.getHashedMessage())) {
                throw new IOException("Digest octets changed in timestamp token");
            } else {
                TBigInteger var10 = var9.getNonce();
                if(var10 == null && var5 != null) {
                    throw new IOException("Nonce missing in timestamp token");
                } else if(var10 != null && !var10.equals(var5)) {
                    throw new IOException("Nonce changed in timestamp token");
                } else {
                    TSignerInfo[] var11 = var8.getSignerInfos();
                    int var12 = var11.length;

                    for(int var13 = 0; var13 < var12; ++var13) {
                        TSignerInfo var14 = var11[var13];
                        X509Certificate var15 = var14.getCertificate(var8);
                        if(var15 == null) {
                            throw new CertificateException("Certificate not included in timestamp token");
                        }

                        if(!var15.getCriticalExtensionOIDs().contains("2.5.29.37")) {
                            throw new CertificateException("Certificate is not valid for timestamping");
                        }

                        List var16 = var15.getExtendedKeyUsage();
                        if(var16 == null || !var16.contains("1.3.6.1.5.5.7.3.8")) {
                            throw new CertificateException("Certificate is not valid for timestamping");
                        }
                    }

                    return var6.getEncodedToken();
                }
            }
        }
    }

    private static class SecureRandomHolder {
        static final SecureRandom RANDOM;

        private SecureRandomHolder() {
        }

        static {
            SecureRandom var0 = null;

            try {
                var0 = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException var2) {
                ;
            }

            RANDOM = var0;
        }
    }
}
