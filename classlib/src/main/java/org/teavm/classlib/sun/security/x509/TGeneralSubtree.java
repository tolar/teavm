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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TGeneralSubtree {
    private static final byte TAG_MIN = 0;
    private static final byte TAG_MAX = 1;
    private static final int MIN_DEFAULT = 0;
    private TGeneralName name;
    private int minimum = 0;
    private int maximum = -1;
    private int myhash = -1;

    public TGeneralSubtree(TGeneralName var1, int var2, int var3) {
        this.name = var1;
        this.minimum = var2;
        this.maximum = var3;
    }

    public TGeneralSubtree(TDerValue var1) throws TIOException {
        if(var1.tag != 48) {
            throw new TIOException(TString.wrap("Invalid encoding for GeneralSubtree."));
        } else {
            this.name = new TGeneralName(var1.data.getDerValue(), true);

            while(true) {
                while(var1.data.available() != 0) {
                    TDerValue var2 = var1.data.getDerValue();
                    if(!var2.isContextSpecific((byte) 0) || var2.isConstructed()) {
                        if(!var2.isContextSpecific((byte) 1) || var2.isConstructed()) {
                            throw new TIOException(TString.wrap("Invalid encoding of GeneralSubtree."));
                        }

                        var2.resetTag((byte) 2);
                        this.maximum = var2.getInteger();
                    } else {
                        var2.resetTag((byte) 2);
                        this.minimum = var2.getInteger();
                    }
                }

                return;
            }
        }
    }

    public TGeneralName getName() {
        return this.name;
    }

    public int getMinimum() {
        return this.minimum;
    }

    public int getMaximum() {
        return this.maximum;
    }

    public String toString() {
        String var1 = "\n   GeneralSubtree: [\n    GeneralName: " + (this.name == null?"":this.name.toString()) + "\n    Minimum: " + this.minimum;
        if(this.maximum == -1) {
            var1 = var1 + "\t    Maximum: undefined";
        } else {
            var1 = var1 + "\t    Maximum: " + this.maximum;
        }

        var1 = var1 + "    ]\n";
        return var1;
    }

    public boolean equals(Object var1) {
        if(!(var1 instanceof TGeneralSubtree)) {
            return false;
        } else {
            TGeneralSubtree var2 = (TGeneralSubtree)var1;
            if(this.name == null) {
                if(var2.name != null) {
                    return false;
                }
            } else if(!this.name.equals(var2.name)) {
                return false;
            }

            return this.minimum != var2.minimum?false:this.maximum == var2.maximum;
        }
    }

    public int hashCode() {
        if(this.myhash == -1) {
            this.myhash = 17;
            if(this.name != null) {
                this.myhash = 37 * this.myhash + this.name.hashCode();
            }

            if(this.minimum != 0) {
                this.myhash = 37 * this.myhash + this.minimum;
            }

            if(this.maximum != -1) {
                this.myhash = 37 * this.myhash + this.maximum;
            }
        }

        return this.myhash;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        this.name.encode(var2);
        TDerOutputStream var3;
        if(this.minimum != 0) {
            var3 = new TDerOutputStream();
            var3.putInteger(this.minimum);
            var2.writeImplicit(TDerValue.createTag((byte)-128, false, (byte) 0), var3);
        }

        if(this.maximum != -1) {
            var3 = new TDerOutputStream();
            var3.putInteger(this.maximum);
            var2.writeImplicit(TDerValue.createTag((byte)-128, false, (byte) 1), var3);
        }

        var1.write((byte) 48, var2);
    }
}
