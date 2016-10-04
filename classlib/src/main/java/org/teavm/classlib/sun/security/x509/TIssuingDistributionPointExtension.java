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
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TIssuingDistributionPointExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.IssuingDistributionPoint";
    public static final String NAME = "IssuingDistributionPoint";
    public static final String POINT = "point";
    public static final String REASONS = "reasons";
    public static final String ONLY_USER_CERTS = "only_user_certs";
    public static final String ONLY_CA_CERTS = "only_ca_certs";
    public static final String ONLY_ATTRIBUTE_CERTS = "only_attribute_certs";
    public static final String INDIRECT_CRL = "indirect_crl";
    private TDistributionPointName distributionPoint = null;
    private TReasonFlags revocationReasons = null;
    private boolean hasOnlyUserCerts = false;
    private boolean hasOnlyCACerts = false;
    private boolean hasOnlyAttributeCerts = false;
    private boolean isIndirectCRL = false;
    private static final byte TAG_DISTRIBUTION_POINT = 0;
    private static final byte TAG_ONLY_USER_CERTS = 1;
    private static final byte TAG_ONLY_CA_CERTS = 2;
    private static final byte TAG_ONLY_SOME_REASONS = 3;
    private static final byte TAG_INDIRECT_CRL = 4;
    private static final byte TAG_ONLY_ATTRIBUTE_CERTS = 5;

    public TIssuingDistributionPointExtension(TDistributionPointName var1, TReasonFlags var2, boolean var3, boolean var4, boolean var5, boolean var6) throws
            IOException {
        if((!var3 || !var4 && !var5) && (!var4 || !var3 && !var5) && (!var5 || !var3 && !var4)) {
            this.extensionId = TPKIXExtensions.IssuingDistributionPoint_Id;
            this.critical = true;
            this.distributionPoint = var1;
            this.revocationReasons = var2;
            this.hasOnlyUserCerts = var3;
            this.hasOnlyCACerts = var4;
            this.hasOnlyAttributeCerts = var5;
            this.isIndirectCRL = var6;
            this.encodeThis();
        } else {
            throw new IllegalArgumentException("Only one of hasOnlyUserCerts, hasOnlyCACerts, hasOnlyAttributeCerts may be set to true");
        }
    }

    public TIssuingDistributionPointExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = TPKIXExtensions.IssuingDistributionPoint_Id;
        this.critical = var1.booleanValue();
        if(!(var2 instanceof byte[])) {
            throw new IOException("Illegal argument type");
        } else {
            this.extensionValue = (byte[])((byte[])var2);
            TDerValue var3 = new TDerValue(this.extensionValue);
            if(var3.tag != 48) {
                throw new IOException("Invalid encoding for IssuingDistributionPointExtension.");
            } else if(var3.data != null && var3.data.available() != 0) {
                TDerInputStream var4 = var3.data;

                while(true) {
                    if(var4 != null && var4.available() != 0) {
                        TDerValue var5 = var4.getDerValue();
                        if(var5.isContextSpecific((byte)0) && var5.isConstructed()) {
                            this.distributionPoint = new TDistributionPointName(var5.data.getDerValue());
                            continue;
                        }

                        if(var5.isContextSpecific((byte)1) && !var5.isConstructed()) {
                            var5.resetTag((byte)1);
                            this.hasOnlyUserCerts = var5.getBoolean();
                            continue;
                        }

                        if(var5.isContextSpecific((byte)2) && !var5.isConstructed()) {
                            var5.resetTag((byte)1);
                            this.hasOnlyCACerts = var5.getBoolean();
                            continue;
                        }

                        if(var5.isContextSpecific((byte)3) && !var5.isConstructed()) {
                            this.revocationReasons = new TReasonFlags(var5);
                            continue;
                        }

                        if(var5.isContextSpecific((byte)4) && !var5.isConstructed()) {
                            var5.resetTag((byte)1);
                            this.isIndirectCRL = var5.getBoolean();
                            continue;
                        }

                        if(var5.isContextSpecific((byte)5) && !var5.isConstructed()) {
                            var5.resetTag((byte)1);
                            this.hasOnlyAttributeCerts = var5.getBoolean();
                            continue;
                        }

                        throw new IOException("Invalid encoding of IssuingDistributionPoint");
                    }

                    return;
                }
            }
        }
    }

    public String getName() {
        return "IssuingDistributionPoint";
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.IssuingDistributionPoint_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("point"))) {
            if(!(var2 instanceof DistributionPointName)) {
                throw new TIOException(TString.wrap("Attribute value should be of type DistributionPointName."));
            }

            this.distributionPoint = (TDistributionPointName)var2;
        } else if(var1.equalsIgnoreCase(TString.wrap("reasons"))) {
            if(!(var2 instanceof ReasonFlags)) {
                throw new TIOException(TString.wrap("Attribute value should be of type ReasonFlags."));
            }

            this.revocationReasons = (TReasonFlags)var2;
        } else if(var1.equalsIgnoreCase(TString.wrap("indirect_crl"))) {
            if(!(var2 instanceof Boolean)) {
                throw new TIOException(TString.wrap("Attribute value should be of type Boolean."));
            }

            this.isIndirectCRL = ((Boolean)var2).booleanValue();
        } else if(var1.equalsIgnoreCase(TString.wrap("only_user_certs"))) {
            if(!(var2 instanceof Boolean)) {
                throw new TIOException(TString.wrap("Attribute value should be of type Boolean."));
            }

            this.hasOnlyUserCerts = ((Boolean)var2).booleanValue();
        } else if(var1.equalsIgnoreCase(TString.wrap("only_ca_certs"))) {
            if(!(var2 instanceof Boolean)) {
                throw new TIOException(TString.wrap("Attribute value should be of type Boolean."));
            }

            this.hasOnlyCACerts = ((Boolean)var2).booleanValue();
        } else {
            if(!var1.equalsIgnoreCase(TString.wrap("only_attribute_certs"))) {
                throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:IssuingDistributionPointExtension."));
            }

            if(!(var2 instanceof Boolean)) {
                throw new TIOException(TString.wrap("Attribute value should be of type Boolean."));
            }

            this.hasOnlyAttributeCerts = ((Boolean)var2).booleanValue();
        }

        this.encodeThis();
    }

    public Object get(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("point"))) {
            return this.distributionPoint;
        } else if(var1.equalsIgnoreCase(TString.wrap("indirect_crl"))) {
            return Boolean.valueOf(this.isIndirectCRL);
        } else if(var1.equalsIgnoreCase(TString.wrap("reasons"))) {
            return this.revocationReasons;
        } else if(var1.equalsIgnoreCase(TString.wrap("only_user_certs"))) {
            return Boolean.valueOf(this.hasOnlyUserCerts);
        } else if(var1.equalsIgnoreCase(TString.wrap("only_ca_certs"))) {
            return Boolean.valueOf(this.hasOnlyCACerts);
        } else if(var1.equalsIgnoreCase(TString.wrap("only_attribute_certs"))) {
            return Boolean.valueOf(this.hasOnlyAttributeCerts);
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:IssuingDistributionPointExtension."));
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("point"))) {
            this.distributionPoint = null;
        } else if(var1.equalsIgnoreCase(TString.wrap("indirect_crl"))) {
            this.isIndirectCRL = false;
        } else if(var1.equalsIgnoreCase(TString.wrap("reasons"))) {
            this.revocationReasons = null;
        } else if(var1.equalsIgnoreCase(TString.wrap("only_user_certs"))) {
            this.hasOnlyUserCerts = false;
        } else if(var1.equalsIgnoreCase(TString.wrap("only_ca_certs"))) {
            this.hasOnlyCACerts = false;
        } else {
            if(!var1.equalsIgnoreCase(TString.wrap("only_attribute_certs"))) {
                throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:IssuingDistributionPointExtension."));
            }

            this.hasOnlyAttributeCerts = false;
        }

        this.encodeThis();
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("point");
        var1.addElement("reasons");
        var1.addElement("only_user_certs");
        var1.addElement("only_ca_certs");
        var1.addElement("only_attribute_certs");
        var1.addElement("indirect_crl");
        return var1.elements();
    }

    private void encodeThis() throws TIOException {
        if(this.distributionPoint == null && this.revocationReasons == null && !this.hasOnlyUserCerts && !this.hasOnlyCACerts && !this.hasOnlyAttributeCerts && !this.isIndirectCRL) {
            this.extensionValue = null;
        } else {
            TDerOutputStream var1 = new TDerOutputStream();
            TDerOutputStream var2;
            if(this.distributionPoint != null) {
                var2 = new TDerOutputStream();
                this.distributionPoint.encode(var2);
                var1.writeImplicit(TDerValue.createTag((byte)-128, true, (byte)0), var2);
            }

            if(this.hasOnlyUserCerts) {
                var2 = new TDerOutputStream();
                var2.putBoolean(this.hasOnlyUserCerts);
                var1.writeImplicit(TDerValue.createTag((byte)-128, false, (byte)1), var2);
            }

            if(this.hasOnlyCACerts) {
                var2 = new TDerOutputStream();
                var2.putBoolean(this.hasOnlyCACerts);
                var1.writeImplicit(TDerValue.createTag((byte)-128, false, (byte)2), var2);
            }

            if(this.revocationReasons != null) {
                var2 = new TDerOutputStream();
                this.revocationReasons.encode(var2);
                var1.writeImplicit(TDerValue.createTag((byte)-128, false, (byte)3), var2);
            }

            if(this.isIndirectCRL) {
                var2 = new TDerOutputStream();
                var2.putBoolean(this.isIndirectCRL);
                var1.writeImplicit(TDerValue.createTag((byte)-128, false, (byte)4), var2);
            }

            if(this.hasOnlyAttributeCerts) {
                var2 = new TDerOutputStream();
                var2.putBoolean(this.hasOnlyAttributeCerts);
                var1.writeImplicit(TDerValue.createTag((byte)-128, false, (byte)5), var2);
            }

            var2 = new TDerOutputStream();
            var2.write((byte)48, var1);
            this.extensionValue = var2.toByteArray();
        }
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder(super.toString());
        var1.append("IssuingDistributionPoint [\n  ");
        if(this.distributionPoint != null) {
            var1.append(this.distributionPoint);
        }

        if(this.revocationReasons != null) {
            var1.append(this.revocationReasons);
        }

        var1.append(this.hasOnlyUserCerts?"  Only contains user certs: true":"  Only contains user certs: false").append("\n");
        var1.append(this.hasOnlyCACerts?"  Only contains CA certs: true":"  Only contains CA certs: false").append("\n");
        var1.append(this.hasOnlyAttributeCerts?"  Only contains attribute certs: true":"  Only contains attribute certs: false").append("\n");
        var1.append(this.isIndirectCRL?"  Indirect CRL: true":"  Indirect CRL: false").append("\n");
        var1.append("]\n");
        return var1.toString();
    }
}
