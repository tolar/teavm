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

import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.nio.TByteBuffer;
import org.teavm.classlib.java.security.TAlgorithmParameters;
import org.teavm.classlib.java.security.TInvalidAlgorithmParameterException;
import org.teavm.classlib.java.security.TInvalidKeyException;
import org.teavm.classlib.java.security.TKey;
import org.teavm.classlib.java.security.TNoSuchAlgorithmException;
import org.teavm.classlib.java.security.TProviderException;
import org.teavm.classlib.java.security.TSecureRandom;
import org.teavm.classlib.java.security.spec.TAlgorithmParameterSpec;

public abstract class TCipherSpi {
    public TCipherSpi() {
    }

    protected abstract void engineSetMode(String var1) throws TNoSuchAlgorithmException;

    protected abstract void engineSetPadding(String var1) throws TNoSuchPaddingException;

    protected abstract int engineGetBlockSize();

    protected abstract int engineGetOutputSize(int var1);

    protected abstract byte[] engineGetIV();

    protected abstract TAlgorithmParameters engineGetParameters();

    protected abstract void engineInit(int var1, TKey var2, TSecureRandom var3) throws TInvalidKeyException;

    protected abstract void engineInit(int var1, TKey var2, TAlgorithmParameterSpec var3, TSecureRandom var4) throws TInvalidKeyException,
            TInvalidAlgorithmParameterException;

    protected abstract void engineInit(int var1, TKey var2, TAlgorithmParameters var3, TSecureRandom var4) throws TInvalidKeyException, TInvalidAlgorithmParameterException;

    protected abstract byte[] engineUpdate(byte[] var1, int var2, int var3);

    protected abstract int engineUpdate(byte[] var1, int var2, int var3, byte[] var4, int var5) throws
            TShortBufferException;

    protected int engineUpdate(TByteBuffer var1, TByteBuffer var2) throws TShortBufferException {
        try {
            return this.bufferCrypt(var1, var2, true);
        } catch (TIllegalBlockSizeException var4) {
            throw new TProviderException(TString.wrap("Internal error in update()"));
        } catch (TBadPaddingException var5) {
            throw new TProviderException(TString.wrap("Internal error in update()"));
        }
    }

    protected abstract byte[] engineDoFinal(byte[] var1, int var2, int var3) throws TIllegalBlockSizeException, TBadPaddingException;

    protected abstract int engineDoFinal(byte[] var1, int var2, int var3, byte[] var4, int var5) throws TShortBufferException, TIllegalBlockSizeException, TBadPaddingException;

    protected int engineDoFinal(TByteBuffer var1, TByteBuffer var2) throws TShortBufferException, TIllegalBlockSizeException, TBadPaddingException {
        return this.bufferCrypt(var1, var2, false);
    }

    static int getTempArraySize(int var0) {
        return Math.min(4096, var0);
    }

    private int bufferCrypt(TByteBuffer var1, TByteBuffer var2, boolean var3) throws TShortBufferException, TIllegalBlockSizeException, TBadPaddingException {
        if(var1 != null && var2 != null) {
            int var4 = var1.position();
            int var5 = var1.limit();
            int var6 = var5 - var4;
            if(var3 && var6 == 0) {
                return 0;
            } else {
                int var7 = this.engineGetOutputSize(var6);
                if(var2.remaining() < var7) {
                    throw new TShortBufferException(TString.wrap("Need at least " + var7 + " bytes of space in output buffer"));
                } else {
                    boolean var8 = var1.hasArray();
                    boolean var9 = var2.hasArray();
                    byte[] var10;
                    int var11;
                    byte[] var12;
                    int var13;
                    int var14;
                    int var23;
                    if(var8 && var9) {
                        var10 = var1.array();
                        var11 = var1.arrayOffset() + var4;
                        var12 = var2.array();
                        var13 = var2.position();
                        var14 = var2.arrayOffset() + var13;
                        if(var3) {
                            var23 = this.engineUpdate(var10, var11, var6, var12, var14);
                        } else {
                            var23 = this.engineDoFinal(var10, var11, var6, var12, var14);
                        }

                        var1.position(var5);
                        var2.position(var13 + var23);
                        return var23;
                    } else {
                        int var16;
                        if(!var8 && var9) {
                            int var19 = var2.position();
                            byte[] var21 = var2.array();
                            int var20 = var2.arrayOffset() + var19;
                            byte[] var22 = new byte[getTempArraySize(var6)];
                            var14 = 0;

                            do {
                                var23 = Math.min(var6, var22.length);
                                if(var23 > 0) {
                                    var1.get(var22, 0, var23);
                                }

                                if(!var3 && var6 == var23) {
                                    var16 = this.engineDoFinal(var22, 0, var23, var21, var20);
                                } else {
                                    var16 = this.engineUpdate(var22, 0, var23, var21, var20);
                                }

                                var14 += var16;
                                var20 += var16;
                                var6 -= var23;
                            } while(var6 > 0);

                            var2.position(var19 + var14);
                            return var14;
                        } else {
                            if(var8) {
                                var10 = var1.array();
                                var11 = var1.arrayOffset() + var4;
                            } else {
                                var10 = new byte[getTempArraySize(var6)];
                                var11 = 0;
                            }

                            var12 = new byte[getTempArraySize(var7)];
                            var13 = var12.length;
                            var14 = 0;
                            boolean var15 = false;

                            do {
                                var16 = Math.min(var6, var13 == 0?var10.length:var13);
                                if(!var8 && !var15 && var16 > 0) {
                                    var1.get(var10, 0, var16);
                                    var11 = 0;
                                }

                                try {
                                    int var17;
                                    if(!var3 && var6 == var16) {
                                        var17 = this.engineDoFinal(var10, var11, var16, var12, 0);
                                    } else {
                                        var17 = this.engineUpdate(var10, var11, var16, var12, 0);
                                    }

                                    var15 = false;
                                    var11 += var16;
                                    var6 -= var16;
                                    if(var17 > 0) {
                                        var2.put(var12, 0, var17);
                                        var14 += var17;
                                    }
                                } catch (TShortBufferException var18) {
                                    if(var15) {
                                        throw (TProviderException)(new TProviderException(TString.wrap("Could not determine buffer size"))).initCause(var18);
                                    }

                                    var15 = true;
                                    var13 = this.engineGetOutputSize(var16);
                                    var12 = new byte[var13];
                                }
                            } while(var6 > 0);

                            if(var8) {
                                var1.position(var5);
                            }

                            return var14;
                        }
                    }
                }
            }
        } else {
            throw new NullPointerException("Input and output buffers must not be null");
        }
    }

    protected byte[] engineWrap(TKey var1) throws TIllegalBlockSizeException, TInvalidKeyException {
        throw new UnsupportedOperationException();
    }

    protected TKey engineUnwrap(byte[] var1, String var2, int var3) throws TInvalidKeyException, TNoSuchAlgorithmException {
        throw new UnsupportedOperationException();
    }

    protected int engineGetKeySize(TKey var1) throws TInvalidKeyException {
        throw new UnsupportedOperationException();
    }

    protected void engineUpdateAAD(byte[] var1, int var2, int var3) {
        throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
    }

    protected void engineUpdateAAD(TByteBuffer var1) {
        throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
    }
}
