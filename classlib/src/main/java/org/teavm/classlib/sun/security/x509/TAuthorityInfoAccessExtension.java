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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TAuthorityInfoAccessExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.AuthorityInfoAccess";
    public static final String NAME = "AuthorityInfoAccess";
    public static final String DESCRIPTIONS = "descriptions";
    private List<TAccessDescription> accessDescriptions;

    public TAuthorityInfoAccessExtension(List<TAccessDescription> var1) throws IOException {
        this.extensionId = TPKIXExtensions.AuthInfoAccess_Id;
        this.critical = false;
        this.accessDescriptions = var1;
        this.encodeThis();
    }

    public TAuthorityInfoAccessExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = TPKIXExtensions.AuthInfoAccess_Id;
        this.critical = var1.booleanValue();
        if(!(var2 instanceof byte[])) {
            throw new IOException("Illegal argument type");
        } else {
            this.extensionValue = (byte[])((byte[])var2);
            TDerValue var3 = new TDerValue(this.extensionValue);
            if(var3.tag != 48) {
                throw new IOException("Invalid encoding for AuthorityInfoAccessExtension.");
            } else {
                this.accessDescriptions = new ArrayList();

                while(var3.data.available() != 0) {
                    TDerValue var4 = var3.data.getDerValue();
                    TAccessDescription var5 = new TAccessDescription(var4);
                    this.accessDescriptions.add(var5);
                }

            }
        }
    }

    public List<TAccessDescription> getAccessDescriptions() {
        return this.accessDescriptions;
    }

    public String getName() {
        return "AuthorityInfoAccess";
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.AuthInfoAccess_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase("descriptions")) {
            if(!(var2 instanceof List)) {
                throw new TIOException(TString.wrap("Attribute value should be of type List."));
            } else {
                this.accessDescriptions = (List)var2;
                this.encodeThis();
            }
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:AuthorityInfoAccessExtension."));
        }
    }

    public List<TAccessDescription> get(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("descriptions")) {
            return this.accessDescriptions;
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:AuthorityInfoAccessExtension."));
        }
    }

    public void delete(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("descriptions")) {
            this.accessDescriptions = new ArrayList();
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:AuthorityInfoAccessExtension."));
        }
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("descriptions");
        return var1.elements();
    }

    private void encodeThis() throws TIOException {
        if(this.accessDescriptions.isEmpty()) {
            this.extensionValue = null;
        } else {
            TDerOutputStream var1 = new TDerOutputStream();
            Iterator var2 = this.accessDescriptions.iterator();

            while(var2.hasNext()) {
                TAccessDescription var3 = (TAccessDescription)var2.next();
                var3.encode(var1);
            }

            TDerOutputStream var4 = new TDerOutputStream();
            var4.write((byte) 48, var1);
            this.extensionValue = var4.toByteArray();
        }

    }

    public String toString() {
        return super.toString() + "AuthorityInfoAccess [\n  " + this.accessDescriptions + "\n]\n";
    }
}
