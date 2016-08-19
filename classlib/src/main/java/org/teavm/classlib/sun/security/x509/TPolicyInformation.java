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
import java.security.cert.PolicyQualifierInfo;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TPolicyInformation {
    public static final String NAME = "PolicyInformation";
    public static final String ID = "id";
    public static final String QUALIFIERS = "qualifiers";
    private TCertificatePolicyId policyIdentifier;
    private Set<PolicyQualifierInfo> policyQualifiers;

    public TPolicyInformation(TCertificatePolicyId var1, Set<PolicyQualifierInfo> var2) throws IOException {
        if(var2 == null) {
            throw new NullPointerException("policyQualifiers is null");
        } else {
            this.policyQualifiers = new LinkedHashSet(var2);
            this.policyIdentifier = var1;
        }
    }

    public TPolicyInformation(TDerValue var1) throws IOException {
        if(var1.tag != 48) {
            throw new IOException("Invalid encoding of PolicyInformation");
        } else {
            this.policyIdentifier = new TCertificatePolicyId(var1.data.getDerValue());
            if(var1.data.available() != 0) {
                this.policyQualifiers = new LinkedHashSet();
                TDerValue var2 = var1.data.getDerValue();
                if(var2.tag != 48) {
                    throw new IOException("Invalid encoding of PolicyInformation");
                }

                if(var2.data.available() == 0) {
                    throw new IOException("No data available in policyQualifiers");
                }

                while(var2.data.available() != 0) {
                    this.policyQualifiers.add(new PolicyQualifierInfo(var2.data.getDerValue().toByteArray()));
                }
            } else {
                this.policyQualifiers = Collections.emptySet();
            }

        }
    }

    public boolean equals(Object var1) {
        if(!(var1 instanceof sun.security.x509.PolicyInformation)) {
            return false;
        } else {
            sun.security.x509.PolicyInformation var2 = (sun.security.x509.PolicyInformation)var1;
            return !this.policyIdentifier.equals(var2.getPolicyIdentifier())?false:this.policyQualifiers.equals(var2.getPolicyQualifiers());
        }
    }

    public int hashCode() {
        int var1 = 37 + this.policyIdentifier.hashCode();
        var1 = 37 * var1 + this.policyQualifiers.hashCode();
        return var1;
    }

    public TCertificatePolicyId getPolicyIdentifier() {
        return this.policyIdentifier;
    }

    public Set<PolicyQualifierInfo> getPolicyQualifiers() {
        return this.policyQualifiers;
    }

    public Object get(String var1) throws IOException {
        if(var1.equalsIgnoreCase("id")) {
            return this.policyIdentifier;
        } else if(var1.equalsIgnoreCase("qualifiers")) {
            return this.policyQualifiers;
        } else {
            throw new IOException("Attribute name [" + var1 + "] not recognized by PolicyInformation.");
        }
    }

    public void set(String var1, Object var2) throws IOException {
        if(var1.equalsIgnoreCase("id")) {
            if(!(var2 instanceof TCertificatePolicyId)) {
                throw new IOException("Attribute value must be instance of CertificatePolicyId.");
            }

            this.policyIdentifier = (TCertificatePolicyId)var2;
        } else {
            if(!var1.equalsIgnoreCase("qualifiers")) {
                throw new IOException("Attribute name [" + var1 + "] not recognized by PolicyInformation");
            }

            if(this.policyIdentifier == null) {
                throw new IOException("Attribute must have a CertificatePolicyIdentifier value before PolicyQualifierInfo can be set.");
            }

            if(!(var2 instanceof Set)) {
                throw new IOException("Attribute value must be of type Set.");
            }

            Iterator var3 = ((Set)var2).iterator();

            while(var3.hasNext()) {
                Object var4 = var3.next();
                if(!(var4 instanceof PolicyQualifierInfo)) {
                    throw new IOException("Attribute value must be aSet of PolicyQualifierInfo objects.");
                }
            }

            this.policyQualifiers = (Set)var2;
        }

    }

    public void delete(String var1) throws IOException {
        if(var1.equalsIgnoreCase("qualifiers")) {
            this.policyQualifiers = Collections.emptySet();
        } else if(var1.equalsIgnoreCase("id")) {
            throw new IOException("Attribute ID may not be deleted from PolicyInformation.");
        } else {
            throw new IOException("Attribute name [" + var1 + "] not recognized by PolicyInformation.");
        }
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("id");
        var1.addElement("qualifiers");
        return var1.elements();
    }

    public String getName() {
        return "PolicyInformation";
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder("  [" + this.policyIdentifier.toString());
        var1.append(this.policyQualifiers + "  ]\n");
        return var1.toString();
    }

    public void encode(TDerOutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        this.policyIdentifier.encode(var2);
        if(!this.policyQualifiers.isEmpty()) {
            TDerOutputStream var3 = new TDerOutputStream();
            Iterator var4 = this.policyQualifiers.iterator();

            while(var4.hasNext()) {
                PolicyQualifierInfo var5 = (PolicyQualifierInfo)var4.next();
                var3.write(var5.getEncoded());
            }

            var2.write((byte) 48, var3);
        }

        var1.write((byte) 48, var2);
    }
}

