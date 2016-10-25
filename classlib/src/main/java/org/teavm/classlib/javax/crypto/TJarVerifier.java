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
package org.teavm.classlib.javax.crypto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.teavm.classlib.java.io.TByteArrayInputStream;
import org.teavm.classlib.java.security.cert.TCertificateFactory;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.sun.security.validator.TValidator;

final class TJarVerifier {
    private static final String PLUGIN_IMPL_NAME = "sun.plugin.net.protocol.jar.CachedJarURLConnection";
    private static TX509Certificate frameworkCertificate;
    private static TValidator providerValidator;
    private static TValidator exemptValidator;
    private Vector<TX509Certificate> verifiedSignerCache = null;
    private URL jarURL;
    private TValidator validator;
    private boolean savePerms;

    private TCryptoPermissions appPerms = null;

    private static TX509Certificate parseCertificate(String var0, TCertificateFactory var1) throws Exception {
        TByteArrayInputStream var2 = new TByteArrayInputStream(var0.getBytes("UTF8"));
        return (TX509Certificate)var1.generateCertificate(var2);
    }

    TJarVerifier(URL var1, boolean var2) {
        this.jarURL = var1;
        this.savePerms = var2;
        this.validator = var2?providerValidator:exemptValidator;
        this.verifiedSignerCache = new Vector(2);
    }

    void verify() throws JarException, IOException {
        if(this.jarURL == null) {
            throw new JarException("Class is on the bootclasspath");
        } else {
            try {
                this.verifyJars(this.jarURL, (Vector)null);
            } catch (NoSuchProviderException var7) {
                throw new JarException("Cannot verify " + this.jarURL.toString());
            } catch (CertificateException var8) {
                throw new JarException("Cannot verify " + this.jarURL.toString());
            } catch (TCryptoPolicyParser.ParsingException var9) {
                throw new JarException("Cannot parse " + this.jarURL.toString());
            } finally {
                if(this.verifiedSignerCache != null) {
                    this.verifiedSignerCache.clear();
                }

            }

        }
    }

    static void verifyPolicySigned(Certificate[] var0) throws Exception {
        List var1 = convertCertsToChains(var0);
        boolean var2 = false;
        Iterator var3 = var1.iterator();

        while(var3.hasNext()) {
            X509Certificate[] var4 = (X509Certificate[])var3.next();
            X509Certificate var5 = var4[0];
            if(var5.equals(frameworkCertificate)) {
                var2 = true;
                break;
            }
        }

        if(!var2) {
            throw new SecurityException("The jurisdiction policy files are not signed by a trusted signer!");
        }
    }

    TCryptoPermissions getPermissions() {
        return this.appPerms;
    }

    private void verifyJars(URL var1, Vector<String> var2) throws NoSuchProviderException, CertificateException, IOException,
            TCryptoPolicyParser.ParsingException {
        String var3 = var1.toString();
        if(var2 == null || !var2.contains(var3)) {
            String var4 = this.verifySingleJar(var1);
            if(var2 != null) {
                var2.addElement(var3);
            }

            if(var4 != null) {
                if(var2 == null) {
                    var2 = new Vector();
                    var2.addElement(var3);
                }

                this.verifyManifestClassPathJars(var1, var4, var2);
            }
        }

    }

    private void verifyManifestClassPathJars(URL var1, String var2, Vector<String> var3) throws NoSuchProviderException, CertificateException, IOException,
            TCryptoPolicyParser.ParsingException {
        String[] var4 = parseAttrClasspath(var2);

        try {
            for(int var5 = 0; var5 < var4.length; ++var5) {
                URL var8 = new URL(var1, var4[var5]);
                this.verifyJars(var8, var3);
            }

        } catch (MalformedURLException var7) {
            MalformedURLException var6 = new MalformedURLException("The JAR file " + var1.toString() + " contains invalid URLs in its Class-Path attribute");
            var6.initCause(var7);
            throw var6;
        }
    }

