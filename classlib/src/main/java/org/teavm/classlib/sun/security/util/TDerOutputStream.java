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
package org.teavm.classlib.sun.security.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.teavm.classlib.java.io.TByteArrayOutputStream;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TInteger;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.text.TSimpleDateFormat;
import org.teavm.classlib.java.util.TDate;
import org.teavm.classlib.java.util.TLocale;
import org.teavm.classlib.java.util.TTimeZone;

public class TDerOutputStream extends TByteArrayOutputStream implements TDerEncoder {
    private static TByteArrayLexOrder lexOrder = new TByteArrayLexOrder();
    private static TByteArrayTagOrder tagOrder = new TByteArrayTagOrder();

    public TDerOutputStream(int var1) {
        super(var1);
    }

    public TDerOutputStream() {
    }

    public void write(byte var1, byte[] var2) throws TIOException {
        this.write(var1);
        this.putLength(var2.length);
        this.write(var2, 0, var2.length);
    }

    public void write(byte var1, TDerOutputStream var2) throws TIOException {
        this.write(var1);
        this.putLength(var2.count);
        this.write(var2.buf, 0, var2.count);
    }

    public void writeImplicit(byte var1, TDerOutputStream var2) throws TIOException {
        this.write(var1);
        this.write(var2.buf, 1, var2.count - 1);
    }

    public void putDerValue(TDerValue var1) throws TIOException {
        var1.encode(this);
    }

    public void putBoolean(boolean var1) throws TIOException {
        this.write(1);
        this.putLength(1);
        if(var1) {
            this.write(255);
        } else {
            this.write(0);
        }

    }

    public void putEnumerated(int var1) throws TIOException {
        this.write(10);
        this.putIntegerContents(var1);
    }

    public void putInteger(TBigInteger var1) throws TIOException {
        this.write(2);
        byte[] var2 = var1.toByteArray();
        this.putLength(var2.length);
        this.write(var2, 0, var2.length);
    }

    public void putInteger(TInteger var1) throws TIOException {
        this.putInteger(var1.intValue());
    }

    public void putInteger(int var1) throws TIOException {
        this.write(2);
        this.putIntegerContents(var1);
    }

    private void putIntegerContents(int var1) throws TIOException {
        byte[] var2 = new byte[4];
        int var3 = 0;
        var2[3] = (byte)(var1 & 255);
        var2[2] = (byte)((var1 & '\uff00') >>> 8);
        var2[1] = (byte)((var1 & 16711680) >>> 16);
        var2[0] = (byte)((var1 & -16777216) >>> 24);
        int var4;
        if(var2[0] == -1) {
            for(var4 = 0; var4 < 3 && var2[var4] == -1 && (var2[var4 + 1] & 128) == 128; ++var4) {
                ++var3;
            }
        } else if(var2[0] == 0) {
            for(var4 = 0; var4 < 3 && var2[var4] == 0 && (var2[var4 + 1] & 128) == 0; ++var4) {
                ++var3;
            }
        }

        this.putLength(4 - var3);

        for(var4 = var3; var4 < 4; ++var4) {
            this.write(var2[var4]);
        }

    }

    public void putBitString(byte[] var1) throws IOException {
        this.write(3);
        this.putLength(var1.length + 1);
        this.write(0);
        this.write(var1);
    }

    public void putUnalignedBitString(TBitArray var1) throws TIOException {
        byte[] var2 = var1.toByteArray();
        this.write(3);
        this.putLength(var2.length + 1);
        this.write(var2.length * 8 - var1.length());
        this.write(var2);
    }

    public void putTruncatedUnalignedBitString(TBitArray var1) throws TIOException {
        this.putUnalignedBitString(var1.truncate());
    }

    public void putOctetString(byte[] var1) throws TIOException {
        this.write((byte) 4, (byte[])var1);
    }

    public void putNull() throws TIOException {
        this.write(5);
        this.putLength(0);
    }

    public void putOID(TObjectIdentifier var1) throws TIOException {
        var1.encode(this);
    }

    public void putSequence(TDerValue[] var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();

        for(int var3 = 0; var3 < var1.length; ++var3) {
            var1[var3].encode(var2);
        }

        this.write((byte) 48, (TDerOutputStream)var2);
    }

