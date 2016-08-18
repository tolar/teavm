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
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AttributeNameEnumeration;
import sun.security.x509.CertAttrSet;
import sun.security.x509.Extension;
import sun.security.x509.PKIXExtensions;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TPolicyConstraintsExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.PolicyConstraints";
    public static final String NAME = "PolicyConstraints";
    public static final String REQUIRE = "require";
    public static final String INHIBIT = "inhibit";
    private static final byte TAG_REQUIRE = 0;
    private static final byte TAG_INHIBIT = 1;
    private int require;
    private int inhibit;

    private void encodeThis() throws IOException {
        if(this.require == -1 && this.inhibit == -1) {
            this.extensionValue = null;
        } else {
            DerOutputStream var1 = new DerOutputStream();
            DerOutputStream var2 = new DerOutputStream();
            DerOutputStream var3;
            if(this.require != -1) {
                var3 = new DerOutputStream();
                var3.putInteger(this.require);
                var1.writeImplicit(DerValue.createTag(-128, false, 0), var3);
            }

            if(this.inhibit != -1) {
                var3 = new DerOutputStream();
                var3.putInteger(this.inhibit);
                var1.writeImplicit(DerValue.createTag(-128, false, 1), var3);
            }

            var2.write(48, var1);
            this.extensionValue = var2.toByteArray();
        }
    }

    public TPolicyConstraintsExtension(int var1, int var2) throws IOException {
        this(Boolean.FALSE, var1, var2);
    }

    public TPolicyConstraintsExtension(Boolean var1, int var2, int var3) throws IOException {
        this.require = -1;
        this.inhibit = -1;
        this.require = var2;
        this.inhibit = var3;
        this.extensionId = PKIXExtensions.PolicyConstraints_Id;
        this.critical = var1.booleanValue();
        this.encodeThis();
    }

    public TPolicyConstraintsExtension(Boolean var1, Object var2) throws IOException {
        this.require = -1;
        this.inhibit = -1;
        this.extensionId = PKIXExtensions.PolicyConstraints_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        DerValue var3 = new DerValue(this.extensionValue);
        if(var3.tag != 48) {
            throw new IOException("Sequence tag missing for PolicyConstraint.");
        } else {
            DerInputStream var4 = var3.data;

            while(true) {
                if(var4 != null && var4.available() != 0) {
                    DerValue var5 = var4.getDerValue();
                    if(var5.isContextSpecific(0) && !var5.isConstructed()) {
                        if(this.require != -1) {
                            throw new IOException("Duplicate requireExplicitPolicyfound in the TPolicyConstraintsExtension");
                        }

                        var5.resetTag(2);
                        this.require = var5.getInteger();
                        continue;
                    }

                    if(var5.isContextSpecific(1) && !var5.isConstructed()) {
                        if(this.inhibit != -1) {
                            throw new IOException("Duplicate inhibitPolicyMappingfound in the TPolicyConstraintsExtension");
                        }

                        var5.resetTag(2);
                        this.inhibit = var5.getInteger();
                        continue;
                    }

                    throw new IOException("Invalid encoding of PolicyConstraint");
                }

                return;
            }
        }
    }

    public String toString() {
        String var1 = super.toString() + "PolicyConstraints: [" + "  Require: ";
        if(this.require == -1) {
            var1 = var1 + "unspecified;";
        } else {
            var1 = var1 + this.require + ";";
        }

        var1 = var1 + "\tInhibit: ";
        if(this.inhibit == -1) {
            var1 = var1 + "unspecified";
        } else {
            var1 = var1 + this.inhibit;
        }

        var1 = var1 + " ]\n";
        return var1;
    }

    public void encode(OutputStream var1) throws IOException {
        DerOutputStream var2 = new DerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = PKIXExtensions.PolicyConstraints_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws IOException {
        if(!(var2 instanceof Integer)) {
            throw new IOException("Attribute value should be of type Integer.");
        } else {
            if(var1.equalsIgnoreCase("require")) {
                this.require = ((Integer)var2).intValue();
            } else {
                if(!var1.equalsIgnoreCase("inhibit")) {
                    throw new IOException("Attribute name [" + var1 + "]" + " not recognized by " + "CertAttrSet:PolicyConstraints.");
                }

                this.inhibit = ((Integer)var2).intValue();
            }

            this.encodeThis();
        }
    }

    public Integer get(String var1) throws IOException {
        if(var1.equalsIgnoreCase("require")) {
            return new Integer(this.require);
        } else if(var1.equalsIgnoreCase("inhibit")) {
            return new Integer(this.inhibit);
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:PolicyConstraints.");
        }
    }

    public void delete(String var1) throws IOException {
        if(var1.equalsIgnoreCase("require")) {
            this.require = -1;
        } else {
            if(!var1.equalsIgnoreCase("inhibit")) {
                throw new IOException("Attribute name not recognized by CertAttrSet:PolicyConstraints.");
            }

            this.inhibit = -1;
        }

        this.encodeThis();
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration var1 = new AttributeNameEnumeration();
        var1.addElement("require");
        var1.addElement("inhibit");
        return var1.elements();
    }

    public String getName() {
        return "PolicyConstraints";
    }
}