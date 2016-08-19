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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TArrays;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

/**
 * Created by vasek on 18. 8. 2016.
 */
public class TRDN {
    final TAVA[] assertion;
    private volatile List<TAVA> avaList;
    private volatile String canonicalString;

    public TRDN(String var1) throws IOException {
        this(var1, Collections.emptyMap());
    }

    public TRDN(String var1, Map<TString, TString> var2) throws IOException {
        int var3 = 0;
        int var4 = 0;
        int var5 = 0;
        ArrayList var6 = new ArrayList(3);

        String var8;
        TAVA var9;
        for(int var7 = var1.indexOf(43); var7 >= 0; var7 = var1.indexOf(43, var4)) {
            var3 += TX500Name.countQuotes(var1, var4, var7);
            if(var7 > 0 && var1.charAt(var7 - 1) != 92 && var3 != 1) {
                var8 = var1.substring(var5, var7);
                if(var8.length() == 0) {
                    throw new IOException("empty TAVA in TRDN \"" + var1 + "\"");
                }

                var9 = new TAVA(new StringReader(var8), var2);
                var6.add(var9);
                var5 = var7 + 1;
                var3 = 0;
            }

            var4 = var7 + 1;
        }

        var8 = var1.substring(var5);
        if(var8.length() == 0) {
            throw new IOException("empty TAVA in TRDN \"" + var1 + "\"");
        } else {
            var9 = new TAVA(new StringReader(var8), var2);
            var6.add(var9);
            this.assertion = (TAVA[])var6.toArray(new TAVA[var6.size()]);
        }
    }

    TRDN(String var1, String var2) throws IOException {
        this(var1, var2, Collections.emptyMap());
    }

    TRDN(String var1, String var2, Map<TString, TString> var3) throws IOException {
        if(!var2.equalsIgnoreCase("RFC2253")) {
            throw new IOException("Unsupported format " + var2);
        } else {
            boolean var4 = false;
            int var5 = 0;
            ArrayList var6 = new ArrayList(3);

            String var8;
            TAVA var9;
            int var10;
            for(int var7 = var1.indexOf(43); var7 >= 0; var7 = var1.indexOf(43, var10)) {
                if(var7 > 0 && var1.charAt(var7 - 1) != 92) {
                    var8 = var1.substring(var5, var7);
                    if(var8.length() == 0) {
                        throw new IOException("empty TAVA in TRDN \"" + var1 + "\"");
                    }

                    var9 = new TAVA(new StringReader(var8), 3, var3);
                    var6.add(var9);
                    var5 = var7 + 1;
                }

                var10 = var7 + 1;
            }

            var8 = var1.substring(var5);
            if(var8.length() == 0) {
                throw new IOException("empty TAVA in TRDN \"" + var1 + "\"");
            } else {
                var9 = new TAVA(new StringReader(var8), 3, var3);
                var6.add(var9);
                this.assertion = (TAVA[])var6.toArray(new TAVA[var6.size()]);
            }
        }
    }

    TRDN(TDerValue var1) throws TIOException {
        if(var1.tag != 49) {
            throw new TIOException(TString.wrap("X500 TRDN"));
        } else {
            TDerInputStream var2 = new TDerInputStream(var1.toByteArray());
            TDerValue[] var3 = var2.getSet(5);
            this.assertion = new TAVA[var3.length];

            for(int var4 = 0; var4 < var3.length; ++var4) {
                this.assertion[var4] = new TAVA(var3[var4]);
            }

        }
    }

    TRDN(int var1) {
        this.assertion = new TAVA[var1];
    }

    public TRDN(TAVA var1) {
        if(var1 == null) {
            throw new NullPointerException();
        } else {
            this.assertion = new TAVA[]{var1};
        }
    }

    public TRDN(TAVA[] var1) {
        this.assertion = (TAVA[])var1.clone();

        for(int var2 = 0; var2 < this.assertion.length; ++var2) {
            if(this.assertion[var2] == null) {
                throw new NullPointerException();
            }
        }

    }

    public List<TAVA> avas() {
        List var1 = this.avaList;
        if(var1 == null) {
            var1 = Collections.unmodifiableList(Arrays.asList(this.assertion));
            this.avaList = var1;
        }

        return var1;
    }

    public int size() {
        return this.assertion.length;
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof sun.security.x509.RDN)) {
            return false;
        } else {
            TRDN var2 = (TRDN)var1;
            if(this.assertion.length != var2.assertion.length) {
                return false;
            } else {
                String var3 = this.toRFC2253String(true);
                String var4 = var2.toRFC2253String(true);
                return var3.equals(var4);
            }
        }
    }

    public int hashCode() {
        return this.toRFC2253String(true).hashCode();
    }

    TDerValue findAttribute(TObjectIdentifier var1) {
        for(int var2 = 0; var2 < this.assertion.length; ++var2) {
            if(this.assertion[var2].oid.equals(var1)) {
                return this.assertion[var2].value;
            }
        }

        return null;
    }

    void encode(TDerOutputStream var1) throws TIOException {
        var1.putOrderedSetOf((byte) 49, this.assertion);
    }

    public String toString() {
        if(this.assertion.length == 1) {
            return this.assertion[0].toString();
        } else {
            StringBuilder var1 = new StringBuilder();

            for(int var2 = 0; var2 < this.assertion.length; ++var2) {
                if(var2 != 0) {
                    var1.append(" + ");
                }

                var1.append(this.assertion[var2].toString());
            }

            return var1.toString();
        }
    }

    public String toRFC1779String() {
        return this.toRFC1779String(Collections.emptyMap());
    }

    public String toRFC1779String(Map<String, String> var1) {
        if(this.assertion.length == 1) {
            return this.assertion[0].toRFC1779String(var1);
        } else {
            StringBuilder var2 = new StringBuilder();

            for(int var3 = 0; var3 < this.assertion.length; ++var3) {
                if(var3 != 0) {
                    var2.append(" + ");
                }

                var2.append(this.assertion[var3].toRFC1779String(var1));
            }

            return var2.toString();
        }
    }

    public String toRFC2253String() {
        return this.toRFC2253StringInternal(false, Collections.emptyMap());
    }

    public String toRFC2253String(Map<String, String> var1) {
        return this.toRFC2253StringInternal(false, var1);
    }

    public String toRFC2253String(boolean var1) {
        if(!var1) {
            return this.toRFC2253StringInternal(false, Collections.emptyMap());
        } else {
            String var2 = this.canonicalString;
            if(var2 == null) {
                var2 = this.toRFC2253StringInternal(true, Collections.emptyMap());
                this.canonicalString = var2;
            }

            return var2;
        }
    }

    private String toRFC2253StringInternal(boolean var1, Map<String, String> var2) {
        if(this.assertion.length == 1) {
            return var1?this.assertion[0].toRFC2253CanonicalString():this.assertion[0].toRFC2253String(var2);
        } else {
            TAVA[] var3 = this.assertion;
            if(var1) {
                var3 = (TAVA[])this.assertion.clone();
                TArrays.sort(var3, TAVAComparator.getInstance());
            }

            StringJoiner var4 = new StringJoiner("+");
            TAVA[] var5 = var3;
            int var6 = var3.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                TAVA var8 = var5[var7];
                var4.add(var1?var8.toRFC2253CanonicalString():var8.toRFC2253String(var2));
            }

            return var4.toString();
        }
    }
}
