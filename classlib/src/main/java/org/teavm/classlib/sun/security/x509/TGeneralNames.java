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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;


public class TGeneralNames {
    private final List<TGeneralName> names;

    public TGeneralNames(TDerValue var1) throws IOException {
        this();
        if(var1.tag != 48) {
            throw new IOException("Invalid encoding for GeneralNames.");
        } else if(var1.data.available() == 0) {
            throw new IOException("No data available in passed DER encoded value.");
        } else {
            while(var1.data.available() != 0) {
                TDerValue var2 = var1.data.getDerValue();
                TGeneralName var3 = new TGeneralName(var2);
                this.add(var3);
            }

        }
    }

    public TGeneralNames() {
        this.names = new ArrayList();
    }

    public TGeneralNames add(TGeneralName var1) {
        if(var1 == null) {
            throw new NullPointerException();
        } else {
            this.names.add(var1);
            return this;
        }
    }

    public TGeneralName get(int var1) {
        return (TGeneralName)this.names.get(var1);
    }

    public boolean isEmpty() {
        return this.names.isEmpty();
    }

    public int size() {
        return this.names.size();
    }

    public Iterator<TGeneralName> iterator() {
        return this.names.iterator();
    }

    public List<TGeneralName> names() {
        return this.names;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        if(!this.isEmpty()) {
            TDerOutputStream var2 = new TDerOutputStream();
            Iterator var3 = this.names.iterator();

            while(var3.hasNext()) {
                TGeneralName var4 = (TGeneralName)var3.next();
                var4.encode(var2);
            }

            var1.write((byte) 48, var2);
        }
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TGeneralNames)) {
            return false;
        } else {
            TGeneralNames var2 = (TGeneralNames)var1;
            return this.names.equals(var2.names);
        }
    }

    public int hashCode() {
        return this.names.hashCode();
    }

    public String toString() {
        return this.names.toString();
    }
}
