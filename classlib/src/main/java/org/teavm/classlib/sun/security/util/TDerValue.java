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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;

import sun.misc.IOUtils;
import sun.security.util.BitArray;
import sun.security.util.DerInputStream;

public class TDerValue {
    public static final byte TAG_UNIVERSAL = 0;
    public static final byte TAG_APPLICATION = 64;
    public static final byte TAG_CONTEXT = -128;
    public static final byte TAG_PRIVATE = -64;
    public byte tag;
    protected TDerInputBuffer buffer;
    public final TDerInputStream data;
    private int length;
    public static final byte tag_Boolean = 1;
    public static final byte tag_Integer = 2;
    public static final byte tag_BitString = 3;
    public static final byte tag_OctetString = 4;
    public static final byte tag_Null = 5;
    public static final byte tag_ObjectId = 6;
    public static final byte tag_Enumerated = 10;
    public static final byte tag_UTF8String = 12;
    public static final byte tag_PrintableString = 19;
    public static final byte tag_T61String = 20;
    public static final byte tag_IA5String = 22;
    public static final byte tag_UtcTime = 23;
    public static final byte tag_GeneralizedTime = 24;
    public static final byte tag_GeneralString = 27;
    public static final byte tag_UniversalString = 28;
    public static final byte tag_BMPString = 30;
    public static final byte tag_Sequence = 48;
    public static final byte tag_SequenceOf = 48;
    public static final byte tag_Set = 49;
    public static final byte tag_SetOf = 49;

    public boolean isUniversal() {
        return (this.tag & 192) == 0;
    }

    public boolean isApplication() {
        return (this.tag & 192) == 64;
    }

    public boolean isContextSpecific() {
        return (this.tag & 192) == 128;
    }

    public boolean isContextSpecific(byte var1) {
        return !this.isContextSpecific()?false:(this.tag & 31) == var1;
    }

    boolean isPrivate() {
        return (this.tag & 192) == 192;
    }

    public boolean isConstructed() {
        return (this.tag & 32) == 32;
    }

    public boolean isConstructed(byte var1) {
        return !this.isConstructed()?false:(this.tag & 31) == var1;
    }

    public TDerValue(String var1) throws IOException {
        boolean var2 = true;

        for(int var3 = 0; var3 < var1.length(); ++var3) {
            if(!isPrintableStringChar(var1.charAt(var3))) {
                var2 = false;
                break;
            }
        }

        this.data = this.init((byte)(var2?19:12), var1);
    }

    public TDerValue(byte var1, String var2) throws IOException {
        this.data = this.init(var1, var2);
    }

    public TDerValue(byte var1, byte[] var2) {
        this.tag = var1;
        this.buffer = new TDerInputBuffer((byte[])var2.clone());
        this.length = var2.length;
        this.data = new TDerInputStream(this.buffer);
        this.data.mark(2147483647);
    }

    TDerValue(TDerInputBuffer var1) throws IOException {
        this.tag = (byte)var1.read();
        byte var2 = (byte)var1.read();
        this.length = TDerInputStream.getLength(var2 & 255, var1);
        if(this.length == -1) {
            TDerInputBuffer var3 = var1.dup();
            int var4 = var3.available();
            byte var5 = 2;
            byte[] var6 = new byte[var4 + var5];
            var6[0] = this.tag;
            var6[1] = var2;
            DataInputStream var7 = new DataInputStream(var3);
            var7.readFully(var6, var5, var4);
            var7.close();
            TDerIndefLenConverter var8 = new TDerIndefLenConverter();
            var3 = new TDerInputBuffer(var8.convert(var6));
            if(this.tag != var3.read()) {
                throw new IOException("Indefinite length encoding not supported");
            }

            this.length = TDerInputStream.getLength(var3);
            this.buffer = var3.dup();
            this.buffer.truncate(this.length);
            this.data = new TDerInputStream(this.buffer);
            var1.skip((long)(this.length + var5));
        } else {
            this.buffer = var1.dup();
            this.buffer.truncate(this.length);
            this.data = new TDerInputStream(this.buffer);
            var1.skip((long)this.length);
        }

    }

