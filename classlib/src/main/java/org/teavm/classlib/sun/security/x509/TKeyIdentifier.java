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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.sun.misc.THexDumpEncoder;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TKeyIdentifier {
    private byte[] octetString;

    public TKeyIdentifier(byte[] var1) {
        this.octetString = (byte[])var1.clone();
    }

    public TKeyIdentifier(TDerValue var1) throws IOException {
        this.octetString = var1.getOctetString();
    }

    public TKeyIdentifier(PublicKey var1) throws IOException {
        TDerValue var2 = new TDerValue(var1.getEncoded());
        if(var2.tag != 48) {
            throw new IOException("PublicKey value is not a valid X.509 public key");
        } else {
            TAlgorithmId var3 = TAlgorithmId.parse(var2.data.getDerValue());
            byte[] var4 = var2.data.getUnalignedBitString().toByteArray();
            MessageDigest var5 = null;

            try {
                var5 = MessageDigest.getInstance("SHA1");
            } catch (NoSuchAlgorithmException var7) {
                throw new IOException("SHA1 not supported");
            }

            var5.update(var4);
            this.octetString = var5.digest();
        }
    }

    public byte[] getIdentifier() {
        return (byte[])this.octetString.clone();
    }

    public String toString() {
        String var1 = "TKeyIdentifier [\n";
        THexDumpEncoder var2 = new THexDumpEncoder();
        var1 = var1 + var2.encodeBuffer(this.octetString);
        var1 = var1 + "]\n";
        return var1;
    }

    void encode(TDerOutputStream var1) throws TIOException {
        var1.putOctetString(this.octetString);
    }

    public int hashCode() {
        int var1 = 0;

        for(int var2 = 0; var2 < this.octetString.length; ++var2) {
            var1 += this.octetString[var2] * var2;
        }

        return var1;
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TKeyIdentifier)) {
            return false;
        } else {
            byte[] var2 = ((TKeyIdentifier)var1).octetString;
            return Arrays.equals(this.octetString, var2);
        }
    }
}
