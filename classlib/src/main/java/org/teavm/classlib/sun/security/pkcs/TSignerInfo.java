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

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.x509.TAlgorithmId;
import org.teavm.classlib.sun.security.x509.TX500Name;

import sun.misc.HexDumpEncoder;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.pkcs.PKCS9Attributes;
import sun.security.pkcs.ParsingException;
import sun.security.timestamp.TimestampToken;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.X500Name;

public class TSignerInfo implements DerEncoder {
    TBigInteger version;
    TX500Name issuerName;
    TBigInteger certificateSerialNumber;
    TAlgorithmId digestAlgorithmId;
    TAlgorithmId digestEncryptionAlgorithmId;
    byte[] encryptedDigest;
    Timestamp timestamp;
    private boolean hasTimestamp;
    private static final Debug debug = Debug.getInstance("jar");
    TPKCS9Attributes authenticatedAttributes;
    TPKCS9Attributes unauthenticatedAttributes;

    public TSignerInfo(TX500Name var1, TBigInteger var2, TAlgorithmId var3, TAlgorithmId var4, byte[] var5) {
        this.hasTimestamp = true;
        this.version = TBigInteger.ONE;
        this.issuerName = var1;
        this.certificateSerialNumber = var2;
        this.digestAlgorithmId = var3;
        this.digestEncryptionAlgorithmId = var4;
        this.encryptedDigest = var5;
    }

    public TSignerInfo(TX500Name var1, TBigInteger var2, TAlgorithmId var3, TPKCS9Attributes var4, TAlgorithmId var5, byte[] var6, TPKCS9Attributes var7) {
        this.hasTimestamp = true;
        this.version = TBigInteger.ONE;
        this.issuerName = var1;
        this.certificateSerialNumber = var2;
        this.digestAlgorithmId = var3;
        this.authenticatedAttributes = var4;
        this.digestEncryptionAlgorithmId = var5;
        this.encryptedDigest = var6;
        this.unauthenticatedAttributes = var7;
    }

    public TSignerInfo(TDerInputStream var1) throws IOException, ParsingException {
        this(var1, false);
    }

    public TSignerInfo(TDerInputStream var1, boolean var2) throws IOException, ParsingException {
        this.hasTimestamp = true;
        this.version = var1.getBigInteger();
        TDerValue[] var3 = var1.getSequence(2);
        byte[] var4 = var3[0].toByteArray();
        this.issuerName = new TX500Name(new TDerValue((byte)48, var4));
        this.certificateSerialNumber = var3[1].getBigInteger();
        TDerValue var5 = var1.getDerValue();
        this.digestAlgorithmId = TAlgorithmId.parse(var5);
        if(var2) {
            var1.getSet(0);
        } else if((byte)var1.peekByte() == -96) {
            this.authenticatedAttributes = new TPKCS9Attributes(var1);
        }

        var5 = var1.getDerValue();
        this.digestEncryptionAlgorithmId = TAlgorithmId.parse(var5);
        this.encryptedDigest = var1.getOctetString();
        if(var2) {
            var1.getSet(0);
        } else if(var1.available() != 0 && (byte)var1.peekByte() == -95) {
            this.unauthenticatedAttributes = new TPKCS9Attributes(var1, true);
        }

        if(var1.available() != 0) {
            throw new ParsingException("extra data at the end");
        }
    }

    public void encode(TDerOutputStream var1) throws IOException {
        this.derEncode(var1);
    }

    public void derEncode(TOutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        var2.putInteger(this.version);
        TDerOutputStream var3 = new TDerOutputStream();
        this.issuerName.encode(var3);
        var3.putInteger(this.certificateSerialNumber);
        var2.write(48, var3);
        this.digestAlgorithmId.encode(var2);
        if(this.authenticatedAttributes != null) {
            this.authenticatedAttributes.encode(-96, var2);
        }

        this.digestEncryptionAlgorithmId.encode(var2);
        var2.putOctetString(this.encryptedDigest);
        if(this.unauthenticatedAttributes != null) {
            this.unauthenticatedAttributes.encode(-95, var2);
        }

        TDerOutputStream var4 = new TDerOutputStream();
        var4.write(48, var2);
        var1.write(var4.toByteArray());
    }

