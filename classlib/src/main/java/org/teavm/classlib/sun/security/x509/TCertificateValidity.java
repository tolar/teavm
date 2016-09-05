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
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Date;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TCertificateValidity implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.validity";
    public static final String NAME = "validity";
    public static final String NOT_BEFORE = "notBefore";
    public static final String NOT_AFTER = "notAfter";
    private static final long YR_2050 = 2524636800000L;
    private Date notBefore;
    private Date notAfter;

    private Date getNotBefore() {
        return new Date(this.notBefore.getTime());
    }

    private Date getNotAfter() {
        return new Date(this.notAfter.getTime());
    }

    private void construct(TDerValue var1) throws IOException {
        if(var1.tag != 48) {
            throw new IOException("Invalid encoded CertificateValidity, starting sequence tag missing.");
        } else if(var1.data.available() == 0) {
            throw new IOException("No data encoded for CertificateValidity");
        } else {
            TDerInputStream var2 = new TDerInputStream(var1.toByteArray());
            TDerValue[] var3 = var2.getSequence(2);
            if(var3.length != 2) {
                throw new IOException("Invalid encoding for CertificateValidity");
            } else {
                if(var3[0].tag == 23) {
                    this.notBefore = var1.data.getUTCTime();
                } else {
                    if(var3[0].tag != 24) {
                        throw new IOException("Invalid encoding for CertificateValidity");
                    }

                    this.notBefore = var1.data.getGeneralizedTime();
                }

                if(var3[1].tag == 23) {
                    this.notAfter = var1.data.getUTCTime();
                } else {
                    if(var3[1].tag != 24) {
                        throw new IOException("Invalid encoding for CertificateValidity");
                    }

                    this.notAfter = var1.data.getGeneralizedTime();
                }

            }
        }
    }

    public TCertificateValidity() {
    }

    public TCertificateValidity(Date var1, Date var2) {
        this.notBefore = var1;
        this.notAfter = var2;
    }

    public TCertificateValidity(TDerInputStream var1) throws IOException {
        TDerValue var2 = var1.getDerValue();
        this.construct(var2);
    }

    public String toString() {
        return this.notBefore != null && this.notAfter != null?"Validity: [From: " + this.notBefore.toString() + ",\n               To: " + this.notAfter.toString() + "]":"";
    }

    public void encode(TOutputStream var1) throws TIOException {
        if(this.notBefore != null && this.notAfter != null) {
            TDerOutputStream var2 = new TDerOutputStream();
            if(this.notBefore.getTime() < 2524636800000L) {
                var2.putUTCTime(this.notBefore);
            } else {
                var2.putGeneralizedTime(this.notBefore);
            }

            if(this.notAfter.getTime() < 2524636800000L) {
                var2.putUTCTime(this.notAfter);
            } else {
                var2.putGeneralizedTime(this.notAfter);
            }

            TDerOutputStream var3 = new TDerOutputStream();
            var3.write((byte) 48, var2);
            var1.write(var3.toByteArray());
        } else {
            throw new TIOException(TString.wrap("CertAttrSet:CertificateValidity: null values to encode.\n"));
        }
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(!(var2 instanceof Date)) {
            throw new TIOException(TString.wrap("Attribute must be of type Date."));
        } else {
            if(var1.equalsIgnoreCase(TString.wrap("notBefore"))) {
                this.notBefore = (Date)var2;
            } else {
                if(!var1.equalsIgnoreCase(TString.wrap("notAfter"))) {
                    throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet: CertificateValidity."));
                }

                this.notAfter = (Date)var2;
            }

        }
    }

    public Date get(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("notBefore"))) {
            return this.getNotBefore();
        } else if(var1.equalsIgnoreCase(TString.wrap("notAfter"))) {
            return this.getNotAfter();
        } else {
            throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet: CertificateValidity."));
        }
    }

    public void delete(TString var1) throws TIOException {
        if(var1.equalsIgnoreCase(TString.wrap("notBefore"))) {
            this.notBefore = null;
        } else {
            if(!var1.equalsIgnoreCase(TString.wrap("notAfter"))) {
                throw new TIOException(TString.wrap("Attribute name not recognized by CertAttrSet: CertificateValidity."));
            }

            this.notAfter = null;
        }

    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("notBefore");
        var1.addElement("notAfter");
        return var1.elements();
    }

    public String getName() {
        return "validity";
    }

    public void valid() throws CertificateNotYetValidException, CertificateExpiredException {
        Date var1 = new Date();
        this.valid(var1);
    }

    public void valid(Date var1) throws CertificateNotYetValidException, CertificateExpiredException {
        if(this.notBefore.after(var1)) {
            throw new CertificateNotYetValidException("NotBefore: " + this.notBefore.toString());
        } else if(this.notAfter.before(var1)) {
            throw new CertificateExpiredException("NotAfter: " + this.notAfter.toString());
        }
    }
}
