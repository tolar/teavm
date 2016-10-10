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

import javax.crypto.ExemptionMechanismException;
import javax.crypto.ShortBufferException;

import org.teavm.classlib.java.security.TAlgorithmParameters;
import org.teavm.classlib.java.security.TKey;
import org.teavm.classlib.java.security.spec.TAlgorithmParameterSpec;

public abstract class TExemptionMechanismSpi {
    public TExemptionMechanismSpi() {
    }

    protected abstract int engineGetOutputSize(int var1);

    protected abstract void engineInit(TKey var1) throws InvalidKeyException, ExemptionMechanismException;

    protected abstract void engineInit(TKey var1, TAlgorithmParameterSpec var2) throws InvalidKeyException,
            InvalidAlgorithmParameterException, ExemptionMechanismException;

    protected abstract void engineInit(TKey var1, TAlgorithmParameters var2) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException;

    protected abstract byte[] engineGenExemptionBlob() throws ExemptionMechanismException;

    protected abstract int engineGenExemptionBlob(byte[] var1, int var2) throws ShortBufferException, ExemptionMechanismException;
}
