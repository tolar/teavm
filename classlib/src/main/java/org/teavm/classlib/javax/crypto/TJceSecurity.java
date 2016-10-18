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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.Provider;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TProvider;
import org.teavm.classlib.java.security.TSecureRandom;
import org.teavm.classlib.sun.security.jca.TGetInstance;

final class TJceSecurity {
    static final TSecureRandom RANDOM = new TSecureRandom();
    private static TCryptoPermissions defaultPolicy = null;
    private static TCryptoPermissions exemptPolicy = null;
    private static final Map<TProvider, Object> verificationResults = new IdentityHashMap();
    private static final Map<TProvider, Object> verifyingProviders = new IdentityHashMap();
    private static boolean isRestricted = true;
    private static final Object PROVIDER_VERIFIED;
    private static final URL NULL_URL;
    private static final Map<Class<?>, URL> codeBaseCacheRef;

    private TJceSecurity() {
    }

    static TGetInstance.Instance getInstance(TString var0, Class<?> var1, TString var2, TString var3) throws
            NoSuchAlgorithmException, NoSuchProviderException {
        TProvider.Service var4 = TGetInstance.getService(var0, var2, var3);
        Exception var5 = getVerificationResult(var4.getProvider());
        if(var5 != null) {
            String var6 = "JCE cannot authenticate the provider " + var3;
            throw (NoSuchProviderException)(new NoSuchProviderException(var6)).initCause(var5);
        } else {
            return TGetInstance.getInstance(var4, var1);
        }
    }

    static TGetInstance.Instance getInstance(String var0, Class<?> var1, String var2, Provider var3) throws NoSuchAlgorithmException {
        TProvider.Service var4 = TGetInstance.getService(var0, var2, var3);
        Exception var5 = getVerificationResult(var3);
        if(var5 != null) {
            String var6 = "JCE cannot authenticate the provider " + var3.getName();
            throw new SecurityException(var6, var5);
        } else {
            return TGetInstance.getInstance(var4, var1);
        }
    }

    static TGetInstance.Instance getInstance(String var0, Class<?> var1, String var2) throws NoSuchAlgorithmException {
        List var3 = TGetInstance.getServices(var0, var2);
        NoSuchAlgorithmException var4 = null;
        Iterator var5 = var3.iterator();

        while(true) {
            Provider.Service var6;
            do {
                if(!var5.hasNext()) {
                    throw new NoSuchAlgorithmException("Algorithm " + var2 + " not available", var4);
                }

                var6 = (Provider.Service)var5.next();
            } while(!canUseProvider(var6.getProvider()));

            try {
                TGetInstance.Instance var7 = TGetInstance.getInstance(var6, var1);
                return var7;
            } catch (NoSuchAlgorithmException var8) {
                var4 = var8;
            }
        }
    }

    static TCryptoPermissions verifyExemptJar(URL var0) throws Exception {
        TJarVerifier var1 = new TJarVerifier(var0, true);
        var1.verify();
        return var1.getPermissions();
    }

    static void verifyProviderJar(URL var0) throws Exception {
        TJarVerifier var1 = new TJarVerifier(var0, false);
        var1.verify();
    }

    static synchronized Exception getVerificationResult(TProvider var0) {
        Object var1 = verificationResults.get(var0);
        if(var1 == PROVIDER_VERIFIED) {
            return null;
        } else if(var1 != null) {
            return (Exception)var1;
        } else if(verifyingProviders.get(var0) != null) {
            return new NoSuchProviderException("Recursion during verification");
        } else {
            Exception var3;
            try {
                verifyingProviders.put(var0, Boolean.FALSE);
                URL var2 = getCodeBase(var0.getClass());
                verifyProviderJar(var2);
                verificationResults.put(var0, PROVIDER_VERIFIED);
                var3 = null;
                return var3;
            } catch (Exception var7) {
                verificationResults.put(var0, var7);
                var3 = var7;
            } finally {
                verifyingProviders.remove(var0);
            }

            return var3;
        }
    }

