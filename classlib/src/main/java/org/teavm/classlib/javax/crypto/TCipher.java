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

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import javax.crypto.NoSuchPaddingException;

import org.teavm.classlib.java.lang.TException;
import org.teavm.classlib.java.nio.TByteBuffer;
import org.teavm.classlib.java.security.TKey;
import org.teavm.classlib.java.security.TProvider;
import org.teavm.classlib.java.util.TIterator;
import org.teavm.classlib.sun.security.jca.TGetInstance;
import org.teavm.classlib.sun.security.jca.TServiceId;

public class TCipher {
    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;
    public static final int WRAP_MODE = 3;
    public static final int UNWRAP_MODE = 4;
    public static final int PUBLIC_KEY = 1;
    public static final int PRIVATE_KEY = 2;
    public static final int SECRET_KEY = 3;
    private Provider provider;
    private TCipherSpi spi;
    private String transformation;
    private TCryptoPermission cryptoPerm;
    private TExemptionMechanism exmech;
    private boolean initialized = false;
    private int opmode = 0;
    private static final String KEY_USAGE_EXTENSION_OID = "2.5.29.15";
    private TCipherSpi firstSpi;
    private TProvider.Service firstService;
    private TIterator<TProvider.Service> serviceIterator;
    private List<TCipher.Transform> transforms;
    private final Object lock;
    private static final String ATTR_MODE = "SupportedModes";
    private static final String ATTR_PAD = "SupportedPaddings";
    private static final int S_NO = 0;
    private static final int S_MAYBE = 1;
    private static final int S_YES = 2;
    private static int warnCount = 10;
    private static final int I_KEY = 1;
    private static final int I_PARAMSPEC = 2;
    private static final int I_PARAMS = 3;
    private static final int I_CERT = 4;

    protected TCipher(TCipherSpi var1, Provider var2, String var3) {
        if(!TJceSecurityManager.INSTANCE.isCallerTrusted()) {
            throw new NullPointerException();
        } else {
            this.spi = var1;
            this.provider = var2;
            this.transformation = var3;
            this.cryptoPerm = TCryptoAllPermission.INSTANCE;
            this.lock = null;
        }
    }

    TCipher(TCipherSpi var1, String var2) {
        this.spi = var1;
        this.transformation = var2;
        this.cryptoPerm = TCryptoAllPermission.INSTANCE;
        this.lock = null;
    }

    private TCipher(TCipherSpi var1, TProvider.Service var2, TIterator<TProvider.Service> var3, String var4, List<TCipher.Transform> var5) {
        this.firstSpi = var1;
        this.firstService = var2;
        this.serviceIterator = var3;
        this.transforms = var5;
        this.transformation = var4;
        this.lock = new Object();
    }

    private static String[] tokenizeTransformation(String var0) throws NoSuchAlgorithmException {
        if(var0 == null) {
            throw new NoSuchAlgorithmException("No transformation given");
        } else {
            String[] var1 = new String[3];
            int var2 = 0;
            StringTokenizer var3 = new StringTokenizer(var0, "/");

            try {
                while(var3.hasMoreTokens() && var2 < 3) {
                    var1[var2++] = var3.nextToken().trim();
                }

                if(var2 == 0 || var2 == 2 || var3.hasMoreTokens()) {
                    throw new NoSuchAlgorithmException("Invalid transformation format:" + var0);
                }
            } catch (NoSuchElementException var5) {
                throw new NoSuchAlgorithmException("Invalid transformation format:" + var0);
            }

            if(var1[0] != null && var1[0].length() != 0) {
                return var1;
            } else {
                throw new NoSuchAlgorithmException("Invalid transformation:algorithm not specified-" + var0);
            }
        }
    }

    private static List<TCipher.Transform> getTransforms(String var0) throws NoSuchAlgorithmException {
        String[] var1 = tokenizeTransformation(var0);
        String var2 = var1[0];
        String var3 = var1[1];
        String var4 = var1[2];
        if(var3 != null && var3.length() == 0) {
            var3 = null;
        }

        if(var4 != null && var4.length() == 0) {
            var4 = null;
        }

        if(var3 == null && var4 == null) {
            TCipher.Transform var6 = new TCipher.Transform(var2, "", (String)null, (String)null);
            return Collections.singletonList(var6);
        } else {
            ArrayList var5 = new ArrayList(4);
            var5.add(new TCipher.Transform(var2, "/" + var3 + "/" + var4, (String)null, (String)null));
            var5.add(new TCipher.Transform(var2, "/" + var3, (String)null, var4));
            var5.add(new TCipher.Transform(var2, "//" + var4, var3, (String)null));
            var5.add(new TCipher.Transform(var2, "", var3, var4));
            return var5;
        }
    }

