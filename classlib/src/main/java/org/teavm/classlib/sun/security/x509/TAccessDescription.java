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
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public final class TAccessDescription {
    private int myhash = -1;
    private TObjectIdentifier accessMethod;
    private TGeneralName accessLocation;
    public static final TObjectIdentifier Ad_OCSP_Id = TObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 1});
    public static final TObjectIdentifier Ad_CAISSUERS_Id = TObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 2});
    public static final TObjectIdentifier Ad_TIMESTAMPING_Id = TObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 3});
    public static final TObjectIdentifier Ad_CAREPOSITORY_Id = TObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 5});

    public TAccessDescription(TObjectIdentifier var1, TGeneralName var2) {
        this.accessMethod = var1;
        this.accessLocation = var2;
    }

    public TAccessDescription(TDerValue var1) throws IOException {
        TDerInputStream var2 = var1.getData();
        this.accessMethod = var2.getOID();
        this.accessLocation = new TGeneralName(var2.getDerValue());
    }

    public TObjectIdentifier getAccessMethod() {
        return this.accessMethod;
    }

    public TGeneralName getAccessLocation() {
        return this.accessLocation;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        var2.putOID(this.accessMethod);
        this.accessLocation.encode(var2);
        var1.write((byte) 48, var2);
    }

    public int hashCode() {
        if(this.myhash == -1) {
            this.myhash = this.accessMethod.hashCode() + this.accessLocation.hashCode();
        }

        return this.myhash;
    }

    public boolean equals(Object var1) {
        if(var1 != null && var1 instanceof TAccessDescription) {
            TAccessDescription var2 = (TAccessDescription)var1;
            return this == var2?true:this.accessMethod.equals(var2.getAccessMethod()) && this.accessLocation.equals(var2.getAccessLocation());
        } else {
            return false;
        }
    }

    public String toString() {
        String var1 = null;
        if(this.accessMethod.equals(Ad_CAISSUERS_Id)) {
            var1 = "caIssuers";
        } else if(this.accessMethod.equals(Ad_CAREPOSITORY_Id)) {
            var1 = "caRepository";
        } else if(this.accessMethod.equals(Ad_TIMESTAMPING_Id)) {
            var1 = "timeStamping";
        } else if(this.accessMethod.equals(Ad_OCSP_Id)) {
            var1 = "ocsp";
        } else {
            var1 = this.accessMethod.toString();
        }

        return "\n   accessMethod: " + var1 + "\n   accessLocation: " + this.accessLocation.toString() + "\n";
    }
}
