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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TExtendedKeyUsageExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.ExtendedKeyUsage";
    public static final String NAME = "ExtendedKeyUsage";
    public static final String USAGES = "usages";
    private static final Map<TObjectIdentifier, String> map = new HashMap();
    private static final int[] anyExtendedKeyUsageOidData = new int[]{2, 5, 29, 37, 0};
    private static final int[] serverAuthOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 1};
    private static final int[] clientAuthOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 2};
    private static final int[] codeSigningOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 3};
    private static final int[] emailProtectionOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 4};
    private static final int[] ipsecEndSystemOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 5};
    private static final int[] ipsecTunnelOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 6};
    private static final int[] ipsecUserOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 7};
    private static final int[] timeStampingOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 8};
    private static final int[] OCSPSigningOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 9};
    private Vector<TObjectIdentifier> keyUsages;

    private void encodeThis() throws TIOException {
        if(this.keyUsages != null && !this.keyUsages.isEmpty()) {
            TDerOutputStream var1 = new TDerOutputStream();
            TDerOutputStream var2 = new TDerOutputStream();

            for(int var3 = 0; var3 < this.keyUsages.size(); ++var3) {
                var2.putOID((TObjectIdentifier)this.keyUsages.elementAt(var3));
            }

            var1.write((byte) 48, var2);
            this.extensionValue = var1.toByteArray();
        } else {
            this.extensionValue = null;
        }
    }

    public TExtendedKeyUsageExtension(Vector<TObjectIdentifier> var1) throws IOException {
        this(Boolean.FALSE, var1);
    }

    public TExtendedKeyUsageExtension(Boolean var1, Vector<TObjectIdentifier> var2) throws IOException {
        this.keyUsages = var2;
        this.extensionId = TPKIXExtensions.ExtendedKeyUsage_Id;
        this.critical = var1.booleanValue();
        this.encodeThis();
    }

    public TExtendedKeyUsageExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = TPKIXExtensions.ExtendedKeyUsage_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        TDerValue var3 = new TDerValue(this.extensionValue);
        if(var3.tag != 48) {
            throw new IOException("Invalid encoding for ExtendedKeyUsageExtension.");
        } else {
            this.keyUsages = new Vector();

            while(var3.data.available() != 0) {
                TDerValue var4 = var3.data.getDerValue();
                TObjectIdentifier var5 = var4.getOID();
                this.keyUsages.addElement(var5);
            }

        }
    }

    public String toString() {
        if(this.keyUsages == null) {
            return "";
        } else {
            String var1 = "  ";
            boolean var2 = true;

            for(Iterator var3 = this.keyUsages.iterator(); var3.hasNext(); var2 = false) {
                TObjectIdentifier var4 = (TObjectIdentifier)var3.next();
                if(!var2) {
                    var1 = var1 + "\n  ";
                }

                String var5 = (String)map.get(var4);
                if(var5 != null) {
                    var1 = var1 + var5;
                } else {
                    var1 = var1 + var4.toString();
                }
            }

            return super.toString() + "ExtendedKeyUsages [\n" + var1 + "\n]\n";
        }
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.ExtendedKeyUsage_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("usages"))) {
            if(!(var2 instanceof Vector)) {
                throw new TIOException(TString.wrap("Attribute value should be of type Vector."));
            } else {
                this.keyUsages = (Vector)var2;
                this.encodeThis();
            }
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:ExtendedKeyUsageExtension."));
        }
    }

    public Vector<TObjectIdentifier> get(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("usages"))) {
            return this.keyUsages;
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:ExtendedKeyUsageExtension."));
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("usages"))) {
            this.keyUsages = null;
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:ExtendedKeyUsageExtension."));
        }
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("usages");
        return var1.elements();
    }

    public String getName() {
        return "ExtendedKeyUsage";
    }

    public List<String> getExtendedKeyUsage() {
        ArrayList var1 = new ArrayList(this.keyUsages.size());
        Iterator var2 = this.keyUsages.iterator();

        while(var2.hasNext()) {
            TObjectIdentifier var3 = (TObjectIdentifier)var2.next();
            var1.add(var3.toString());
        }

        return var1;
    }

    static {
        map.put(TObjectIdentifier.newInternal(anyExtendedKeyUsageOidData), "anyExtendedKeyUsage");
        map.put(TObjectIdentifier.newInternal(serverAuthOidData), "serverAuth");
        map.put(TObjectIdentifier.newInternal(clientAuthOidData), "clientAuth");
        map.put(TObjectIdentifier.newInternal(codeSigningOidData), "codeSigning");
        map.put(TObjectIdentifier.newInternal(emailProtectionOidData), "emailProtection");
        map.put(TObjectIdentifier.newInternal(ipsecEndSystemOidData), "ipsecEndSystem");
        map.put(TObjectIdentifier.newInternal(ipsecTunnelOidData), "ipsecTunnel");
        map.put(TObjectIdentifier.newInternal(ipsecUserOidData), "ipsecUser");
        map.put(TObjectIdentifier.newInternal(timeStampingOidData), "timeStamping");
        map.put(TObjectIdentifier.newInternal(OCSPSigningOidData), "OCSPSigning");
    }
}