    private static TCipher.Transform getTransform(Provider.Service var0, List<TCipher.Transform> var1) {
        String var2 = var0.getAlgorithm().toUpperCase(Locale.ENGLISH);
        Iterator var3 = var1.iterator();

        TCipher.Transform var4;
        do {
            if(!var3.hasNext()) {
                return null;
            }

            var4 = (TCipher.Transform)var3.next();
        } while(!var2.endsWith(var4.suffix));

        return var4;
    }

    public static final TCipher getInstance(String var0) throws NoSuchAlgorithmException,
            TNoSuchPaddingException {
        List var1 = getTransforms(var0);
        ArrayList var2 = new ArrayList(var1.size());
        Iterator var3 = var1.iterator();

        while(var3.hasNext()) {
            TCipher.Transform var4 = (TCipher.Transform)var3.next();
            var2.add(new TServiceId("TCipher", var4.transform));
        }

        List var11 = TGetInstance.getServices(var2);
        Iterator var12 = var11.iterator();
        Exception var5 = null;

        while(true) {
            Provider.Service var6;
            TCipher.Transform var7;
            int var8;
            do {
                do {
                    do {
                        if(!var12.hasNext()) {
                            throw new NoSuchAlgorithmException("Cannot find any provider supporting " + var0, var5);
                        }

                        var6 = (Provider.Service)var12.next();
                    } while(!TJceSecurity.canUseProvider(var6.getProvider()));

                    var7 = getTransform(var6, var1);
                } while(var7 == null);

                var8 = var7.supportsModePadding(var6);
            } while(var8 == 0);

            if(var8 == 2) {
                return new TCipher((TCipherSpi)null, var6, var12, var0, var1);
            }

            try {
                TCipherSpi var9 = (TCipherSpi)var6.newInstance((Object)null);
                var7.setModePadding(var9);
                return new TCipher(var9, var6, var12, var0, var1);
            } catch (Exception var10) {
                var5 = var10;
            }
        }
    }

    public static final javax.crypto.Cipher getInstance(String var0, String var1) throws NoSuchAlgorithmException,
            NoSuchProviderException, TNoSuchPaddingException {
        if(var1 != null && var1.length() != 0) {
            Provider var2 = Security.getProvider(var1);
            if(var2 == null) {
                throw new NoSuchProviderException("No such provider: " + var1);
            } else {
                return getInstance(var0, var2);
            }
        } else {
            throw new IllegalArgumentException("Missing provider");
        }
    }

    public static final TCipher getInstance(String var0, Provider var1) throws NoSuchAlgorithmException, NoSuchPaddingException {
        if(var1 == null) {
            throw new IllegalArgumentException("Missing provider");
        } else {
            TException var2 = null;
            List var3 = getTransforms(var0);
            boolean var4 = false;
            String var5 = null;
            Iterator var6 = var3.iterator();

            while(true) {
                while(true) {
                    TCipher.Transform var7;
                    Provider.Service var8;
                    do {
                        do {
                            if(!var6.hasNext()) {
                                if(var2 instanceof TNoSuchPaddingException) {
                                    throw (TNoSuchPaddingException)var2;
                                }

                                if(var5 != null) {
                                    throw new NoSuchPaddingException("Padding not supported: " + var5);
                                }

                                throw new NoSuchAlgorithmException("No such algorithm: " + var0, var2);
                            }

                            var7 = (TCipher.Transform)var6.next();
                            var8 = var1.getService("TCipher", var7.transform);
                        } while(var8 == null);

                        if(!var4) {
                            Exception var9 = TJceSecurity.getVerificationResult(var1);
                            if(var9 != null) {
                                String var12 = "JCE cannot authenticate the provider " + var1.getName();
                                throw new SecurityException(var12, var9);
                            }

                            var4 = true;
                        }
                    } while(var7.supportsMode(var8) == 0);

                    if(var7.supportsPadding(var8) != 0) {
                        try {
                            TCipherSpi var13 = (TCipherSpi)var8.newInstance((Object)null);
                            var7.setModePadding(var13);
                            TCipher var10 = new TCipher(var13, var0);
                            var10.provider = var8.getProvider();
                            var10.initCryptoPermission();
                            return var10;
                        } catch (Exception var11) {
                            var2 = var11;
                        }
                    } else {
                        var5 = var7.pad;
                    }
                }
            }
        }
    }

