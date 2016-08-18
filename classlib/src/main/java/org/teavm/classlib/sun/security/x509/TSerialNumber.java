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
import java.io.InputStream;
import java.math.BigInteger;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.sun.security.util.TDerValue;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

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

    public TSerialNumber(DerInputStream var1) throws IOException {
        DerValue var2 = var1.getDerValue();
        this.construct(var2);
    }

    public TSerialNumber(DerValue var1) throws IOException {
        this.construct(var1);
    }

    public TSerialNumber(InputStream var1) throws IOException {
        DerValue var2 = new DerValue(var1);
        this.construct(var2);
    }

    public String toString() {
        return "TSerialNumber: [" + Debug.toHexString(this.serialNum) + "]";
    }

    public void encode(DerOutputStream var1) throws IOException {
        var1.putInteger(this.serialNum);
    }

    public BigInteger getNumber() {
        return this.serialNum;
    }
}
