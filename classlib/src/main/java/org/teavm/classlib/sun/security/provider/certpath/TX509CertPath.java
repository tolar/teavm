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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.teavm.classlib.java.io.TByteArrayInputStream;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.security.cert.TCertPath;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.java.util.TList;
import org.teavm.classlib.sun.security.pkcs.TContentInfo;
import org.teavm.classlib.sun.security.pkcs.TPKCS7;
import org.teavm.classlib.sun.security.pkcs.TSignerInfo;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.x509.TAlgorithmId;

public class TX509CertPath extends TCertPath {
    private static final long serialVersionUID = 4989800333263052980L;
    private List<X509Certificate> certs;
    private static final String COUNT_ENCODING = "count";
    private static final String PKCS7_ENCODING = "PKCS7";
    private static final String PKIPATH_ENCODING = "PkiPath";
    private static final Collection<String> encodingList;

    public TX509CertPath(List<? extends Certificate> var1) throws CertificateException {
        super("X.509");
        Iterator var2 = var1.iterator();

        Object var3;
        do {
            if(!var2.hasNext()) {
                this.certs = Collections.unmodifiableList(new ArrayList(var1));
                return;
            }

            var3 = var2.next();
        } while(var3 instanceof X509Certificate);

        throw new CertificateException("List is not all X509Certificates: " + var3.getClass().getName());
    }

    public TX509CertPath(TInputStream var1) throws CertificateException {
        this(var1, "PkiPath");
    }

    public TX509CertPath(TInputStream var1, String var2) throws CertificateException {
        super("X.509");
        byte var4 = -1;
        switch(var2.hashCode()) {
            case 76183020:
                if(var2.equals("PKCS7")) {
                    var4 = 1;
                }
                break;
            case 1148619507:
                if(var2.equals("PkiPath")) {
                    var4 = 0;
                }
        }

        switch(var4) {
            case 0:
                this.certs = parsePKIPATH(var1);
                break;
            case 1:
                this.certs = parsePKCS7(var1);
                break;
            default:
                throw new CertificateException("unsupported encoding");
        }

    }

    private static List<X509Certificate> parsePKIPATH(TInputStream var0) throws CertificateException {
        ArrayList var1 = null;
        CertificateFactory var2 = null;
        if(var0 == null) {
            throw new CertificateException("input stream is null");
        } else {
            try {
                TDerInputStream var3 = new TDerInputStream(readAllBytes(var0));
                TDerValue[] var4 = var3.getSequence(3);
                if(var4.length == 0) {
                    return Collections.emptyList();
                } else {
                    var2 = CertificateFactory.getInstance("X.509");
                    var1 = new ArrayList(var4.length);

                    for(int var5 = var4.length - 1; var5 >= 0; --var5) {
                        var1.add((X509Certificate)var2.generateCertificate(new ByteArrayInputStream(var4[var5].toByteArray())));
                    }

                    return Collections.unmodifiableList(var1);
                }
            } catch (IOException var6) {
                throw new CertificateException("IOException parsing PkiPath data: " + var6, var6);
            }
        }
    }

    private static List<X509Certificate> parsePKCS7(TInputStream var0) throws CertificateException {
        if(var0 == null) {
            throw new CertificateException("input stream is null");
        } else {
            Object var1;
            try {
                if(!((TInputStream)var0).markSupported()) {
                    var0 = new TByteArrayInputStream(readAllBytes((TInputStream)var0));
                }

                TPKCS7 var2 = new TPKCS7((TInputStream)var0);
                TX509Certificate[] var3 = var2.getCertificates();
                if(var3 != null) {
                    var1 = Arrays.asList(var3);
                } else {
                    var1 = new ArrayList(0);
                }
            } catch (IOException var4) {
                throw new CertificateException("IOException parsing PKCS7 data: " + var4);
            }

            return Collections.unmodifiableList((List)var1);
        }
    }

    private static byte[] readAllBytes(TInputStream var0) throws IOException {
        byte[] var1 = new byte[8192];
        ByteArrayOutputStream var2 = new ByteArrayOutputStream(2048);

        int var3;
        while((var3 = var0.read(var1)) != -1) {
            var2.write(var1, 0, var3);
        }

        return var2.toByteArray();
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        return this.encodePKIPATH();
    }

    private byte[] encodePKIPATH() throws CertificateEncodingException {
        ListIterator var1 = this.certs.listIterator(this.certs.size());

        try {
            TDerOutputStream var2 = new TDerOutputStream();

            while(var1.hasPrevious()) {
                X509Certificate var3 = (X509Certificate)var1.previous();
                if(this.certs.lastIndexOf(var3) != this.certs.indexOf(var3)) {
                    throw new CertificateEncodingException("Duplicate Certificate");
                }

                byte[] var4 = var3.getEncoded();
                var2.write(var4);
            }

            TDerOutputStream var6 = new TDerOutputStream();
            var6.write((byte)48, var2);
            return var6.toByteArray();
        } catch (TIOException var5) {
            throw new CertificateEncodingException("IOException encoding PkiPath data: " + var5, var5);
        }
    }

    private byte[] encodePKCS7() throws CertificateEncodingException {
        TPKCS7 var1 = new TPKCS7(new TAlgorithmId[0], new TContentInfo(TContentInfo.DATA_OID, (TDerValue)null), (TX509Certificate[])this.certs.toArray(new TX509Certificate[this.certs.size()]), new TSignerInfo[0]);
        TDerOutputStream var2 = new TDerOutputStream();

        try {
            var1.encodeSignedData(var2);
        } catch (IOException var4) {
            throw new CertificateEncodingException(var4.getMessage());
        }

        return var2.toByteArray();
    }

    public byte[] getEncoded(String var1) throws CertificateEncodingException {
        byte var3 = -1;
        switch(var1.hashCode()) {
            case 76183020:
                if(var1.equals("PKCS7")) {
                    var3 = 1;
                }
                break;
            case 1148619507:
                if(var1.equals("PkiPath")) {
                    var3 = 0;
                }
        }

        switch(var3) {
            case 0:
                return this.encodePKIPATH();
            case 1:
                return this.encodePKCS7();
            default:
                throw new CertificateEncodingException("unsupported encoding");
        }
    }

    public static Iterator<String> getEncodingsStatic() {
        return encodingList.iterator();
    }

    public Iterator<String> getEncodings() {
        return getEncodingsStatic();
    }

    public TList<TX509Certificate> getCertificates() {
        return this.certs;
    }

    static {
        ArrayList var0 = new ArrayList(2);
        var0.add("PkiPath");
        var0.add("PKCS7");
        encodingList = Collections.unmodifiableCollection(var0);
    }
}