    private void initCryptoPermission() throws NoSuchAlgorithmException {
        if(!TJceSecurity.isRestricted()) {
            this.cryptoPerm = TCryptoAllPermission.INSTANCE;
            this.exmech = null;
        } else {
            this.cryptoPerm = getConfiguredPermission(this.transformation);
            String var1 = this.cryptoPerm.getExemptionMechanism();
            if(var1 != null) {
                this.exmech = TExemptionMechanism.getInstance(var1);
            }

        }
    }

    void chooseFirstProvider() {
        if(this.spi == null) {
            Object var1 = this.lock;
            synchronized(this.lock) {
                if(this.spi == null) {
                    if(debug != null) {
                        int var2 = --warnCount;
                        if(var2 >= 0) {
                            debug.println("TCipher.init() not first method called, disabling delayed provider selection");
                            if(var2 == 0) {
                                debug.println("Further warnings of this type will be suppressed");
                            }

                            (new Exception("Call trace")).printStackTrace();
                        }
                    }

                    Exception var10 = null;

                    while(true) {
                        Provider.Service var3;
                        TCipherSpi var4;
                        TCipher.Transform var5;
                        do {
                            do {
                                do {
                                    if(this.firstService == null && !this.serviceIterator.hasNext()) {
                                        ProviderException var11 = new ProviderException("Could not construct TCipherSpi instance");
                                        if(var10 != null) {
                                            var11.initCause(var10);
                                        }

                                        throw var11;
                                    }

                                    if(this.firstService != null) {
                                        var3 = this.firstService;
                                        var4 = this.firstSpi;
                                        this.firstService = null;
                                        this.firstSpi = null;
                                    } else {
                                        var3 = (Provider.Service)this.serviceIterator.next();
                                        var4 = null;
                                    }
                                } while(!TJceSecurity.canUseProvider(var3.getProvider()));

                                var5 = getTransform(var3, this.transforms);
                            } while(var5 == null);
                        } while(var5.supportsModePadding(var3) == 0);

                        try {
                            if(var4 == null) {
                                Object var6 = var3.newInstance((Object)null);
                                if(!(var6 instanceof TCipherSpi)) {
                                    continue;
                                }

                                var4 = (TCipherSpi)var6;
                            }

                            var5.setModePadding(var4);
                            this.initCryptoPermission();
                            this.spi = var4;
                            this.provider = var3.getProvider();
                            this.firstService = null;
                            this.serviceIterator = null;
                            this.transforms = null;
                            return;
                        } catch (Exception var8) {
                            var10 = var8;
                        }
                    }
                }
            }
        }
    }

    private void implInit(TCipherSpi var1, int var2, int var3, Key var4, AlgorithmParameterSpec var5, AlgorithmParameters var6, SecureRandom var7) throws
            InvalidKeyException, InvalidAlgorithmParameterException {
        switch(var2) {
            case 1:
                this.checkCryptoPerm(var1, var4);
                var1.engineInit(var3, var4, var7);
                break;
            case 2:
                this.checkCryptoPerm(var1, var4, var5);
                var1.engineInit(var3, var4, var5, var7);
                break;
            case 3:
                this.checkCryptoPerm(var1, var4, var6);
                var1.engineInit(var3, var4, var6, var7);
                break;
            case 4:
                this.checkCryptoPerm(var1, var4);
                var1.engineInit(var3, var4, var7);
                break;
            default:
                throw new AssertionError("Internal TCipher error: " + var2);
        }

    }

