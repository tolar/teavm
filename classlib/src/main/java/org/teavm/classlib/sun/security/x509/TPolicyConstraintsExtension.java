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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TPolicyConstraintsExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.PolicyConstraints";
    public static final String NAME = "PolicyConstraints";
    public static final String REQUIRE = "require";
    public static final String INHIBIT = "inhibit";
    private static final byte TAG_REQUIRE = 0;
    private static final byte TAG_INHIBIT = 1;
    private int require;
    private int inhibit;

    private void encodeThis() throws TIOException {
        if(this.require == -1 && this.inhibit == -1) {
            this.extensionValue = null;
        } else {
            TDerOutputStream var1 = new TDerOutputStream();
            TDerOutputStream var2 = new TDerOutputStream();
            TDerOutputStream var3;
            if(this.require != -1) {
                var3 = new TDerOutputStream();
                var3.putInteger(this.require);
                var1.writeImplicit(TDerValue.createTag(-128, false, 0), var3);
            }

            if(this.inhibit != -1) {
                var3 = new TDerOutputStream();
                var3.putInteger(this.inhibit);
                var1.writeImplicit(TDerValue.createTag(-128, false, 1), var3);
            }

            var2.write(48, var1);
            this.extensionValue = var2.toByteArray();
        }
    }

    public TPolicyConstraintsExtension(int var1, int var2) throws TIOException {
        this(Boolean.FALSE, var1, var2);
    }

    public TPolicyConstraintsExtension(Boolean var1, int var2, int var3) throws TIOException {
        this.require = -1;
        this.inhibit = -1;
        this.require = var2;
        this.inhibit = var3;
        this.extensionId = TPKIXExtensions.PolicyConstraints_Id;
        this.critical = var1.booleanValue();
        this.encodeThis();
    }

    public TPolicyConstraintsExtension(Boolean var1, Object var2) throws TIOException {
        this.require = -1;
        this.inhibit = -1;
        this.extensionId = TPKIXExtensions.PolicyConstraints_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        TDerValue var3 = new TDerValue(this.extensionValue);
        if(var3.tag != 48) {
            throw new TIOException("Sequence tag missing for PolicyConstraint.");
        } else {
            TDerInputStream var4 = var3.data;

            while(true) {
                if(var4 != null && var4.available() != 0) {
                    TDerValue var5 = var4.getDerValue();
                    if(var5.isContextSpecific(0) && !var5.isConstructed()) {
                        if(this.require != -1) {
                            throw new TIOException(TString.wrap("Duplicate requireExplicitPolicyfound in the TPolicyConstraintsExtension"));
                        }

                        var5.resetTag(2);
                        this.require = var5.getInteger();
                        continue;
                    }

                    if(var5.isContextSpecific(1) && !var5.isConstructed()) {
                        if(this.inhibit != -1) {
                            throw new TIOException(TString.wrap("Duplicate inhibitPolicyMappingfound in the TPolicyConstraintsExtension"));
                        }

                        var5.resetTag(2);
                        this.inhibit = var5.getInteger();
                        continue;
                    }

                    throw new TIOException(TString.wrap("Invalid encoding of PolicyConstraint"));
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

    public void encode(TOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.PolicyConstraints_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws TIOException {
        if(!(var2 instanceof Integer)) {
            throw new TIOException(TString.wrap("Attribute value should be of type Integer."));
        } else {
            if(var1.equalsIgnoreCase("require")) {
                this.require = ((Integer)var2).intValue();
            } else {
                if(!var1.equalsIgnoreCase("inhibit")) {
                    throw new TIOException(TString.wrap("Attribute name [" + var1 + "]" + " not recognized by " + "CertAttrSet:PolicyConstraints."));
                }

                this.inhibit = ((Integer)var2).intValue();
            }

            this.encodeThis();
        }
    }

    public Integer get(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("require")) {
            return new Integer(this.require);
        } else if(var1.equalsIgnoreCase("inhibit")) {
            return new Integer(this.inhibit);
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:PolicyConstraints."));
        }
    }

    public void delete(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("require")) {
            this.require = -1;
        } else {
            if(!var1.equalsIgnoreCase("inhibit")) {
                throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:PolicyConstraints."));
            }

            this.inhibit = -1;
        }

        this.encodeThis();
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("require");
        var1.addElement("inhibit");
        return var1.elements();
    }

    public String getName() {
        return "PolicyConstraints";
    }
}