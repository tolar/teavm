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

public class TDNSName implements TGeneralNameInterface {
    private TString name;
    private static final String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String digitsAndHyphen = "0123456789-";
    private static final String alphaDigitsAndHyphen = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-";

    public TDNSName(TDerValue var1) throws TIOException {
        this.name = var1.getIA5String();
    }

    public TDNSName(TString var1) throws TIOException {
        if(var1 != null && var1.length() != 0) {
            if(var1.indexOf(32) != -1) {
                throw new TIOException(TString.wrap("DNS names or NameConstraints with blank components are not permitted"));
            } else if(var1.charAt(0) != 46 && var1.charAt(var1.length() - 1) != 46) {
                int var2;
                for(int var3 = 0; var3 < var1.length(); var3 = var2 + 1) {
                    var2 = var1.indexOf(46, var3);
                    if(var2 < 0) {
                        var2 = var1.length();
                    }

                    if(var2 - var3 < 1) {
                        throw new TIOException(TString.wrap("DNSName SubjectAltNames with empty components are not permitted"));
                    }

                    if("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(var1.charAt(var3)) < 0) {
                        throw new TIOException(TString.wrap("DNSName components must begin with a letter"));
                    }

                    for(int var4 = var3 + 1; var4 < var2; ++var4) {
                        char var5 = var1.charAt(var4);
                        if("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-".indexOf(var5) < 0) {
                            throw new TIOException(TString.wrap("DNSName components must consist of letters, digits, and hyphens"));
                        }
                    }
                }

                this.name = var1;
            } else {
                throw new TIOException(TString.wrap("DNS names or NameConstraints may not begin or end with a ."));
            }
        } else {
            throw new TIOException(TString.wrap("DNS name must not be null"));
        }
    }

    public int getType() {
        return 2;
    }

    public TString getName() {
        return this.name;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        var1.putIA5String(this.name);
    }

    public String toString() {
        return "DNSName: " + this.name;
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof sun.security.x509.DNSName)) {
            return false;
        } else {
            TDNSName var2 = (TDNSName)var1;
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
        } else if(var1.getType() != 2) {
            var2 = -1;
        } else {
            TString var3 = ((TDNSName)var1).getName().toLowerCase(Locale.ENGLISH);
            TString var4 = this.name.toLowerCase(Locale.ENGLISH);
            if(var3.equals(var4)) {
                var2 = 0;
            } else {
                int var5;
                if(var4.endsWith(var3)) {
                    var5 = var4.lastIndexOf(var3);
                    if(var4.charAt(var5 - 1) == 46) {
                        var2 = 2;
                    } else {
                        var2 = 3;
                    }
                } else if(var3.endsWith(var4)) {
                    var5 = var3.lastIndexOf(var4);
                    if(var3.charAt(var5 - 1) == 46) {
                        var2 = 1;
                    } else {
                        var2 = 3;
                    }
                } else {
                    var2 = 3;
                }
            }
        }

        return var2;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        int var1 = 1;

        for(int var2 = this.name.indexOf(46); var2 >= 0; var2 = this.name.indexOf(46, var2 + 1)) {
            ++var1;
        }

        return var1;
    }
}
