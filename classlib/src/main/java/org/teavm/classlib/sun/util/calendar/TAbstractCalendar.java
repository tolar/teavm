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
package org.teavm.classlib.sun.util.calendar;

import org.teavm.classlib.java.util.TTimeZone;

public abstract class TAbstractCalendar extends TCalendarSystem {
    static final int SECOND_IN_MILLIS = 1000;
    static final int MINUTE_IN_MILLIS = 60000;
    static final int HOUR_IN_MILLIS = 3600000;
    static final int DAY_IN_MILLIS = 86400000;
    static final int EPOCH_OFFSET = 719163;
    private TEra[] eras;

    protected TAbstractCalendar() {
    }

    public TEra getEra(String var1) {
        if(this.eras != null) {
            for(int var2 = 0; var2 < this.eras.length; ++var2) {
                if(this.eras[var2].equals(var1)) {
                    return this.eras[var2];
                }
            }
        }

        return null;
    }

    public TEra[] getEras() {
        TEra[] var1 = null;
        if(this.eras != null) {
            var1 = new TEra[this.eras.length];
            System.arraycopy(this.eras, 0, var1, 0, this.eras.length);
        }

        return var1;
    }

    public void setEra(TCalendarDate var1, String var2) {
        if(this.eras != null) {
            for(int var3 = 0; var3 < this.eras.length; ++var3) {
                TEra var4 = this.eras[var3];
                if(var4 != null && var4.getName().equals(var2)) {
                    var1.setEra(var4);
                    return;
                }
            }

            throw new IllegalArgumentException("unknown era name: " + var2);
        }
    }

    protected void setEras(TEra[] var1) {
        this.eras = var1;
    }

    public TCalendarDate getCalendarDate() {
        return this.getCalendarDate(System.currentTimeMillis(), this.newCalendarDate());
    }

    public TCalendarDate getCalendarDate(long var1) {
        return this.getCalendarDate(var1, this.newCalendarDate());
    }

    public TCalendarDate getCalendarDate(long var1, TTimeZone var3) {
        TCalendarDate var4 = this.newCalendarDate(var3);
        return this.getCalendarDate(var1, var4);
    }

    public TCalendarDate getCalendarDate(long var1, TCalendarDate var3) {
        int var4 = 0;
        int var5 = 0;
        int var6 = 0;
        long var7 = 0L;
        TTimeZone var9 = var3.getZone();
        if(var9 != null) {
            int[] var10 = new int[2];
            if(var9 instanceof TZoneInfo) {
                var5 = ((TZoneInfo)var9).getOffsets(var1, var10);
            } else {
                var5 = var9.getOffset(var1);
                var10[0] = var9.getRawOffset();
                var10[1] = var5 - var10[0];
            }

            var7 = (long)(var5 / 86400000);
            var4 = var5 % 86400000;
            var6 = var10[1];
        }

        var3.setZoneOffset(var5);
        var3.setDaylightSaving(var6);
        var7 += var1 / 86400000L;
        var4 += (int)(var1 % 86400000L);
        if(var4 >= 86400000) {
            var4 -= 86400000;
            ++var7;
        } else {
            while(var4 < 0) {
                var4 += 86400000;
                --var7;
            }
        }

        var7 += 719163L;
        this.getCalendarDateFromFixedDate(var3, var7);
        this.setTimeOfDay(var3, var4);
        var3.setLeapYear(this.isLeapYear(var3));
        var3.setNormalized(true);
        return var3;
    }

    public long getTime(TCalendarDate var1) {
        long var2 = this.getFixedDate(var1);
        long var4 = (var2 - 719163L) * 86400000L + this.getTimeOfDay(var1);
        int var6 = 0;
        TTimeZone var7 = var1.getZone();
        if(var7 != null) {
            if(var1.isNormalized()) {
                return var4 - (long)var1.getZoneOffset();
            }

            int[] var8 = new int[2];
            if(var1.isStandardTime()) {
                if(var7 instanceof TZoneInfo) {
                    ((TZoneInfo)var7).getOffsetsByStandard(var4, var8);
                    var6 = var8[0];
                } else {
                    var6 = var7.getOffset(var4 - (long)var7.getRawOffset());
                }
            } else if(var7 instanceof TZoneInfo) {
                var6 = ((TZoneInfo)var7).getOffsetsByWall(var4, var8);
            } else {
                var6 = var7.getOffset(var4 - (long)var7.getRawOffset());
            }
        }

        var4 -= (long)var6;
        this.getCalendarDate(var4, var1);
        return var4;
    }

