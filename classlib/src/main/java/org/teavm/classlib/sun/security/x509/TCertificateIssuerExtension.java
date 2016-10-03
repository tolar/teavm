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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TCertificateIssuerExtension extends TExtension implements TCertAttrSet<String> {
    public static final String NAME = "CertificateIssuer";
    public static final String ISSUER = "issuer";
    private TGeneralNames names;

    private void encodeThis() throws TIOException {
        if(this.names != null && !this.names.isEmpty()) {
            TDerOutputStream var1 = new TDerOutputStream();
            this.names.encode(var1);
            this.extensionValue = var1.toByteArray();
        } else {
            this.extensionValue = null;
        }
    }

    public TCertificateIssuerExtension(TGeneralNames var1) throws TIOException {
        this.extensionId = TPKIXExtensions.CertificateIssuer_Id;
        this.critical = true;
        this.names = var1;
        this.encodeThis();
    }

    public TCertificateIssuerExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = TPKIXExtensions.CertificateIssuer_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        TDerValue var3 = new TDerValue(this.extensionValue);
        this.names = new TGeneralNames(var3);
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("issuer"))) {
            if(!(var2 instanceof TGeneralNames)) {
                throw new TIOException(TString.wrap("Attribute value must be of type GeneralNames"));
            } else {
                this.names = (TGeneralNames)var2;
                this.encodeThis();
            }
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:CertificateIssuer"));
        }
    }

    public TGeneralNames get(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("issuer"))) {
            return this.names;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:CertificateIssuer"));
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("issuer"))) {
            this.names = null;
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:CertificateIssuer"));
        }
    }

    public String toString() {
        return super.toString() + "Certificate Issuer [\n" + this.names + "]\n";
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.CertificateIssuer_Id;
            this.critical = true;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("issuer");
        return var1.elements();
    }

    public String getName() {
        return "CertificateIssuer";
    }
}
