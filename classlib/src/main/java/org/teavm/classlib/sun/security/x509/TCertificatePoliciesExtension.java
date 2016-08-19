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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerOutputStream;

public class TCertificatePoliciesExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.CertificatePolicies";
    public static final String NAME = "CertificatePolicies";
    public static final String POLICIES = "policies";
    private List<TPolicyInformation> certPolicies;

    private void encodeThis() throws TIOException {
        if(this.certPolicies != null && !this.certPolicies.isEmpty()) {
            TDerOutputStream var1 = new TDerOutputStream();
            TDerOutputStream var2 = new TDerOutputStream();
            Iterator var3 = this.certPolicies.iterator();

            while(var3.hasNext()) {
                TPolicyInformation var4 = (TPolicyInformation)var3.next();
                var4.encode(var2);
            }

            var1.write(48, var2);
            this.extensionValue = var1.toByteArray();
        } else {
            this.extensionValue = null;
        }

    }

    public TCertificatePoliciesExtension(List<TPolicyInformation> var1) throws IOException {
        this(Boolean.FALSE, var1);
    }

    public TCertificatePoliciesExtension(Boolean var1, List<TPolicyInformation> var2) throws IOException {
        this.certPolicies = var2;
        this.extensionId = PKIXExtensions.CertificatePolicies_Id;
        this.critical = var1.booleanValue();
        this.encodeThis();
    }

    public TCertificatePoliciesExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = PKIXExtensions.CertificatePolicies_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        DerValue var3 = new DerValue(this.extensionValue);
        if(var3.tag != 48) {
            throw new IOException("Invalid encoding for CertificatePoliciesExtension.");
        } else {
            this.certPolicies = new ArrayList();

            while(var3.data.available() != 0) {
                DerValue var4 = var3.data.getDerValue();
                TPolicyInformation var5 = new TPolicyInformation(var4);
                this.certPolicies.add(var5);
            }

        }
    }

    public String toString() {
        if(this.certPolicies == null) {
            return "";
        } else {
            StringBuilder var1 = new StringBuilder(super.toString());
            var1.append("CertificatePolicies [\n");
            Iterator var2 = this.certPolicies.iterator();

            while(var2.hasNext()) {
                TPolicyInformation var3 = (TPolicyInformation)var2.next();
                var1.append(var3.toString());
            }

            var1.append("]\n");
            return var1.toString();
        }
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.CertificatePolicies_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase("policies")) {
            if(!(var2 instanceof List)) {
                throw new TIOException(TString.wrap("Attribute value should be of type List."));
            } else {
                this.certPolicies = (List)var2;
                this.encodeThis();
            }
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:CertificatePoliciesExtension."));
        }
    }

    public List<TPolicyInformation> get(String var1) throws IOException {
        if(var1.equalsIgnoreCase("policies")) {
            return this.certPolicies;
        } else {
            throw new IOException("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:CertificatePoliciesExtension.");
        }
    }

    public void delete(String var1) throws IOException {
        if(var1.equalsIgnoreCase("policies")) {
            this.certPolicies = null;
            this.encodeThis();
        } else {
            throw new IOException("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:CertificatePoliciesExtension.");
        }
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration var1 = new AttributeNameEnumeration();
        var1.addElement("policies");
        return var1.elements();
    }

    public String getName() {
        return "CertificatePolicies";
    }
}
