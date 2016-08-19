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
import java.util.Iterator;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TIssuerAlternativeNameExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.IssuerAlternativeName";
    public static final String NAME = "IssuerAlternativeName";
    public static final String ISSUER_NAME = "issuer_name";
    TGeneralNames names = null;

    private void encodeThis() throws TIOException {
        if(this.names != null && !this.names.isEmpty()) {
            TDerOutputStream var1 = new TDerOutputStream();
            this.names.encode(var1);
            this.extensionValue = var1.toByteArray();
        } else {
            this.extensionValue = null;
        }
    }

    public TIssuerAlternativeNameExtension(TGeneralNames var1) throws IOException {
        this.names = var1;
        this.extensionId = TPKIXExtensions.IssuerAlternativeName_Id;
        this.critical = false;
        this.encodeThis();
    }

    public TIssuerAlternativeNameExtension(Boolean var1, TGeneralNames var2) throws IOException {
        this.names = var2;
        this.extensionId = TPKIXExtensions.IssuerAlternativeName_Id;
        this.critical = var1.booleanValue();
        this.encodeThis();
    }

    public TIssuerAlternativeNameExtension() {
        this.extensionId = TPKIXExtensions.IssuerAlternativeName_Id;
        this.critical = false;
        this.names = new TGeneralNames();
    }

    public TIssuerAlternativeNameExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = TPKIXExtensions.IssuerAlternativeName_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        TDerValue var3 = new TDerValue(this.extensionValue);
        if(var3.data == null) {
            this.names = new TGeneralNames();
        } else {
            this.names = new TGeneralNames(var3);
        }
    }

    public String toString() {
        String var1 = super.toString() + "IssuerAlternativeName [\n";
        TGeneralName var3;
        if(this.names == null) {
            var1 = var1 + "  null\n";
        } else {
            for(Iterator var2 = this.names.names().iterator(); var2.hasNext(); var1 = var1 + "  " + var3 + "\n") {
                var3 = (TGeneralName)var2.next();
            }
        }

        var1 = var1 + "]\n";
        return var1;
    }

    public void encode(TOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.IssuerAlternativeName_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase("issuer_name")) {
            if(!(var2 instanceof TGeneralNames)) {
                throw new TIOException(TString.wrap("Attribute value should be of type GeneralNames."));
            } else {
                this.names = (TGeneralNames)var2;
                this.encodeThis();
            }
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:IssuerAlternativeName."));
        }
    }

    public TGeneralNames get(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("issuer_name")) {
            return this.names;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:IssuerAlternativeName."));
        }
    }

    public void delete(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("issuer_name")) {
            this.names = null;
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:IssuerAlternativeName."));
        }
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("issuer_name");
        return var1.elements();
    }

    public String getName() {
        return "IssuerAlternativeName";
    }
}
