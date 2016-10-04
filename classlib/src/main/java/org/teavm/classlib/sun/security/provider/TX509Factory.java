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
package org.teavm.classlib.sun.security.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.security.cert.TCertPath;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.security.cert.TCertificateFactorySpi;
import org.teavm.classlib.java.security.cert.TX509CRL;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.sun.security.x509.TX509CRLImpl;
import org.teavm.classlib.sun.security.x509.TX509CertImpl;
import org.teavm.classlib.sun.util.TCache;

public class TX509Factory extends TCertificateFactorySpi {
    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    private static final int ENC_MAX_LENGTH = 4194304;
    private static final TCache<Object, TX509CertImpl> certCache = TCache.newSoftMemoryCache(750);
    private static final TCache<Object, TX509CRLImpl> crlCache = TCache.newSoftMemoryCache(750);

    public TX509Factory() {
    }

    public Certificate engineGenerateCertificate(InputStream var1) throws CertificateException {
        if(var1 == null) {
            certCache.clear();
            X509CertificatePair.clearCache();
            throw new CertificateException("Missing input stream");
        } else {
            try {
                byte[] var2 = readOneBlock(var1);
                if(var2 != null) {
                    X509CertImpl var3 = (X509CertImpl)getFromCache(certCache, var2);
                    if(var3 != null) {
                        return var3;
                    } else {
                        var3 = new X509CertImpl(var2);
                        addToCache(certCache, var3.getEncodedInternal(), var3);
                        return var3;
                    }
                } else {
                    throw new IOException("Empty input");
                }
            } catch (IOException var4) {
                throw new CertificateException("Could not parse certificate: " + var4.toString(), var4);
            }
        }
    }

    private static int readFully(InputStream var0, ByteArrayOutputStream var1, int var2) throws IOException {
        int var3 = 0;

        int var5;
        for(byte[] var4 = new byte[2048]; var2 > 0; var2 -= var5) {
            var5 = var0.read(var4, 0, var2 < 2048?var2:2048);
            if(var5 <= 0) {
                break;
            }

            var1.write(var4, 0, var5);
            var3 += var5;
        }

        return var3;
    }

    public static synchronized TX509CertImpl intern(TX509Certificate var0) throws TCertificateException {
        if(var0 == null) {
            return null;
        } else {
            boolean var1 = var0 instanceof TX509CertImpl;
            byte[] var2;
            if(var1) {
                var2 = ((TX509CertImpl)var0).getEncodedInternal();
            } else {
                var2 = var0.getEncoded();
            }

            TX509CertImpl var3 = (TX509CertImpl)getFromCache(certCache, var2);
            if(var3 != null) {
                return var3;
            } else {
                if(var1) {
                    var3 = (TX509CertImpl)var0;
                } else {
                    var3 = new TX509CertImpl(var2);
                    var2 = var3.getEncodedInternal();
                }

                addToCache(certCache, var2, var3);
                return var3;
            }
        }
    }

    public static synchronized TX509CRLImpl intern(TX509CRL var0) throws CRLException {
        if(var0 == null) {
            return null;
        } else {
            boolean var1 = var0 instanceof TX509CRLImpl;
            byte[] var2;
            if(var1) {
                var2 = ((TX509CRLImpl)var0).getEncodedInternal();
            } else {
                var2 = var0.getEncoded();
            }

            TX509CRLImpl var3 = (TX509CRLImpl)getFromCache(crlCache, var2);
            if(var3 != null) {
                return var3;
            } else {
                if(var1) {
                    var3 = (TX509CRLImpl)var0;
                } else {
                    var3 = new TX509CRLImpl(var2);
                    var2 = var3.getEncodedInternal();
                }

                addToCache(crlCache, var2, var3);
                return var3;
            }
        }
    }

    private static synchronized <K, V> V getFromCache(TCache<K, V> var0, byte[] var1) {
        TCache.EqualByteArray var2 = new TCache.EqualByteArray(var1);
        return var0.get(var2);
    }

    private static synchronized <V> void addToCache(TCache<Object, V> var0, byte[] var1, V var2) {
        if(var1.length <= 4194304) {
            TCache.EqualByteArray var3 = new TCache.EqualByteArray(var1);
            var0.put(var3, var2);
        }
    }

    public TCertPath engineGenerateCertPath(InputStream var1) throws CertificateException {
        if(var1 == null) {
            throw new CertificateException("Missing input stream");
        } else {
            try {
                byte[] var2 = readOneBlock(var1);
                if(var2 != null) {
                    return new X509CertPath(new ByteArrayInputStream(var2));
                } else {
                    throw new TIOException("Empty input");
                }
            } catch (IOException var3) {
                throw new CertificateException(var3.getMessage());
            }
        }
    }

