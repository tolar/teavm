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

public class TCertificatePolicyMap {
    private TCertificatePolicyId issuerDomain;
    private TCertificatePolicyId subjectDomain;

    public TCertificatePolicyMap(TCertificatePolicyId var1, TCertificatePolicyId var2) {
        this.issuerDomain = var1;
        this.subjectDomain = var2;
    }

    public TCertificatePolicyMap(TDerValue var1) throws IOException {
        if(var1.tag != 48) {
            throw new IOException("Invalid encoding for CertificatePolicyMap");
        } else {
            this.issuerDomain = new TCertificatePolicyId(var1.data.getDerValue());
            this.subjectDomain = new TCertificatePolicyId(var1.data.getDerValue());
        }
    }

    public TCertificatePolicyId getIssuerIdentifier() {
        return this.issuerDomain;
    }

    public TCertificatePolicyId getSubjectIdentifier() {
        return this.subjectDomain;
    }

    public String toString() {
        String var1 = "CertificatePolicyMap: [\nIssuerDomain:" + this.issuerDomain.toString() + "SubjectDomain:" + this.subjectDomain.toString() + "]\n";
        return var1;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        this.issuerDomain.encode(var2);
        this.subjectDomain.encode(var2);
        var1.write((byte) 48, var2);
    }
}
