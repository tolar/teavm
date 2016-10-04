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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TBitArray;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TKeyUsageExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.KeyUsage";
    public static final String NAME = "KeyUsage";
    public static final String DIGITAL_SIGNATURE = "digital_signature";
    public static final String NON_REPUDIATION = "non_repudiation";
    public static final String KEY_ENCIPHERMENT = "key_encipherment";
    public static final String DATA_ENCIPHERMENT = "data_encipherment";
    public static final String KEY_AGREEMENT = "key_agreement";
    public static final String KEY_CERTSIGN = "key_certsign";
    public static final String CRL_SIGN = "crl_sign";
    public static final String ENCIPHER_ONLY = "encipher_only";
    public static final String DECIPHER_ONLY = "decipher_only";
    private boolean[] bitString;

    private void encodeThis() throws TIOException {
        TDerOutputStream var1 = new TDerOutputStream();
        var1.putTruncatedUnalignedBitString(new TBitArray(this.bitString));
        this.extensionValue = var1.toByteArray();
    }

    private boolean isSet(int var1) {
        return var1 < this.bitString.length && this.bitString[var1];
    }

    private void set(int var1, boolean var2) {
        if(var1 >= this.bitString.length) {
            boolean[] var3 = new boolean[var1 + 1];
            System.arraycopy(this.bitString, 0, var3, 0, this.bitString.length);
            this.bitString = var3;
        }

        this.bitString[var1] = var2;
    }

    public TKeyUsageExtension(byte[] var1) throws IOException {
        this.bitString = (new TBitArray(var1.length * 8, var1)).toBooleanArray();
        this.extensionId = TPKIXExtensions.KeyUsage_Id;
        this.critical = true;
        this.encodeThis();
    }

    public TKeyUsageExtension(boolean[] var1) throws IOException {
        this.bitString = var1;
        this.extensionId = TPKIXExtensions.KeyUsage_Id;
        this.critical = true;
        this.encodeThis();
    }

    public TKeyUsageExtension(TBitArray var1) throws IOException {
        this.bitString = var1.toBooleanArray();
        this.extensionId = TPKIXExtensions.KeyUsage_Id;
        this.critical = true;
        this.encodeThis();
    }

    public TKeyUsageExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = TPKIXExtensions.KeyUsage_Id;
        this.critical = var1.booleanValue();
        byte[] var3 = (byte[])((byte[])var2);
        if(var3[0] == 4) {
            this.extensionValue = (new TDerValue(var3)).getOctetString();
        } else {
            this.extensionValue = var3;
        }

        TDerValue var4 = new TDerValue(this.extensionValue);
        this.bitString = var4.getUnalignedBitString().toBooleanArray();
    }

    public TKeyUsageExtension() {
        this.extensionId = TPKIXExtensions.KeyUsage_Id;
        this.critical = true;
        this.bitString = new boolean[0];
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(!(var2 instanceof Boolean)) {
            throw new TIOException(TString.wrap("Attribute must be of type Boolean."));
        } else {
            boolean var3 = ((Boolean)var2).booleanValue();
            if(var1.equalsIgnoreCase(TString.wrap("digital_signature"))) {
                this.set(0, var3);
            } else if(var1.equalsIgnoreCase(TString.wrap("non_repudiation"))) {
                this.set(1, var3);
            } else if(var1.equalsIgnoreCase(TString.wrap("key_encipherment"))) {
                this.set(2, var3);
            } else if(var1.equalsIgnoreCase(TString.wrap("data_encipherment"))) {
                this.set(3, var3);
            } else if(var1.equalsIgnoreCase(TString.wrap("key_agreement"))) {
                this.set(4, var3);
            } else if(var1.equalsIgnoreCase(TString.wrap("key_certsign"))) {
                this.set(5, var3);
            } else if(var1.equalsIgnoreCase(TString.wrap("crl_sign"))) {
                this.set(6, var3);
            } else if(var1.equalsIgnoreCase(TString.wrap("encipher_only"))) {
                this.set(7, var3);
            } else {
                if(!var1.equalsIgnoreCase(TString.wrap("decipher_only"))) {
                    throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:KeyUsage."));
                }

                this.set(8, var3);
            }

            this.encodeThis();
        }
    }

    public Boolean get(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("digital_signature"))) {
            return Boolean.valueOf(this.isSet(0));
        } else if(var1.equalsIgnoreCase(TString.wrap("non_repudiation"))) {
            return Boolean.valueOf(this.isSet(1));
        } else if(var1.equalsIgnoreCase(TString.wrap("key_encipherment"))) {
            return Boolean.valueOf(this.isSet(2));
        } else if(var1.equalsIgnoreCase(TString.wrap("data_encipherment"))) {
            return Boolean.valueOf(this.isSet(3));
        } else if(var1.equalsIgnoreCase(TString.wrap("key_agreement"))) {
            return Boolean.valueOf(this.isSet(4));
        } else if(var1.equalsIgnoreCase(TString.wrap("key_certsign"))) {
            return Boolean.valueOf(this.isSet(5));
        } else if(var1.equalsIgnoreCase(TString.wrap("crl_sign"))) {
            return Boolean.valueOf(this.isSet(6));
        } else if(var1.equalsIgnoreCase(TString.wrap("encipher_only"))) {
            return Boolean.valueOf(this.isSet(7));
        } else if(var1.equalsIgnoreCase(TString.wrap("decipher_only"))) {
            return Boolean.valueOf(this.isSet(8));
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:KeyUsage."));
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("digital_signature"))) {
            this.set(0, false);
        } else if(var1.equalsIgnoreCase(TString.wrap("non_repudiation"))) {
            this.set(1, false);
        } else if(var1.equalsIgnoreCase(TString.wrap("key_encipherment"))) {
            this.set(2, false);
        } else if(var1.equalsIgnoreCase(TString.wrap("data_encipherment"))) {
            this.set(3, false);
        } else if(var1.equalsIgnoreCase(TString.wrap("key_agreement"))) {
            this.set(4, false);
        } else if(var1.equalsIgnoreCase(TString.wrap("key_certsign"))) {
            this.set(5, false);
        } else if(var1.equalsIgnoreCase(TString.wrap("crl_sign"))) {
            this.set(6, false);
        } else if(var1.equalsIgnoreCase(TString.wrap("encipher_only"))) {
            this.set(7, false);
        } else {
            if(!var1.equalsIgnoreCase(TString.wrap("decipher_only"))) {
                throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:KeyUsage."));
            }

            this.set(8, false);
        }

        this.encodeThis();
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        var1.append(super.toString());
        var1.append("KeyUsage [\n");
        if(this.isSet(0)) {
            var1.append("  DigitalSignature\n");
        }

        if(this.isSet(1)) {
            var1.append("  Non_repudiation\n");
        }

        if(this.isSet(2)) {
            var1.append("  Key_Encipherment\n");
        }

        if(this.isSet(3)) {
            var1.append("  Data_Encipherment\n");
        }

        if(this.isSet(4)) {
            var1.append("  Key_Agreement\n");
        }

        if(this.isSet(5)) {
            var1.append("  Key_CertSign\n");
        }

        if(this.isSet(6)) {
            var1.append("  Crl_Sign\n");
        }

        if(this.isSet(7)) {
            var1.append("  Encipher_Only\n");
        }

        if(this.isSet(8)) {
            var1.append("  Decipher_Only\n");
        }

        var1.append("]\n");
        return var1.toString();
    }

    public void encode(TOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.KeyUsage_Id;
            this.critical = true;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("digital_signature");
        var1.addElement("non_repudiation");
        var1.addElement("key_encipherment");
        var1.addElement("data_encipherment");
        var1.addElement("key_agreement");
        var1.addElement("key_certsign");
        var1.addElement("crl_sign");
        var1.addElement("encipher_only");
        var1.addElement("decipher_only");
        return var1.elements();
    }

    public boolean[] getBits() {
        return (boolean[])this.bitString.clone();
    }

    public String getName() {
        return "KeyUsage";
    }
}
