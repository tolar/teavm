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
package org.teavm.classlib.sun.security.x509;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import org.teavm.classlib.java.io.TByteArrayInputStream;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TInvalidKeyException;
import org.teavm.classlib.java.security.TKeyFactory;
import org.teavm.classlib.java.security.TPublicKey;
import org.teavm.classlib.java.security.spec.TInvalidKeySpecException;
import org.teavm.classlib.java.security.spec.TX509EncodedKeySpec;
import org.teavm.classlib.sun.misc.THexDumpEncoder;
import org.teavm.classlib.sun.security.util.TBitArray;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TX509Key implements TPublicKey {
    private static final long serialVersionUID = -5359250853002055002L;
    protected TAlgorithmId algid;
    /** @deprecated */
    @Deprecated
    protected byte[] key = null;
    /** @deprecated */
    @Deprecated
    private int unusedBits = 0;
    private TBitArray bitStringKey = null;
    protected byte[] encodedKey;

    public TX509Key() {
    }

    private TX509Key(TAlgorithmId var1, TBitArray var2) throws TInvalidKeyException {
        this.algid = var1;
        this.setKey(var2);
        this.encode();
    }

    protected void setKey(TBitArray var1) {
        this.bitStringKey = (TBitArray)var1.clone();
        this.key = var1.toByteArray();
        int var2 = var1.length() % 8;
        this.unusedBits = var2 == 0?0:8 - var2;
    }

    protected TBitArray getKey() {
        this.bitStringKey = new TBitArray(this.key.length * 8 - this.unusedBits, this.key);
        return (TBitArray)this.bitStringKey.clone();
    }

    public static TPublicKey parse(TDerValue var0) throws TIOException {
        if(var0.tag != 48) {
            throw new TIOException(TString.wrap("corrupt subject key"));
        } else {
            TAlgorithmId var1 = TAlgorithmId.parse(var0.data.getDerValue());

            TPublicKey var2;
            try {
                var2 = buildX509Key(var1, var0.data.getUnalignedBitString());
            } catch (TInvalidKeyException var4) {
                throw new TIOException(TString.wrap("subject key, " + var4.getMessage()), var4);
            }

            if(var0.data.available() != 0) {
                throw new TIOException(TString.wrap("excess subject key"));
            } else {
                return var2;
            }
        }
    }

    protected void parseKeyBits() throws IOException, InvalidKeyException {
        this.encode();
    }

    static TPublicKey buildX509Key(TAlgorithmId var0, TBitArray var1) throws TIOException, TInvalidKeyException {
        TDerOutputStream var2 = new TDerOutputStream();
        encode(var2, var0, var1);
        TX509EncodedKeySpec var3 = new TX509EncodedKeySpec(var2.toByteArray());

        try {
            TKeyFactory var18 = TKeyFactory.getInstance(var0.getName());
            return var18.generatePublic(var3);
        } catch (NoSuchAlgorithmException var15) {
            String var4 = "";

            try {
                Provider var7 = Security.getProvider("SUN");
                if(var7 == null) {
                    throw new InstantiationException();
                }

                var4 = var7.getProperty("PublicKey.X.509." + var0.getName());
                if(var4 == null) {
                    throw new InstantiationException();
                }

                Class var8 = null;

                try {
                    var8 = Class.forName(var4);
                } catch (ClassNotFoundException var11) {
                    ClassLoader var10 = ClassLoader.getSystemClassLoader();
                    if(var10 != null) {
                        var8 = var10.loadClass(var4);
                    }
                }

                Object var9 = null;
                if(var8 != null) {
                    var9 = var8.newInstance();
                }

                if(var9 instanceof TX509Key) {
                    TX509Key var17 = (TX509Key)var9;
                    var17.algid = var0;
                    var17.setKey(var1);
                    var17.parseKeyBits();
                    return var17;
                }
            } catch (ClassNotFoundException var12) {
                ;
            } catch (InstantiationException var13) {
                ;
            } catch (IllegalAccessException var14) {
                throw new TIOException(TString.wrap(var4 + " [internal error]"));
            }

            TX509Key var5 = new TX509Key(var0, var1);
            return var5;
        } catch (TInvalidKeySpecException var16) {
            throw new TInvalidKeyException(TString.wrap(var16.getMessage()), var16);
        }
    }

    public String getAlgorithm() {
        return this.algid.getName();
    }

    public TAlgorithmId getAlgorithmId() {
        return this.algid;
    }

    public final void encode(TDerOutputStream var1) throws IOException {
        encode(var1, this.algid, this.getKey());
    }

    public byte[] getEncoded() {
        try {
            return (byte[])this.getEncodedInternal().clone();
        } catch (TInvalidKeyException var2) {
            return null;
        }
    }

    public byte[] getEncodedInternal() throws TInvalidKeyException {
        byte[] var1 = this.encodedKey;
        if(var1 == null) {
            try {
                TDerOutputStream var2 = new TDerOutputStream();
                this.encode(var2);
                var1 = var2.toByteArray();
            } catch (IOException var3) {
                throw new TInvalidKeyException(TString.wrap("IOException : " + var3.getMessage()));
            }

            this.encodedKey = var1;
        }

        return var1;
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] encode() throws TInvalidKeyException {
        return (byte[])this.getEncodedInternal().clone();
    }

    public String toString() {
        THexDumpEncoder var1 = new THexDumpEncoder();
        return "algorithm = " + this.algid.toString() + ", unparsed keybits = \n" + var1.encodeBuffer(this.key);
    }

    public void decode(TInputStream var1) throws InvalidKeyException {
        try {
            TDerValue var2 = new TDerValue(var1);
            if(var2.tag != 48) {
                throw new InvalidKeyException("invalid key format");
            } else {
                this.algid = TAlgorithmId.parse(var2.data.getDerValue());
                this.setKey(var2.data.getUnalignedBitString());
                this.parseKeyBits();
                if(var2.data.available() != 0) {
                    throw new InvalidKeyException("excess key data");
                }
            }
        } catch (IOException var4) {
            throw new InvalidKeyException("IOException: " + var4.getMessage());
        }
    }

    public void decode(byte[] var1) throws InvalidKeyException {
        this.decode((TInputStream)(new TByteArrayInputStream(var1)));
    }

//    private void writeObject(ObjectOutputStream var1) throws IOException {
//        var1.write(this.getEncoded());
//    }
//
//    private void readObject(ObjectInputStream var1) throws IOException {
//        try {
//            this.decode((TInputStream)var1);
//        } catch (InvalidKeyException var3) {
//            var3.printStackTrace();
//            throw new IOException("deserialized key is invalid: " + var3.getMessage());
//        }
//    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof Key)) {
            return false;
        } else {
            try {
                byte[] var2 = this.getEncodedInternal();
                byte[] var3;
                if(var1 instanceof TX509Key) {
                    var3 = ((TX509Key)var1).getEncodedInternal();
                } else {
                    var3 = ((Key)var1).getEncoded();
                }

                return Arrays.equals(var2, var3);
            } catch (TInvalidKeyException var4) {
                return false;
            }
        }
    }

    public int hashCode() {
        try {
            byte[] var1 = this.getEncodedInternal();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var1.length; ++var3) {
                var2 += (var1[var3] & 255) * 37;
            }

            return var2;
        } catch (TInvalidKeyException var4) {
            return 0;
        }
    }

    static void encode(TDerOutputStream var0, TAlgorithmId var1, TBitArray var2) throws TIOException {
        TDerOutputStream var3 = new TDerOutputStream();
        var1.encode(var3);
        var3.putUnalignedBitString(var2);
        var0.write((byte) 48, var3);
    }
}