    public CertPath engineGenerateCertPath(InputStream var1, String var2) throws CertificateException {
        if(var1 == null) {
            throw new CertificateException("Missing input stream");
        } else {
            try {
                byte[] var3 = readOneBlock(var1);
                if(var3 != null) {
                    return new X509CertPath(new ByteArrayInputStream(var3), var2);
                } else {
                    throw new IOException("Empty input");
                }
            } catch (IOException var4) {
                throw new CertificateException(var4.getMessage());
            }
        }
    }

    public CertPath engineGenerateCertPath(List<? extends Certificate> var1) throws CertificateException {
        return new X509CertPath(var1);
    }

    public Iterator<String> engineGetCertPathEncodings() {
        return X509CertPath.getEncodingsStatic();
    }

    public Collection<? extends Certificate> engineGenerateCertificates(InputStream var1) throws CertificateException {
        if(var1 == null) {
            throw new CertificateException("Missing input stream");
        } else {
            try {
                return this.parseX509orPKCS7Cert(var1);
            } catch (IOException var3) {
                throw new CertificateException(var3);
            }
        }
    }

    public CRL engineGenerateCRL(InputStream var1) throws CRLException {
        if(var1 == null) {
            crlCache.clear();
            throw new CRLException("Missing input stream");
        } else {
            try {
                byte[] var2 = readOneBlock(var1);
                if(var2 != null) {
                    X509CRLImpl var3 = (X509CRLImpl)getFromCache(crlCache, var2);
                    if(var3 != null) {
                        return var3;
                    } else {
                        var3 = new X509CRLImpl(var2);
                        addToCache(crlCache, var3.getEncodedInternal(), var3);
                        return var3;
                    }
                } else {
                    throw new IOException("Empty input");
                }
            } catch (IOException var4) {
                throw new CRLException(var4.getMessage());
            }
        }
    }

    public Collection<? extends CRL> engineGenerateCRLs(InputStream var1) throws CRLException {
        if(var1 == null) {
            throw new CRLException("Missing input stream");
        } else {
            try {
                return this.parseX509orPKCS7CRL(var1);
            } catch (IOException var3) {
                throw new CRLException(var3.getMessage());
            }
        }
    }

    private Collection<? extends Certificate> parseX509orPKCS7Cert(InputStream var1) throws CertificateException, IOException {
        PushbackInputStream var4 = new PushbackInputStream(var1);
        ArrayList var5 = new ArrayList();
        int var2 = var4.read();
        if(var2 == -1) {
            return new ArrayList(0);
        } else {
            var4.unread(var2);
            byte[] var3 = readOneBlock(var4);
            if(var3 == null) {
                throw new CertificateException("No certificate data found");
            } else {
                try {
                    PKCS7 var6 = new PKCS7(var3);
                    X509Certificate[] var7 = var6.getCertificates();
                    return (Collection)(var7 != null? Arrays.asList(var7):new ArrayList(0));
                } catch (ParsingException var8) {
                    while(var3 != null) {
                        var5.add(new X509CertImpl(var3));
                        var3 = readOneBlock(var4);
                    }

                    return var5;
                }
            }
        }
    }

    private Collection<? extends CRL> parseX509orPKCS7CRL(InputStream var1) throws CRLException, IOException {
        PushbackInputStream var4 = new PushbackInputStream(var1);
        ArrayList var5 = new ArrayList();
        int var2 = var4.read();
        if(var2 == -1) {
            return new ArrayList(0);
        } else {
            var4.unread(var2);
            byte[] var3 = readOneBlock(var4);
            if(var3 == null) {
                throw new CRLException("No CRL data found");
            } else {
                try {
                    PKCS7 var6 = new PKCS7(var3);
                    X509CRL[] var7 = var6.getCRLs();
                    return (Collection)(var7 != null?Arrays.asList(var7):new ArrayList(0));
                } catch (ParsingException var8) {
                    while(var3 != null) {
                        var5.add(new X509CRLImpl(var3));
                        var3 = readOneBlock(var4);
                    }

                    return var5;
                }
            }
        }
    }