    public void putSet(TDerValue[] var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();

        for(int var3 = 0; var3 < var1.length; ++var3) {
            var1[var3].encode(var2);
        }

        this.write((byte) 49, (TDerOutputStream)var2);
    }

    public void putOrderedSetOf(byte var1, TDerEncoder[] var2) throws TIOException {
        this.putOrderedSet(var1, var2, lexOrder);
    }

    public void putOrderedSet(byte var1, TDerEncoder[] var2) throws TIOException {
        this.putOrderedSet(var1, var2, tagOrder);
    }

    private void putOrderedSet(byte var1, TDerEncoder[] var2, Comparator<byte[]> var3) throws TIOException {
        TDerOutputStream[] var4 = new TDerOutputStream[var2.length];

        for(int var5 = 0; var5 < var2.length; ++var5) {
            var4[var5] = new TDerOutputStream();
            var2[var5].derEncode(var4[var5]);
        }

        byte[][] var8 = new byte[var4.length][];

        for(int var6 = 0; var6 < var4.length; ++var6) {
            var8[var6] = var4[var6].toByteArray();
        }

        Arrays.sort(var8, var3);
        TDerOutputStream var9 = new TDerOutputStream();

        for(int var7 = 0; var7 < var4.length; ++var7) {
            var9.write(var8[var7]);
        }

        this.write(var1, var9);
    }

    public void putUTF8String(TString var1) throws TIOException {
        this.writeString(var1, (byte) 12, TString.wrap("UTF8"));
    }

    public void putPrintableString(TString var1) throws TIOException {
        this.writeString(var1, (byte) 19, TString.wrap("ASCII"));
    }

    public void putT61String(TString var1) throws TIOException {
        this.writeString(var1, (byte) 20, TString.wrap("ISO-8859-1"));
    }

    public void putIA5String(TString var1) throws TIOException {
        this.writeString(var1, (byte) 22, TString.wrap("ASCII"));
    }

    public void putBMPString(TString var1) throws IOException {
        this.writeString(var1, (byte) 30, TString.wrap("UnicodeBigUnmarked"));
    }

    public void putGeneralString(TString var1) throws IOException {
        this.writeString(var1, (byte) 27, TString.wrap("ASCII"));
    }

    private void writeString(TString var1, byte var2, TString var3) throws TIOException {
        byte[] var4 = var1.getBytes(var3);
        this.write(var2);
        this.putLength(var4.length);
        this.write(var4);
    }

    public void putUTCTime(TDate var1) throws TIOException {
        this.putTime(var1, (byte) 23);
    }

    public void putGeneralizedTime(TDate var1) throws TIOException {
        this.putTime(var1, (byte) 24);
    }

    private void putTime(TDate var1, byte var2) throws TIOException {
        TTimeZone var3 = TTimeZone.getTimeZone("GMT");
        String var4 = null;
        if(var2 == 23) {
            var4 = "yyMMddHHmmss\'Z\'";
        } else {
            var2 = 24;
            var4 = "yyyyMMddHHmmss\'Z\'";
        }

        TSimpleDateFormat var5 = new TSimpleDateFormat(var4, TLocale.US);
        var5.setTimeZone(var3);
        byte[] var6 = var5.format(var1).getBytes(TString.wrap("ISO-8859-1"));
        this.write(var2);
        this.putLength(var6.length);
        this.write(var6);
    }

    public void putLength(int var1) throws TIOException {
        if(var1 < 128) {
            this.write((byte)var1);
        } else if(var1 < 256) {
            this.write(-127);
            this.write((byte)var1);
        } else if(var1 < 65536) {
            this.write(-126);
            this.write((byte)(var1 >> 8));
            this.write((byte)var1);
        } else if(var1 < 16777216) {
            this.write(-125);
            this.write((byte)(var1 >> 16));
            this.write((byte)(var1 >> 8));
            this.write((byte)var1);
        } else {
            this.write(-124);
            this.write((byte)(var1 >> 24));
            this.write((byte)(var1 >> 16));
            this.write((byte)(var1 >> 8));
            this.write((byte)var1);
        }

    }

    public void putTag(byte var1, boolean var2, byte var3) {
        byte var4 = (byte)(var1 | var3);
        if(var2) {
            var4 = (byte)(var4 | 32);
        }

        this.write(var4);
    }

    public void derEncode(TOutputStream var1) throws TIOException {
        var1.write(this.toByteArray());
    }
}
