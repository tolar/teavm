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
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TCertificatePolicyId {
    private TObjectIdentifier id;

    public TCertificatePolicyId(TObjectIdentifier var1) {
        this.id = var1;
    }

    public TCertificatePolicyId(TDerValue var1) throws IOException {
        this.id = var1.getOID();
    }

    public TObjectIdentifier getIdentifier() {
        return this.id;
    }

    public String toString() {
        String var1 = "CertificatePolicyId: [" + this.id.toString() + "]\n";
        return var1;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        var1.putOID(this.id);
    }

    public boolean equals(Object var1) {
        return var1 instanceof sun.security.x509.CertificatePolicyId
                ?this.id.equals(((sun.security.x509.CertificatePolicyId)var1).getIdentifier()):false;
    }

    public int hashCode() {
        return this.id.hashCode();
    }
}
