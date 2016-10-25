/*
 *  Copyright 2016 vasek.
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
package org.teavm.classlib.sun.security.util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.interfaces.DSAKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import org.teavm.classlib.java.security.TSecureRandom;
import org.teavm.classlib.sun.security.jca.TJCAUtil;

/**
 * Created by vasek on 25. 10. 2016.
 */
public final class TKeyUtil {
    public TKeyUtil() {
    }

    public static final int getKeySize(Key var0) {
        int var1 = -1;
        if(var0 instanceof TLength) {
            try {
                TLength var2 = (TLength)var0;
                var1 = var2.length();
            } catch (UnsupportedOperationException var4) {
                ;
            }

            if(var1 >= 0) {
                return var1;
            }
        }

        if(var0 instanceof SecretKey) {
            SecretKey var5 = (SecretKey)var0;
            String var3 = var5.getFormat();
            if("RAW".equals(var3) && var5.getEncoded() != null) {
                var1 = var5.getEncoded().length * 8;
            }
        } else if(var0 instanceof RSAKey) {
            RSAKey var6 = (RSAKey)var0;
            var1 = var6.getModulus().bitLength();
        } else if(var0 instanceof ECKey) {
            ECKey var7 = (ECKey)var0;
            var1 = var7.getParams().getOrder().bitLength();
        } else if(var0 instanceof DSAKey) {
            DSAKey var8 = (DSAKey)var0;
            var1 = var8.getParams().getP().bitLength();
        } else if(var0 instanceof DHKey) {
            DHKey var9 = (DHKey)var0;
            var1 = var9.getParams().getP().bitLength();
        }

        return var1;
    }

    public static final void validate(Key var0) throws InvalidKeyException {
        if(var0 == null) {
            throw new NullPointerException("The key to be validated cannot be null");
        } else {
            if(var0 instanceof DHPublicKey) {
                validateDHPublicKey((DHPublicKey)var0);
            }

        }
    }

    public static final void validate(KeySpec var0) throws InvalidKeyException {
        if(var0 == null) {
            throw new NullPointerException("The key spec to be validated cannot be null");
        } else {
            if(var0 instanceof DHPublicKeySpec) {
                validateDHPublicKey((DHPublicKeySpec)var0);
            }

        }
    }

    public static final boolean isOracleJCEProvider(String var0) {
        return var0 != null && (var0.equals("SunJCE") || var0.equals("SunMSCAPI") || var0.equals("OracleUcrypto") || var0.startsWith("SunPKCS11"));
    }

    public static byte[] checkTlsPreMasterSecretKey(int var0, int var1, TSecureRandom var2, byte[] var3, boolean var4) {
        if(var2 == null) {
            var2 = TJCAUtil.getSecureRandom();
        }

        byte[] var5 = new byte[48];
        var2.nextBytes(var5);
        if(!var4 && var3 != null) {
            if(var3.length != 48) {
                return var5;
            } else {
                int var6 = (var3[0] & 255) << 8 | var3[1] & 255;
                if(var0 != var6 && (var0 > 769 || var1 != var6)) {
                    var3 = var5;
                }

                return var3;
            }
        } else {
            return var5;
        }
    }

    private static void validateDHPublicKey(DHPublicKey var0) throws InvalidKeyException {
        DHParameterSpec var1 = var0.getParams();
        BigInteger var2 = var1.getP();
        BigInteger var3 = var1.getG();
        BigInteger var4 = var0.getY();
        validateDHPublicKey(var2, var3, var4);
    }

    private static void validateDHPublicKey(DHPublicKeySpec var0) throws InvalidKeyException {
        validateDHPublicKey(var0.getP(), var0.getG(), var0.getY());
    }

    private static void validateDHPublicKey(BigInteger var0, BigInteger var1, BigInteger var2) throws InvalidKeyException {
        BigInteger var3 = BigInteger.ONE;
        BigInteger var4 = var0.subtract(BigInteger.ONE);
        if(var2.compareTo(var3) <= 0) {
            throw new InvalidKeyException("Diffie-Hellman public key is too small");
        } else if(var2.compareTo(var4) >= 0) {
            throw new InvalidKeyException("Diffie-Hellman public key is too large");
        } else {
            BigInteger var5 = var0.remainder(var2);
            if(var5.equals(BigInteger.ZERO)) {
                throw new InvalidKeyException("Invalid Diffie-Hellman parameters");
            }
        }
    }

    public static byte[] trimZeroes(byte[] var0) {
        int var1;
        for(var1 = 0; var1 < var0.length - 1 && var0[var1] == 0; ++var1) {
            ;
        }

        if(var1 == 0) {
            return var0;
        } else {
            byte[] var2 = new byte[var0.length - var1];
            System.arraycopy(var0, var1, var2, 0, var2.length);
            return var2;
        }
    }
}