    private String verifySingleJar(URL var1) throws NoSuchProviderException, CertificateException, IOException,
            TCryptoPolicyParser.ParsingException {
        final URL var2 = var1.getProtocol().equalsIgnoreCase("jar")?var1:new URL("jar:" + var1.toString() + "!/");
        TJarVerifier.JarHolder var3 = null;

        try {
            try {
                var3 = (TJarVerifier.JarHolder) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public TJarVerifier.JarHolder run() throws Exception {
                        boolean var1 = false;
                        JarURLConnection var2x = (JarURLConnection)var2.openConnection();
                        if(var2x.getClass().getName().equals("sun.plugin.net.protocol.jar.CachedJarURLConnection")) {
                            var1 = true;
                        }

                        var2x.setUseCaches(var1);
                        JarFile var3 = var2x.getJarFile();
                        return var3 != null?new TJarVerifier.JarHolder(var3, var1):null;
                    }
                });
            } catch (PrivilegedActionException var21) {
                SecurityException var5 = new SecurityException("Cannot verify " + var2.toString());
                var5.initCause(var21);
                throw var5;
            }

            JarFile var4;
            if(var3 == null) {
                var4 = null;
                return var4;
            } else {
                var4 = var3.file;
                byte[] var24 = new byte[8192];
                Enumeration var6 = var4.entries();

                while(var6.hasMoreElements()) {
                    JarEntry var7 = (JarEntry)var6.nextElement();
                    BufferedInputStream var8 = new BufferedInputStream(var4.getInputStream(var7));

                    try {
                        while(true) {
                            if(var8.read(var24, 0, var24.length) != -1) {
                                continue;
                            }
                        }
                    } finally {
                        var8.close();
                    }
                }

                Manifest var25 = var4.getManifest();
                if(var25 == null) {
                    throw new JarException(var1.toString() + " is not signed.");
                } else {
                    Enumeration var26 = var4.entries();

                    JarEntry var9;
                    while(var26.hasMoreElements()) {
                        var9 = (JarEntry)var26.nextElement();
                        if(!var9.isDirectory()) {
                            Certificate[] var10 = var9.getCertificates();
                            if(var10 != null && var10.length != 0) {
                                int var11 = 0;

                                X509Certificate[] var12;
                                boolean var13;
                                for(var13 = false; (var12 = getAChain(var10, var11)) != null; var11 += var12.length) {
                                    if(this.verifiedSignerCache.contains(var12[0])) {
                                        var13 = true;
                                        break;
                                    }

                                    if(this.isTrusted(var12)) {
                                        var13 = true;
                                        this.verifiedSignerCache.addElement(var12[0]);
                                        break;
                                    }
                                }

                                if(!var13) {
                                    throw new JarException(var1.toString() + " is not signed by a" + " trusted signer.");
                                }
                            } else if(!var9.getName().startsWith("META-INF")) {
                                throw new JarException(var1.toString() + " has unsigned entries - " + var9.getName());
                            }
                        }
                    }

                    if(this.jarURL.equals(var1) && this.savePerms) {
                        var9 = var3.file.getJarEntry("cryptoPerms");
                        if(var9 == null) {
                            throw new JarException("Can not find cryptoPerms");
                        }

                        this.appPerms = new CryptoPermissions();
                        this.appPerms.load(var3.file.getInputStream(var9));
                    }

                    String var27 = var25.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
                    return var27;
                }
            }
        } finally {
            if(var3 != null && !var3.useCaches) {
                var3.file.close();
            }

        }
    }

    private static String[] parseAttrClasspath(String var0) throws JarException {
        var0 = var0.trim();
        int var1 = var0.indexOf(32);
        String var2 = null;
        Vector var3 = new Vector();
        boolean var4 = false;

        do {
            if(var1 > 0) {
                var2 = var0.substring(0, var1);
                var0 = var0.substring(var1 + 1).trim();
                var1 = var0.indexOf(32);
            } else {
                var2 = var0;
                var4 = true;
            }

            if(!var2.endsWith(".jar")) {
                throw new JarException("The provider contains un-verifiable components");
            }

            var3.addElement(var2);
        } while(!var4);

        return (String[])var3.toArray(new String[0]);
    }

    private boolean isTrusted(X509Certificate[] var1) {
        try {
            this.validator.validate(var1);
            return true;
        } catch (CertificateException var3) {
            return false;
        }
    }

    private static X509Certificate[] getAChain(Certificate[] var0, int var1) {
        if(var1 > var0.length - 1) {
            return null;
        } else {
            int var2;
            for(var2 = var1; var2 < var0.length - 1 && ((X509Certificate)var0[var2 + 1]).getSubjectDN().equals(((X509Certificate)var0[var2]).getIssuerDN()); ++var2) {
                ;
            }

            int var3 = var2 - var1 + 1;
            X509Certificate[] var4 = new X509Certificate[var3];

            for(int var5 = 0; var5 < var3; ++var5) {
                var4[var5] = (X509Certificate)var0[var1 + var5];
            }

            return var4;
        }
    }

    private static List<X509Certificate[]> convertCertsToChains(Certificate[] var0) throws CertificateException {
        if(var0 == null) {
            return Collections.emptyList();
        } else {
            ArrayList var1 = new ArrayList();
            X509Certificate[] var2 = null;

            for(int var3 = 0; (var2 = getAChain(var0, var3)) != null; var3 += var2.length) {
                var1.add(var2);
            }

            return var1;
        }
    }

    private static void testSignatures(X509Certificate var0, CertificateFactory var1) throws Exception {
        String var2 = "-----BEGIN CERTIFICATE-----\nMIIDLDCCAukCBDf5OeUwCwYHKoZIzjgEAwUAMHsxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQTESMBAGA1UEBxMJQ3VwZXJ0aW5vMRkwFwYDVQQKExBTdW4gTWljcm9zeXN0ZW1zMRYwFAYDVQQLEw1KYXZhIFNvZnR3YXJlMRgwFgYDVQQDEw9KQ0UgRGV2ZWxvcG1lbnQwHhcNOTkxMDA0MjMzNjA1WhcNMDAxMDAzMjMzNjA1WjB7MQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExEjAQBgNVBAcTCUN1cGVydGlubzEZMBcGA1UEChMQU3VuIE1pY3Jvc3lzdGVtczEWMBQGA1UECxMNSmF2YSBTb2Z0d2FyZTEYMBYGA1UEAxMPSkNFIERldmVsb3BtZW50MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAOGsR8waR5aiuOk1yBLemRlVCY+APJv3xqmPRxWAF6nwV2xrFUB8ghSEMFcHywoe4vBDvkGSoAFzeB5jy5wjDiFsN5AFPEVRfveS4NNZ1dgRdHbbh3h5O1dZE4MAKQwQfUoh9Oa3aahlB+orRzKOHLlGDpbNRQLST5BClvohramCMAsGByqGSM44BAMFAAMwADAtAhRF46T3nS+inP9TA1pLd3LIV0NNDQIVAIafi+1/+JKxu0rcoXWMFSxNaRb3\n-----END CERTIFICATE-----";
        byte[] var6 = getSystemEntropy();
        int var7 = var6[0] & 255 | (var6[1] & 255) << 8 | (var6[2] & 255) << 16 | var6[3] << 24;
        X509Certificate[] var8 = new X509Certificate[]{var0, parseCertificate(var2, var1), parseCertificate("-----BEGIN CERTIFICATE-----\nMIIB4DCCAYoCAQEwDQYJKoZIhvcNAQEEBQAwezELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkNBMRIwEAYDVQQHEwlDdXBlcnRpbm8xGTAXBgNVBAoTEFN1biBNaWNyb3N5c3RlbXMxFjAUBgNVBAsTDUphdmEgU29mdHdhcmUxGDAWBgNVBAMTD0pDRSBEZXZlbG9wbWVudDAeFw0wMjEwMzExNTI3NDRaFw0wNzEwMzExNTI3NDRaMHsxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQTESMBAGA1UEBxMJQ3VwZXJ0aW5vMRkwFwYDVQQKExBTdW4gTWljcm9zeXN0ZW1zMRYwFAYDVQQLEw1KYXZhIFNvZnR3YXJlMRgwFgYDVQQDEw9KQ0UgRGV2ZWxvcG1lbnQwXDANBgkqhkiG9w0BAQEFAANLADBIAkEAo/4CddEOa3M6v9JFAhnBYgTq54Y30++F8yzCK9EeYaG3AzvzZqNshDy579647p0cOM/4VO6rU2PgbzgKXPcs8wIDAQABMA0GCSqGSIb3DQEBBAUAA0EACqPlFmVdKdYSCTNltXKQnBqss9GNjbnB+CitvWrwN+oOK8qQpvV+5LB6LruvRy6zCedCV95Z2kXKg/Fnj0gvsg==\n-----END CERTIFICATE-----", var1), parseCertificate("-----BEGIN CERTIFICATE-----\nMIIB4DCCAYoCAQIwDQYJKoZIhvcNAQEEBQAwezELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkNBMRIwEAYDVQQHEwlDdXBlcnRpbm8xGTAXBgNVBAoTEFN1biBNaWNyb3N5c3RlbXMxFjAUBgNVBAsTDUphdmEgU29mdHdhcmUxGDAWBgNVBAMTD0pDRSBEZXZlbG9wbWVudDAeFw0wMjEwMzExNTI3NDRaFw0wNzEwMzExNTI3NDRaMHsxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQTESMBAGA1UEBxMJQ3VwZXJ0aW5vMRkwFwYDVQQKExBTdW4gTWljcm9zeXN0ZW1zMRYwFAYDVQQLEw1KYXZhIFNvZnR3YXJlMRgwFgYDVQQDEw9KQ0UgRGV2ZWxvcG1lbnQwXDANBgkqhkiG9w0BAQEFAANLADBIAkEAr1OSXaOzpnVoqL2LqS5+HLy1kVvBwiM/E5iYT9eZaghE8qvF+4fETipWUNTWCQzHR4cDJGJOl9Nm77tELhES4QIDAQABMA0GCSqGSIb3DQEBBAUAA0EAL+WcVFyj+iXlEVNVQbNOOUlWmlmXGiNKKXnIdNcc1ZUyi+JW0zmlfZ7iU/eRYhEEJBwdrUoyiGOGLo7pi6JzAA==\n-----END CERTIFICATE-----", var1)};
        PublicKey[] var9 = new PublicKey[4];
        var9[0] = var0.getPublicKey();
        var9[1] = var9[0];
        var9[2] = var8[2].getPublicKey();
        var9[3] = var9[2];
        boolean[] var10 = new boolean[]{true, false, true, false};

        for(int var11 = 0; var11 < 12; ++var11) {
            int var12 = var7 & 3;
            var7 >>= 2;

            boolean var13;
            try {
                var8[var12].verify(var9[var12]);
                var13 = true;
            } catch (SignatureException var15) {
                var13 = false;
            } catch (InvalidKeyException var16) {
                var13 = false;
            }

            if(var13 != var10[var12]) {
                throw new SecurityException("Signature classes have been tampered with");
            }
        }

    }

    private static byte[] getSystemEntropy() {
        MessageDigest var0;
        try {
            var0 = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException var6) {
            throw new InternalError("internal error: SHA-1 not available.");
        }

        byte var1 = (byte)((int)System.currentTimeMillis());
        var0.update(var1);

        try {
            Properties var2 = System.getProperties();
            Iterator var3 = var2.stringPropertyNames().iterator();

            while(var3.hasNext()) {
                String var4 = (String)var3.next();
                var0.update(var4.getBytes());
                var0.update(var2.getProperty(var4).getBytes());
            }

            var0.update(InetAddress.getLocalHost().toString().getBytes());
            File var9 = new File(var2.getProperty("java.io.tmpdir"));
            String[] var11 = var9.list();

            for(int var5 = 0; var5 < var11.length; ++var5) {
                var0.update(var11[var5].getBytes());
            }
        } catch (Exception var7) {
            var0.update((byte)var7.hashCode());
        }

        Runtime var8 = Runtime.getRuntime();
        byte[] var10 = longToByteArray(var8.totalMemory());
        var0.update(var10, 0, var10.length);
        var10 = longToByteArray(var8.freeMemory());
        var0.update(var10, 0, var10.length);
        return var0.digest();
    }

    private static byte[] longToByteArray(long var0) {
        byte[] var2 = new byte[8];

        for(int var3 = 0; var3 < 8; ++var3) {
            var2[var3] = (byte)((int)var0);
            var0 >>= 8;
        }

        return var2;
    }

    static {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Void run() throws Exception {
                    CertificateFactory var1 = CertificateFactory.getInstance("X.509");
                    TJarVerifier.frameworkCertificate = TJarVerifier.parseCertificate("-----BEGIN CERTIFICATE-----\nMIICoTCCAl+gAwIBAgICAzkwCwYHKoZIzjgEAwUAMIGQMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExEjAQBgNVBAcTCVBhbG8gQWx0bzEdMBsGA1UEChMUU3VuIE1pY3Jvc3lzdGVtcyBJbmMxIzAhBgNVBAsTGkphdmEgU29mdHdhcmUgQ29kZSBTaWduaW5nMRwwGgYDVQQDExNKQ0UgQ29kZSBTaWduaW5nIENBMB4XDTExMDQxMTA2MDA0M1oXDTE2MDQxNDA2MDA0M1owYTEdMBsGA1UEChMUU3VuIE1pY3Jvc3lzdGVtcyBJbmMxIzAhBgNVBAsTGkphdmEgU29mdHdhcmUgQ29kZSBTaWduaW5nMRswGQYDVQQDExJPcmFjbGUgQ29ycG9yYXRpb24wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBALR6pnmTTdvYtjj0EH7nQTa52aHuWTsxIgX+sVzy5MyYcGZJk23QI623tCNLk1MgPf0ntUKe/HZjuvdrBIfgBcu2C+Htw0PwmQyjHQToAMUt5CfWpGLmBh0LVblnOb9mcOp/Ety4myc9V8c3LSVXpgvNgIUhu8Vv3IEM966NKtmLAgMBAAGjgY4wgYswEQYJYIZIAYb4QgEBBAQDAgQQMA4GA1UdDwEB/wQEAwIF4DAdBgNVHQ4EFgQU5YHrhAD3Wo9gQZEycFmm7NAgzUUwHwYDVR0jBBgwFoAUZeL0hsnTTvCRTliiavXYeFqawaYwJgYDVR0RBB8wHYEbYnJhZGZvcmQud2V0bW9yZUBvcmFjbGUuY29tMAsGByqGSM44BAMFAAMvADAsAhRVoQglrJDMgxGzsGFS7oHMbzLioQIUSps7E1B/RSMh6ooea/mGwKX4iVc=\n-----END CERTIFICATE-----", var1);
                    TX509Certificate[] var2 = new TX509Certificate[]{TJarVerifier.parseCertificate("-----BEGIN CERTIFICATE-----\nMIIDwDCCA36gAwIBAgIBEDALBgcqhkjOOAQDBQAwgZAxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQTESMBAGA1UEBxMJUGFsbyBBbHRvMR0wGwYDVQQKExRTdW4gTWljcm9zeXN0ZW1zIEluYzEjMCEGA1UECxMaSmF2YSBTb2Z0d2FyZSBDb2RlIFNpZ25pbmcxHDAaBgNVBAMTE0pDRSBDb2RlIFNpZ25pbmcgQ0EwHhcNMDEwNDI1MDcwMDAwWhcNMjAwNDI1MDcwMDAwWjCBkDELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkNBMRIwEAYDVQQHEwlQYWxvIEFsdG8xHTAbBgNVBAoTFFN1biBNaWNyb3N5c3RlbXMgSW5jMSMwIQYDVQQLExpKYXZhIFNvZnR3YXJlIENvZGUgU2lnbmluZzEcMBoGA1UEAxMTSkNFIENvZGUgU2lnbmluZyBDQTCCAbcwggEsBgcqhkjOOAQBMIIBHwKBgQDrrzcEHspRHmldsPKP9rVJH8akmQXXKb90t2r1Gdge5Bv4CgGamP9wq+JKVoZsU7P84ciBjDHwxPOwi+ZwBuz3aWjbg0xyKYkpNhdcO0oHoCACKkaXUR1wyAgYC84Mbpt29wXj5/vTYXnhYJokjQaVgzxRIOEwzzhXgqYacg3O0wIVAIQlReG6ualiq3noWzC4iWsb/3t1AoGBAKvJdHt07+5CtWpTTTvdkAZyaJEPC6Qpdi5VO9WuTWVcfio6BKZnptBxqqXXt+LBcg2k0aoeklRMIAAJorAJQRkzALLDXK5C+LGLynyW2BB/N0Rbqsx4yNdydjdrQJmoVWb6qAMei0oRAmnLTLglBhygd9LJrNI96QoQ+nZwt/vcA4GEAAKBgC0JmFysuJzHmX7uIBkqNJD516urrt1rcpUNZvjvJ49Esu0oRMf+r7CmJ28AZ0WCWweoVlY70ilRYV5pOdcudHcSzxlK9S3Iy3JhxE5v+kdDPxS7+rwYZijC2WaLei0vwmCSSxT+WD4hf2hivmxISfmgS16FnRkQ+RVFURtx1PcLo2YwZDARBglghkgBhvhCAQEEBAMCAAcwDwYDVR0TAQH/BAUwAwEB/zAfBgNVHSMEGDAWgBRl4vSGydNO8JFOWKJq9dh4WprBpjAdBgNVHQ4EFgQUZeL0hsnTTvCRTliiavXYeFqawaYwCwYHKoZIzjgEAwUAAy8AMCwCFCr3zzyXXfl4tgjXQbTZDUVM5LScAhRFzXVpDiH6HdazKbLp9zMdM/38SQ==\n-----END CERTIFICATE-----", var1), TJarVerifier.parseCertificate("-----BEGIN CERTIFICATE-----\nMIIDUTCCAw2gAwIBAgIEQCFoETALBgcqhkjOOAQDBQAwYDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD0lCTSBDb3Jwb3JhdGlvbjEZMBcGA1UECxMQSUJNIENvZGUgU2lnbmluZzEcMBoGA1UEAxMTSkNFIENvZGUgU2lnbmluZyBDQTAeFw0wNDAyMDQyMTQ1NTNaFw0yMDA1MjYyMDQ1NTNaMGAxCzAJBgNVBAYTAlVTMRgwFgYDVQQKEw9JQk0gQ29ycG9yYXRpb24xGTAXBgNVBAsTEElCTSBDb2RlIFNpZ25pbmcxHDAaBgNVBAMTE0pDRSBDb2RlIFNpZ25pbmcgQ0EwggG4MIIBLAYHKoZIzjgEATCCAR8CgYEA/X9TgR11EilS30qcLuzk5/YRt1I870QAwx4/gLZRJmlFXUAiUftZPY1Y+r/F9bow9subVWzXgTuAHTRv8mZgt2uZUKWkn5/oBHsQIsJPu6nX/rfGG/g7V+fGqKYVDwT7g/bTxR7DAjVUE1oWkTL2dfOuK2HXKu/yIgMZndFIAccCFQCXYFCPFSMLzLKSuYKi64QL8Fgc9QKBgQD34aCF1ps93su8q1w2uFe5eZSvu/o66oL5V0wLPQeCZ1FZV4661FlP5nEHEIGAtEkWcSPoTCgWE7fPCTKMyKbhPBZ6i1R8jSjgo64eK7OmdZFuo38L+iE1YvH7YnoBJDvMpPG+qFGQiaiD3+Fa5Z8GkotmXoB7VSVkAUw7/s9JKgOBhQACgYEA6msAx98QO7l0NafhbWaCTfdbVnHCJkUncj1REGL/s9wQyftRE9Sti6glbl3JeNJbJ9MTQUcUBnzLgjhexgthoEyDLZTMjC6EkDqPQgppUtN0JnekFH0qcUGIiXemLWKaoViYbWzPzqjqut3ooRBEjIRCwbgfK7S8s110YICNQlSjUzBRMB0GA1UdDgQWBBR+PU1NzBBZuvmuQj3lyVdaUgt+hzAfBgNVHSMEGDAWgBR+PU1NzBBZuvmuQj3lyVdaUgt+hzAPBgNVHRMBAf8EBTADAQH/MAsGByqGSM44BAMFAAMxADAuAhUAi5ncRzk0NqFYt4yWsnlcVBPt+zsCFQCM9M0mv0t9iodsOOHJhqUrW1QjAA==\n-----END CERTIFICATE-----", var1), TJarVerifier.frameworkCertificate};
                    TJarVerifier.providerValidator = TValidator.getInstance("Simple", "jce signing", Arrays.asList(var2));
                    TJarVerifier.exemptValidator = TJarVerifier.providerValidator;
                    TJarVerifier.testSignatures(var2[0], var1);
                    return null;
                }
            });
        } catch (Exception var5) {
            SecurityException var4 = new SecurityException("Framework jar verification can not be initialized");
            var4.initCause(var5);
            throw var4;
        }
    }

    private static class JarHolder {
        JarFile file;
        boolean useCaches;

        JarHolder(JarFile var1, boolean var2) {
            this.file = var1;
            this.useCaches = var2;
        }
    }
}
