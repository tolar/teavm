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
import sun.security.x509.Extension;
import sun.security.x509.GeneralNames;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.SerialNumber;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TAuthorityKeyIdentifierExtension extends Extension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.AuthorityKeyIdentifier";
    public static final String NAME = "AuthorityKeyIdentifier";
    public static final String KEY_ID = "key_id";
    public static final String AUTH_NAME = "auth_name";
    public static final String SERIAL_NUMBER = "serial_number";
    private static final byte TAG_ID = 0;
    private static final byte TAG_NAMES = 1;
    private static final byte TAG_SERIAL_NUM = 2;
    private KeyIdentifier id = null;
    private GeneralNames names = null;
    private SerialNumber serialNum = null;

    private void encodeThis() throws IOException {
        if(this.id == null && this.names == null && this.serialNum == null) {
            this.extensionValue = null;
        } else {
            DerOutputStream var1 = new DerOutputStream();
            DerOutputStream var2 = new DerOutputStream();
            DerOutputStream var3;
            if(this.id != null) {
                var3 = new DerOutputStream();
                this.id.encode(var3);
                var2.writeImplicit(DerValue.createTag(-128, false, 0), var3);
            }

            try {
                if(this.names != null) {
                    var3 = new DerOutputStream();
                    this.names.encode(var3);
                    var2.writeImplicit(DerValue.createTag(-128, true, 1), var3);
                }
            } catch (Exception var4) {
                throw new IOException(var4.toString());
            }

            if(this.serialNum != null) {
                var3 = new DerOutputStream();
                this.serialNum.encode(var3);
                var2.writeImplicit(DerValue.createTag(-128, false, 2), var3);
            }

            var1.write(48, var2);
            this.extensionValue = var1.toByteArray();
        }
    }

    public TAuthorityKeyIdentifierExtension(KeyIdentifier var1, GeneralNames var2, SerialNumber var3) throws IOException {
        this.id = var1;
        this.names = var2;
        this.serialNum = var3;
        this.extensionId = PKIXExtensions.AuthorityKey_Id;
        this.critical = false;
        this.encodeThis();
    }

    public TAuthorityKeyIdentifierExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = PKIXExtensions.AuthorityKey_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        DerValue var3 = new DerValue(this.extensionValue);
        if(var3.tag != 48) {
            throw new IOException("Invalid encoding for TAuthorityKeyIdentifierExtension.");
        } else {
            while(true) {
                if(var3.data != null && var3.data.available() != 0) {
                    DerValue var4 = var3.data.getDerValue();
                    if(var4.isContextSpecific(0) && !var4.isConstructed()) {
                        if(this.id != null) {
                            throw new IOException("Duplicate KeyIdentifier in AuthorityKeyIdentifier.");
                        }

                        var4.resetTag(4);
                        this.id = new KeyIdentifier(var4);
                        continue;
                    }

                    if(var4.isContextSpecific(1) && var4.isConstructed()) {
                        if(this.names != null) {
                            throw new IOException("Duplicate GeneralNames in AuthorityKeyIdentifier.");
                        }

                        var4.resetTag(48);
                        this.names = new GeneralNames(var4);
                        continue;
                    }

                    if(var4.isContextSpecific(2) && !var4.isConstructed()) {
                        if(this.serialNum != null) {
                            throw new IOException("Duplicate SerialNumber in AuthorityKeyIdentifier.");
                        }

                        var4.resetTag(2);
                        this.serialNum = new SerialNumber(var4);
                        continue;
                    }

                    throw new IOException("Invalid encoding of TAuthorityKeyIdentifierExtension.");
                }

                return;
            }
        }
    }

    public String toString() {
        String var1 = super.toString() + "AuthorityKeyIdentifier [\n";
        if(this.id != null) {
            var1 = var1 + this.id.toString();
        }

        if(this.names != null) {
            var1 = var1 + this.names.toString() + "\n";
        }

        if(this.serialNum != null) {
            var1 = var1 + this.serialNum.toString() + "\n";
        }

        return var1 + "]\n";
    }

    public void encode(OutputStream var1) throws IOException {
        DerOutputStream var2 = new DerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = PKIXExtensions.AuthorityKey_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws IOException {
        if(var1.equalsIgnoreCase("key_id")) {
            if(!(var2 instanceof KeyIdentifier)) {
                throw new IOException("Attribute value should be of type KeyIdentifier.");
            }

            this.id = (KeyIdentifier)var2;
        } else if(var1.equalsIgnoreCase("auth_name")) {
            if(!(var2 instanceof GeneralNames)) {
                throw new IOException("Attribute value should be of type GeneralNames.");
            }

            this.names = (GeneralNames)var2;
        } else {
            if(!var1.equalsIgnoreCase("serial_number")) {
                throw new IOException("Attribute name not recognized by CertAttrSet:AuthorityKeyIdentifier.");
            }

            if(!(var2 instanceof SerialNumber)) {
                throw new IOException("Attribute value should be of type SerialNumber.");
            }

            this.serialNum = (SerialNumber)var2;
        }

        this.encodeThis();
    }

    public Object get(String var1) throws IOException {
        if(var1.equalsIgnoreCase("key_id")) {
            return this.id;
        } else if(var1.equalsIgnoreCase("auth_name")) {
            return this.names;
        } else if(var1.equalsIgnoreCase("serial_number")) {
            return this.serialNum;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:AuthorityKeyIdentifier.");
        }
    }

    public void delete(String var1) throws IOException {
        if(var1.equalsIgnoreCase("key_id")) {
            this.id = null;
        } else if(var1.equalsIgnoreCase("auth_name")) {
            this.names = null;
        } else {
            if(!var1.equalsIgnoreCase("serial_number")) {
                throw new IOException("Attribute name not recognized by CertAttrSet:AuthorityKeyIdentifier.");
            }

            this.serialNum = null;
        }

        this.encodeThis();
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration var1 = new AttributeNameEnumeration();
        var1.addElement("key_id");
        var1.addElement("auth_name");
        var1.addElement("serial_number");
        return var1.elements();
    }

    public String getName() {
        return "AuthorityKeyIdentifier";
    }
}
