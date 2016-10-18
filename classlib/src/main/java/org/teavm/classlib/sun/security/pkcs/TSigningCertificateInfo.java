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
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TSigningCertificateInfo {
    private byte[] ber = null;
    private TESSCertId[] certId = null;

    public TSigningCertificateInfo(byte[] var1) throws TIOException {
        this.parse(var1);
    }

    public String toString() {
        StringBuffer var1 = new StringBuffer();
        var1.append("[\n");

        for(int var2 = 0; var2 < this.certId.length; ++var2) {
            var1.append(this.certId[var2].toString());
        }

        var1.append("\n]");
        return var1.toString();
    }

    public void parse(byte[] var1) throws TIOException {
        TDerValue var2 = new TDerValue(var1);
        if(var2.tag != 48) {
            throw new TIOException(TString.wrap("Bad encoding for signingCertificate"));
        } else {
            TDerValue[] var3 = var2.data.getSequence(1);
            this.certId = new TESSCertId[var3.length];

            for(int var4 = 0; var4 < var3.length; ++var4) {
                this.certId[var4] = new TESSCertId(var3[var4]);
            }

            if(var2.data.available() > 0) {
                TDerValue[] var6 = var2.data.getSequence(1);

                for(int var5 = 0; var5 < var6.length; ++var5) {
                    ;
                }
            }

        }
    }
}