    protected long getTimeOfDay(TCalendarDate var1) {
        long var2 = var1.getTimeOfDay();
        if(var2 != -9223372036854775808L) {
            return var2;
        } else {
            var2 = this.getTimeOfDayValue(var1);
            var1.setTimeOfDay(var2);
            return var2;
        }
    }

    public long getTimeOfDayValue(TCalendarDate var1) {
        long var2 = (long)var1.getHours();
        var2 *= 60L;
        var2 += (long)var1.getMinutes();
        var2 *= 60L;
        var2 += (long)var1.getSeconds();
        var2 *= 1000L;
        var2 += (long)var1.getMillis();
        return var2;
    }

    public TCalendarDate setTimeOfDay(TCalendarDate var1, int var2) {
        if(var2 < 0) {
            throw new IllegalArgumentException();
        } else {
            boolean var3 = var1.isNormalized();
            int var5 = var2 / 3600000;
            int var4 = var2 % 3600000;
            int var6 = var4 / '\uea60';
            var4 %= '\uea60';
            int var7 = var4 / 1000;
            var4 %= 1000;
            var1.setHours(var5);
            var1.setMinutes(var6);
            var1.setSeconds(var7);
            var1.setMillis(var4);
            var1.setTimeOfDay((long)var2);
            if(var5 < 24 && var3) {
                var1.setNormalized(var3);
            }

            return var1;
        }
    }

    public int getWeekLength() {
        return 7;
    }

    protected abstract boolean isLeapYear(TCalendarDate var1);

    public TCalendarDate getNthDayOfWeek(int var1, int var2, TCalendarDate var3) {
        TCalendarDate var4 = (TCalendarDate)var3.clone();
        this.normalize(var4);
        long var5 = this.getFixedDate(var4);
        long var7;
        if(var1 > 0) {
            var7 = (long)(7 * var1) + getDayOfWeekDateBefore(var5, var2);
        } else {
            var7 = (long)(7 * var1) + getDayOfWeekDateAfter(var5, var2);
        }

        this.getCalendarDateFromFixedDate(var4, var7);
        return var4;
    }

    static long getDayOfWeekDateBefore(long var0, int var2) {
        return getDayOfWeekDateOnOrBefore(var0 - 1L, var2);
    }

    static long getDayOfWeekDateAfter(long var0, int var2) {
        return getDayOfWeekDateOnOrBefore(var0 + 7L, var2);
    }

    public static long getDayOfWeekDateOnOrBefore(long var0, int var2) {
        long var3 = var0 - (long)(var2 - 1);
        return var3 >= 0L?var0 - var3 % 7L:var0 - TCalendarUtils.mod(var3, 7L);
    }

    protected abstract long getFixedDate(TCalendarDate var1);

    protected abstract void getCalendarDateFromFixedDate(TCalendarDate var1, long var2);

    public boolean validateTime(TCalendarDate var1) {
        int var2 = var1.getHours();
        if(var2 >= 0 && var2 < 24) {
            var2 = var1.getMinutes();
            if(var2 >= 0 && var2 < 60) {
                var2 = var1.getSeconds();
                if(var2 >= 0 && var2 < 60) {
                    var2 = var1.getMillis();
                    return var2 >= 0 && var2 < 1000;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    int normalizeTime(TCalendarDate var1) {
        long var2 = this.getTimeOfDay(var1);
        long var4 = 0L;
        if(var2 >= 86400000L) {
            var4 = var2 / 86400000L;
            var2 %= 86400000L;
        } else if(var2 < 0L) {
            var4 = TCalendarUtils.floorDivide(var2, 86400000L);
            if(var4 != 0L) {
                var2 -= 86400000L * var4;
            }
        }

        if(var4 != 0L) {
            var1.setTimeOfDay(var2);
        }

        var1.setMillis((int)(var2 % 1000L));
        var2 /= 1000L;
        var1.setSeconds((int)(var2 % 60L));
        var2 /= 60L;
        var1.setMinutes((int)(var2 % 60L));
        var1.setHours((int)(var2 / 60L));
        return (int)var4;
    }
}
