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

public class TPBEParameterSpec implements TAlgorithmParameterSpec {
    private byte[] salt;
    private int iterationCount;
    private TAlgorithmParameterSpec paramSpec = null;

    public TPBEParameterSpec(byte[] var1, int var2) {
        this.salt = (byte[])var1.clone();
        this.iterationCount = var2;
    }

    public TPBEParameterSpec(byte[] var1, int var2, TAlgorithmParameterSpec var3) {
        this.salt = (byte[])var1.clone();
        this.iterationCount = var2;
        this.paramSpec = var3;
    }

    public byte[] getSalt() {
        return (byte[])this.salt.clone();
    }

    public int getIterationCount() {
        return this.iterationCount;
    }

    public TAlgorithmParameterSpec getParameterSpec() {
        return this.paramSpec;
    }
}