    public X509Certificate getCertificate(PKCS7 var1) throws IOException {
        return var1.getCertificate(this.certificateSerialNumber, this.issuerName);
    }

    public ArrayList<X509Certificate> getCertificateChain(PKCS7 var1) throws IOException {
        X509Certificate var2 = var1.getCertificate(this.certificateSerialNumber, this.issuerName);
        if(var2 == null) {
            return null;
        } else {
            ArrayList var3 = new ArrayList();
            var3.add(var2);
            X509Certificate[] var4 = var1.getCertificates();
            if(var4 != null && !var2.getSubjectDN().equals(var2.getIssuerDN())) {
                Principal var5 = var2.getIssuerDN();
                int var6 = 0;

                boolean var7;
                do {
                    var7 = false;

                    for(int var8 = var6; var8 < var4.length; ++var8) {
                        if(var5.equals(var4[var8].getSubjectDN())) {
                            var3.add(var4[var8]);
                            if(var4[var8].getSubjectDN().equals(var4[var8].getIssuerDN())) {
                                var6 = var4.length;
                            } else {
                                var5 = var4[var8].getIssuerDN();
                                X509Certificate var9 = var4[var6];
                                var4[var6] = var4[var8];
                                var4[var8] = var9;
                                ++var6;
                            }

                            var7 = true;
                            break;
                        }
                    }
                } while(var7);

                return var3;
            } else {
                return var3;
            }
        }
    }

    sun.security.pkcs.SignerInfo verify(PKCS7 var1, byte[] var2) throws NoSuchAlgorithmException, SignatureException {
        try {
            ContentInfo var3 = var1.getContentInfo();
            if(var2 == null) {
                var2 = var3.getContentBytes();
            }

            String var4 = this.getDigestAlgorithmId().getName();
            byte[] var5;
            if(this.authenticatedAttributes == null) {
                var5 = var2;
            } else {
                ObjectIdentifier var6 = (ObjectIdentifier)this.authenticatedAttributes.getAttributeValue(PKCS9Attribute.CONTENT_TYPE_OID);
                if(var6 == null || !var6.equals(var3.contentType)) {
                    return null;
                }

                byte[] var7 = (byte[])((byte[])this.authenticatedAttributes.getAttributeValue(PKCS9Attribute.MESSAGE_DIGEST_OID));
                if(var7 == null) {
                    return null;
                }

                MessageDigest var8 = MessageDigest.getInstance(var4);
                byte[] var9 = var8.digest(var2);
                if(var7.length != var9.length) {
                    return null;
                }

                for(int var10 = 0; var10 < var7.length; ++var10) {
                    if(var7[var10] != var9[var10]) {
                        return null;
                    }
                }

                var5 = this.authenticatedAttributes.getDerEncoding();
            }

            String var18 = this.getDigestEncryptionAlgorithmId().getName();
            String var19 = AlgorithmId.getEncAlgFromSigAlg(var18);
            if(var19 != null) {
                var18 = var19;
            }

            String var20 = AlgorithmId.makeSigAlg(var4, var18);
            Signature var21 = Signature.getInstance(var20);
            X509Certificate var22 = this.getCertificate(var1);
            if(var22 == null) {
                return null;
            } else if(var22.hasUnsupportedCriticalExtension()) {
                throw new SignatureException("Certificate has unsupported critical extension(s)");
            } else {
                boolean[] var11 = var22.getKeyUsage();
                if(var11 != null) {
                    KeyUsageExtension var12;
                    try {
                        var12 = new KeyUsageExtension(var11);
                    } catch (IOException var15) {
                        throw new SignatureException("Failed to parse keyUsage extension");
                    }

                    boolean var13 = var12.get("digital_signature").booleanValue();
                    boolean var14 = var12.get("non_repudiation").booleanValue();
                    if(!var13 && !var14) {
                        throw new SignatureException("Key usage restricted: cannot be used for digital signatures");
                    }
                }

                PublicKey var23 = var22.getPublicKey();
                var21.initVerify(var23);
                var21.update(var5);
                if(var21.verify(this.encryptedDigest)) {
                    return this;
                } else {
                    return null;
                }
            }
        } catch (IOException var16) {
            throw new SignatureException("IO error verifying signature:\n" + var16.getMessage());
        } catch (InvalidKeyException var17) {
            throw new SignatureException("InvalidKey: " + var17.getMessage());
        }
    }

