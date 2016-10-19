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
package org.teavm.classlib.sun.security.timestamp;

import java.security.NoSuchAlgorithmException;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.security.TMessageDigest;
import org.teavm.classlib.java.security.cert.TX509Extension;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;
import org.teavm.classlib.sun.security.x509.TAlgorithmId;

/**
 * Created by vasek on 7. 10. 2016.
 */
public class TTSRequest {
    private int version = 1;
    private TAlgorithmId hashAlgorithmId = null;
    private byte[] hashValue;
    private TString policyId = null;
    private TBigInteger nonce = null;
    private boolean returnCertificate = false;
    private TX509Extension[] extensions = null;

    public TTSRequest(TString var1, byte[] var2, TMessageDigest var3) throws NoSuchAlgorithmException {
        this.policyId = var1;
        this.hashAlgorithmId = TAlgorithmId.get(var3.getAlgorithm());
        this.hashValue = var3.digest(var2);
    }

    public byte[] getHashedMessage() {
        return (byte[])this.hashValue.clone();
    }

    public void setVersion(int var1) {
        this.version = var1;
    }

    public void setPolicyId(TString var1) {
        this.policyId = var1;
    }

    public void setNonce(TBigInteger var1) {
        this.nonce = var1;
    }

    public void requestCertificate(boolean var1) {
        this.returnCertificate = var1;
    }

    public void setExtensions(TX509Extension[] var1) {
        this.extensions = var1;
    }

    public byte[] encode() throws TIOException {
        TDerOutputStream var1 = new TDerOutputStream();
        var1.putInteger(this.version);
        TDerOutputStream var2 = new TDerOutputStream();
        this.hashAlgorithmId.encode(var2);
        var2.putOctetString(this.hashValue);
        var1.write((byte)48, var2);
        if(this.policyId != null) {
            var1.putOID(new TObjectIdentifier(this.policyId));
        }

        if(this.nonce != null) {
            var1.putInteger(this.nonce);
        }

        if(this.returnCertificate) {
            var1.putBoolean(true);
        }

        TDerOutputStream var3 = new TDerOutputStream();
        var3.write((byte)48, var1);
        return var3.toByteArray();
    }
}
