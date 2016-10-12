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
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TOIDName implements TGeneralNameInterface {
    private TObjectIdentifier oid;

    public TOIDName(TDerValue var1) throws TIOException {
        this.oid = var1.getOID();
    }

    public TOIDName(TObjectIdentifier var1) {
        this.oid = var1;
    }

    public TOIDName(TString var1) throws IOException {
        try {
            this.oid = new TObjectIdentifier(var1);
        } catch (Exception var3) {
            throw new IOException("Unable to create OIDName: " + var3);
        }
    }

    public int getType() {
        return 8;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        var1.putOID(this.oid);
    }

    public String toString() {
        return "OIDName: " + this.oid.toString();
    }

    public TObjectIdentifier getOID() {
        return this.oid;
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TOIDName)) {
            return false;
        } else {
            TOIDName var2 = (TOIDName)var1;
            return this.oid.equals(var2.oid);
        }
    }

    public int hashCode() {
        return this.oid.hashCode();
    }

    public int constrains(TGeneralNameInterface var1) throws UnsupportedOperationException {
        byte var2;
        if(var1 == null) {
            var2 = -1;
        } else if(var1.getType() != 8) {
            var2 = -1;
        } else {
            if(!this.equals((TOIDName)var1)) {
                throw new UnsupportedOperationException("Narrowing and widening are not supported for OIDNames");
            }

            var2 = 0;
        }

        return var2;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth() not supported for OIDName.");
    }
}
