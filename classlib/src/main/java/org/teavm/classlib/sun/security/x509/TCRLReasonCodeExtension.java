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
import java.security.cert.CRLReason;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TCRLReasonCodeExtension extends TExtension implements TCertAttrSet<TString> {
    public static final String NAME = "CRLReasonCode";
    public static final String REASON = "reason";
    private static CRLReason[] values = CRLReason.values();
    private int reasonCode;

    private void encodeThis() throws TIOException {
        if(this.reasonCode == 0) {
            this.extensionValue = null;
        } else {
            TDerOutputStream var1 = new TDerOutputStream();
            var1.putEnumerated(this.reasonCode);
            this.extensionValue = var1.toByteArray();
        }
    }

    public TCRLReasonCodeExtension(int var1) throws IOException {
        this(false, var1);
    }

    public TCRLReasonCodeExtension(boolean var1, int var2) throws IOException {
        this.reasonCode = 0;
        this.extensionId = TPKIXExtensions.ReasonCode_Id;
        this.critical = var1;
        this.reasonCode = var2;
        this.encodeThis();
    }

    public TCRLReasonCodeExtension(Boolean var1, Object var2) throws TIOException {
        this.reasonCode = 0;
        this.extensionId = TPKIXExtensions.ReasonCode_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        TDerValue var3 = new TDerValue(this.extensionValue);
        this.reasonCode = var3.getEnumerated();
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(!(var2 instanceof Integer)) {
            throw new TIOException(TString.wrap("Attribute must be of type Integer."));
        } else if(var1.equalsIgnoreCase(TString.wrap("reason"))) {
            this.reasonCode = ((Integer)var2).intValue();
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Name not supported by CRLReasonCodeExtension"));
        }
    }

    public Integer get(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("reason"))) {
            return new Integer(this.reasonCode);
        } else {
            throw new TIOException(TString.wrap("Name not supported by CRLReasonCodeExtension"));
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("reason"))) {
            this.reasonCode = 0;
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Name not supported by CRLReasonCodeExtension"));
        }
    }

    public String toString() {
        return super.toString() + "    Reason Code: " + this.getReasonCode();
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.ReasonCode_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public TEnumeration<TString> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("reason");
        return var1.elements();
    }

    public String getName() {
        return "CRLReasonCode";
    }

    public CRLReason getReasonCode() {
        return this.reasonCode > 0 && this.reasonCode < values.length?values[this.reasonCode]:CRLReason.UNSPECIFIED;
    }
}
