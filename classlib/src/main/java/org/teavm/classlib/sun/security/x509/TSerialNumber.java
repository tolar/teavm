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
package org.teavm.classlib.sun.security.x509;

import java.io.IOException;
import java.math.BigInteger;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TSerialNumber {
    private TBigInteger serialNum;

    private void construct(TDerValue var1) throws TIOException {
        this.serialNum = var1.getBigInteger();
        if(var1.data.available() != 0) {
            throw new TIOException(TString.wrap("Excess TSerialNumber data"));
        }
    }

    public TSerialNumber(TBigInteger var1) {
        this.serialNum = var1;
    }

    public TSerialNumber(int var1) {
        this.serialNum = TBigInteger.valueOf((long)var1);
    }

    public TSerialNumber(TDerInputStream var1) throws IOException {
        TDerValue var2 = var1.getDerValue();
        this.construct(var2);
    }

    public TSerialNumber(TDerValue var1) throws IOException {
        this.construct(var1);
    }

    public TSerialNumber(TInputStream var1) throws IOException {
        TDerValue var2 = new TDerValue(var1);
        this.construct(var2);
    }

    public String toString() {
        return "TSerialNumber: [" + (this.serialNum) + "]";
    }

    public void encode(TDerOutputStream var1) throws IOException {
        var1.putInteger(this.serialNum);
    }

    public BigInteger getNumber() {
        return this.serialNum;
    }
}
