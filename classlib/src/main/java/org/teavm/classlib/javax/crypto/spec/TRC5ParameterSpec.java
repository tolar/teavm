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
package org.teavm.classlib.javax.crypto.spec;

import org.teavm.classlib.java.security.spec.TAlgorithmParameterSpec;
import org.teavm.classlib.java.util.TArrays;

public class TRC5ParameterSpec implements TAlgorithmParameterSpec {
    private byte[] iv;
    private int version;
    private int rounds;
    private int wordSize;

    public TRC5ParameterSpec(int var1, int var2, int var3) {
        this.iv = null;
        this.version = var1;
        this.rounds = var2;
        this.wordSize = var3;
    }

    public TRC5ParameterSpec(int var1, int var2, int var3, byte[] var4) {
        this(var1, var2, var3, var4, 0);
    }

    public TRC5ParameterSpec(int var1, int var2, int var3, byte[] var4, int var5) {
        this.iv = null;
        this.version = var1;
        this.rounds = var2;
        this.wordSize = var3;
        if(var4 == null) {
            throw new IllegalArgumentException("IV missing");
        } else {
            int var6 = var3 / 8 * 2;
            if(var4.length - var5 < var6) {
                throw new IllegalArgumentException("IV too short");
            } else {
                this.iv = new byte[var6];
                System.arraycopy(var4, var5, this.iv, 0, var6);
            }
        }
    }

    public int getVersion() {
        return this.version;
    }

    public int getRounds() {
        return this.rounds;
    }

    public int getWordSize() {
        return this.wordSize;
    }

    public byte[] getIV() {
        return this.iv == null?null:(byte[])this.iv.clone();
    }

    public boolean equals(Object var1) {
        if(var1 == this) {
            return true;
        } else if(!(var1 instanceof javax.crypto.spec.RC5ParameterSpec)) {
            return false;
        } else {
            TRC5ParameterSpec var2 = (TRC5ParameterSpec)var1;
            return this.version == var2.version && this.rounds == var2.rounds && this.wordSize == var2.wordSize && TArrays
                    .equals(this.iv, var2.iv);
        }
    }

    public int hashCode() {
        int var1 = 0;
        if(this.iv != null) {
            for(int var2 = 1; var2 < this.iv.length; ++var2) {
                var1 += this.iv[var2] * var2;
            }
        }

        var1 += this.version + this.rounds + this.wordSize;
        return var1;
    }
}
