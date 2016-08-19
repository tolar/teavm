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
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.pkcs.TPKCS9Attribute;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TNameConstraintsExtension extends TExtension implements TCertAttrSet<String>, Cloneable {
    public static final String IDENT = "x509.info.extensions.NameConstraints";
    public static final String NAME = "NameConstraints";
    public static final String PERMITTED_SUBTREES = "permitted_subtrees";
    public static final String EXCLUDED_SUBTREES = "excluded_subtrees";
    private static final byte TAG_PERMITTED = 0;
    private static final byte TAG_EXCLUDED = 1;
    private TGeneralSubtrees permitted = null;
    private TGeneralSubtrees excluded = null;
    private boolean hasMin;
    private boolean hasMax;
    private boolean minMaxValid = false;

    private void calcMinMax() throws IOException {
        this.hasMin = false;
        this.hasMax = false;
        int var1;
        TGeneralSubtree var2;
        if(this.excluded != null) {
            for(var1 = 0; var1 < this.excluded.size(); ++var1) {
                var2 = this.excluded.get(var1);
                if(var2.getMinimum() != 0) {
                    this.hasMin = true;
                }

                if(var2.getMaximum() != -1) {
                    this.hasMax = true;
                }
            }
        }

        if(this.permitted != null) {
            for(var1 = 0; var1 < this.permitted.size(); ++var1) {
                var2 = this.permitted.get(var1);
                if(var2.getMinimum() != 0) {
                    this.hasMin = true;
                }

                if(var2.getMaximum() != -1) {
                    this.hasMax = true;
                }
            }
        }

        this.minMaxValid = true;
    }

    private void encodeThis() throws IOException {
        this.minMaxValid = false;
        if(this.permitted == null && this.excluded == null) {
            this.extensionValue = null;
        } else {
            TDerOutputStream var1 = new TDerOutputStream();
            TDerOutputStream var2 = new TDerOutputStream();
            TDerOutputStream var3;
            if(this.permitted != null) {
                var3 = new TDerOutputStream();
                this.permitted.encode(var3);
                var2.writeImplicit(TDerValue.createTag((byte)-128, true, (byte) 0), var3);
            }

            if(this.excluded != null) {
                var3 = new TDerOutputStream();
                this.excluded.encode(var3);
                var2.writeImplicit(TDerValue.createTag((byte)-128, true, (byte) 1), var3);
            }

            var1.write((byte) 48, var2);
            this.extensionValue = var1.toByteArray();
        }
    }

    public TNameConstraintsExtension(TGeneralSubtrees var1, TGeneralSubtrees var2) throws IOException {
        this.permitted = var1;
        this.excluded = var2;
        this.extensionId = TPKIXExtensions.NameConstraints_Id;
        this.critical = true;
        this.encodeThis();
    }

    public TNameConstraintsExtension(Boolean var1, Object var2) throws IOException {
        this.extensionId = TPKIXExtensions.NameConstraints_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        TDerValue var3 = new TDerValue(this.extensionValue);
        if(var3.tag != 48) {
            throw new IOException("Invalid encoding for NameConstraintsExtension.");
        } else if(var3.data != null) {
            while(true) {
                while(var3.data.available() != 0) {
                    TDerValue var4 = var3.data.getDerValue();
                    if(!var4.isContextSpecific((byte) 0) || !var4.isConstructed()) {
                        if(!var4.isContextSpecific((byte) 1) || !var4.isConstructed()) {
                            throw new IOException("Invalid encoding of NameConstraintsExtension.");
                        }

                        if(this.excluded != null) {
                            throw new IOException("Duplicate excluded GeneralSubtrees in NameConstraintsExtension.");
                        }

                        var4.resetTag((byte) 48);
                        this.excluded = new TGeneralSubtrees(var4);
                    } else {
                        if(this.permitted != null) {
                            throw new IOException("Duplicate permitted GeneralSubtrees in NameConstraintsExtension.");
                        }

                        var4.resetTag((byte) 48);
                        this.permitted = new TGeneralSubtrees(var4);
                    }
                }

                this.minMaxValid = false;
                return;
            }
        }
    }

    public String toString() {
        return super.toString() + "NameConstraints: [" + (this.permitted == null?"":"\n    Permitted:" + this.permitted.toString()) + (this.excluded == null?"":"\n    Excluded:" + this.excluded.toString()) + "   ]\n";
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.NameConstraints_Id;
            this.critical = true;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase("permitted_subtrees")) {
            if(!(var2 instanceof TGeneralSubtrees)) {
                throw new TIOException(TString.wrap("Attribute value should be of type GeneralSubtrees."));
            }

            this.permitted = (TGeneralSubtrees)var2;
        } else {
            if(!var1.equalsIgnoreCase("excluded_subtrees")) {
                throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:NameConstraintsExtension."));
            }

            if(!(var2 instanceof TGeneralSubtrees)) {
                throw new TIOException(TString.wrap("Attribute value should be of type GeneralSubtrees."));
            }

            this.excluded = (TGeneralSubtrees)var2;
        }

        this.encodeThis();
    }

    public TGeneralSubtrees get(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("permitted_subtrees")) {
            return this.permitted;
        } else if(var1.equalsIgnoreCase("excluded_subtrees")) {
            return this.excluded;
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:NameConstraintsExtension."));
        }
    }

    public void delete(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("permitted_subtrees")) {
            this.permitted = null;
        } else {
            if(!var1.equalsIgnoreCase("excluded_subtrees")) {
                throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet:NameConstraintsExtension."));
            }

            this.excluded = null;
        }

        this.encodeThis();
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("permitted_subtrees");
        var1.addElement("excluded_subtrees");
        return var1.elements();
    }

    public String getName() {
        return "NameConstraints";
    }

    public void merge(TNameConstraintsExtension var1) throws IOException {
        if(var1 != null) {
            TGeneralSubtrees var2 = var1.get("excluded_subtrees");
            if(this.excluded == null) {
                this.excluded = var2 != null?(TGeneralSubtrees)var2.clone():null;
            } else if(var2 != null) {
                this.excluded.union(var2);
            }

            TGeneralSubtrees var3 = var1.get("permitted_subtrees");
            if(this.permitted == null) {
                this.permitted = var3 != null?(TGeneralSubtrees)var3.clone():null;
            } else if(var3 != null) {
                var2 = this.permitted.intersect(var3);
                if(var2 != null) {
                    if(this.excluded != null) {
                        this.excluded.union(var2);
                    } else {
                        this.excluded = (TGeneralSubtrees)var2.clone();
                    }
                }
            }

            if(this.permitted != null) {
                this.permitted.reduce(this.excluded);
            }

            this.encodeThis();
        }
    }

    public boolean verify(X509Certificate var1) throws IOException {
        if(var1 == null) {
            throw new IOException("Certificate is null");
        } else {
            if(!this.minMaxValid) {
                this.calcMinMax();
            }

            if(this.hasMin) {
                throw new IOException("Non-zero minimum BaseDistance in name constraints not supported");
            } else if(this.hasMax) {
                throw new IOException("Maximum BaseDistance in name constraints not supported");
            } else {
                X500Principal var2 = var1.getSubjectX500Principal();
                TX500Name var3 = TX500Name.asX500Name(var2);
                if(!var3.isEmpty() && !this.verify((TGeneralNameInterface)var3)) {
                    return false;
                } else {
                    TGeneralNames var4 = null;

                    try {
                        TX509CertImpl var5 = TX509CertImpl.toImpl(var1);
                        TSubjectAlternativeNameExtension var6 = var5.getSubjectAlternativeNameExtension();
                        if(var6 != null) {
                            var4 = var6.get("subject_name");
                        }
                    } catch (TCertificateException var7) {
                        throw new IOException("Unable to extract extensions from certificate: " + var7.getMessage());
                    }

                    if(var4 == null) {
                        return this.verifyRFC822SpecialCase(var3);
                    } else {
                        for(int var8 = 0; var8 < var4.size(); ++var8) {
                            TGeneralNameInterface var9 = var4.get(var8).getName();
                            if(!this.verify(var9)) {
                                return false;
                            }
                        }

                        return true;
                    }
                }
            }
        }
    }

    public boolean verify(TGeneralNameInterface var1) throws IOException {
        if(var1 == null) {
            throw new IOException("name is null");
        } else {
            if(this.excluded != null && this.excluded.size() > 0) {
                for(int var2 = 0; var2 < this.excluded.size(); ++var2) {
                    TGeneralSubtree var3 = this.excluded.get(var2);
                    if(var3 != null) {
                        TGeneralName var4 = var3.getName();
                        if(var4 != null) {
                            TGeneralNameInterface var5 = var4.getName();
                            if(var5 != null) {
                                switch(var5.constrains(var1)) {
                                    case -1:
                                    case 2:
                                    case 3:
                                    default:
                                        break;
                                    case 0:
                                    case 1:
                                        return false;
                                }
                            }
                        }
                    }
                }
            }

            if(this.permitted != null && this.permitted.size() > 0) {
                boolean var7 = false;

                for(int var8 = 0; var8 < this.permitted.size(); ++var8) {
                    TGeneralSubtree var9 = this.permitted.get(var8);
                    if(var9 != null) {
                        TGeneralName var10 = var9.getName();
                        if(var10 != null) {
                            TGeneralNameInterface var6 = var10.getName();
                            if(var6 != null) {
                                switch(var6.constrains(var1)) {
                                    case -1:
                                    default:
                                        break;
                                    case 0:
                                    case 1:
                                        return true;
                                    case 2:
                                    case 3:
                                        var7 = true;
                                }
                            }
                        }
                    }
                }

                if(var7) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean verifyRFC822SpecialCase(TX500Name var1) throws IOException {
        Iterator var2 = var1.allAvas().iterator();

        while(true) {
            String var5;
            do {
                TAVA var3;
                TObjectIdentifier var4;
                do {
                    if(!var2.hasNext()) {
                        return true;
                    }

                    var3 = (TAVA)var2.next();
                    var4 = var3.getObjectIdentifier();
                } while(!var4.equals(TPKCS9Attribute.EMAIL_ADDRESS_OID));

                var5 = var3.getValueString();
            } while(var5 == null);

            TRFC822Name var6;
            try {
                var6 = new TRFC822Name(var5);
            } catch (IOException var8) {
                continue;
            }

            if(!this.verify((TGeneralNameInterface)var6)) {
                return false;
            }
        }
    }

    public Object clone() {
        try {
            TNameConstraintsExtension var1 = (TNameConstraintsExtension)super.clone();
            if(this.permitted != null) {
                var1.permitted = (TGeneralSubtrees)this.permitted.clone();
            }

            if(this.excluded != null) {
                var1.excluded = (TGeneralSubtrees)this.excluded.clone();
            }

            return var1;
        } catch (CloneNotSupportedException var2) {
            throw new RuntimeException("CloneNotSupportedException while cloning NameConstraintsException. This should never happen.");
        }
    }
}
