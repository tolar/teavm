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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

import javax.crypto.ExemptionMechanismException;
import javax.crypto.ShortBufferException;

import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TAlgorithmParameters;
import org.teavm.classlib.java.security.TKey;
import org.teavm.classlib.java.security.TProvider;
import org.teavm.classlib.java.security.spec.TAlgorithmParameterSpec;
import org.teavm.classlib.sun.security.jca.TGetInstance;

public class TExemptionMechanism {
    private TProvider provider;
    private TExemptionMechanismSpi exmechSpi;
    private TString mechanism;
    private boolean done = false;
    private boolean initialized = false;
    private TKey keyStored = null;

    protected TExemptionMechanism(TExemptionMechanismSpi var1, TProvider var2, TString var3) {
        this.exmechSpi = var1;
        this.provider = var2;
        this.mechanism = var3;
    }

    public final TString getName() {
        return this.mechanism;
    }

    public static final TExemptionMechanism getInstance(TString var0) throws NoSuchAlgorithmException {
        TGetInstance.Instance var1 = TJceSecurity.getInstance("TExemptionMechanism", TExemptionMechanismSpi.class, var0);
        return new TExemptionMechanism((TExemptionMechanismSpi)var1.impl, var1.provider, var0);
    }

    public static final TExemptionMechanism getInstance(String var0, String var1) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        TGetInstance.Instance
                var2 = TJceSecurity.getInstance("TExemptionMechanism", TExemptionMechanismSpi.class, var0, var1);
        return new TExemptionMechanism((TExemptionMechanismSpi)var2.impl, var2.provider, var0);
    }

    public static final TExemptionMechanism getInstance(String var0, Provider var1) throws NoSuchAlgorithmException {
        TGetInstance.Instance
                var2 = TJceSecurity.getInstance("TExemptionMechanism", TExemptionMechanismSpi.class, var0, var1);
        return new TExemptionMechanism((TExemptionMechanismSpi)var2.impl, var2.provider, var0);
    }

    public final TProvider getProvider() {
        return this.provider;
    }

    public final boolean isCryptoAllowed(Key var1) throws ExemptionMechanismException {
        boolean var2 = false;
        if(this.done && var1 != null) {
            var2 = this.keyStored.equals(var1);
        }

        return var2;
    }

    public final int getOutputSize(int var1) throws IllegalStateException {
        if(!this.initialized) {
            throw new IllegalStateException("ExemptionMechanism not initialized");
        } else if(var1 < 0) {
            throw new IllegalArgumentException("Input size must be equal to or greater than zero");
        } else {
            return this.exmechSpi.engineGetOutputSize(var1);
        }
    }

    public final void init(TKey var1) throws InvalidKeyException, ExemptionMechanismException {
        this.done = false;
        this.initialized = false;
        this.keyStored = var1;
        this.exmechSpi.engineInit(var1);
        this.initialized = true;
    }

    public final void init(TKey var1, TAlgorithmParameterSpec var2) throws InvalidKeyException,
            InvalidAlgorithmParameterException, ExemptionMechanismException {
        this.done = false;
        this.initialized = false;
        this.keyStored = var1;
        this.exmechSpi.engineInit(var1, var2);
        this.initialized = true;
    }

    public final void init(TKey var1, TAlgorithmParameters var2) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException {
        this.done = false;
        this.initialized = false;
        this.keyStored = var1;
        this.exmechSpi.engineInit(var1, var2);
        this.initialized = true;
    }

    public final byte[] genExemptionBlob() throws IllegalStateException, ExemptionMechanismException {
        if(!this.initialized) {
            throw new IllegalStateException("ExemptionMechanism not initialized");
        } else {
            byte[] var1 = this.exmechSpi.engineGenExemptionBlob();
            this.done = true;
            return var1;
        }
    }

    public final int genExemptionBlob(byte[] var1) throws IllegalStateException, ShortBufferException, ExemptionMechanismException {
        if(!this.initialized) {
            throw new IllegalStateException("ExemptionMechanism not initialized");
        } else {
            int var2 = this.exmechSpi.engineGenExemptionBlob(var1, 0);
            this.done = true;
            return var2;
        }
    }

    public final int genExemptionBlob(byte[] var1, int var2) throws IllegalStateException, ShortBufferException, ExemptionMechanismException {
        if(!this.initialized) {
            throw new IllegalStateException("ExemptionMechanism not initialized");
        } else {
            int var3 = this.exmechSpi.engineGenExemptionBlob(var1, var2);
            this.done = true;
            return var3;
        }
    }

    protected void finalize() {
        this.keyStored = null;
    }
}
