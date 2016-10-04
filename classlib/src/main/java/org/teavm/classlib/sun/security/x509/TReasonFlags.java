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
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.sun.security.util.TBitArray;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TReasonFlags {
    public static final String UNUSED = "unused";
    public static final String KEY_COMPROMISE = "key_compromise";
    public static final String CA_COMPROMISE = "ca_compromise";
    public static final String AFFILIATION_CHANGED = "affiliation_changed";
    public static final String SUPERSEDED = "superseded";
    public static final String CESSATION_OF_OPERATION = "cessation_of_operation";
    public static final String CERTIFICATE_HOLD = "certificate_hold";
    public static final String PRIVILEGE_WITHDRAWN = "privilege_withdrawn";
    public static final String AA_COMPROMISE = "aa_compromise";
    private static final String[] NAMES = new String[]{"unused", "key_compromise", "ca_compromise", "affiliation_changed", "superseded", "cessation_of_operation", "certificate_hold", "privilege_withdrawn", "aa_compromise"};
    private boolean[] bitString;

    private static int name2Index(String var0) throws IOException {
        for(int var1 = 0; var1 < NAMES.length; ++var1) {
            if(NAMES[var1].equalsIgnoreCase(var0)) {
                return var1;
            }
        }

        throw new IOException("Name not recognized by ReasonFlags");
    }

    private boolean isSet(int var1) {
        return var1 < this.bitString.length && this.bitString[var1];
    }

    private void set(int var1, boolean var2) {
        if(var1 >= this.bitString.length) {
            boolean[] var3 = new boolean[var1 + 1];
            System.arraycopy(this.bitString, 0, var3, 0, this.bitString.length);
            this.bitString = var3;
        }

        this.bitString[var1] = var2;
    }

    public TReasonFlags(byte[] var1) {
        this.bitString = (new TBitArray(var1.length * 8, var1)).toBooleanArray();
    }

    public TReasonFlags(boolean[] var1) {
        this.bitString = var1;
    }

    public TReasonFlags(TBitArray var1) {
        this.bitString = var1.toBooleanArray();
    }

    public TReasonFlags(TDerInputStream var1) throws IOException {
        TDerValue var2 = var1.getDerValue();
        this.bitString = var2.getUnalignedBitString(true).toBooleanArray();
    }

    public TReasonFlags(TDerValue var1) throws IOException {
        this.bitString = var1.getUnalignedBitString(true).toBooleanArray();
    }

    public boolean[] getFlags() {
        return this.bitString;
    }

    public void set(String var1, Object var2) throws IOException {
        if(!(var2 instanceof Boolean)) {
            throw new IOException("Attribute must be of type Boolean.");
        } else {
            boolean var3 = ((Boolean)var2).booleanValue();
            this.set(name2Index(var1), var3);
        }
    }

    public Object get(String var1) throws IOException {
        return Boolean.valueOf(this.isSet(name2Index(var1)));
    }

    public void delete(String var1) throws IOException {
        this.set(var1, Boolean.FALSE);
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder("Reason Flags [\n");
        if(this.isSet(0)) {
            var1.append("  Unused\n");
        }

        if(this.isSet(1)) {
            var1.append("  Key Compromise\n");
        }

        if(this.isSet(2)) {
            var1.append("  CA Compromise\n");
        }

        if(this.isSet(3)) {
            var1.append("  Affiliation_Changed\n");
        }

        if(this.isSet(4)) {
            var1.append("  Superseded\n");
        }

        if(this.isSet(5)) {
            var1.append("  Cessation Of Operation\n");
        }

        if(this.isSet(6)) {
            var1.append("  Certificate Hold\n");
        }

        if(this.isSet(7)) {
            var1.append("  Privilege Withdrawn\n");
        }

        if(this.isSet(8)) {
            var1.append("  AA Compromise\n");
        }

        var1.append("]\n");
        return var1.toString();
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        var1.putTruncatedUnalignedBitString(new TBitArray(this.bitString));
    }

    public TEnumeration<String> getElements() {
        TAttributeNameEnumeration var1 = new TAttributeNameEnumeration();

        for(int var2 = 0; var2 < NAMES.length; ++var2) {
            var1.addElement(NAMES[var2]);
        }

        return var1.elements();
    }
}
