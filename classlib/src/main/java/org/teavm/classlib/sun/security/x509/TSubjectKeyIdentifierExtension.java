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

public class TSubjectKeyIdentifierExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.SubjectKeyIdentifier";
    public static final String NAME = "SubjectKeyIdentifier";
    public static final String KEY_ID = "key_id";
    private TKeyIdentifier id = null;

    private void encodeThis() throws TIOException {
        if(this.id == null) {
            this.extensionValue = null;
        } else {
            TDerOutputStream var1 = new TDerOutputStream();
            this.id.encode(var1);
            this.extensionValue = var1.toByteArray();
        }
    }

    public TSubjectKeyIdentifierExtension(byte[] var1) throws IOException {
        this.id = new TKeyIdentifier(var1);
        this.extensionId = TPKIXExtensions.SubjectKey_Id;
        this.critical = false;
        this.encodeThis();
    }

    public TSubjectKeyIdentifierExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = TPKIXExtensions.SubjectKey_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        TDerValue var3 = new TDerValue(this.extensionValue);
        this.id = new TKeyIdentifier(var3);
    }

    public String toString() {
        return super.toString() + "SubjectKeyIdentifier [\n" + this.id + "]\n";
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.SubjectKey_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("key_id"))) {
            if(!(var2 instanceof TKeyIdentifier)) {
                throw new TIOException(TString.wrap("Attribute value should be of type KeyIdentifier."));
            } else {
                this.id = (TKeyIdentifier)var2;
                this.encodeThis();
            }
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:SubjectKeyIdentifierExtension."));
        }
    }

    public TKeyIdentifier get(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("key_id"))) {
            return this.id;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:SubjectKeyIdentifierExtension."));
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("key_id"))) {
            this.id = null;
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:SubjectKeyIdentifierExtension."));
        }
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("key_id");
        return var1.elements();
    }

    public String getName() {
        return "SubjectKeyIdentifier";
    }
}