    public TDerValue(byte[] var1) throws IOException {
        this.data = this.init(true, new ByteArrayInputStream(var1));
    }

    public TDerValue(byte[] var1, int var2, int var3) throws IOException {
        this.data = this.init(true, new ByteArrayInputStream(var1, var2, var3));
    }

    public TDerValue(InputStream var1) throws IOException {
        this.data = this.init(false, var1);
    }

    private TDerInputStream init(byte var1, String var2) throws IOException {
        String var3 = null;
        this.tag = var1;
        switch(var1) {
            case 12:
                var3 = "UTF8";
                break;
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 21:
            case 23:
            case 24:
            case 25:
            case 26:
            case 28:
            case 29:
            default:
                throw new IllegalArgumentException("Unsupported DER string type");
            case 19:
            case 22:
            case 27:
                var3 = "ASCII";
                break;
            case 20:
                var3 = "ISO-8859-1";
                break;
            case 30:
                var3 = "UnicodeBigUnmarked";
        }

        byte[] var4 = var2.getBytes(var3);
        this.length = var4.length;
        this.buffer = new TDerInputBuffer(var4);
        TDerInputStream var5 = new TDerInputStream(this.buffer);
        var5.mark(2147483647);
        return var5;
    }

    private TDerInputStream init(boolean var1, InputStream var2) throws IOException {
        this.tag = (byte)((InputStream)var2).read();
        byte var3 = (byte)((InputStream)var2).read();
        this.length = TDerInputStream.getLength(var3 & 255, (InputStream)var2);
        if(this.length == -1) {
            int var4 = ((InputStream)var2).available();
            byte var5 = 2;
            byte[] var6 = new byte[var4 + var5];
            var6[0] = this.tag;
            var6[1] = var3;
            DataInputStream var7 = new DataInputStream((InputStream)var2);
            var7.readFully(var6, var5, var4);
            var7.close();
            TDerIndefLenConverter var8 = new TDerIndefLenConverter();
            var2 = new ByteArrayInputStream(var8.convert(var6));
            if(this.tag != ((InputStream)var2).read()) {
                throw new IOException("Indefinite length encoding not supported");
            }

            this.length = TDerInputStream.getLength((InputStream)var2);
        }

        if(var1 && ((InputStream)var2).available() != this.length) {
            throw new IOException("extra data given to DerValue constructor");
        } else {
            byte[] var9 = IOUtils.readFully((InputStream)var2, this.length, true);
            this.buffer = new TDerInputBuffer(var9);
            return new TDerInputStream(this.buffer);
        }
    }

    public void encode(TDerOutputStream var1) throws IOException {
        var1.write(this.tag);
        var1.putLength(this.length);
        if(this.length > 0) {
            byte[] var2 = new byte[this.length];
            TDerInputStream var3 = this.data;
            synchronized(this.data) {
                this.buffer.reset();
                if(this.buffer.read(var2) != this.length) {
                    throw new IOException("short DER value read (encode)");
                }

                var1.write(var2);
            }
        }

    }

    public final TDerInputStream getData() {
        return this.data;
    }

    public final byte getTag() {
        return this.tag;
    }

    public boolean getBoolean() throws IOException {
        if(this.tag != 1) {
            throw new IOException("DerValue.getBoolean, not a BOOLEAN " + this.tag);
        } else if(this.length != 1) {
            throw new IOException("DerValue.getBoolean, invalid length " + this.length);
        } else {
            return this.buffer.read() != 0;
        }
    }

    public TObjectIdentifier getOID() throws IOException {
        if(this.tag != 6) {
            throw new IOException("DerValue.getOID, not an OID " + this.tag);
        } else {
            return new TObjectIdentifier(this.buffer);
        }
    }

