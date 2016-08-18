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
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TCertificateAlgorithmId implements TCertAttrSet<String> {
    private TAlgorithmId algId;
    public static final String IDENT = "x509.info.algorithmID";
    public static final String NAME = "algorithmID";
    public static final String ALGORITHM = "algorithm";

    public TCertificateAlgorithmId(TAlgorithmId var1) {
        this.algId = var1;
    }

    public TCertificateAlgorithmId(TDerInputStream var1) throws IOException {
        TDerValue var2 = var1.getDerValue();
        this.algId = TAlgorithmId.parse(var2);
    }

    public TCertificateAlgorithmId(TInputStream var1) throws IOException {
        TDerValue var2 = new TDerValue(var1);
        this.algId = TAlgorithmId.parse(var2);
    }

    public String toString() {
        return this.algId == null?"":this.algId.toString() + ", OID = " + this.algId.getOID().toString() + "\n";
    }

    public void encode(OutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        this.algId.encode(var2);
        var1.write(var2.toByteArray());
    }

    public void set(String var1, Object var2) throws IOException {
        if(!(var2 instanceof TAlgorithmId)) {
            throw new IOException("Attribute must be of type AlgorithmId.");
        } else if(var1.equalsIgnoreCase("algorithm")) {
            this.algId = (TAlgorithmId)var2;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:TCertificateAlgorithmId.");
        }
    }

    public TAlgorithmId get(String var1) throws IOException {
        if(var1.equalsIgnoreCase("algorithm")) {
            return this.algId;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:TCertificateAlgorithmId.");
        }
    }

    public void delete(String var1) throws IOException {
        if(var1.equalsIgnoreCase("algorithm")) {
            this.algId = null;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:TCertificateAlgorithmId.");
        }
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();
        var1.addElement("algorithm");
        return var1.elements();
    }

    public String getName() {
        return "algorithmID";
    }
}
