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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TPolicyMappingsExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.PolicyMappings";
    public static final String NAME = "PolicyMappings";
    public static final String MAP = "map";
    private List<TCertificatePolicyMap> maps;

    private void encodeThis() throws TIOException {
        if(this.maps != null && !this.maps.isEmpty()) {
            TDerOutputStream var1 = new TDerOutputStream();
            TDerOutputStream var2 = new TDerOutputStream();
            Iterator var3 = this.maps.iterator();

            while(var3.hasNext()) {
                TCertificatePolicyMap var4 = (TCertificatePolicyMap)var3.next();
                var4.encode(var2);
            }

            var1.write((byte) 48, var2);
            this.extensionValue = var1.toByteArray();
        } else {
            this.extensionValue = null;
        }
    }

    public TPolicyMappingsExtension(List<TCertificatePolicyMap> var1) throws IOException {
        this.maps = var1;
        this.extensionId = TPKIXExtensions.PolicyMappings_Id;
        this.critical = false;
        this.encodeThis();
    }

    public TPolicyMappingsExtension() {
        this.extensionId = TPKIXExtensions.KeyUsage_Id;
        this.critical = false;
        this.maps = Collections.emptyList();
    }

    public TPolicyMappingsExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = TPKIXExtensions.PolicyMappings_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        TDerValue var3 = new TDerValue(this.extensionValue);
        if(var3.tag != 48) {
            throw new IOException("Invalid encoding for PolicyMappingsExtension.");
        } else {
            this.maps = new ArrayList();

            while(var3.data.available() != 0) {
                TDerValue var4 = var3.data.getDerValue();
                TCertificatePolicyMap var5 = new TCertificatePolicyMap(var4);
                this.maps.add(var5);
            }

        }
    }

    public String toString() {
        if(this.maps == null) {
            return "";
        } else {
            String var1 = super.toString() + "PolicyMappings [\n" + this.maps.toString() + "]\n";
            return var1;
        }
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.PolicyMappings_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase("map")) {
            if(!(var2 instanceof List)) {
                throw new TIOException(TString.wrap("Attribute value should be of type List."));
            } else {
                this.maps = (List)var2;
                this.encodeThis();
            }
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension."));
        }
    }

    public List<TCertificatePolicyMap> get(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("map")) {
            return this.maps;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension."));
        }
    }

    public void delete(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("map")) {
            this.maps = null;
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension."));
        }
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("map");
        return var1.elements();
    }

    public String getName() {
        return "PolicyMappings";
    }
}
