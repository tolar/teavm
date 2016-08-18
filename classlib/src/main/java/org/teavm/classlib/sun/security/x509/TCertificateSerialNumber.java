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
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import sun.security.util.DerOutputStream;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TCertificateSerialNumber implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.serialNumber";
    public static final String NAME = "serialNumber";
    public static final String NUMBER = "number";
    private TSerialNumber serial;

    public TCertificateSerialNumber(BigInteger var1) {
        this.serial = new TSerialNumber(var1);
    }

    public TCertificateSerialNumber(int var1) {
        this.serial = new TSerialNumber(var1);
    }

    public TCertificateSerialNumber(TDerInputStream var1) throws IOException {
        this.serial = new TSerialNumber(var1);
    }

    public TCertificateSerialNumber(InputStream var1) throws IOException {
        this.serial = new TSerialNumber(var1);
    }

    public TCertificateSerialNumber(TDerValue var1) throws IOException {
        this.serial = new TSerialNumber(var1);
    }

    public String toString() {
        return this.serial == null?"":this.serial.toString();
    }

    public void encode(OutputStream var1) throws IOException {
        DerOutputStream var2 = new DerOutputStream();
        this.serial.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws IOException {
        if(!(var2 instanceof TSerialNumber)) {
            throw new IOException("Attribute must be of type TSerialNumber.");
        } else if(var1.equalsIgnoreCase("number")) {
            this.serial = (TSerialNumber)var2;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:TCertificateSerialNumber.");
        }
    }

    public TSerialNumber get(String var1) throws IOException {
        if(var1.equalsIgnoreCase("number")) {
            return this.serial;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:TCertificateSerialNumber.");
        }
    }

    public void delete(String var1) throws IOException {
        if(var1.equalsIgnoreCase("number")) {
            this.serial = null;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:TCertificateSerialNumber.");
        }
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration var1 = new AttributeNameEnumeration();
        var1.addElement("number");
        return var1.elements();
    }

    public String getName() {
        return "serialNumber";
    }
}
