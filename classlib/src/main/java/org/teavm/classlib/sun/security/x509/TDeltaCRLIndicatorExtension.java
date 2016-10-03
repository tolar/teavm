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
import java.io.OutputStream;
import java.math.BigInteger;

import org.teavm.classlib.sun.security.util.TDerOutputStream;

public class TDeltaCRLIndicatorExtension extends TCRLNumberExtension {
    public static final String NAME = "DeltaCRLIndicator";
    private static final String LABEL = "Base CRL Number";

    public TDeltaCRLIndicatorExtension(int var1) throws IOException {
        super(TPKIXExtensions.DeltaCRLIndicator_Id, true, BigInteger.valueOf((long)var1), "DeltaCRLIndicator", "Base CRL Number");
    }

    public TDeltaCRLIndicatorExtension(BigInteger var1) throws IOException {
        super(TPKIXExtensions.DeltaCRLIndicator_Id, true, var1, "DeltaCRLIndicator", "Base CRL Number");
    }

    public TDeltaCRLIndicatorExtension(Boolean var1, Object var2) throws IOException {
        super(TPKIXExtensions.DeltaCRLIndicator_Id, Boolean.valueOf(var1.booleanValue()), var2, "DeltaCRLIndicator", "Base CRL Number");
    }

    public void encode(OutputStream var1) throws IOException {
        new TDerOutputStream();
        super.encode(var1, TPKIXExtensions.DeltaCRLIndicator_Id, true);
    }
}
