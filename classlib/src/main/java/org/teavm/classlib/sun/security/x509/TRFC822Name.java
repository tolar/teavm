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

import java.util.Locale;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TRFC822Name implements TGeneralNameInterface {
    private TString name;

    public TRFC822Name(TDerValue var1) throws TIOException {
        this.name = var1.getIA5String();
        this.parseName(this.name);
    }

    public TRFC822Name(TString var1) throws TIOException {
        this.parseName(var1);
        this.name = var1;
    }

    public void parseName(TString var1) throws TIOException {
        if(var1 != null && var1.length() != 0) {
            TString var2 = var1.substring(var1.indexOf(64) + 1);
            if(var2.length() == 0) {
                throw new TIOException(TString.wrap("RFC822Name may not end with @"));
            } else if(var2.startsWith(TString.wrap(".")) && var2.length() == 1) {
                throw new TIOException(TString.wrap("RFC822Name domain may not be just ."));
            }
        } else {
            throw new TIOException(TString.wrap("RFC822Name may not be null or empty"));
        }
    }

    public int getType() {
        return 1;
    }

    public TString getName() {
        return this.name;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        var1.putIA5String(this.name);
    }

    public String toString() {
        return "RFC822Name: " + this.name;
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof sun.security.x509.RFC822Name)) {
            return false;
        } else {
            TRFC822Name var2 = (TRFC822Name)var1;
            return this.name.equalsIgnoreCase(var2.name);
        }
    }

    public int hashCode() {
        return this.name.toUpperCase(Locale.ENGLISH).hashCode();
    }

    public int constrains(TGeneralNameInterface var1) throws UnsupportedOperationException {
        byte var2;
        if(var1 == null) {
            var2 = -1;
        } else if(var1.getType() != 1) {
            var2 = -1;
        } else {
            TString var3 = ((TRFC822Name)var1).getName().toLowerCase(Locale.ENGLISH);
            TString var4 = this.name.toLowerCase(Locale.ENGLISH);
            if(var3.equals(var4)) {
                var2 = 0;
            } else {
                int var5;
                if(var4.endsWith(var3)) {
                    if(var3.indexOf(64) != -1) {
                        var2 = 3;
                    } else if(var3.startsWith(TString.wrap("."))) {
                        var2 = 2;
                    } else {
                        var5 = var4.lastIndexOf(var3);
                        if(var4.charAt(var5 - 1) == 64) {
                            var2 = 2;
                        } else {
                            var2 = 3;
                        }
                    }
                } else if(var3.endsWith(var4)) {
                    if(var4.indexOf(64) != -1) {
                        var2 = 3;
                    } else if(var4.startsWith(TString.wrap("."))) {
                        var2 = 1;
                    } else {
                        var5 = var3.lastIndexOf(var4);
                        if(var3.charAt(var5 - 1) == 64) {
                            var2 = 1;
                        } else {
                            var2 = 3;
                        }
                    }
                } else {
                    var2 = 3;
                }
            }
        }

        return var2;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        TString var1 = this.name;
        int var2 = 1;
        int var3 = var1.lastIndexOf(64);
        if(var3 >= 0) {
            ++var2;
            var1 = var1.substring(var3 + 1);
        }

        while(var1.lastIndexOf(46) >= 0) {
            var1 = var1.substring(0, var1.lastIndexOf(46));
            ++var2;
        }

        return var2;
    }
}
