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
import java.io.OutputStream;
import java.math.BigInteger;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDebug;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TCRLNumberExtension extends TExtension implements TCertAttrSet<String> {
    public static final TString NAME = TString.wrap("CRLNumber");
    public static final TString NUMBER = TString.wrap("value");
    private static final String LABEL = "CRL Number";
    private TBigInteger crlNumber;
    private String extensionName;
    private String extensionLabel;

    private void encodeThis() throws TIOException {
        if(this.crlNumber == null) {
            this.extensionValue = null;
        } else {
            TDerOutputStream var1 = new TDerOutputStream();
            var1.putInteger(this.crlNumber);
            this.extensionValue = var1.toByteArray();
        }
    }

    public TCRLNumberExtension(int var1) throws IOException {
        this(TPKIXExtensions.CRLNumber_Id, false, BigInteger.valueOf((long)var1), "CRLNumber", "CRL Number");
    }

    public TCRLNumberExtension(BigInteger var1) throws IOException {
        this(TPKIXExtensions.CRLNumber_Id, false, var1, "CRLNumber", "CRL Number");
    }

    protected TCRLNumberExtension(TObjectIdentifier var1, boolean var2, TBigInteger var3, String var4, String var5) throws IOException {
        this.crlNumber = null;
        this.extensionId = var1;
        this.critical = var2;
        this.crlNumber = var3;
        this.extensionName = var4;
        this.extensionLabel = var5;
        this.encodeThis();
    }

    public TCRLNumberExtension(Boolean var1, Object var2) throws IOException {
        this(TPKIXExtensions.CRLNumber_Id, var1, var2, "CRLNumber", "CRL Number");
    }

    protected TCRLNumberExtension(TObjectIdentifier var1, Boolean var2, Object var3, String var4, String var5) throws IOException {
        this.crlNumber = null;
        this.extensionId = var1;
        this.critical = var2.booleanValue();
        this.extensionValue = (byte[])((byte[])var3);
        TDerValue var6 = new TDerValue(this.extensionValue);
        this.crlNumber = var6.getBigInteger();
        this.extensionName = var4;
        this.extensionLabel = var5;
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("value"))) {
            if(!(var2 instanceof BigInteger)) {
                throw new TIOException(TString.wrap("Attribute must be of type BigInteger."));
            } else {
                this.crlNumber = (TBigInteger)var2;
                this.encodeThis();
            }
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:" + this.extensionName + "."));
        }
    }

    public TBigInteger get(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("value"))) {
            return this.crlNumber;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:" + this.extensionName + '.'));
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("value"))) {
            this.crlNumber = null;
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:" + this.extensionName + "."));
        }
    }

    public String toString() {
        String var1 = super.toString() + this.extensionLabel + ": " + (this.crlNumber == null?"": TDebug.toHexString(this.crlNumber)) + "\n";
        return var1;
    }

    public void encode(OutputStream var1) throws IOException {
        new TDerOutputStream();
        this.encode(var1, TPKIXExtensions.CRLNumber_Id, true);
    }

    protected void encode(OutputStream var1, TObjectIdentifier var2, boolean var3) throws IOException {
        TDerOutputStream var4 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = var2;
            this.critical = var3;
            this.encodeThis();
        }

        super.encode(var4);
        var1.write(var4.toByteArray());
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("value");
        return var1.elements();
    }

    public String getName() {
        return this.extensionName;
    }
}
