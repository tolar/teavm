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
package org.teavm.classlib.sun.security.pkcs;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.sun.misc.THexDumpEncoder;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.x509.TGeneralNames;
import org.teavm.classlib.sun.security.x509.TSerialNumber;

class TESSCertId {
    private static volatile THexDumpEncoder hexDumper;
    private byte[] certHash;
    private TGeneralNames issuer;
    private TSerialNumber serialNumber;

    TESSCertId(TDerValue var1) throws TIOException {
        this.certHash = var1.data.getDerValue().toByteArray();
        if(var1.data.available() > 0) {
            TDerValue var2 = var1.data.getDerValue();
            this.issuer = new TGeneralNames(var2.data.getDerValue());
            this.serialNumber = new TSerialNumber(var2.data.getDerValue());
        }

    }

    public String toString() {
        StringBuffer var1 = new StringBuffer();
        var1.append("[\n\tCertificate hash (SHA-1):\n");
        if(hexDumper == null) {
            hexDumper = new THexDumpEncoder();
        }

        var1.append(hexDumper.encode(this.certHash));
        if(this.issuer != null && this.serialNumber != null) {
            var1.append("\n\tIssuer: " + this.issuer + "\n");
            var1.append("\t" + this.serialNumber);
        }

        var1.append("\n]");
        return var1.toString();
    }
}
