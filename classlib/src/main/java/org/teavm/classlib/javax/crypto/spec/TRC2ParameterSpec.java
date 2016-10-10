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
package org.teavm.classlib.javax.crypto.spec;

import java.util.Arrays;

import org.teavm.classlib.java.security.spec.TAlgorithmParameterSpec;

/**
 * Created by vasek on 8. 10. 2016.
 */
public class TRC2ParameterSpec implements TAlgorithmParameterSpec {
    private byte[] iv;
    private int effectiveKeyBits;

    public TRC2ParameterSpec(int var1) {
        this.iv = null;
        this.effectiveKeyBits = var1;
    }

    public TRC2ParameterSpec(int var1, byte[] var2) {
        this(var1, var2, 0);
    }

    public TRC2ParameterSpec(int var1, byte[] var2, int var3) {
        this.iv = null;
        this.effectiveKeyBits = var1;
        if(var2 == null) {
            throw new IllegalArgumentException("IV missing");
        } else {
            byte var4 = 8;
            if(var2.length - var3 < var4) {
                throw new IllegalArgumentException("IV too short");
            } else {
                this.iv = new byte[var4];
                System.arraycopy(var2, var3, this.iv, 0, var4);
            }
        }
    }

    public int getEffectiveKeyBits() {
        return this.effectiveKeyBits;
    }

    public byte[] getIV() {
        return this.iv == null?null:(byte[])this.iv.clone();
    }

    public boolean equals(Object var1) {
        if(var1 == this) {
            return true;
        } else if(!(var1 instanceof javax.crypto.spec.RC2ParameterSpec)) {
            return false;
        } else {
            TRC2ParameterSpec var2 = (TRC2ParameterSpec)var1;
            return this.effectiveKeyBits == var2.effectiveKeyBits && Arrays.equals(this.iv, var2.iv);
        }
    }

    public int hashCode() {
        int var1 = 0;
        if(this.iv != null) {
            for(int var2 = 1; var2 < this.iv.length; ++var2) {
                var1 += this.iv[var2] * var2;
            }
        }

        return var1 + this.effectiveKeyBits;
    }
}
