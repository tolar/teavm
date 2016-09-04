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
import java.security.PublicKey;
import java.util.Enumeration;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TCertificateX509Key implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.key";
    public static final String NAME = "key";
    public static final String KEY = "value";
    private PublicKey key;

    public TCertificateX509Key(PublicKey var1) {
        this.key = var1;
    }

    public TCertificateX509Key(TDerInputStream var1) throws IOException {
        TDerValue var2 = var1.getDerValue();
        this.key = TX509Key.parse(var2);
    }

    public TCertificateX509Key(TInputStream var1) throws IOException {
        TDerValue var2 = new TDerValue(var1);
        this.key = TX509Key.parse(var2);
    }

    public String toString() {
        return this.key == null?"":this.key.toString();
    }

    public void encode(OutputStream var1) throws IOException {
        DerOutputStream var2 = new DerOutputStream();
        var2.write(this.key.getEncoded());
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws IOException {
        if(var1.equalsIgnoreCase("value")) {
            this.key = (PublicKey)var2;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet: CertificateX509Key.");
        }
    }

    public PublicKey get(String var1) throws IOException {
        if(var1.equalsIgnoreCase("value")) {
            return this.key;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet: CertificateX509Key.");
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("value"))) {
            this.key = null;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet: CertificateX509Key."));
        }
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration var1 = new AttributeNameEnumeration();
        var1.addElement("value");
        return var1.elements();
    }

    public String getName() {
        return "key";
    }
}
