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
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.util.Date;
import java.util.Objects;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.util.TDate;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TPrivateKeyUsageExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.PrivateKeyUsage";
    public static final String NAME = "PrivateKeyUsage";
    public static final String NOT_BEFORE = "not_before";
    public static final String NOT_AFTER = "not_after";
    private static final byte TAG_BEFORE = 0;
    private static final byte TAG_AFTER = 1;
    private TDate notBefore = null;
    private TDate notAfter = null;

    private void encodeThis() throws TIOException {
        if(this.notBefore == null && this.notAfter == null) {
            this.extensionValue = null;
        } else {
            TDerOutputStream var1 = new TDerOutputStream();
            TDerOutputStream var2 = new TDerOutputStream();
            TDerOutputStream var3;
            if(this.notBefore != null) {
                var3 = new TDerOutputStream();
                var3.putGeneralizedTime(this.notBefore);
                var2.writeImplicit(TDerValue.createTag((byte)-128, false, (byte) 0), var3);
            }

            if(this.notAfter != null) {
                var3 = new TDerOutputStream();
                var3.putGeneralizedTime(this.notAfter);
                var2.writeImplicit(TDerValue.createTag((byte)-128, false, (byte) 1), var3);
            }

            var1.write((byte) 48, var2);
            this.extensionValue = var1.toByteArray();
        }
    }

    public TPrivateKeyUsageExtension(TDate var1, TDate var2) throws IOException {
        this.notBefore = var1;
        this.notAfter = var2;
        this.extensionId = TPKIXExtensions.PrivateKeyUsage_Id;
        this.critical = false;
        this.encodeThis();
    }

    public TPrivateKeyUsageExtension(Boolean var1, Object var2) throws CertificateException, IOException {
        this.extensionId = TPKIXExtensions.PrivateKeyUsage_Id;
        this.critical = var1.booleanValue();
        this.extensionValue = (byte[])((byte[])var2);
        TDerInputStream var3 = new TDerInputStream(this.extensionValue);
        TDerValue[] var4 = var3.getSequence(2);

        for(int var5 = 0; var5 < var4.length; ++var5) {
            TDerValue var6 = var4[var5];
            if(var6.isContextSpecific((byte) 0) && !var6.isConstructed()) {
                if(this.notBefore != null) {
                    throw new CertificateParsingException("Duplicate notBefore in PrivateKeyUsage.");
                }

                var6.resetTag((byte) 24);
                var3 = new TDerInputStream(var6.toByteArray());
                this.notBefore = var3.getGeneralizedTime();
            } else {
                if(!var6.isContextSpecific((byte) 1) || var6.isConstructed()) {
                    throw new IOException("Invalid encoding of PrivateKeyUsageExtension");
                }

                if(this.notAfter != null) {
                    throw new CertificateParsingException("Duplicate notAfter in PrivateKeyUsage.");
                }

                var6.resetTag((byte) 24);
                var3 = new TDerInputStream(var6.toByteArray());
                this.notAfter = var3.getGeneralizedTime();
            }
        }

    }

    public String toString() {
        return super.toString() + "PrivateKeyUsage: [\n" + (this.notBefore == null?"":"From: " + this.notBefore.toString() + ", ") + (this.notAfter == null?"":"To: " + this.notAfter.toString()) + "]\n";
    }

    public void valid() throws CertificateNotYetValidException, CertificateExpiredException {
        TDate var1 = new TDate();
        this.valid(var1);
    }

    public void valid(TDate var1) throws CertificateNotYetValidException, CertificateExpiredException {
        Objects.requireNonNull(var1);
        if(this.notBefore != null && this.notBefore.after(var1)) {
            throw new CertificateNotYetValidException("NotBefore: " + this.notBefore.toString());
        } else if(this.notAfter != null && this.notAfter.before(var1)) {
            throw new CertificateExpiredException("NotAfter: " + this.notAfter.toString());
        }
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = TPKIXExtensions.PrivateKeyUsage_Id;
            this.critical = false;
            this.encodeThis();
        }

        super.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(TString var1, Object var2) throws TCertificateException, TIOException {
        if(!(var2 instanceof Date)) {
            throw new TCertificateException(TString.wrap("Attribute must be of type Date."));
        } else {
            if(var1.equalsIgnoreCase(TString.wrap("not_before"))) {
                this.notBefore = (TDate)var2;
            } else {
                if(!var1.equalsIgnoreCase(TString.wrap("not_after"))) {
                    throw new TCertificateException(TString.wrap("Attribute name not recognized by CertAttrSet:PrivateKeyUsage."));
                }

                this.notAfter = (TDate)var2;
            }

            this.encodeThis();
        }
    }

    public Date get(TString var1) throws TCertificateException {
        if(var1.equalsIgnoreCase(TString.wrap("not_before"))) {
            return new Date(this.notBefore.getTime());
        } else if(var1.equalsIgnoreCase(TString.wrap("not_after"))) {
            return new Date(this.notAfter.getTime());
        } else {
            throw new TCertificateException(TString.wrap("Attribute name not recognized by CertAttrSet:PrivateKeyUsage."));
        }
    }

    public void delete(TString var1) throws TCertificateException, TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("not_before"))) {
            this.notBefore = null;
        } else {
            if(!var1.equalsIgnoreCase(TString.wrap("not_after"))) {
                throw new TCertificateException(TString.wrap("Attribute name not recognized by CertAttrSet:PrivateKeyUsage."));
            }

            this.notAfter = null;
        }

        this.encodeThis();
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("not_before");
        var1.addElement("not_after");
        return var1.elements();
    }

    public String getName() {
        return "PrivateKeyUsage";
    }
}
