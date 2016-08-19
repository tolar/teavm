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
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.util.TCollection;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.misc.THexDumpEncoder;
import org.teavm.classlib.sun.security.cert.TCertificateEncodingException;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TX509CertInfo implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info";
    public static final String NAME = "info";
    public static final String DN_NAME = "dname";
    public static final String VERSION = "version";
    public static final String SERIAL_NUMBER = "serialNumber";
    public static final String ALGORITHM_ID = "algorithmID";
    public static final String ISSUER = "issuer";
    public static final String SUBJECT = "subject";
    public static final String VALIDITY = "validity";
    public static final String KEY = "key";
    public static final String ISSUER_ID = "issuerID";
    public static final String SUBJECT_ID = "subjectID";
    public static final String EXTENSIONS = "extensions";
    protected TCertificateVersion version = new TCertificateVersion();
    protected TCertificateSerialNumber serialNum = null;
    protected TCertificateAlgorithmId algId = null;
    protected TX500Name issuer = null;
    protected TX500Name subject = null;
    protected TCertificateValidity interval = null;
    protected TCertificateX509Key pubKey = null;
    protected TUniqueIdentity issuerUniqueId = null;
    protected TUniqueIdentity subjectUniqueId = null;
    protected TCertificateExtensions extensions = null;
    private static final int ATTR_VERSION = 1;
    private static final int ATTR_SERIAL = 2;
    private static final int ATTR_ALGORITHM = 3;
    private static final int ATTR_ISSUER = 4;
    private static final int ATTR_VALIDITY = 5;
    private static final int ATTR_SUBJECT = 6;
    private static final int ATTR_KEY = 7;
    private static final int ATTR_ISSUER_ID = 8;
    private static final int ATTR_SUBJECT_ID = 9;
    private static final int ATTR_EXTENSIONS = 10;
    private byte[] rawCertInfo = null;
    private static final Map<String, Integer> map = new HashMap();

    public TX509CertInfo() {
    }

    public TX509CertInfo(byte[] var1) throws CertificateParsingException {
        try {
            TDerValue var2 = new TDerValue(var1);
            this.parse(var2);
        } catch (IOException var3) {
            throw new CertificateParsingException(var3);
        }
    }

    public TX509CertInfo(TDerValue var1) throws CertificateParsingException {
        try {
            this.parse(var1);
        } catch (IOException var3) {
            throw new CertificateParsingException(var3);
        }
    }

    public void encode(OutputStream var1) throws CertificateException, IOException {
        if(this.rawCertInfo == null) {
            TDerOutputStream var2 = new TDerOutputStream();
            this.emit(var2);
            this.rawCertInfo = var2.toByteArray();
        }

        var1.write((byte[])this.rawCertInfo.clone());
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("version");
        var1.addElement("serialNumber");
        var1.addElement("algorithmID");
        var1.addElement("issuer");
        var1.addElement("validity");
        var1.addElement("subject");
        var1.addElement("key");
        var1.addElement("issuerID");
        var1.addElement("subjectID");
        var1.addElement("extensions");
        return var1.elements();
    }

    public String getName() {
        return "info";
    }

    public byte[] getEncodedInfo() throws TCertificateEncodingException {
        try {
            if(this.rawCertInfo == null) {
                TDerOutputStream var1 = new TDerOutputStream();
                this.emit(var1);
                this.rawCertInfo = var1.toByteArray();
            }

            return (byte[])this.rawCertInfo.clone();
        } catch (TIOException var2) {
            throw new TCertificateEncodingException(var2.toString());
        } catch (TCertificateException var3) {
            throw new TCertificateEncodingException(var3.toString());
        }
    }

    public boolean equals(Object var1) {
        return var1 instanceof TX509CertInfo ?this.equals((TX509CertInfo)var1):false;
    }

    public boolean equals(TX509CertInfo var1) {
        if(this == var1) {
            return true;
        } else if(this.rawCertInfo != null && var1.rawCertInfo != null) {
            if(this.rawCertInfo.length != var1.rawCertInfo.length) {
                return false;
            } else {
                for(int var2 = 0; var2 < this.rawCertInfo.length; ++var2) {
                    if(this.rawCertInfo[var2] != var1.rawCertInfo[var2]) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int var1 = 0;

        for(int var2 = 1; var2 < this.rawCertInfo.length; ++var2) {
            var1 += this.rawCertInfo[var2] * var2;
        }

        return var1;
    }

    public String toString() {
        if(this.subject != null && this.pubKey != null && this.interval != null && this.issuer != null && this.algId != null && this.serialNum != null) {
            StringBuilder var1 = new StringBuilder();
            var1.append("[\n");
            var1.append("  " + this.version.toString() + "\n");
            var1.append("  Subject: " + this.subject.toString() + "\n");
            var1.append("  Signature Algorithm: " + this.algId.toString() + "\n");
            var1.append("  Key:  " + this.pubKey.toString() + "\n");
            var1.append("  " + this.interval.toString() + "\n");
            var1.append("  Issuer: " + this.issuer.toString() + "\n");
            var1.append("  " + this.serialNum.toString() + "\n");
            if(this.issuerUniqueId != null) {
                var1.append("  Issuer Id:\n" + this.issuerUniqueId.toString() + "\n");
            }

            if(this.subjectUniqueId != null) {
                var1.append("  Subject Id:\n" + this.subjectUniqueId.toString() + "\n");
            }

            if(this.extensions != null) {
                TCollection var2 = this.extensions.getAllExtensions();
                TExtension[] var3 = (TExtension[])var2.toArray(new TExtension[0]);
                var1.append("\nCertificate Extensions: " + var3.length);

                for(int var4 = 0; var4 < var3.length; ++var4) {
                    var1.append("\n[" + (var4 + 1) + "]: ");
                    TExtension var5 = var3[var4];

                    try {
                        if(TOIDMap.getClass(var5.getExtensionId()) == null) {
                            var1.append(var5.toString());
                            byte[] var6 = var5.getExtensionValue();
                            if(var6 != null) {
                                TDerOutputStream var7 = new TDerOutputStream();
                                var7.putOctetString(var6);
                                var6 = var7.toByteArray();
                                THexDumpEncoder var8 = new THexDumpEncoder();
                                var1.append("TExtension unknown: DER encoded OCTET string =\n" + var8.encodeBuffer(var6) + "\n");
                            }
                        } else {
                            var1.append(var5.toString());
                        }
                    } catch (Exception var9) {
                        var1.append(", Error parsing this extension");
                    }
                }

                Map var10 = this.extensions.getUnparseableExtensions();
                if(!var10.isEmpty()) {
                    var1.append("\nUnparseable certificate extensions: " + var10.size());
                    int var11 = 1;
                    Iterator var12 = var10.values().iterator();

                    while(var12.hasNext()) {
                        TExtension var13 = (TExtension)var12.next();
                        var1.append("\n[" + var11++ + "]: ");
                        var1.append(var13);
                    }
                }
            }

            var1.append("\n]");
            return var1.toString();
        } else {
            throw new NullPointerException("X.509 cert is incomplete");
        }
    }

    public void set(String var1, Object var2) throws TCertificateException, TIOException {
        TX509AttributeName var3 = new TX509AttributeName(TString.wrap(var1));
        int var4 = this.attributeMap(var3.getPrefix());
        if(var4 == 0) {
            throw new TCertificateException(TString.wrap("Attribute name not recognized: " + var1));
        } else {
            this.rawCertInfo = null;
            TString var5 = var3.getSuffix();
            switch(var4) {
                case 1:
                    if(var5 == null) {
                        this.setVersion(var2);
                    } else {
                        this.version.set(var5, var2);
                    }
                    break;
                case 2:
                    if(var5 == null) {
                        this.setSerialNumber(var2);
                    } else {
                        this.serialNum.set(var5, var2);
                    }
                    break;
                case 3:
                    if(var5 == null) {
                        this.setAlgorithmId(var2);
                    } else {
                        this.algId.set(var5, var2);
                    }
                    break;
                case 4:
                    this.setIssuer(var2);
                    break;
                case 5:
                    if(var5 == null) {
                        this.setValidity(var2);
                    } else {
                        this.interval.set(var5, var2);
                    }
                    break;
                case 6:
                    this.setSubject(var2);
                    break;
                case 7:
                    if(var5 == null) {
                        this.setKey(var2);
                    } else {
                        this.pubKey.set(var5, var2);
                    }
                    break;
                case 8:
                    this.setIssuerUniqueId(var2);
                    break;
                case 9:
                    this.setSubjectUniqueId(var2);
                    break;
                case 10:
                    if(var5 == null) {
                        this.setExtensions(var2);
                    } else {
                        if(this.extensions == null) {
                            this.extensions = new TCertificateExtensions();
                        }

                        this.extensions.set(var5, var2);
                    }
            }

        }
    }

    public void delete(String var1) throws TCertificateException, TIOException {
        TX509AttributeName var2 = new TX509AttributeName(var1);
        int var3 = this.attributeMap(var2.getPrefix());
        if(var3 == 0) {
            throw new TCertificateException(TString.wrap("Attribute name not recognized: " + var1));
        } else {
            this.rawCertInfo = null;
            TString var4 = var2.getSuffix();
            switch(var3) {
                case 1:
                    if(var4 == null) {
                        this.version = null;
                    } else {
                        this.version.delete(var4);
                    }
                    break;
                case 2:
                    if(var4 == null) {
                        this.serialNum = null;
                    } else {
                        this.serialNum.delete(var4);
                    }
                    break;
                case 3:
                    if(var4 == null) {
                        this.algId = null;
                    } else {
                        this.algId.delete(var4);
                    }
                    break;
                case 4:
                    this.issuer = null;
                    break;
                case 5:
                    if(var4 == null) {
                        this.interval = null;
                    } else {
                        this.interval.delete(var4);
                    }
                    break;
                case 6:
                    this.subject = null;
                    break;
                case 7:
                    if(var4 == null) {
                        this.pubKey = null;
                    } else {
                        this.pubKey.delete(var4);
                    }
                    break;
                case 8:
                    this.issuerUniqueId = null;
                    break;
                case 9:
                    this.subjectUniqueId = null;
                    break;
                case 10:
                    if(var4 == null) {
                        this.extensions = null;
                    } else if(this.extensions != null) {
                        this.extensions.delete(var4);
                    }
            }

        }
    }

    public Object get(TString var1) throws CertificateException, IOException {
        TX509AttributeName var2 = new TX509AttributeName(var1);
        int var3 = this.attributeMap(var2.getPrefix());
        if(var3 == 0) {
            throw new CertificateParsingException("Attribute name not recognized: " + var1);
        } else {
            String var4 = var2.getSuffix();
            switch(var3) {
                case 1:
                    if(var4 == null) {
                        return this.version;
                    }

                    return this.version.get(var4);
                case 2:
                    if(var4 == null) {
                        return this.serialNum;
                    }

                    return this.serialNum.get(var4);
                case 3:
                    if(var4 == null) {
                        return this.algId;
                    }

                    return this.algId.get(var4);
                case 4:
                    if(var4 == null) {
                        return this.issuer;
                    }

                    return this.getX500Name(var4, true);
                case 5:
                    if(var4 == null) {
                        return this.interval;
                    }

                    return this.interval.get(var4);
                case 6:
                    if(var4 == null) {
                        return this.subject;
                    }

                    return this.getX500Name(var4, false);
                case 7:
                    if(var4 == null) {
                        return this.pubKey;
                    }

                    return this.pubKey.get(var4);
                case 8:
                    return this.issuerUniqueId;
                case 9:
                    return this.subjectUniqueId;
                case 10:
                    if(var4 == null) {
                        return this.extensions;
                    } else {
                        if(this.extensions == null) {
                            return null;
                        }

                        return this.extensions.get(var4);
                    }
                default:
                    return null;
            }
        }
    }

    private Object getX500Name(String var1, boolean var2) throws IOException {
        if(var1.equalsIgnoreCase("dname")) {
            return var2?this.issuer:this.subject;
        } else if(var1.equalsIgnoreCase("x500principal")) {
            return var2?this.issuer.asX500Principal():this.subject.asX500Principal();
        } else {
            throw new IOException("Attribute name not recognized.");
        }
    }

    private void parse(TDerValue var1) throws CertificateParsingException, IOException {
        if(var1.tag != 48) {
            throw new CertificateParsingException("signed fields invalid");
        } else {
            this.rawCertInfo = var1.toByteArray();
            TDerInputStream var2 = var1.data;
            TDerValue var3 = var2.getDerValue();
            if(var3.isContextSpecific((byte) 0)) {
                this.version = new TCertificateVersion(var3);
                var3 = var2.getDerValue();
            }

            this.serialNum = new TCertificateSerialNumber(var3);
            this.algId = new TCertificateAlgorithmId(var2);
            this.issuer = new TX500Name(var2);
            if(this.issuer.isEmpty()) {
                throw new CertificateParsingException("Empty issuer DN not allowed in X509Certificates");
            } else {
                this.interval = new TCertificateValidity(var2);
                this.subject = new TX500Name(var2);
                if(this.version.compare(0) == 0 && this.subject.isEmpty()) {
                    throw new CertificateParsingException("Empty subject DN not allowed in v1 certificate");
                } else {
                    this.pubKey = new TCertificateX509Key(var2);
                    if(var2.available() != 0) {
                        if(this.version.compare(0) == 0) {
                            throw new CertificateParsingException("no more data allowed for version 1 certificate");
                        } else {
                            var3 = var2.getDerValue();
                            if(var3.isContextSpecific((byte) 1)) {
                                this.issuerUniqueId = new UniqueIdentity(var3);
                                if(var2.available() == 0) {
                                    return;
                                }

                                var3 = var2.getDerValue();
                            }

                            if(var3.isContextSpecific(2)) {
                                this.subjectUniqueId = new UniqueIdentity(var3);
                                if(var2.available() == 0) {
                                    return;
                                }

                                var3 = var2.getDerValue();
                            }

                            if(this.version.compare(2) != 0) {
                                throw new CertificateParsingException("Extensions not allowed in v2 certificate");
                            } else {
                                if(var3.isConstructed() && var3.isContextSpecific(3)) {
                                    this.extensions = new TCertificateExtensions(var3.data);
                                }

                                this.verifyCert(this.subject, this.extensions);
                            }
                        }
                    }
                }
            }
        }
    }

    private void verifyCert(TX500Name var1, TCertificateExtensions var2) throws CertificateParsingException, IOException {
        if(var1.isEmpty()) {
            if(var2 == null) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and certificate has no extensions");
            }

            SubjectAlternativeNameExtension var3 = null;
            Object var4 = null;
            TGeneralNames var5 = null;

            try {
                var3 = (SubjectAlternativeNameExtension)var2.get("SubjectAlternativeName");
                var5 = var3.get("subject_name");
            } catch (IOException var7) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is absent");
            }

            if(var5 == null || var5.isEmpty()) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is empty");
            }

            if(!var3.isCritical()) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: SubjectAlternativeName extension MUST be marked critical when subject field is empty");
            }
        }

    }

    private void emit(TDerOutputStream var1) throws TCertificateException, TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        this.version.encode(var2);
        this.serialNum.encode(var2);
        this.algId.encode(var2);
        if(this.version.compare(0) == 0 && this.issuer.toString() == null) {
            throw new CertificateParsingException("Null issuer DN not allowed in v1 certificate");
        } else {
            this.issuer.encode(var2);
            this.interval.encode(var2);
            if(this.version.compare(0) == 0 && this.subject.toString() == null) {
                throw new CertificateParsingException("Null subject DN not allowed in v1 certificate");
            } else {
                this.subject.encode(var2);
                this.pubKey.encode(var2);
                if(this.issuerUniqueId != null) {
                    this.issuerUniqueId.encode(var2, DerValue.createTag(-128, false, 1));
                }

                if(this.subjectUniqueId != null) {
                    this.subjectUniqueId.encode(var2, DerValue.createTag(-128, false, 2));
                }

                if(this.extensions != null) {
                    this.extensions.encode(var2);
                }

                var1.write(48, var2);
            }
        }
    }

    private int attributeMap(TString var1) {
        Integer var2 = (Integer)map.get(var1);
        return var2 == null?0:var2.intValue();
    }

    private void setVersion(Object var1) throws TCertificateException {
        if(!(var1 instanceof TCertificateVersion)) {
            throw new TCertificateException(TString.wrap("Version class type invalid."));
        } else {
            this.version = (TCertificateVersion)var1;
        }
    }

    private void setSerialNumber(Object var1) throws TCertificateException {
        if(!(var1 instanceof TCertificateSerialNumber)) {
            throw new TCertificateException(TString.wrap("TSerialNumber class type invalid."));
        } else {
            this.serialNum = (TCertificateSerialNumber)var1;
        }
    }

    private void setAlgorithmId(Object var1) throws TCertificateException {
        if(!(var1 instanceof TCertificateAlgorithmId)) {
            throw new TCertificateException(TString.wrap("AlgorithmId class type invalid."));
        } else {
            this.algId = (TCertificateAlgorithmId)var1;
        }
    }

    private void setIssuer(Object var1) throws TCertificateException {
        if(!(var1 instanceof TX500Name)) {
            throw new TCertificateException(TString.wrap("Issuer class type invalid."));
        } else {
            this.issuer = (TX500Name)var1;
        }
    }

    private void setValidity(Object var1) throws TCertificateException {
        if(!(var1 instanceof TCertificateValidity)) {
            throw new TCertificateException(TString.wrap("CertificateValidity class type invalid."));
        } else {
            this.interval = (TCertificateValidity)var1;
        }
    }

    private void setSubject(Object var1) throws TCertificateException {
        if(!(var1 instanceof TX500Name)) {
            throw new TCertificateException(TString.wrap("Subject class type invalid."));
        } else {
            this.subject = (TX500Name)var1;
        }
    }

    private void setKey(Object var1) throws TCertificateException {
        if(!(var1 instanceof TCertificateX509Key)) {
            throw new TCertificateException(TString.wrap("Key class type invalid."));
        } else {
            this.pubKey = (TCertificateX509Key)var1;
        }
    }

    private void setIssuerUniqueId(Object var1) throws TCertificateException {
        if(this.version.compare(1) < 0) {
            throw new TCertificateException(TString.wrap("Invalid version"));
        } else if(!(var1 instanceof TUniqueIdentity)) {
            throw new TCertificateException(TString.wrap("IssuerUniqueId class type invalid."));
        } else {
            this.issuerUniqueId = (TUniqueIdentity)var1;
        }
    }

    private void setSubjectUniqueId(Object var1) throws TCertificateException {
        if(this.version.compare(1) < 0) {
            throw new TCertificateException(TString.wrap("Invalid version"));
        } else if(!(var1 instanceof TUniqueIdentity)) {
            throw new TCertificateException(TString.wrap("SubjectUniqueId class type invalid."));
        } else {
            this.subjectUniqueId = (TUniqueIdentity)var1;
        }
    }

    private void setExtensions(Object var1) throws TCertificateException {
        if(this.version.compare(2) < 0) {
            throw new TCertificateException(TString.wrap("Invalid version"));
        } else if(!(var1 instanceof TCertificateExtensions)) {
            throw new TCertificateException(TString.wrap("Extensions class type invalid."));
        } else {
            this.extensions = (TCertificateExtensions)var1;
        }
    }

    static {
        map.put("version", Integer.valueOf(1));
        map.put("serialNumber", Integer.valueOf(2));
        map.put("algorithmID", Integer.valueOf(3));
        map.put("issuer", Integer.valueOf(4));
        map.put("validity", Integer.valueOf(5));
        map.put("subject", Integer.valueOf(6));
        map.put("key", Integer.valueOf(7));
        map.put("issuerID", Integer.valueOf(8));
        map.put("subjectID", Integer.valueOf(9));
        map.put("extensions", Integer.valueOf(10));
    }
}
