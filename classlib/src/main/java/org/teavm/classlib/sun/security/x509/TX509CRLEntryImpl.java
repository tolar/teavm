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
import java.security.cert.CRLException;
import java.security.cert.CRLReason;
import java.security.cert.X509CRLEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.security.auth.x500.X500Principal;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.security.cert.TCRLException;
import org.teavm.classlib.java.security.cert.TX509CRLEntry;
import org.teavm.classlib.javax.auth.x500.TX500Principal;
import org.teavm.classlib.sun.misc.THexDumpEncoder;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TX509CRLEntryImpl extends TX509CRLEntry implements Comparable<TX509CRLEntryImpl> {
    private TSerialNumber serialNumber = null;
    private Date revocationDate = null;
    private TCRLExtensions extensions = null;
    private byte[] revokedCert = null;
    private TX500Principal certIssuer;
    private static final boolean isExplicit = false;
    private static final long YR_2050 = 2524636800000L;

    public TX509CRLEntryImpl(TBigInteger var1, Date var2) {
        this.serialNumber = new TSerialNumber(var1);
        this.revocationDate = var2;
    }

    public TX509CRLEntryImpl(TBigInteger var1, Date var2, TCRLExtensions var3) {
        this.serialNumber = new TSerialNumber(var1);
        this.revocationDate = var2;
        this.extensions = var3;
    }

    public TX509CRLEntryImpl(byte[] var1) throws TCRLException {
        try {
            this.parse(new TDerValue(var1));
        } catch (TIOException var3) {
            this.revokedCert = null;
            throw new TCRLException("Parsing error: " + var3.toString());
        }
    }

    public TX509CRLEntryImpl(TDerValue var1) throws CRLException {
        try {
            this.parse(var1);
        } catch (IOException var3) {
            this.revokedCert = null;
            throw new CRLException("Parsing error: " + var3.toString());
        }
    }

    public boolean hasExtensions() {
        return this.extensions != null;
    }

    public void encode(TDerOutputStream var1) throws CRLException {
        try {
            if(this.revokedCert == null) {
                TDerOutputStream var2 = new TDerOutputStream();
                this.serialNumber.encode(var2);
                if(this.revocationDate.getTime() < 2524636800000L) {
                    var2.putUTCTime(this.revocationDate);
                } else {
                    var2.putGeneralizedTime(this.revocationDate);
                }

                if(this.extensions != null) {
                    this.extensions.encode(var2, false);
                }

                TDerOutputStream var3 = new TDerOutputStream();
                var3.write((byte)48, var2);
                this.revokedCert = var3.toByteArray();
            }

            var1.write(this.revokedCert);
        } catch (TIOException var4) {
            throw new CRLException("Encoding error: " + var4.toString());
        }
    }

    public byte[] getEncoded() throws CRLException {
        return (byte[])this.getEncoded0().clone();
    }

    private byte[] getEncoded0() throws CRLException {
        if(this.revokedCert == null) {
            this.encode(new TDerOutputStream());
        }

        return this.revokedCert;
    }

    public X500Principal getCertificateIssuer() {
        return this.certIssuer;
    }

    void setCertificateIssuer(TX500Principal var1, TX500Principal var2) {
        if(var1.equals(var2)) {
            this.certIssuer = null;
        } else {
            this.certIssuer = var2;
        }

    }

    public TBigInteger getSerialNumber() {
        return this.serialNumber.getNumber();
    }

    public Date getRevocationDate() {
        return new Date(this.revocationDate.getTime());
    }

    public CRLReason getRevocationReason() {
        TExtension var1 = this.getExtension(TPKIXExtensions.ReasonCode_Id);
        if(var1 == null) {
            return null;
        } else {
            TCRLReasonCodeExtension var2 = (TCRLReasonCodeExtension)var1;
            return var2.getReasonCode();
        }
    }

    public static CRLReason getRevocationReason(TX509CRLEntry var0) {
        try {
            byte[] var1 = var0.getExtensionValue("2.5.29.21");
            if(var1 == null) {
                return null;
            } else {
                TDerValue var2 = new TDerValue(var1);
                byte[] var3 = var2.getOctetString();
                TCRLReasonCodeExtension var4 = new TCRLReasonCodeExtension(Boolean.FALSE, var3);
                return var4.getReasonCode();
            }
        } catch (TIOException var5) {
            return null;
        }
    }

    public Integer getReasonCode() throws IOException {
        TExtension var1 = this.getExtension(TPKIXExtensions.ReasonCode_Id);
        if(var1 == null) {
            return null;
        } else {
            TCRLReasonCodeExtension var2 = (TCRLReasonCodeExtension)var1;
            return var2.get(TString.wrap("reason"));
        }
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        var1.append(this.serialNumber.toString());
        var1.append("  On: " + this.revocationDate.toString());
        if(this.certIssuer != null) {
            var1.append("\n    Certificate issuer: " + this.certIssuer);
        }

        if(this.extensions != null) {
            Collection var2 = this.extensions.getAllExtensions();
            TExtension[] var3 = (TExtension[])var2.toArray(new TExtension[0]);
            var1.append("\n    CRL Entry Extensions: " + var3.length);

            for(int var4 = 0; var4 < var3.length; ++var4) {
                var1.append("\n    [" + (var4 + 1) + "]: ");
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
                            var1.append("Extension unknown: DER encoded OCTET string =\n" + var8.encodeBuffer(var6) + "\n");
                        }
                    } else {
                        var1.append(var5.toString());
                    }
                } catch (Exception var9) {
                    var1.append(", Error parsing this extension");
                }
            }
        }

        var1.append("\n");
        return var1.toString();
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.extensions == null?false:this.extensions.hasUnsupportedCriticalExtension();
    }

    public Set<String> getCriticalExtensionOIDs() {
        if(this.extensions == null) {
            return null;
        } else {
            TreeSet var1 = new TreeSet();
            Iterator var2 = this.extensions.getAllExtensions().iterator();

            while(var2.hasNext()) {
                TExtension var3 = (TExtension)var2.next();
                if(var3.isCritical()) {
                    var1.add(var3.getExtensionId().toString());
                }
            }

            return var1;
        }
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        if(this.extensions == null) {
            return null;
        } else {
            TreeSet var1 = new TreeSet();
            Iterator var2 = this.extensions.getAllExtensions().iterator();

            while(var2.hasNext()) {
                TExtension var3 = (TExtension)var2.next();
                if(!var3.isCritical()) {
                    var1.add(var3.getExtensionId().toString());
                }
            }

            return var1;
        }
    }

    public byte[] getExtensionValue(TString var1) {
        if(this.extensions == null) {
            return null;
        } else {
            try {
                TString var2 = TOIDMap.getName(new TObjectIdentifier(var1));
                TExtension var3 = null;
                if(var2 == null) {
                    TObjectIdentifier var4 = new TObjectIdentifier(var1);
                    TExtension var5 = null;
                    Enumeration var7 = this.extensions.getElements();

                    while(var7.hasMoreElements()) {
                        var5 = (TExtension)var7.nextElement();
                        TObjectIdentifier var6 = var5.getExtensionId();
                        if(var6.equals(var4)) {
                            var3 = var5;
                            break;
                        }
                    }
                } else {
                    var3 = this.extensions.get(var2);
                }

                if(var3 == null) {
                    return null;
                } else {
                    byte[] var9 = var3.getExtensionValue();
                    if(var9 == null) {
                        return null;
                    } else {
                        TDerOutputStream var10 = new TDerOutputStream();
                        var10.putOctetString(var9);
                        return var10.toByteArray();
                    }
                }
            } catch (Exception var8) {
                return null;
            }
        }
    }

    public TExtension getExtension(TObjectIdentifier var1) {
        return this.extensions == null?null:this.extensions.get(TOIDMap.getName(var1));
    }

    private void parse(TDerValue var1) throws CRLException, TIOException {
        if(var1.tag != 48) {
            throw new CRLException("Invalid encoded RevokedCertificate, starting sequence tag missing.");
        } else if(var1.data.available() == 0) {
            throw new CRLException("No data encoded for RevokedCertificates");
        } else {
            this.revokedCert = var1.toByteArray();
            TDerInputStream var2 = var1.toDerInputStream();
            TDerValue var3 = var2.getDerValue();
            this.serialNumber = new TSerialNumber(var3);
            int var4 = var1.data.peekByte();
            if((byte)var4 == 23) {
                this.revocationDate = var1.data.getUTCTime();
            } else {
                if((byte)var4 != 24) {
                    throw new CRLException("Invalid encoding for revocation date");
                }

                this.revocationDate = var1.data.getGeneralizedTime();
            }

            if(var1.data.available() != 0) {
                this.extensions = new TCRLExtensions(var1.toDerInputStream());
            }
        }
    }

    public static sun.security.x509.X509CRLEntryImpl toImpl(X509CRLEntry var0) throws CRLException {
        return var0 instanceof sun.security.x509.X509CRLEntryImpl
                ?(sun.security.x509.X509CRLEntryImpl)var0:new sun.security.x509.X509CRLEntryImpl(var0.getEncoded());
    }

    TCertificateIssuerExtension getCertificateIssuerExtension() {
        return (TCertificateIssuerExtension)this.getExtension(TPKIXExtensions.CertificateIssuer_Id);
    }

    public Map<String, java.security.cert.Extension> getExtensions() {
        if(this.extensions == null) {
            return Collections.emptyMap();
        } else {
            Collection var1 = this.extensions.getAllExtensions();
            TreeMap var2 = new TreeMap();
            Iterator var3 = var1.iterator();

            while(var3.hasNext()) {
                TExtension var4 = (TExtension)var3.next();
                var2.put(var4.getId(), var4);
            }

            return var2;
        }
    }

    public int compareTo(TX509CRLEntryImpl var1) {
        int var2 = this.getSerialNumber().compareTo(var1.getSerialNumber());
        if(var2 != 0) {
            return var2;
        } else {
            try {
                byte[] var3 = this.getEncoded0();
                byte[] var4 = var1.getEncoded0();

                for(int var5 = 0; var5 < var3.length && var5 < var4.length; ++var5) {
                    int var6 = var3[var5] & 255;
                    int var7 = var4[var5] & 255;
                    if(var6 != var7) {
                        return var6 - var7;
                    }
                }

                return var3.length - var4.length;
            } catch (CRLException var8) {
                return -1;
            }
        }
    }
}
