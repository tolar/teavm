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
package org.teavm.classlib.javax.crypto;

import java.security.spec.AlgorithmParameterSpec;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TPermission;
import org.teavm.classlib.java.security.spec.TAlgorithmParameterSpec;

class TCryptoPermission extends TPermission {
    private static final long serialVersionUID = 8987399626114087514L;
    private TString alg;
    private int maxKeySize = 2147483647;
    private TString exemptionMechanism = null;
    private TAlgorithmParameterSpec algParamSpec = null;
    private boolean checkParam = false;
    static final String ALG_NAME_WILDCARD = "*";

    TCryptoPermission(TString var1) {
        super((String)null);
        this.alg = var1;
    }

    TCryptoPermission(TString var1, int var2) {
        super((String)null);
        this.alg = var1;
        this.maxKeySize = var2;
    }

    TCryptoPermission(TString var1, int var2, TAlgorithmParameterSpec var3) {
        super((String)null);
        this.alg = var1;
        this.maxKeySize = var2;
        this.checkParam = true;
        this.algParamSpec = var3;
    }

    TCryptoPermission(TString var1, TString var2) {
        super((String)null);
        this.alg = var1;
        this.exemptionMechanism = var2;
    }

    TCryptoPermission(TString var1, int var2, TString var3) {
        super((String)null);
        this.alg = var1;
        this.exemptionMechanism = var3;
        this.maxKeySize = var2;
    }

    TCryptoPermission(TString var1, int var2, TAlgorithmParameterSpec var3, TString var4) {
        super((String)null);
        this.alg = var1;
        this.exemptionMechanism = var4;
        this.maxKeySize = var2;
        this.checkParam = true;
        this.algParamSpec = var3;
    }

    public boolean implies(TPermission var1) {
        if(!(var1 instanceof TCryptoPermission)) {
            return false;
        } else {
            TCryptoPermission var2 = (TCryptoPermission)var1;
            if(!this.alg.equalsIgnoreCase(var2.alg) && !this.alg.equalsIgnoreCase(TString.wrap("*"))) {
                return false;
            } else {
                if(var2.maxKeySize <= this.maxKeySize) {
                    if(!this.impliesParameterSpec(var2.checkParam, var2.algParamSpec)) {
                        return false;
                    }

                    if(this.impliesExemptionMechanism(var2.exemptionMechanism)) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public boolean equals(Object var1) {
        if(var1 == this) {
            return true;
        } else if(!(var1 instanceof javax.crypto.CryptoPermission)) {
            return false;
        } else {
            javax.crypto.CryptoPermission var2 = (javax.crypto.CryptoPermission)var1;
            return this.alg.equalsIgnoreCase(var2.alg) && this.maxKeySize == var2.maxKeySize?(this.checkParam != var2.checkParam?false:this.equalObjects(this.exemptionMechanism, var2.exemptionMechanism) && this.equalObjects(this.algParamSpec, var2.algParamSpec)):false;
        }
    }

    public int hashCode() {
        int var1 = this.alg.hashCode();
        var1 ^= this.maxKeySize;
        if(this.exemptionMechanism != null) {
            var1 ^= this.exemptionMechanism.hashCode();
        }

        if(this.checkParam) {
            var1 ^= 100;
        }

        if(this.algParamSpec != null) {
            var1 ^= this.algParamSpec.hashCode();
        }

        return var1;
    }

    public String getActions() {
        return null;
    }

    public PermissionCollection newPermissionCollection() {
        return new CryptoPermissionCollection();
    }

    final String getAlgorithm() {
        return this.alg;
    }

    final String getExemptionMechanism() {
        return this.exemptionMechanism;
    }

    final int getMaxKeySize() {
        return this.maxKeySize;
    }

    final boolean getCheckParam() {
        return this.checkParam;
    }

    final AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return this.algParamSpec;
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder(100);
        var1.append("(TCryptoPermission " + this.alg + " " + this.maxKeySize);
        if(this.algParamSpec != null) {
            if(this.algParamSpec instanceof RC2ParameterSpec) {
                var1.append(" , effective " + ((RC2ParameterSpec)this.algParamSpec).getEffectiveKeyBits());
            } else if(this.algParamSpec instanceof RC5ParameterSpec) {
                var1.append(" , rounds " + ((RC5ParameterSpec)this.algParamSpec).getRounds());
            }
        }

        if(this.exemptionMechanism != null) {
            var1.append(" " + this.exemptionMechanism);
        }

        var1.append(")");
        return var1.toString();
    }

    private boolean impliesExemptionMechanism(TString var1) {
        return this.exemptionMechanism == null?true:(var1 == null?false:this.exemptionMechanism.equals(var1));
    }

    private boolean impliesParameterSpec(boolean var1, TAlgorithmParameterSpec var2) {
        return this.checkParam && var1?(var2 == null?true:(this.algParamSpec == null?false:(this.algParamSpec.getClass() != var2.getClass()?false:(var2 instanceof RC2ParameterSpec && ((RC2ParameterSpec)var2).getEffectiveKeyBits() <= ((RC2ParameterSpec)((RC2ParameterSpec)this.algParamSpec)).getEffectiveKeyBits()?true:(var2 instanceof RC5ParameterSpec && ((RC5ParameterSpec)var2).getRounds() <= ((RC5ParameterSpec)this.algParamSpec).getRounds()?true:(var2 instanceof PBEParameterSpec
                && ((PBEParameterSpec)var2).getIterationCount() <= ((PBEParameterSpec)this.algParamSpec).getIterationCount()?true:this.algParamSpec.equals(var2))))))):!this.checkParam;
    }

    private boolean equalObjects(Object var1, Object var2) {
        return var1 == null?var2 == null:var1.equals(var2);
    }
}
