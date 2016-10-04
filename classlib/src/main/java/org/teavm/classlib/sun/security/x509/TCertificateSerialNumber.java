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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TCertificateSerialNumber implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.serialNumber";
    public static final String NAME = "serialNumber";
    public static final String NUMBER = "number";
    private TSerialNumber serial;

    public TCertificateSerialNumber(TBigInteger var1) {
        this.serial = new TSerialNumber(var1);
    }

    public TCertificateSerialNumber(int var1) {
        this.serial = new TSerialNumber(var1);
    }

    public TCertificateSerialNumber(TDerInputStream var1) throws IOException {
        this.serial = new TSerialNumber(var1);
    }

    public TCertificateSerialNumber(TInputStream var1) throws IOException {
        this.serial = new TSerialNumber(var1);
    }

    public TCertificateSerialNumber(TDerValue var1) throws TIOException {
        this.serial = new TSerialNumber(var1);
    }

    public String toString() {
        return this.serial == null?"":this.serial.toString();
    }

    public void encode(TOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        this.serial.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(!(var2 instanceof TSerialNumber)) {
            throw new TIOException(TString.wrap("Attribute must be of type TSerialNumber."));
        } else if(var1.equalsIgnoreCase(TString.wrap("number"))) {
            this.serial = (TSerialNumber)var2;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:TCertificateSerialNumber."));
        }
    }

    public TSerialNumber get(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("number"))) {
            return this.serial;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:TCertificateSerialNumber."));
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("number"))) {
            this.serial = null;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:TCertificateSerialNumber."));
        }
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("number");
        return var1.elements();
    }

    public String getName() {
        return "serialNumber";
    }
}
