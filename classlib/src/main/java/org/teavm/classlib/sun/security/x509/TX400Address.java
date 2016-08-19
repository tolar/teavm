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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TX400Address implements TGeneralNameInterface {
    byte[] nameValue = null;

    public TX400Address(byte[] var1) {
        this.nameValue = var1;
    }

    public TX400Address(TDerValue var1) throws IOException {
        this.nameValue = var1.toByteArray();
    }

    public int getType() {
        return 3;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerValue var2 = new TDerValue(this.nameValue);
        var1.putDerValue(var2);
    }

    public String toString() {
        return "X400Address: <DER-encoded value>";
    }

    public int constrains(TGeneralNameInterface var1) throws UnsupportedOperationException {
        byte var2;
        if(var1 == null) {
            var2 = -1;
        } else {
            if(var1.getType() == 3) {
                throw new UnsupportedOperationException("Narrowing, widening, and match are not supported for X400Address.");
            }

            var2 = -1;
        }

        return var2;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth not supported for X400Address");
    }
}
