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
import java.util.Objects;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TDistributionPointName {
    private static final byte TAG_FULL_NAME = 0;
    private static final byte TAG_RELATIVE_NAME = 1;
    private TGeneralNames fullName = null;
    private TRDN relativeName = null;
    private volatile int hashCode;

    public TDistributionPointName(TGeneralNames var1) {
        if(var1 == null) {
            throw new IllegalArgumentException("fullName must not be null");
        } else {
            this.fullName = var1;
        }
    }

    public TDistributionPointName(TRDN var1) {
        if(var1 == null) {
            throw new IllegalArgumentException("relativeName must not be null");
        } else {
            this.relativeName = var1;
        }
    }

    public TDistributionPointName(TDerValue var1) throws IOException {
        if(var1.isContextSpecific((byte)0) && var1.isConstructed()) {
            var1.resetTag((byte)48);
            this.fullName = new TGeneralNames(var1);
        } else {
            if(!var1.isContextSpecific((byte)1) || !var1.isConstructed()) {
                throw new IOException("Invalid encoding for DistributionPointName");
            }

            var1.resetTag((byte)49);
            this.relativeName = new TRDN(var1);
        }

    }

    public TGeneralNames getFullName() {
        return this.fullName;
    }

    public TRDN getRelativeName() {
        return this.relativeName;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        if(this.fullName != null) {
            this.fullName.encode(var2);
            var1.writeImplicit(TDerValue.createTag((byte)-128, true, (byte)0), var2);
        } else {
            this.relativeName.encode(var2);
            var1.writeImplicit(TDerValue.createTag((byte)-128, true, (byte)1), var2);
        }

    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TDistributionPointName)) {
            return false;
        } else {
            TDistributionPointName var2 = (TDistributionPointName)var1;
            return Objects.equals(this.fullName, var2.fullName) && Objects.equals(this.relativeName, var2.relativeName);
        }
    }

    public int hashCode() {
        int var1 = this.hashCode;
        if(var1 == 0) {
            byte var2 = 1;
            if(this.fullName != null) {
                var1 = var2 + this.fullName.hashCode();
            } else {
                var1 = var2 + this.relativeName.hashCode();
            }

            this.hashCode = var1;
        }

        return var1;
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        if(this.fullName != null) {
            var1.append("DistributionPointName:\n     " + this.fullName + "\n");
        } else {
            var1.append("DistributionPointName:\n     " + this.relativeName + "\n");
        }

        return var1.toString();
    }
}
