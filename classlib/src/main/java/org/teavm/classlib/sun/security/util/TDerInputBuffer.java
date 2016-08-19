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
import java.util.Date;

import org.teavm.classlib.java.io.TByteArrayInputStream;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TCloneNotSupportedException;
import org.teavm.classlib.java.lang.TCloneable;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.math.TBigInteger;
import org.teavm.classlib.java.util.TTimeZone;
import org.teavm.classlib.sun.util.calendar.TCalendarDate;
import org.teavm.classlib.sun.util.calendar.TCalendarSystem;
import org.teavm.classlib.sun.util.calendar.TGregorian;

class TDerInputBuffer extends TByteArrayInputStream implements TCloneable {
    TDerInputBuffer(byte[] var1) {
        super(var1);
    }

    TDerInputBuffer(byte[] var1, int var2, int var3) {
        super(var1, var2, var3);
    }

    TDerInputBuffer dup() {
        try {
            TDerInputBuffer var1 = (TDerInputBuffer)this.clone();
            var1.mark(2147483647);
            return var1;
        } catch (TCloneNotSupportedException var2) {
            throw new IllegalArgumentException(var2.toString());
        }
    }

    byte[] toByteArray() {
        int var1 = this.available();
        if(var1 <= 0) {
            return null;
        } else {
            byte[] var2 = new byte[var1];
            System.arraycopy(this.buf, this.pos, var2, 0, var1);
            return var2;
        }
    }

    int peek() throws IOException {
        if(this.pos >= this.count) {
            throw new IOException("out of data");
        } else {
            return this.buf[this.pos];
        }
    }

    public boolean equals(Object var1) {
        return var1 instanceof TDerInputBuffer ?this.equals((TDerInputBuffer)var1):false;
    }