    private byte[] append(byte[] var1, byte[] var2) {
        if(var1 == null) {
            return var2;
        } else {
            byte[] var3 = new byte[var1.length + var2.length];
            System.arraycopy(var1, 0, var3, 0, var1.length);
            System.arraycopy(var2, 0, var3, var1.length, var2.length);
            return var3;
        }
    }

    public byte[] getOctetString() throws IOException {
        if(this.tag != 4 && !this.isConstructed((byte) 4)) {
            throw new IOException("DerValue.getOctetString, not an Octet String: " + this.tag);
        } else {
            byte[] var1 = new byte[this.length];
            if(this.length == 0) {
                return var1;
            } else if(this.buffer.read(var1) != this.length) {
                throw new IOException("short read on DerValue buffer");
            } else {
                if(this.isConstructed()) {
                    DerInputStream var2 = new DerInputStream(var1);

                    for(var1 = null; var2.available() != 0; var1 = this.append(var1, var2.getOctetString())) {
                        ;
                    }
                }

                return var1;
            }
        }
    }

    public int getInteger() throws IOException {
        if(this.tag != 2) {
            throw new IOException("DerValue.getInteger, not an int " + this.tag);
        } else {
            return this.buffer.getInteger(this.data.available());
        }
    }

    public BigInteger getBigInteger() throws IOException {
        if(this.tag != 2) {
            throw new IOException("DerValue.getBigInteger, not an int " + this.tag);
        } else {
            return this.buffer.getBigInteger(this.data.available(), false);
        }
    }

    public BigInteger getPositiveBigInteger() throws IOException {
        if(this.tag != 2) {
            throw new IOException("DerValue.getBigInteger, not an int " + this.tag);
        } else {
            return this.buffer.getBigInteger(this.data.available(), true);
        }
    }

    public int getEnumerated() throws IOException {
        if(this.tag != 10) {
            throw new IOException("DerValue.getEnumerated, incorrect tag: " + this.tag);
        } else {
            return this.buffer.getInteger(this.data.available());
        }
    }

    public byte[] getBitString() throws IOException {
        if(this.tag != 3) {
            throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
        } else {
            return this.buffer.getBitString();
        }
    }

    public BitArray getUnalignedBitString() throws IOException {
        if(this.tag != 3) {
            throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
        } else {
            return this.buffer.getUnalignedBitString();
        }
    }

    public String getAsString() throws IOException {
        return this.tag == 12?this.getUTF8String():(this.tag == 19?this.getPrintableString():(this.tag == 20?this.getT61String():(this.tag == 22?this.getIA5String():(this.tag == 30?this.getBMPString():(this.tag == 27?this.getGeneralString():null)))));
    }

    public byte[] getBitString(boolean var1) throws IOException {
        if(!var1 && this.tag != 3) {
            throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
        } else {
            return this.buffer.getBitString();
        }
    }

    public BitArray getUnalignedBitString(boolean var1) throws IOException {
        if(!var1 && this.tag != 3) {
            throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
        } else {
            return this.buffer.getUnalignedBitString();
        }
    }

    public byte[] getDataBytes() throws IOException {
        byte[] var1 = new byte[this.length];
        TDerInputStream var2 = this.data;
        synchronized(this.data) {
            this.data.reset();
            this.data.getBytes(var1);
            return var1;
        }
    }

    public String getPrintableString() throws IOException {
        if(this.tag != 19) {
            throw new IOException("DerValue.getPrintableString, not a string " + this.tag);
        } else {
            return new String(this.getDataBytes(), "ASCII");
        }
    }

    public String getT61String() throws IOException {
        if(this.tag != 20) {
            throw new IOException("DerValue.getT61String, not T61 " + this.tag);
        } else {
            return new String(this.getDataBytes(), "ISO-8859-1");
        }
    }

    public String getIA5String() throws IOException {
        if(this.tag != 22) {
            throw new IOException("DerValue.getIA5String, not IA5 " + this.tag);
        } else {
            return new String(this.getDataBytes(), "ASCII");
        }
    }