    static boolean canUseProvider(TProvider var0) {
        return getVerificationResult(var0) == null;
    }

    static URL getCodeBase(final Class<?> var0) {
        Map var1 = codeBaseCacheRef;
        synchronized(codeBaseCacheRef) {
            URL var2 = (URL)codeBaseCacheRef.get(var0);
            if(var2 == null) {
                var2 = (URL) AccessController.doPrivileged(new PrivilegedAction() {
                    public URL run() {
                        ProtectionDomain var1 = var0.getProtectionDomain();
                        if(var1 != null) {
                            CodeSource var2 = var1.getCodeSource();
                            if(var2 != null) {
                                return var2.getLocation();
                            }
                        }

                        return TJceSecurity.NULL_URL;
                    }
                });
                codeBaseCacheRef.put(var0, var2);
            }

            return var2 == NULL_URL?null:var2;
        }
    }

    private static void setupJurisdictionPolicies() throws Exception {
        String var0 = System.getProperty("java.home");
        String var1 = File.separator;
        String var2 = var0 + var1 + "lib" + var1 + "security" + var1;
        File var3 = new File(var2, "US_export_policy.jar");
        File var4 = new File(var2, "local_policy.jar");
        URL var5 = ClassLoader.getSystemResource("javax/crypto/Cipher.class");
        if(var5 != null && var3.exists() && var4.exists()) {
            TCryptoPermissions var6 = new TCryptoPermissions();
            TCryptoPermissions var7 = new TCryptoPermissions();
            loadPolicies(var3, var6, var7);
            TCryptoPermissions var8 = new TCryptoPermissions();
            TCryptoPermissions var9 = new TCryptoPermissions();
            loadPolicies(var4, var8, var9);
            if(!var6.isEmpty() && !var8.isEmpty()) {
                defaultPolicy = var6.getMinimum(var8);
                if(var7.isEmpty()) {
                    exemptPolicy = var9.isEmpty()?null:var9;
                } else {
                    exemptPolicy = var7.getMinimum(var9);
                }

            } else {
                throw new SecurityException("Missing mandatory jurisdiction policy files");
            }
        } else {
            throw new SecurityException("Cannot locate policy or framework files!");
        }
    }

    private static void loadPolicies(File var0, TCryptoPermissions var1, TCryptoPermissions var2) throws Exception {
        JarFile var3 = new JarFile(var0);
        Enumeration var4 = var3.entries();

        while(var4.hasMoreElements()) {
            JarEntry var5 = (JarEntry)var4.nextElement();
            InputStream var6 = null;

            try {
                if(var5.getName().startsWith("default_")) {
                    var6 = var3.getInputStream(var5);
                    var1.load(var6);
                } else {
                    if(!var5.getName().startsWith("exempt_")) {
                        continue;
                    }

                    var6 = var3.getInputStream(var5);
                    var2.load(var6);
                }
            } finally {
                if(var6 != null) {
                    var6.close();
                }

            }

            TJarVerifier.verifyPolicySigned(var5.getCertificates());
        }

        var3.close();
        var3 = null;
    }

    static TCryptoPermissions getDefaultPolicy() {
        return defaultPolicy;
    }

    static TCryptoPermissions getExemptPolicy() {
        return exemptPolicy;
    }

    static boolean isRestricted() {
        return isRestricted;
    }

    static {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    TJceSecurity.setupJurisdictionPolicies();
                    return null;
                }
            });
            isRestricted = !defaultPolicy.implies(TCryptoAllPermission.INSTANCE);
        } catch (Exception var2) {
            throw new SecurityException("Can not initialize cryptographic mechanism", var2);
        }

        PROVIDER_VERIFIED = Boolean.TRUE;

        try {
            NULL_URL = new URL("http://null.sun.com/");
        } catch (Exception var1) {
            throw new RuntimeException(var1);
        }

        codeBaseCacheRef = new WeakHashMap();
    }
}
