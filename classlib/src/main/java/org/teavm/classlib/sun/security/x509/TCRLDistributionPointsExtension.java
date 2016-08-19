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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TCRLDistributionPointsExtension extends TExtension implements TCertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.CRLDistributionPoints";
    public static final String NAME = "CRLDistributionPoints";
    public static final String POINTS = "points";
    private List<TDistributionPoint> distributionPoints;
    private String extensionName;

    public TCRLDistributionPointsExtension(List<TDistributionPoint> var1) throws IOException {
        this(false, var1);
    }

    public TCRLDistributionPointsExtension(boolean var1, List<TDistributionPoint> var2) throws IOException {
        this(TPKIXExtensions.CRLDistributionPoints_Id, var1, var2, "CRLDistributionPoints");
    }

    protected TCRLDistributionPointsExtension(TObjectIdentifier var1, boolean var2, List<TDistributionPoint> var3, String var4) throws IOException {
        this.extensionId = var1;
        this.critical = var2;
        this.distributionPoints = var3;
        this.encodeThis();
        this.extensionName = var4;
    }

    public TCRLDistributionPointsExtension(Boolean var1, Object var2) throws IOException {
        this(TPKIXExtensions.CRLDistributionPoints_Id, var1, var2, "CRLDistributionPoints");
    }

    protected TCRLDistributionPointsExtension(TObjectIdentifier var1, Boolean var2, Object var3, String var4) throws IOException {
        this.extensionId = var1;
        this.critical = var2.booleanValue();
        if(!(var3 instanceof byte[])) {
            throw new IOException("Illegal argument type");
        } else {
            this.extensionValue = (byte[])((byte[])var3);
            TDerValue var5 = new TDerValue(this.extensionValue);
            if(var5.tag != 48) {
                throw new IOException("Invalid encoding for " + var4 + " extension.");
            } else {
                this.distributionPoints = new ArrayList();

                while(var5.data.available() != 0) {
                    TDerValue var6 = var5.data.getDerValue();
                    TDistributionPoint var7 = new TDistributionPoint(var6);
                    this.distributionPoints.add(var7);
                }

                this.extensionName = var4;
            }
        }
    }

    public String getName() {
        return this.extensionName;
    }

    public void encode(OutputStream var1) throws IOException {
        this.encode(var1, TPKIXExtensions.CRLDistributionPoints_Id, false);
    }

    protected void encode(OutputStream var1, TObjectIdentifier var2, boolean var3) throws IOException {
        TDerOutputStream var4 = new TDerOutputStream();
        if(this.extensionValue == null) {
            this.extensionId = var2;
            this.critical = var3;
            this.encodeThis();
        }

        super.encode(var4);
        var1.write(var4.toByteArray());
    }

    public void set(String var1, Object var2) throws TIOException {
        if(var1.equalsIgnoreCase("points")) {
            if(!(var2 instanceof List)) {
                throw new TIOException(TString.wrap("Attribute value should be of type List."));
            } else {
                this.distributionPoints = (List)var2;
                this.encodeThis();
            }
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:" + this.extensionName + "."));
        }
    }

    public List<TDistributionPoint> get(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("points")) {
            return this.distributionPoints;
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:" + this.extensionName + "."));
        }
    }

    public void delete(String var1) throws TIOException {
        if(var1.equalsIgnoreCase("points")) {
            this.distributionPoints = Collections.emptyList();
            this.encodeThis();
        } else {
            throw new TIOException(TString.wrap("Attribute name [" + var1 + "] not recognized by " + "CertAttrSet:" + this.extensionName + '.'));
        }
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("points");
        return var1.elements();
    }

    private void encodeThis() throws TIOException {
        if(this.distributionPoints.isEmpty()) {
            this.extensionValue = null;
        } else {
            TDerOutputStream var1 = new TDerOutputStream();
            Iterator var2 = this.distributionPoints.iterator();

            while(var2.hasNext()) {
                TDistributionPoint var3 = (TDistributionPoint)var2.next();
                var3.encode(var1);
            }

            TDerOutputStream var4 = new TDerOutputStream();
            var4.write((byte) 48, var1);
            this.extensionValue = var4.toByteArray();
        }

    }

    public String toString() {
        return super.toString() + this.extensionName + " [\n  " + this.distributionPoints + "]\n";
    }
}
