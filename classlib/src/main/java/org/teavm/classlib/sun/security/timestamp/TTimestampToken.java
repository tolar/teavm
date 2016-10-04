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
package org.teavm.classlib.sun.security.timestamp;

import java.io.IOException;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.util.TDate;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;
import org.teavm.classlib.sun.security.x509.TAlgorithmId;

public class TTimestampToken {
    private int version;
    private TObjectIdentifier policy;
    private TBigInteger serialNumber;
    private TAlgorithmId hashAlgorithm;
    private byte[] hashedMessage;
    private TDate genTime;
    private TBigInteger nonce;

    public TTimestampToken(byte[] var1) throws IOException {
        if(var1 == null) {
            throw new IOException("No timestamp token info");
        } else {
            this.parse(var1);
        }
    }

    public TDate getDate() {
        return this.genTime;
    }

    public TAlgorithmId getHashAlgorithm() {
        return this.hashAlgorithm;
    }

    public byte[] getHashedMessage() {
        return this.hashedMessage;
    }

    public TBigInteger getNonce() {
        return this.nonce;
    }

    public String getPolicyID() {
        return this.policy.toString();
    }

    public TBigInteger getSerialNumber() {
        return this.serialNumber;
    }

    private void parse(byte[] var1) throws TIOException {
        TDerValue var2 = new TDerValue(var1);
        if(var2.tag != 48) {
            throw new TIOException(TString.wrap("Bad encoding for timestamp token info"));
        } else {
            this.version = var2.data.getInteger();
            this.policy = var2.data.getOID();
            TDerValue var3 = var2.data.getDerValue();
            this.hashAlgorithm = TAlgorithmId.parse(var3.data.getDerValue());
            this.hashedMessage = var3.data.getOctetString();
            this.serialNumber = var2.data.getBigInteger();
            this.genTime = var2.data.getGeneralizedTime();

            while(var2.data.available() > 0) {
                TDerValue var4 = var2.data.getDerValue();
                if(var4.tag == 2) {
                    this.nonce = var4.getBigInteger();
                    break;
                }
            }

        }
    }
}