    public String getBMPString() throws IOException {
        if(this.tag != 30) {
            throw new IOException("DerValue.getBMPString, not BMP " + this.tag);
        } else {
            return new String(this.getDataBytes(), "UnicodeBigUnmarked");
        }
    }

    public String getUTF8String() throws IOException {
        if(this.tag != 12) {
            throw new IOException("DerValue.getUTF8String, not UTF-8 " + this.tag);
        } else {
            return new String(this.getDataBytes(), "UTF8");
        }
    }

    public String getGeneralString() throws IOException {
        if(this.tag != 27) {
            throw new IOException("DerValue.getGeneralString, not GeneralString " + this.tag);
        } else {
            return new String(this.getDataBytes(), "ASCII");
        }
    }

    public Date getUTCTime() throws IOException {
        if(this.tag != 23) {
            throw new IOException("DerValue.getUTCTime, not a UtcTime: " + this.tag);
        } else {
            return this.buffer.getUTCTime(this.data.available());
        }
    }

    public Date getGeneralizedTime() throws IOException {
        if(this.tag != 24) {
            throw new IOException("DerValue.getGeneralizedTime, not a GeneralizedTime: " + this.tag);
        } else {
            return this.buffer.getGeneralizedTime(this.data.available());
        }
    }

    public boolean equals(Object var1) {
        return var1 instanceof sun.security.util.DerValue ?this.equals((sun.security.util.DerValue)var1):false;
    }

    public boolean equals(TDerValue var1) {
        return this == var1?true:(this.tag != var1.tag?false:(this.data == var1.data?true:(System.identityHashCode(this.data) > System.identityHashCode(var1.data)?doEquals(this, var1):doEquals(var1, this))));
    }

    private static boolean doEquals(TDerValue var0, TDerValue var1) {
        TDerInputStream var2 = var0.data;
        synchronized(var0.data) {
            TDerInputStream var3 = var1.data;
            boolean var10000;
            synchronized(var1.data) {
                var0.data.reset();
                var1.data.reset();
                var10000 = var0.buffer.equals(var1.buffer);
            }

            return var10000;
        }
    }

    public String toString() {
        try {
            String var1 = this.getAsString();
            return var1 != null?"\"" + var1 + "\"":(this.tag == 5?"[DerValue, null]":(this.tag == 6?"OID." + this.getOID():"[DerValue, tag = " + this.tag + ", length = " + this.length + "]"));
        } catch (IOException var2) {
            throw new IllegalArgumentException("misformatted DER value");
        }
    }

    public byte[] toByteArray() throws IOException {
        TDerOutputStream var1 = new TDerOutputStream();
        this.encode(var1);
        this.data.reset();
        return var1.toByteArray();
    }

    public TDerInputStream toDerInputStream() throws IOException {
        if(this.tag != 48 && this.tag != 49) {
            throw new IOException("toDerInputStream rejects tag type " + this.tag);
        } else {
            return new TDerInputStream(this.buffer);
        }
    }

    public int length() {
        return this.length;
    }

    public static boolean isPrintableStringChar(char var0) {
        if((var0 < 97 || var0 > 122) && (var0 < 65 || var0 > 90) && (var0 < 48 || var0 > 57)) {
            switch(var0) {
                case ' ':
                case '\'':
                case '(':
                case ')':
                case '+':
                case ',':
                case '-':
                case '.':
                case '/':
                case ':':
                case '=':
                case '?':
                    return true;
                case '!':
                case '\"':
                case '#':
                case '$':
                case '%':
                case '&':
                case '*':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case ';':
                case '<':
                case '>':
                default:
                    return false;
            }
        } else {
            return true;
        }
    }

    public static byte createTag(byte var0, boolean var1, byte var2) {
        byte var3 = (byte)(var0 | var2);
        if(var1) {
            var3 = (byte)(var3 | 32);
        }

        return var3;
    }

    public void resetTag(byte var1) {
        this.tag = var1;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }
}
