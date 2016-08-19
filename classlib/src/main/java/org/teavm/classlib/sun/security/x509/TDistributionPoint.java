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
import java.util.Arrays;
import java.util.Objects;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.sun.security.util.TBitArray;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;


public class TDistributionPoint {
    public static final int KEY_COMPROMISE = 1;
    public static final int CA_COMPROMISE = 2;
    public static final int AFFILIATION_CHANGED = 3;
    public static final int SUPERSEDED = 4;
    public static final int CESSATION_OF_OPERATION = 5;
    public static final int CERTIFICATE_HOLD = 6;
    public static final int PRIVILEGE_WITHDRAWN = 7;
    public static final int AA_COMPROMISE = 8;
    private static final String[] REASON_STRINGS = new String[]{null, "key compromise", "CA compromise", "affiliation changed", "superseded", "cessation of operation", "certificate hold", "privilege withdrawn", "AA compromise"};
    private static final byte TAG_DIST_PT = 0;
    private static final byte TAG_REASONS = 1;
    private static final byte TAG_ISSUER = 2;
    private static final byte TAG_FULL_NAME = 0;
    private static final byte TAG_REL_NAME = 1;
    private TGeneralNames fullName;
    private TRDN relativeName;
    private boolean[] reasonFlags;
    private TGeneralNames crlIssuer;
    private volatile int hashCode;

    public TDistributionPoint(TGeneralNames var1, boolean[] var2, TGeneralNames var3) {
        if(var1 == null && var3 == null) {
            throw new IllegalArgumentException("fullName and crlIssuer may not both be null");
        } else {
            this.fullName = var1;
            this.reasonFlags = var2;
            this.crlIssuer = var3;
        }
    }

    public TDistributionPoint(TRDN var1, boolean[] var2, TGeneralNames var3) {
        if(var1 == null && var3 == null) {
            throw new IllegalArgumentException("relativeName and crlIssuer may not both be null");
        } else {
            this.relativeName = var1;
            this.reasonFlags = var2;
            this.crlIssuer = var3;
        }
    }

    public TDistributionPoint(TDerValue var1) throws IOException {
        if(var1.tag != 48) {
            throw new IOException("Invalid encoding of DistributionPoint.");
        } else {
            while(true) {
                if(var1.data != null && var1.data.available() != 0) {
                    TDerValue var2 = var1.data.getDerValue();
                    if(var2.isContextSpecific((byte) 0) && var2.isConstructed()) {
                        if(this.fullName == null && this.relativeName == null) {
                            TDerValue var3 = var2.data.getDerValue();
                            if(var3.isContextSpecific((byte) 0) && var3.isConstructed()) {
                                var3.resetTag((byte) 48);
                                this.fullName = new TGeneralNames(var3);
                                continue;
                            }

                            if(var3.isContextSpecific((byte) 1) && var3.isConstructed()) {
                                var3.resetTag((byte) 49);
                                this.relativeName = new TRDN(var3);
                                continue;
                            }

                            throw new IOException("Invalid DistributionPointName in DistributionPoint");
                        }

                        throw new IOException("Duplicate DistributionPointName in DistributionPoint.");
                    }

                    if(var2.isContextSpecific((byte) 1) && !var2.isConstructed()) {
                        if(this.reasonFlags != null) {
                            throw new IOException("Duplicate Reasons in DistributionPoint.");
                        }

                        var2.resetTag((byte) 3);
                        this.reasonFlags = var2.getUnalignedBitString().toBooleanArray();
                        continue;
                    }

                    if(var2.isContextSpecific((byte) 2) && var2.isConstructed()) {
                        if(this.crlIssuer != null) {
                            throw new IOException("Duplicate CRLIssuer in DistributionPoint.");
                        }

                        var2.resetTag((byte) 48);
                        this.crlIssuer = new TGeneralNames(var2);
                        continue;
                    }

                    throw new IOException("Invalid encoding of DistributionPoint.");
                }

                if(this.crlIssuer == null && this.fullName == null && this.relativeName == null) {
                    throw new IOException("One of fullName, relativeName,  and crlIssuer has to be set");
                }

                return;
            }
        }
    }

    public TGeneralNames getFullName() {
        return this.fullName;
    }

    public TRDN getRelativeName() {
        return this.relativeName;
    }

    public boolean[] getReasonFlags() {
        return this.reasonFlags;
    }

    public TGeneralNames getCRLIssuer() {
        return this.crlIssuer;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        TDerOutputStream var3;
        if(this.fullName != null || this.relativeName != null) {
            var3 = new TDerOutputStream();
            TDerOutputStream var4;
            if(this.fullName != null) {
                var4 = new TDerOutputStream();
                this.fullName.encode(var4);
                var3.writeImplicit(TDerValue.createTag((byte)-128, true, (byte) 0), var4);
            } else if(this.relativeName != null) {
                var4 = new TDerOutputStream();
                this.relativeName.encode(var4);
                var3.writeImplicit(TDerValue.createTag((byte)-128, true, (byte) 1), var4);
            }

            var2.write(TDerValue.createTag((byte)-128, true, (byte) 0), var3);
        }

        if(this.reasonFlags != null) {
            var3 = new TDerOutputStream();
            TBitArray var5 = new TBitArray(this.reasonFlags);
            var3.putTruncatedUnalignedBitString(var5);
            var2.writeImplicit(TDerValue.createTag((byte)-128, false, (byte) 1), var3);
        }

        if(this.crlIssuer != null) {
            var3 = new TDerOutputStream();
            this.crlIssuer.encode(var3);
            var2.writeImplicit(TDerValue.createTag((byte)-128, true, (byte) 2), var3);
        }

        var1.write((byte) 48, var2);
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TDistributionPoint)) {
            return false;
        } else {
            TDistributionPoint var2 = (TDistributionPoint)var1;
            boolean var3 = Objects.equals(this.fullName, var2.fullName) && Objects.equals(this.relativeName, var2.relativeName) && Objects.equals(this.crlIssuer, var2.crlIssuer) && Arrays
                    .equals(this.reasonFlags, var2.reasonFlags);
            return var3;
        }
    }

    public int hashCode() {
        int var1 = this.hashCode;
        if(var1 == 0) {
            var1 = 1;
            if(this.fullName != null) {
                var1 += this.fullName.hashCode();
            }

            if(this.relativeName != null) {
                var1 += this.relativeName.hashCode();
            }

            if(this.crlIssuer != null) {
                var1 += this.crlIssuer.hashCode();
            }

            if(this.reasonFlags != null) {
                for(int var2 = 0; var2 < this.reasonFlags.length; ++var2) {
                    if(this.reasonFlags[var2]) {
                        var1 += var2;
                    }
                }
            }

            this.hashCode = var1;
        }

        return var1;
    }

    private static String reasonToString(int var0) {
        return var0 > 0 && var0 < REASON_STRINGS.length?REASON_STRINGS[var0]:"Unknown reason " + var0;
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        if(this.fullName != null) {
            var1.append("DistributionPoint:\n     " + this.fullName + "\n");
        }

        if(this.relativeName != null) {
            var1.append("DistributionPoint:\n     " + this.relativeName + "\n");
        }

        if(this.reasonFlags != null) {
            var1.append("   ReasonFlags:\n");

            for(int var2 = 0; var2 < this.reasonFlags.length; ++var2) {
                if(this.reasonFlags[var2]) {
                    var1.append("    " + reasonToString(var2) + "\n");
                }
            }
        }

        if(this.crlIssuer != null) {
            var1.append("   CRLIssuer:" + this.crlIssuer + "\n");
        }

        return var1.toString();
    }
}