    private static byte[] readOneBlock(InputStream var0) throws IOException {
        int var1 = var0.read();
        if(var1 == -1) {
            return null;
        } else if(var1 == 48) {
            ByteArrayOutputStream var10 = new ByteArrayOutputStream(2048);
            var10.write(var1);
            readBERInternal(var0, var10, var1);
            return var10.toByteArray();
        } else {
            char[] var2 = new char[2048];
            int var3 = 0;
            int var4 = var1 == 45?1:0;
            int var5 = var1 == 45?-1:var1;

            do {
                int var6 = var0.read();
                if(var6 == -1) {
                    return null;
                }

                if(var6 == 45) {
                    ++var4;
                } else {
                    var4 = 0;
                    var5 = var6;
                }
            } while(var4 != 5 || var5 != -1 && var5 != 13 && var5 != 10);

            StringBuilder var7 = new StringBuilder("-----");

            int var8;
            byte var11;
            while(true) {
                var8 = var0.read();
                if(var8 == -1) {
                    throw new IOException("Incomplete data");
                }

                if(var8 == 10) {
                    var11 = 10;
                    break;
                }

                if(var8 == 13) {
                    var8 = var0.read();
                    if(var8 == -1) {
                        throw new IOException("Incomplete data");
                    }

                    if(var8 == 10) {
                        var11 = 10;
                    } else {
                        var11 = 13;
                        var2[var3++] = (char)var8;
                    }
                    break;
                }

                var7.append((char)var8);
            }

            while(true) {
                var8 = var0.read();
                if(var8 == -1) {
                    throw new IOException("Incomplete data");
                }

                if(var8 == 45) {
                    StringBuilder var12 = new StringBuilder("-");

                    while(true) {
                        int var9 = var0.read();
                        if(var9 == -1 || var9 == var11 || var9 == 10) {
                            checkHeaderFooter(var7.toString(), var12.toString());
                            return Base64.getMimeDecoder().decode(new String(var2, 0, var3));
                        }

                        if(var9 != 13) {
                            var12.append((char)var9);
                        }
                    }
                }

                var2[var3++] = (char)var8;
                if(var3 >= var2.length) {
                    var2 = Arrays.copyOf(var2, var2.length + 1024);
                }
            }
        }
    }

    private static void checkHeaderFooter(String var0, String var1) throws IOException {
        if(var0.length() >= 16 && var0.startsWith("-----BEGIN ") && var0.endsWith("-----")) {
            if(var1.length() >= 14 && var1.startsWith("-----END ") && var1.endsWith("-----")) {
                String var2 = var0.substring(11, var0.length() - 5);
                String var3 = var1.substring(9, var1.length() - 5);
                if(!var2.equals(var3)) {
                    throw new IOException("Header and footer do not match: " + var0 + " " + var1);
                }
            } else {
                throw new IOException("Illegal footer: " + var1);
            }
        } else {
            throw new IOException("Illegal header: " + var0);
        }
    }

    private static int readBERInternal(InputStream var0, ByteArrayOutputStream var1, int var2) throws IOException {
        if(var2 == -1) {
            var2 = var0.read();
            if(var2 == -1) {
                throw new IOException("BER/DER tag info absent");
            }

            if((var2 & 31) == 31) {
                throw new IOException("Multi octets tag not supported");
            }

            var1.write(var2);
        }

        int var3 = var0.read();
        if(var3 == -1) {
            throw new IOException("BER/DER length info absent");
        } else {
            var1.write(var3);
            int var5;
            if(var3 == 128) {
                if((var2 & 32) != 32) {
                    throw new IOException("Non constructed encoding must have definite length");
                }

                do {
                    var5 = readBERInternal(var0, var1, -1);
                } while(var5 != 0);
            } else {
                int var4;
                if(var3 < 128) {
                    var4 = var3;
                } else if(var3 == 129) {
                    var4 = var0.read();
                    if(var4 == -1) {
                        throw new IOException("Incomplete BER/DER length info");
                    }

                    var1.write(var4);
                } else {
                    int var6;
                    if(var3 == 130) {
                        var5 = var0.read();
                        var6 = var0.read();
                        if(var6 == -1) {
                            throw new IOException("Incomplete BER/DER length info");
                        }

                        var1.write(var5);
                        var1.write(var6);
                        var4 = var5 << 8 | var6;
                    } else {
                        int var7;
                        if(var3 == 131) {
                            var5 = var0.read();
                            var6 = var0.read();
                            var7 = var0.read();
                            if(var7 == -1) {
                                throw new IOException("Incomplete BER/DER length info");
                            }

                            var1.write(var5);
                            var1.write(var6);
                            var1.write(var7);
                            var4 = var5 << 16 | var6 << 8 | var7;
                        } else {
                            if(var3 != 132) {
                                throw new IOException("Invalid BER/DER data (too huge?)");
                            }

                            var5 = var0.read();
                            var6 = var0.read();
                            var7 = var0.read();
                            int var8 = var0.read();
                            if(var8 == -1) {
                                throw new IOException("Incomplete BER/DER length info");
                            }

                            if(var5 > 127) {
                                throw new IOException("Invalid BER/DER data (a little huge?)");
                            }

                            var1.write(var5);
                            var1.write(var6);
                            var1.write(var7);
                            var1.write(var8);
                            var4 = var5 << 24 | var6 << 16 | var7 << 8 | var8;
                        }
                    }
                }

                if(readFully(var0, var1, var4) != var4) {
                    throw new IOException("Incomplete BER/DER data");
                }
            }

            return var2;
        }
    }
}
