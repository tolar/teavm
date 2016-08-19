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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.sun.security.util.TBitArray;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TUniqueIdentity {
    private TBitArray id;

    public TUniqueIdentity(TBitArray var1) {
        this.id = var1;
    }

    public TUniqueIdentity(byte[] var1) {
        this.id = new TBitArray(var1.length * 8, var1);
    }

    public TUniqueIdentity(TDerInputStream var1) throws TIOException {
        TDerValue var2 = var1.getDerValue();
        this.id = var2.getUnalignedBitString(true);
    }

    public TUniqueIdentity(TDerValue var1) throws TIOException {
        this.id = var1.getUnalignedBitString(true);
    }

    public String toString() {
        return "UniqueIdentity:" + this.id.toString() + "\n";
    }

    public void encode(TDerOutputStream var1, byte var2) throws TIOException {
        byte[] var3 = this.id.toByteArray();
        int var4 = var3.length * 8 - this.id.length();
        var1.write(var2);
        var1.putLength(var3.length + 1);
        var1.write(var4);
        var1.write(var3);
    }

    public boolean[] getId() {
        return this.id == null?null:this.id.toBooleanArray();
    }
}
