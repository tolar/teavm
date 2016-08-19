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
import java.util.Enumeration;

import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TCertificateVersion implements TCertAttrSet<String> {
    public static final int V1 = 0;
    public static final int V2 = 1;
    public static final int V3 = 2;
    public static final String IDENT = "x509.info.version";
    public static final String NAME = "version";
    public static final String VERSION = "number";
    int version = 0;

    private int getVersion() {
        return this.version;
    }

    private void construct(TDerValue var1) throws IOException {
        if(var1.isConstructed() && var1.isContextSpecific()) {
            var1 = var1.data.getDerValue();
            this.version = var1.getInteger();
            if(var1.data.available() != 0) {
                throw new IOException("X.509 version, bad format");
            }
        }

    }

    public TCertificateVersion() {
        this.version = 0;
    }

    public TCertificateVersion(int var1) throws IOException {
        if(var1 != 0 && var1 != 1 && var1 != 2) {
            throw new IOException("X.509 Certificate version " + var1 + " not supported.\n");
        } else {
            this.version = var1;
        }
    }

    public TCertificateVersion(TDerInputStream var1) throws IOException {
        this.version = 0;
        TDerValue var2 = var1.getDerValue();
        this.construct(var2);
    }

    public TCertificateVersion(InputStream var1) throws IOException {
        this.version = 0;
        TDerValue var2 = new TDerValue(var1);
        this.construct(var2);
    }

    public TCertificateVersion(TDerValue var1) throws IOException {
        this.version = 0;
        this.construct(var1);
    }

    public String toString() {
        return "Version: V" + (this.version + 1);
    }

    public void encode(TOutputStream var1) throws IOException {
        if(this.version != 0) {
            TDerOutputStream var2 = new TDerOutputStream();
            var2.putInteger(this.version);
            TDerOutputStream var3 = new TDerOutputStream();
            var3.write(TDerValue.createTag((byte)-128, true, (byte)0), var2);
            var1.write(var3.toByteArray());
        }
    }

    public void set(String var1, Object var2) throws IOException {
        if(!(var2 instanceof Integer)) {
            throw new IOException("Attribute must be of type Integer.");
        } else if(var1.equalsIgnoreCase("number")) {
            this.version = ((Integer)var2).intValue();
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet: TCertificateVersion.");
        }
    }

    public Integer get(String var1) throws IOException {
        if(var1.equalsIgnoreCase("number")) {
            return new Integer(this.getVersion());
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet: TCertificateVersion.");
        }
    }

    public void delete(String var1) throws IOException {
        if(var1.equalsIgnoreCase("number")) {
            this.version = 0;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet: TCertificateVersion.");
        }
    }

    public Enumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("number");
        return var1.elements();
    }

    public String getName() {
        return "version";
    }

    public int compare(int var1) {
        return this.version - var1;
    }
}