    private void chooseProvider(int var1, int var2, Key var3, AlgorithmParameterSpec var4, AlgorithmParameters var5, SecureRandom var6) throws InvalidKeyException, InvalidAlgorithmParameterException {
        Object var7 = this.lock;
        synchronized(this.lock) {
            if(this.spi != null) {
                this.implInit(this.spi, var1, var2, var3, var4, var5, var6);
            } else {
                Exception var8 = null;

                while(true) {
                    Provider.Service var9;
                    TCipherSpi var10;
                    javax.crypto.Cipher.Transform var11;
                    do {
                        do {
                            do {
                                do {
                                    if(this.firstService == null && !this.serviceIterator.hasNext()) {
                                        if(var8 instanceof InvalidKeyException) {
                                            throw (InvalidKeyException)var8;
                                        }

                                        if(var8 instanceof InvalidAlgorithmParameterException) {
                                            throw (InvalidAlgorithmParameterException)var8;
                                        }

                                        if(var8 instanceof RuntimeException) {
                                            throw (RuntimeException)var8;
                                        }

                                        String var16 = var3 != null?var3.getClass().getName():"(null)";
                                        throw new InvalidKeyException("No installed provider supports this key: " + var16, var8);
                                    }

                                    if(this.firstService != null) {
                                        var9 = this.firstService;
                                        var10 = this.firstSpi;
                                        this.firstService = null;
                                        this.firstSpi = null;
                                    } else {
                                        var9 = (Provider.Service)this.serviceIterator.next();
                                        var10 = null;
                                    }
                                } while(!var9.supportsParameter(var3));
                            } while(!TJceSecurity.canUseProvider(var9.getProvider()));

                            var11 = getTransform(var9, this.transforms);
                        } while(var11 == null);
                    } while(var11.supportsModePadding(var9) == 0);

                    try {
                        if(var10 == null) {
                            var10 = (TCipherSpi)var9.newInstance((Object)null);
                        }

                        var11.setModePadding(var10);
                        this.initCryptoPermission();
                        this.implInit(var10, var1, var2, var3, var4, var5, var6);
                        this.provider = var9.getProvider();
                        this.spi = var10;
                        this.firstService = null;
                        this.serviceIterator = null;
                        this.transforms = null;
                        return;
                    } catch (Exception var14) {
                        if(var8 == null) {
                            var8 = var14;
                        }
                    }
                }
            }
        }
    }

    public final Provider getProvider() {
        this.chooseFirstProvider();
        return this.provider;
    }

    public final String getAlgorithm() {
        return this.transformation;
    }

    public final int getBlockSize() {
        this.chooseFirstProvider();
        return this.spi.engineGetBlockSize();
    }

    public final int getOutputSize(int var1) {
        if(!this.initialized && !(this instanceof NullCipher)) {
            throw new IllegalStateException("TCipher not initialized");
        } else if(var1 < 0) {
            throw new IllegalArgumentException("Input size must be equal to or greater than zero");
        } else {
            this.chooseFirstProvider();
            return this.spi.engineGetOutputSize(var1);
        }
    }

    public final byte[] getIV() {
        this.chooseFirstProvider();
        return this.spi.engineGetIV();
    }

    public final AlgorithmParameters getParameters() {
        this.chooseFirstProvider();
        return this.spi.engineGetParameters();
    }

    public final TExemptionMechanism getExemptionMechanism() {
        this.chooseFirstProvider();
        return this.exmech;
    }

    private void checkCryptoPerm(TCipherSpi var1, Key var2) throws InvalidKeyException {
        if(this.cryptoPerm != TCryptoAllPermission.INSTANCE) {
            AlgorithmParameterSpec var3;
            try {
                var3 = this.getAlgorithmParameterSpec(var1.engineGetParameters());
            } catch (InvalidParameterSpecException var5) {
                throw new InvalidKeyException("Unsupported default algorithm parameters");
            }

            if(!this.passCryptoPermCheck(var1, var2, var3)) {
                throw new InvalidKeyException("Illegal key size or default parameters");
            }
        }
    }