    sun.security.pkcs.SignerInfo verify(PKCS7 var1) throws NoSuchAlgorithmException, SignatureException {
        return this.verify(var1, (byte[])null);
    }

    public BigInteger getVersion() {
        return this.version;
    }

    public X500Name getIssuerName() {
        return this.issuerName;
    }

    public BigInteger getCertificateSerialNumber() {
        return this.certificateSerialNumber;
    }

    public AlgorithmId getDigestAlgorithmId() {
        return this.digestAlgorithmId;
    }

    public PKCS9Attributes getAuthenticatedAttributes() {
        return this.authenticatedAttributes;
    }

    public AlgorithmId getDigestEncryptionAlgorithmId() {
        return this.digestEncryptionAlgorithmId;
    }

    public byte[] getEncryptedDigest() {
        return this.encryptedDigest;
    }

    public PKCS9Attributes getUnauthenticatedAttributes() {
        return this.unauthenticatedAttributes;
    }

    public Timestamp getTimestamp() throws IOException, NoSuchAlgorithmException, SignatureException,
            CertificateException {
        if(this.timestamp == null && this.hasTimestamp) {
            if(this.unauthenticatedAttributes == null) {
                this.hasTimestamp = false;
                return null;
            } else {
                PKCS9Attribute var1 = this.unauthenticatedAttributes.getAttribute(PKCS9Attribute.SIGNATURE_TIMESTAMP_TOKEN_OID);
                if(var1 == null) {
                    this.hasTimestamp = false;
                    return null;
                } else {
                    PKCS7 var2 = new PKCS7((byte[])((byte[])var1.getValue()));
                    byte[] var3 = var2.getContentInfo().getData();
                    sun.security.pkcs.SignerInfo[] var4 = var2.verify(var3);
                    ArrayList var5 = var4[0].getCertificateChain(var2);
                    CertificateFactory var6 = CertificateFactory.getInstance("X.509");
                    CertPath var7 = var6.generateCertPath(var5);
                    TimestampToken var8 = new TimestampToken(var3);
                    this.verifyTimestamp(var8);
                    this.timestamp = new Timestamp(var8.getDate(), var7);
                    return this.timestamp;
                }
            }
        } else {
            return this.timestamp;
        }
    }

    private void verifyTimestamp(TimestampToken var1) throws NoSuchAlgorithmException, SignatureException {
        MessageDigest var2 = MessageDigest.getInstance(var1.getHashAlgorithm().getName());
        if(!Arrays.equals(var1.getHashedMessage(), var2.digest(this.encryptedDigest))) {
            throw new SignatureException("Signature timestamp (#" + var1.getSerialNumber() + ") generated on " + var1.getDate() + " is inapplicable");
        } else {
            if(debug != null) {
                debug.println();
                debug.println("Detected signature timestamp (#" + var1.getSerialNumber() + ") generated on " + var1.getDate());
                debug.println();
            }

        }
    }

    public String toString() {
        HexDumpEncoder var1 = new HexDumpEncoder();
        String var2 = "";
        var2 = var2 + "Signer Info for (issuer): " + this.issuerName + "\n";
        var2 = var2 + "\tversion: " + Debug.toHexString(this.version) + "\n";
        var2 = var2 + "\tcertificateSerialNumber: " + Debug.toHexString(this.certificateSerialNumber) + "\n";
        var2 = var2 + "\tdigestAlgorithmId: " + this.digestAlgorithmId + "\n";
        if(this.authenticatedAttributes != null) {
            var2 = var2 + "\tauthenticatedAttributes: " + this.authenticatedAttributes + "\n";
        }

        var2 = var2 + "\tdigestEncryptionAlgorithmId: " + this.digestEncryptionAlgorithmId + "\n";
        var2 = var2 + "\tencryptedDigest: \n" + var1.encodeBuffer(this.encryptedDigest) + "\n";
        if(this.unauthenticatedAttributes != null) {
            var2 = var2 + "\tunauthenticatedAttributes: " + this.unauthenticatedAttributes + "\n";
        }

        return var2;
    }
}