    boolean equals(TDerInputBuffer var1) {
        if(this == var1) {
            return true;
        } else {
            int var2 = this.available();
            if(var1.available() != var2) {
                return false;
            } else {
                for(int var3 = 0; var3 < var2; ++var3) {
                    if(this.buf[this.pos + var3] != var1.buf[var1.pos + var3]) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public int hashCode() {
        int var1 = 0;
        int var2 = this.available();
        int var3 = this.pos;

        for(int var4 = 0; var4 < var2; ++var4) {
            var1 += this.buf[var3 + var4] * var4;
        }

        return var1;
    }

    void truncate(int var1) throws TIOException {
        if(var1 > this.available()) {
            throw new TIOException(TString.wrap("insufficient data"));
        } else {
            this.count = this.pos + var1;
        }
    }

    TBigInteger getBigInteger(int var1, boolean var2) throws TIOException {
        if(var1 > this.available()) {
            throw new TIOException(TString.wrap("short read of integer"));
        } else if(var1 == 0) {
            throw new TIOException(TString.wrap("Invalid encoding: zero length Int value"));
        } else {
            byte[] var3 = new byte[var1];
            System.arraycopy(this.buf, this.pos, var3, 0, var1);
            this.skip((long)var1);
            return var2?new TBigInteger(1, var3):new TBigInteger(var3);
        }
    }

    public int getInteger(int var1) throws TIOException {
        TBigInteger var2 = this.getBigInteger(var1, false);
        if(var2.compareTo(TBigInteger.valueOf(-2147483648L)) < 0) {
            throw new TIOException(TString.wrap("Integer below minimum valid value"));
        } else if(var2.compareTo(TBigInteger.valueOf(2147483647L)) > 0) {
            throw new TIOException(TString.wrap("Integer exceeds maximum valid value"));
        } else {
            return var2.intValue();
        }
    }

    public byte[] getBitString(int var1) throws TIOException {
        if(var1 > this.available()) {
            throw new TIOException(TString.wrap("short read of bit string"));
        } else if(var1 == 0) {
            throw new TIOException(TString.wrap("Invalid encoding: zero length bit string"));
        } else {
            byte var2 = this.buf[this.pos];
            if(var2 >= 0 && var2 <= 7) {
                byte[] var3 = new byte[var1 - 1];
                System.arraycopy(this.buf, this.pos + 1, var3, 0, var1 - 1);
                if(var2 != 0) {
                    var3[var1 - 2] = (byte)(var3[var1 - 2] & 255 << var2);
                }

                this.skip((long)var1);
                return var3;
            } else {
                throw new TIOException(TString.wrap("Invalid number of padding bits"));
            }
        }
    }

    byte[] getBitString() throws IOException {
        return this.getBitString(this.available());
    }

    TBitArray getUnalignedBitString() throws TIOException {
        if(this.pos >= this.count) {
            return null;
        } else {
            int var1 = this.available();
            int var2 = this.buf[this.pos] & 255;
            if(var2 > 7) {
                throw new TIOException(TString.wrap("Invalid value for unused bits: " + var2));
            } else {
                byte[] var3 = new byte[var1 - 1];
                int var4 = var3.length == 0?0:var3.length * 8 - var2;
                System.arraycopy(this.buf, this.pos + 1, var3, 0, var1 - 1);
                TBitArray var5 = new TBitArray(var4, var3);
                this.pos = this.count;
                return var5;
            }
        }
    }

    public Date getUTCTime(int var1) throws IOException {
        if(var1 > this.available()) {
            throw new IOException("short read of DER UTC Time");
        } else if(var1 >= 11 && var1 <= 17) {
            return this.getTime(var1, false);
        } else {
            throw new IOException("DER UTC Time length error");
        }
    }

    public Date getGeneralizedTime(int var1) throws IOException {
        if(var1 > this.available()) {
            throw new IOException("short read of DER Generalized Time");
        } else if(var1 >= 13 && var1 <= 23) {
            return this.getTime(var1, true);
        } else {
            throw new IOException("DER Generalized Time length error");
        }
    }

    private Date getTime(int var1, boolean var2) throws IOException {
        String var10 = null;
        int var3;
        if(var2) {
            var10 = "Generalized";
            var3 = 1000 * Character.digit((char)this.buf[this.pos++], 10);
            var3 += 100 * Character.digit((char)this.buf[this.pos++], 10);
            var3 += 10 * Character.digit((char)this.buf[this.pos++], 10);
            var3 += Character.digit((char)this.buf[this.pos++], 10);
            var1 -= 2;
        } else {
            var10 = "UTC";
            var3 = 10 * Character.digit((char)this.buf[this.pos++], 10);
            var3 += Character.digit((char)this.buf[this.pos++], 10);
            if(var3 < 50) {
                var3 += 2000;
            } else {
                var3 += 1900;
            }
        }

        int var4 = 10 * Character.digit((char)this.buf[this.pos++], 10);
        var4 += Character.digit((char)this.buf[this.pos++], 10);
        int var5 = 10 * Character.digit((char)this.buf[this.pos++], 10);
        var5 += Character.digit((char)this.buf[this.pos++], 10);
        int var6 = 10 * Character.digit((char)this.buf[this.pos++], 10);
        var6 += Character.digit((char)this.buf[this.pos++], 10);
        int var7 = 10 * Character.digit((char)this.buf[this.pos++], 10);
        var7 += Character.digit((char)this.buf[this.pos++], 10);
        var1 -= 10;
        int var9 = 0;
        int var8;
        if(var1 > 2 && var1 < 12) {
            var8 = 10 * Character.digit((char)this.buf[this.pos++], 10);
            var8 += Character.digit((char)this.buf[this.pos++], 10);
            var1 -= 2;
            if(this.buf[this.pos] == 46 || this.buf[this.pos] == 44) {
                --var1;
                ++this.pos;
                int var11 = 0;

                for(int var12 = this.pos; this.buf[var12] != 90 && this.buf[var12] != 43 && this.buf[var12] != 45; ++var11) {
                    ++var12;
                }

                switch(var11) {
                    case 1:
                        var9 += 100 * Character.digit((char)this.buf[this.pos++], 10);
                        break;
                    case 2:
                        var9 += 100 * Character.digit((char)this.buf[this.pos++], 10);
                        var9 += 10 * Character.digit((char)this.buf[this.pos++], 10);
                        break;
                    case 3:
                        var9 += 100 * Character.digit((char)this.buf[this.pos++], 10);
                        var9 += 10 * Character.digit((char)this.buf[this.pos++], 10);
                        var9 += Character.digit((char)this.buf[this.pos++], 10);
                        break;
                    default:
                        throw new IOException("Parse " + var10 + " time, unsupported precision for seconds value");
                }

                var1 -= var11;
            }
        } else {
            var8 = 0;
        }

        if(var4 != 0 && var5 != 0 && var4 <= 12 && var5 <= 31 && var6 < 24 && var7 < 60 && var8 < 60) {
            TGregorian var17 = TCalendarSystem.getGregorianCalendar();
            TCalendarDate var18 = var17.newCalendarDate((TTimeZone)null);
            var18.setDate(var3, var4, var5);
            var18.setTimeOfDay(var6, var7, var8, var9);
            long var13 = var17.getTime(var18);
            if(var1 != 1 && var1 != 5) {
                throw new IOException("Parse " + var10 + " time, invalid offset");
            } else {
                int var15;
                int var16;
                switch(this.buf[this.pos++]) {
                    case 43:
                        var15 = 10 * Character.digit((char)this.buf[this.pos++], 10);
                        var15 += Character.digit((char)this.buf[this.pos++], 10);
                        var16 = 10 * Character.digit((char)this.buf[this.pos++], 10);
                        var16 += Character.digit((char)this.buf[this.pos++], 10);
                        if(var15 >= 24 || var16 >= 60) {
                            throw new IOException("Parse " + var10 + " time, +hhmm");
                        }

                        var13 -= (long)((var15 * 60 + var16) * 60 * 1000);
                        break;
                    case 45:
                        var15 = 10 * Character.digit((char)this.buf[this.pos++], 10);
                        var15 += Character.digit((char)this.buf[this.pos++], 10);
                        var16 = 10 * Character.digit((char)this.buf[this.pos++], 10);
                        var16 += Character.digit((char)this.buf[this.pos++], 10);
                        if(var15 >= 24 || var16 >= 60) {
                            throw new IOException("Parse " + var10 + " time, -hhmm");
                        }

                        var13 += (long)((var15 * 60 + var16) * 60 * 1000);
                    case 90:
                        break;
                    default:
                        throw new IOException("Parse " + var10 + " time, garbage offset");
                }

                return new Date(var13);
            }
        } else {
            throw new IOException("Parse " + var10 + " time, invalid format");
        }
    }
}
