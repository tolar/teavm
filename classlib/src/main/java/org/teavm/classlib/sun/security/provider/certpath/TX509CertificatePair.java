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
package org.teavm.classlib.sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.interfaces.DSAPublicKey;

import org.teavm.classlib.java.security.TGeneralSecurityException;
import org.teavm.classlib.java.security.TPublicKey;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.javax.auth.x500.TX500Principal;
import org.teavm.classlib.sun.security.provider.TX509Factory;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.x509.TX509CertImpl;
import org.teavm.classlib.sun.util.TCache;

public class TX509CertificatePair {
    private static final byte TAG_FORWARD = 0;
    private static final byte TAG_REVERSE = 1;
    private TX509Certificate forward;
    private TX509Certificate reverse;
    private byte[] encoded;
    private static final TCache<Object, TX509CertificatePair> cache = TCache.newSoftMemoryCache(750);

    public TX509CertificatePair() {
    }

    public TX509CertificatePair(TX509Certificate var1, TX509Certificate var2) throws CertificateException {
        if(var1 == null && var2 == null) {
            throw new CertificateException("at least one of certificate pair must be non-null");
        } else {
            this.forward = var1;
            this.reverse = var2;
            this.checkPair();
        }
    }

    private TX509CertificatePair(byte[] var1) throws CertificateException {
        try {
            this.parse(new TDerValue(var1));
            this.encoded = var1;
        } catch (IOException var3) {
            throw new CertificateException(var3.toString());
        }

        this.checkPair();
    }

    public static synchronized void clearCache() {
        cache.clear();
    }

    public static synchronized TX509CertificatePair generateCertificatePair(byte[] var0) throws CertificateException {
        TCache.EqualByteArray var1 = new TCache.EqualByteArray(var0);
        TX509CertificatePair var2 = (TX509CertificatePair)cache.get(var1);
        if(var2 != null) {
            return var2;
        } else {
            var2 = new TX509CertificatePair(var0);
            var1 = new TCache.EqualByteArray(var2.encoded);
            cache.put(var1, var2);
            return var2;
        }
    }

    public void setForward(TX509Certificate var1) throws CertificateException {
        this.checkPair();
        this.forward = var1;
    }

    public void setReverse(TX509Certificate var1) throws CertificateException {
        this.checkPair();
        this.reverse = var1;
    }

    public TX509Certificate getForward() {
        return this.forward;
    }

    public TX509Certificate getReverse() {
        return this.reverse;
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        try {
            if(this.encoded == null) {
                TDerOutputStream var1 = new TDerOutputStream();
                this.emit(var1);
                this.encoded = var1.toByteArray();
            }
        } catch (IOException var2) {
            throw new CertificateEncodingException(var2.toString());
        }

        return this.encoded;
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        var1.append("X.509 Certificate Pair: [\n");
        if(this.forward != null) {
            var1.append("  Forward: ").append(this.forward).append("\n");
        }

        if(this.reverse != null) {
            var1.append("  Reverse: ").append(this.reverse).append("\n");
        }

        var1.append("]");
        return var1.toString();
    }

    private void parse(TDerValue var1) throws IOException, CertificateException {
        if(var1.tag != 48) {
            throw new IOException("Sequence tag missing for X509CertificatePair");
        } else {
            while(var1.data != null && var1.data.available() != 0) {
                TDerValue var2 = var1.data.getDerValue();
                short var3 = (short)((byte)(var2.tag & 31));
                switch(var3) {
                    case 0:
                        if(var2.isContextSpecific() && var2.isConstructed()) {
                            if(this.forward != null) {
                                throw new IOException("Duplicate forward certificate in X509CertificatePair");
                            }

                            var2 = var2.data.getDerValue();
                            this.forward = TX509Factory.intern(new TX509CertImpl(var2.toByteArray()));
                        }
                        break;
                    case 1:
                        if(var2.isContextSpecific() && var2.isConstructed()) {
                            if(this.reverse != null) {
                                throw new IOException("Duplicate reverse certificate in X509CertificatePair");
                            }

                            var2 = var2.data.getDerValue();
                            this.reverse = TX509Factory.intern(new TX509CertImpl(var2.toByteArray()));
                        }
                        break;
                    default:
                        throw new IOException("Invalid encoding of X509CertificatePair");
                }
            }

            if(this.forward == null && this.reverse == null) {
                throw new CertificateException("at least one of certificate pair must be non-null");
            }
        }
    }

    private void emit(TDerOutputStream var1) throws IOException, CertificateEncodingException {
        TDerOutputStream var2 = new TDerOutputStream();
        TDerOutputStream var3;
        if(this.forward != null) {
            var3 = new TDerOutputStream();
            var3.putDerValue(new TDerValue(this.forward.getEncoded()));
            var2.write(TDerValue.createTag((byte)-128, true, (byte)0), var3);
        }

        if(this.reverse != null) {
            var3 = new TDerOutputStream();
            var3.putDerValue(new TDerValue(this.reverse.getEncoded()));
            var2.write(TDerValue.createTag((byte)-128, true, (byte)1), var3);
        }

        var1.write((byte)48, var2);
    }

    private void checkPair() throws CertificateException {
        if(this.forward != null && this.reverse != null) {
            TX500Principal var1 = this.forward.getSubjectX500Principal();
            TX500Principal var2 = this.forward.getIssuerX500Principal();
            TX500Principal var3 = this.reverse.getSubjectX500Principal();
            TX500Principal var4 = this.reverse.getIssuerX500Principal();
            if(var2.equals(var3) && var4.equals(var1)) {
                try {
                    TPublicKey var5 = this.reverse.getPublicKey();
                    if(!(var5 instanceof DSAPublicKey) || ((DSAPublicKey)var5).getParams() != null) {
                        this.forward.verify(var5);
                    }

                    var5 = this.forward.getPublicKey();
                    if(!(var5 instanceof DSAPublicKey) || ((DSAPublicKey)var5).getParams() != null) {
                        this.reverse.verify(var5);
                    }

                } catch (TGeneralSecurityException var6) {
                    throw new CertificateException("invalid signature: " + var6.getMessage());
                }
            } else {
                throw new CertificateException("subject and issuer names in forward and reverse certificates do not match");
            }
        }
    }
}
