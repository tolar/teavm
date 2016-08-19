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
import java.util.Arrays;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TExtension implements org.teavm.classlib.java.security.cert.TExtension {
    protected TObjectIdentifier extensionId = null;
    protected boolean critical = false;
    protected byte[] extensionValue = null;
    private static final int hashMagic = 31;

    public TExtension() {
    }

    public TExtension(TDerValue var1) throws IOException {
        TDerInputStream var2 = var1.toDerInputStream();
        this.extensionId = var2.getOID();
        TDerValue var3 = var2.getDerValue();
        if(var3.tag == 1) {
            this.critical = var3.getBoolean();
            var3 = var2.getDerValue();
            this.extensionValue = var3.getOctetString();
        } else {
            this.critical = false;
            this.extensionValue = var3.getOctetString();
        }

    }

    public TExtension(TObjectIdentifier var1, boolean var2, byte[] var3) throws IOException {
        this.extensionId = var1;
        this.critical = var2;
        TDerValue var4 = new TDerValue(var3);
        this.extensionValue = var4.getOctetString();
    }

    public TExtension(TExtension var1) {
        this.extensionId = var1.extensionId;
        this.critical = var1.critical;
        this.extensionValue = var1.extensionValue;
    }

    public static TExtension newExtension(TObjectIdentifier var0, boolean var1, byte[] var2) throws IOException {
        TExtension var3 = new TExtension();
        var3.extensionId = var0;
        var3.critical = var1;
        var3.extensionValue = var2;
        return var3;
    }

    public void encode(TOutputStream var1) throws TIOException {
        if(var1 == null) {
            throw new NullPointerException();
        } else {
            TDerOutputStream var2 = new TDerOutputStream();
            TDerOutputStream var3 = new TDerOutputStream();
            var2.putOID(this.extensionId);
            if(this.critical) {
                var2.putBoolean(this.critical);
            }

            var2.putOctetString(this.extensionValue);
            var3.write((byte) 48, var2);
            var1.write(var3.toByteArray());
        }
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        if(this.extensionId == null) {
            throw new TIOException(TString.wrap("Null OID to encode for the extension!"));
        } else if(this.extensionValue == null) {
            throw new TIOException(TString.wrap("No value to encode for the extension!"));
        } else {
            TDerOutputStream var2 = new TDerOutputStream();
            var2.putOID(this.extensionId);
            if(this.critical) {
                var2.putBoolean(this.critical);
            }

            var2.putOctetString(this.extensionValue);
            var1.write((byte) 48, var2);
        }
    }

    public boolean isCritical() {
        return this.critical;
    }

    public TObjectIdentifier getExtensionId() {
        return this.extensionId;
    }

    public byte[] getValue() {
        return (byte[])this.extensionValue.clone();
    }

    public byte[] getExtensionValue() {
        return this.extensionValue;
    }

    public String getId() {
        return this.extensionId.toString();
    }

    public String toString() {
        String var1 = "ObjectId: " + this.extensionId.toString();
        if(this.critical) {
            var1 = var1 + " Criticality=true\n";
        } else {
            var1 = var1 + " Criticality=false\n";
        }

        return var1;
    }

    public int hashCode() {
        int var1 = 0;
        if(this.extensionValue != null) {
            byte[] var2 = this.extensionValue;

            for(int var3 = var2.length; var3 > 0; var1 += var3-- * var2[var3]) {
                ;
            }
        }

        var1 = var1 * 31 + this.extensionId.hashCode();
        var1 = var1 * 31 + (this.critical?1231:1237);
        return var1;
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TExtension)) {
            return false;
        } else {
            TExtension var2 = (TExtension)var1;
            return this.critical != var2.critical?false:(!this.extensionId.equals(var2.extensionId)?false: Arrays
                    .equals(this.extensionValue, var2.extensionValue));
        }
    }
}