    private void checkCryptoPerm(TCipherSpi var1, Key var2, AlgorithmParameterSpec var3) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if(this.cryptoPerm != TCryptoAllPermission.INSTANCE) {
            if(!this.passCryptoPermCheck(var1, var2, (AlgorithmParameterSpec)null)) {
                throw new InvalidKeyException("Illegal key size");
            } else if(var3 != null && !this.passCryptoPermCheck(var1, var2, var3)) {
                throw new InvalidAlgorithmParameterException("Illegal parameters");
            }
        }
    }

    private void checkCryptoPerm(TCipherSpi var1, Key var2, AlgorithmParameters var3) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if(this.cryptoPerm != TCryptoAllPermission.INSTANCE) {
            AlgorithmParameterSpec var4;
            try {
                var4 = this.getAlgorithmParameterSpec(var3);
            } catch (InvalidParameterSpecException var6) {
                throw new InvalidAlgorithmParameterException("Failed to retrieve algorithm parameter specification");
            }

            this.checkCryptoPerm(var1, var2, var4);
        }
    }

    private boolean passCryptoPermCheck(TCipherSpi var1, Key var2, AlgorithmParameterSpec var3) throws InvalidKeyException {
        String var4 = this.cryptoPerm.getExemptionMechanism();
        int var5 = var1.engineGetKeySize(var2);
        int var7 = this.transformation.indexOf(47);
        String var6;
        if(var7 != -1) {
            var6 = this.transformation.substring(0, var7);
        } else {
            var6 = this.transformation;
        }

        TCryptoPermission var8 = new TCryptoPermission(var6, var5, var3, var4);
        if(!this.cryptoPerm.implies(var8)) {
            return false;
        } else if(this.exmech == null) {
            return true;
        } else {
            try {
                if(!this.exmech.isCryptoAllowed(var2)) {

                    return false;
                } else {
                    return true;
                }
            } catch (TExemptionMechanismException var10) {

                return false;
            }
        }
    }

    private static void checkOpmode(int var0) {
        if(var0 < 1 || var0 > 4) {
            throw new InvalidParameterException("Invalid operation mode");
        }
    }

    private static String getOpmodeString(int var0) {
        switch(var0) {
            case 1:
                return "encryption";
            case 2:
                return "decryption";
            case 3:
                return "key wrapping";
            case 4:
                return "key unwrapping";
            default:
                return "";
        }
    }

    public final void init(int var1, Key var2) throws InvalidKeyException {
        this.init(var1, var2, TJceSecurity.RANDOM);
    }

    public final void init(int var1, Key var2, SecureRandom var3) throws InvalidKeyException {
        this.initialized = false;
        checkOpmode(var1);
        if(this.spi != null) {
            this.checkCryptoPerm(this.spi, var2);
            this.spi.engineInit(var1, var2, var3);
        } else {
            try {
                this.chooseProvider(1, var1, var2, (AlgorithmParameterSpec)null, (AlgorithmParameters)null, var3);
            } catch (InvalidAlgorithmParameterException var5) {
                throw new InvalidKeyException(var5);
            }
        }

        this.initialized = true;
        this.opmode = var1;
        if(!skipDebug && pdebug != null) {
            pdebug.println("TCipher." + this.transformation + " " + getOpmodeString(var1) + " algorithm from: " + this.provider.getName());
        }

    }

    public final void init(int var1, Key var2, AlgorithmParameterSpec var3) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.init(var1, var2, var3, TJceSecurity.RANDOM);
    }

    public final void init(int var1, Key var2, AlgorithmParameterSpec var3, SecureRandom var4) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.initialized = false;
        checkOpmode(var1);
        if(this.spi != null) {
            this.checkCryptoPerm(this.spi, var2, var3);
            this.spi.engineInit(var1, var2, var3, var4);
        } else {
            this.chooseProvider(2, var1, var2, var3, (AlgorithmParameters)null, var4);
        }

        this.initialized = true;
        this.opmode = var1;
        if(!skipDebug && pdebug != null) {
            pdebug.println("TCipher." + this.transformation + " " + getOpmodeString(var1) + " algorithm from: " + this.provider.getName());
        }

    }

    public final void init(int var1, Key var2, AlgorithmParameters var3) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.init(var1, var2, var3, TJceSecurity.RANDOM);
    }

    public final void init(int var1, Key var2, AlgorithmParameters var3, SecureRandom var4) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.initialized = false;
        checkOpmode(var1);
        if(this.spi != null) {
            this.checkCryptoPerm(this.spi, var2, var3);
            this.spi.engineInit(var1, var2, var3, var4);
        } else {
            this.chooseProvider(3, var1, var2, (AlgorithmParameterSpec)null, var3, var4);
        }

        this.initialized = true;
        this.opmode = var1;
        if(!skipDebug && pdebug != null) {
            pdebug.println("TCipher." + this.transformation + " " + getOpmodeString(var1) + " algorithm from: " + this.provider.getName());
        }

    }

    public final void init(int var1, Certificate var2) throws InvalidKeyException {
        this.init(var1, var2, TJceSecurity.RANDOM);
    }

    public final void init(int var1, Certificate var2, SecureRandom var3) throws InvalidKeyException {
        this.initialized = false;
        checkOpmode(var1);
        if(var2 instanceof X509Certificate) {
            X509Certificate var4 = (X509Certificate)var2;
            Set var5 = var4.getCriticalExtensionOIDs();
            if(var5 != null && !var5.isEmpty() && var5.contains("2.5.29.15")) {
                boolean[] var6 = var4.getKeyUsage();
                if(var6 != null && (var1 == 1 && var6.length > 3 && !var6[3] || var1 == 3 && var6.length > 2 && !var6[2])) {
                    throw new InvalidKeyException("Wrong key usage");
                }
            }
        }

        PublicKey var8 = var2 == null?null:var2.getPublicKey();
        if(this.spi != null) {
            this.checkCryptoPerm(this.spi, var8);
            this.spi.engineInit(var1, var8, var3);
        } else {
            try {
                this.chooseProvider(4, var1, var8, (AlgorithmParameterSpec)null, (AlgorithmParameters)null, var3);
            } catch (InvalidAlgorithmParameterException var7) {
                throw new InvalidKeyException(var7);
            }
        }

        this.initialized = true;
        this.opmode = var1;
        if(!skipDebug && pdebug != null) {
            pdebug.println("TCipher." + this.transformation + " " + getOpmodeString(var1) + " algorithm from: " + this.provider.getName());
        }

    }

    private void checkCipherState() {
        if(!(this instanceof TNullCipher)) {
            if(!this.initialized) {
                throw new IllegalStateException("TCipher not initialized");
            }

            if(this.opmode != 1 && this.opmode != 2) {
                throw new IllegalStateException("TCipher not initialized for encryption/decryption");
            }
        }

    }

    public final byte[] update(byte[] var1) {
        this.checkCipherState();
        if(var1 == null) {
            throw new IllegalArgumentException("Null input buffer");
        } else {
            this.chooseFirstProvider();
            return var1.length == 0?null:this.spi.engineUpdate(var1, 0, var1.length);
        }
    }

    public final byte[] update(byte[] var1, int var2, int var3) {
        this.checkCipherState();
        if(var1 != null && var2 >= 0 && var3 <= var1.length - var2 && var3 >= 0) {
            this.chooseFirstProvider();
            return var3 == 0?null:this.spi.engineUpdate(var1, var2, var3);
        } else {
            throw new IllegalArgumentException("Bad arguments");
        }
    }

    public final int update(byte[] var1, int var2, int var3, byte[] var4) throws TShortBufferException {
        this.checkCipherState();
        if(var1 != null && var2 >= 0 && var3 <= var1.length - var2 && var3 >= 0) {
            this.chooseFirstProvider();
            return var3 == 0?0:this.spi.engineUpdate(var1, var2, var3, var4, 0);
        } else {
            throw new IllegalArgumentException("Bad arguments");
        }
    }

    public final int update(byte[] var1, int var2, int var3, byte[] var4, int var5) throws TShortBufferException {
        this.checkCipherState();
        if(var1 != null && var2 >= 0 && var3 <= var1.length - var2 && var3 >= 0 && var5 >= 0) {
            this.chooseFirstProvider();
            return var3 == 0?0:this.spi.engineUpdate(var1, var2, var3, var4, var5);
        } else {
            throw new IllegalArgumentException("Bad arguments");
        }
    }

    public final int update(ByteBuffer var1, ByteBuffer var2) throws TShortBufferException {
        this.checkCipherState();
        if(var1 != null && var2 != null) {
            if(var1 == var2) {
                throw new IllegalArgumentException("Input and output buffers must not be the same object, consider using buffer.duplicate()");
            } else if(var2.isReadOnly()) {
                throw new ReadOnlyBufferException();
            } else {
                this.chooseFirstProvider();
                return this.spi.engineUpdate(var1, var2);
            }
        } else {
            throw new IllegalArgumentException("Buffers must not be null");
        }
    }

    public final byte[] doFinal() throws TIllegalBlockSizeException, TBadPaddingException {
        this.checkCipherState();
        this.chooseFirstProvider();
        return this.spi.engineDoFinal((byte[])null, 0, 0);
    }

    public final int doFinal(byte[] var1, int var2) throws TIllegalBlockSizeException, TShortBufferException, TBadPaddingException {
        this.checkCipherState();
        if(var1 != null && var2 >= 0) {
            this.chooseFirstProvider();
            return this.spi.engineDoFinal((byte[])null, 0, 0, var1, var2);
        } else {
            throw new IllegalArgumentException("Bad arguments");
        }
    }

    public final byte[] doFinal(byte[] var1) throws TIllegalBlockSizeException, TBadPaddingException {
        this.checkCipherState();
        if(var1 == null) {
            throw new IllegalArgumentException("Null input buffer");
        } else {
            this.chooseFirstProvider();
            return this.spi.engineDoFinal(var1, 0, var1.length);
        }
    }

    public final byte[] doFinal(byte[] var1, int var2, int var3) throws TIllegalBlockSizeException, TBadPaddingException {
        this.checkCipherState();
        if(var1 != null && var2 >= 0 && var3 <= var1.length - var2 && var3 >= 0) {
            this.chooseFirstProvider();
            return this.spi.engineDoFinal(var1, var2, var3);
        } else {
            throw new IllegalArgumentException("Bad arguments");
        }
    }

    public final int doFinal(byte[] var1, int var2, int var3, byte[] var4) throws TShortBufferException, TIllegalBlockSizeException, TBadPaddingException {
        this.checkCipherState();
        if(var1 != null && var2 >= 0 && var3 <= var1.length - var2 && var3 >= 0) {
            this.chooseFirstProvider();
            return this.spi.engineDoFinal(var1, var2, var3, var4, 0);
        } else {
            throw new IllegalArgumentException("Bad arguments");
        }
    }

    public final int doFinal(byte[] var1, int var2, int var3, byte[] var4, int var5) throws TShortBufferException, TIllegalBlockSizeException, TBadPaddingException {
        this.checkCipherState();
        if(var1 != null && var2 >= 0 && var3 <= var1.length - var2 && var3 >= 0 && var5 >= 0) {
            this.chooseFirstProvider();
            return this.spi.engineDoFinal(var1, var2, var3, var4, var5);
        } else {
            throw new IllegalArgumentException("Bad arguments");
        }
    }

    public final int doFinal(TByteBuffer var1, TByteBuffer var2) throws TShortBufferException, TIllegalBlockSizeException, TBadPaddingException {
        this.checkCipherState();
        if(var1 != null && var2 != null) {
            if(var1 == var2) {
                throw new IllegalArgumentException("Input and output buffers must not be the same object, consider using buffer.duplicate()");
            } else if(var2.isReadOnly()) {
                throw new ReadOnlyBufferException();
            } else {
                this.chooseFirstProvider();
                return this.spi.engineDoFinal(var1, var2);
            }
        } else {
            throw new IllegalArgumentException("Buffers must not be null");
        }
    }

    public final byte[] wrap(TKey var1) throws TIllegalBlockSizeException, InvalidKeyException {
        if(!(this instanceof TNullCipher)) {
            if(!this.initialized) {
                throw new IllegalStateException("TCipher not initialized");
            }

            if(this.opmode != 3) {
                throw new IllegalStateException("TCipher not initialized for wrapping keys");
            }
        }

        this.chooseFirstProvider();
        return this.spi.engineWrap(var1);
    }

    public final Key unwrap(byte[] var1, String var2, int var3) throws InvalidKeyException, NoSuchAlgorithmException {
        if(!(this instanceof TNullCipher)) {
            if(!this.initialized) {
                throw new IllegalStateException("TCipher not initialized");
            }

            if(this.opmode != 4) {
                throw new IllegalStateException("TCipher not initialized for unwrapping keys");
            }
        }

        if(var3 != 3 && var3 != 2 && var3 != 1) {
            throw new InvalidParameterException("Invalid key type");
        } else {
            this.chooseFirstProvider();
            return this.spi.engineUnwrap(var1, var2, var3);
        }
    }

    private AlgorithmParameterSpec getAlgorithmParameterSpec(AlgorithmParameters var1) throws InvalidParameterSpecException {
        if(var1 == null) {
            return null;
        } else {
            String var2 = var1.getAlgorithm().toUpperCase(Locale.ENGLISH);
            return var2.equalsIgnoreCase("RC2")?var1.getParameterSpec(RC2ParameterSpec.class):(var2.equalsIgnoreCase("RC5")?var1.getParameterSpec(RC5ParameterSpec.class):(var2.startsWith("PBE")?var1.getParameterSpec(PBEParameterSpec.class):(var2.startsWith("DES")?var1.getParameterSpec(IvParameterSpec.class):null)));
        }
    }

    private static TCryptoPermission getConfiguredPermission(String var0) throws NullPointerException, NoSuchAlgorithmException {
        if(var0 == null) {
            throw new NullPointerException();
        } else {
            String[] var1 = tokenizeTransformation(var0);
            return TJceSecurityManager.INSTANCE.getCryptoPermission(var1[0]);
        }
    }

    public static final int getMaxAllowedKeyLength(String var0) throws NoSuchAlgorithmException {
        TCryptoPermission var1 = getConfiguredPermission(var0);
        return var1.getMaxKeySize();
    }

    public static final AlgorithmParameterSpec getMaxAllowedParameterSpec(String var0) throws NoSuchAlgorithmException {
        TCryptoPermission var1 = getConfiguredPermission(var0);
        return var1.getAlgorithmParameterSpec();
    }

    public final void updateAAD(byte[] var1) {
        if(var1 == null) {
            throw new IllegalArgumentException("src buffer is null");
        } else {
            this.updateAAD(var1, 0, var1.length);
        }
    }

    public final void updateAAD(byte[] var1, int var2, int var3) {
        this.checkCipherState();
        if(var1 != null && var2 >= 0 && var3 >= 0 && var3 + var2 <= var1.length) {
            this.chooseFirstProvider();
            if(var3 != 0) {
                this.spi.engineUpdateAAD(var1, var2, var3);
            }
        } else {
            throw new IllegalArgumentException("Bad arguments");
        }
    }

    public final void updateAAD(ByteBuffer var1) {
        this.checkCipherState();
        if(var1 == null) {
            throw new IllegalArgumentException("src ByteBuffer is null");
        } else {
            this.chooseFirstProvider();
            if(var1.remaining() != 0) {
                this.spi.engineUpdateAAD(var1);
            }
        }
    }

    private static class Transform {
        final String transform;
        final String suffix;
        final String mode;
        final String pad;
        private static final ConcurrentMap<String, Pattern> patternCache = new ConcurrentHashMap();

        Transform(String var1, String var2, String var3, String var4) {
            this.transform = var1 + var2;
            this.suffix = var2.toUpperCase(Locale.ENGLISH);
            this.mode = var3;
            this.pad = var4;
        }

        void setModePadding(TCipherSpi var1) throws NoSuchAlgorithmException, NoSuchPaddingException {
            if(this.mode != null) {
                var1.engineSetMode(this.mode);
            }

            if(this.pad != null) {
                var1.engineSetPadding(this.pad);
            }

        }

        int supportsModePadding(Provider.Service var1) {
            int var2 = this.supportsMode(var1);
            if(var2 == 0) {
                return var2;
            } else {
                int var3 = this.supportsPadding(var1);
                return Math.min(var2, var3);
            }
        }

        int supportsMode(Provider.Service var1) {
            return supports(var1, "SupportedModes", this.mode);
        }

        int supportsPadding(Provider.Service var1) {
            return supports(var1, "SupportedPaddings", this.pad);
        }

        private static int supports(Provider.Service var0, String var1, String var2) {
            if(var2 == null) {
                return 2;
            } else {
                String var3 = var0.getAttribute(var1);
                return var3 == null?1:(matches(var3, var2)?2:0);
            }
        }

        private static boolean matches(String var0, String var1) {
            Pattern var2 = (Pattern)patternCache.get(var0);
            if(var2 == null) {
                var2 = Pattern.compile(var0);
                patternCache.putIfAbsent(var0, var2);
            }

            return var2.matcher(var1.toUpperCase(Locale.ENGLISH)).matches();
        }
    }
}
