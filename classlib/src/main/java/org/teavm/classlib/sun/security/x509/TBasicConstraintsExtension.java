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
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AttributeNameEnumeration;
import sun.security.x509.CertAttrSet;
import sun.security.x509.Extension;
import sun.security.x509.PKIXExtensions;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TBasicConstraintsExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.BasicConstraints";
    public static final String NAME = "BasicConstraints";
    public static final String IS_CA = "is_ca";
    public static final String PATH_LEN = "path_len";
    private boolean ca;
    private int pathLen;

    private void encodeThis() throws IOException {
        DerOutputStream var1 = new DerOutputStream();
        DerOutputStream var2 = new DerOutputStream();
        if(this.ca) {
            var2.putBoolean(this.ca);
            if(this.pathLen >= 0) {
                var2.putInteger(this.pathLen);
            }
        }

        var1.write(48, var2);
        this.extensionValue = var1.toByteArray();
    }

    public TBasicConstraintsExtension(boolean var1, int var2) throws IOException {
        this(Boolean.valueOf(var1), var1, var2);
    }

    public TBasicConstraintsExtension(Boolean var1, boolean var2, int var3) throws IOException {
        this.ca = false;
        this.pathLen = -1;
        this.ca = var2;
        this.pathLen = var3;
        this.extensionId = PKIXExtensions.BasicConstraints_Id;
        this.critical = var1.booleanValue();
        this.encodeThis();
    }

    public TBasicConstraintsExtension(Boolean var1, Object var2) throws IOException {
        this.ca = false;
        this.pathLen = -1;
        this.extensionId = PKIXExtensions.BasicConstraints_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        DerValue var3 = new DerValue(this.extensionValue);
        if(var3.tag != 48) {
            throw new IOException("Invalid encoding of BasicConstraints");
        } else if(var3.data != null && var3.data.available() != 0) {
            DerValue var4 = var3.data.getDerValue();
            if(var4.tag == 1) {
                this.ca = var4.getBoolean();
                if(var3.data.available() == 0) {
                    this.pathLen = 2147483647;
                } else {
                    var4 = var3.data.getDerValue();
                    if(var4.tag != 2) {
                        throw new IOException("Invalid encoding of BasicConstraints");
                    } else {
                        this.pathLen = var4.getInteger();
                    }
                }
            }
        }
    }

    public String toString() {
        String var1 = super.toString() + "BasicConstraints:[\n";
        var1 = var1 + (this.ca?"  CA:true":"  CA:false") + "\n";
        if(this.pathLen >= 0) {
            var1 = var1 + "  PathLen:" + this.pathLen + "\n";
        } else {
            var1 = var1 + "  PathLen: undefined\n";
        }

        return var1 + "]\n";
    }

    public void encode(OutputStream var1) throws IOException {
        DerOutputStream var2 = new DerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = PKIXExtensions.BasicConstraints_Id;
            if(this.ca) {
                this.critical = true;
            } else {
                this.critical = false;
            }

            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws IOException {
        if(var1.equalsIgnoreCase("is_ca")) {
            if(!(var2 instanceof Boolean)) {
                throw new IOException("Attribute value should be of type Boolean.");
            }

            this.ca = ((Boolean)var2).booleanValue();
        } else {
            if(!var1.equalsIgnoreCase("path_len")) {
                throw new IOException("Attribute name not recognized by CertAttrSet:BasicConstraints.");
            }

            if(!(var2 instanceof Integer)) {
                throw new IOException("Attribute value should be of type Integer.");
            }

            this.pathLen = ((Integer)var2).intValue();
        }

        this.encodeThis();
    }

    public Object get(String var1) throws IOException {
        if(var1.equalsIgnoreCase("is_ca")) {
            return Boolean.valueOf(this.ca);
        } else if(var1.equalsIgnoreCase("path_len")) {
            return Integer.valueOf(this.pathLen);
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:BasicConstraints.");
        }
    }

    public void delete(String var1) throws IOException {
        if(var1.equalsIgnoreCase("is_ca")) {
            this.ca = false;
        } else {
            if(!var1.equalsIgnoreCase("path_len")) {
                throw new IOException("Attribute name not recognized by CertAttrSet:BasicConstraints.");
            }

            this.pathLen = -1;
        }

        this.encodeThis();
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration var1 = new AttributeNameEnumeration();
        var1.addElement("is_ca");
        var1.addElement("path_len");
        return var1.elements();
    }

    public String getName() {
        return "BasicConstraints";
    }
}
