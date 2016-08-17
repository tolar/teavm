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

import java.util.Locale;

import org.teavm.classlib.java.util.TTimeZone;

import sun.util.calendar.CalendarUtils;

public abstract class TCalendarDate implements Cloneable {
    public static final int FIELD_UNDEFINED = -2147483648;
    public static final long TIME_UNDEFINED = -9223372036854775808L;
    private TEra era;
    private int year;
    private int month;
    private int dayOfMonth;
    private int dayOfWeek;
    private boolean leapYear;
    private int hours;
    private int minutes;
    private int seconds;
    private int millis;
    private long fraction;
    private boolean normalized;
    private TTimeZone zoneinfo;
    private int zoneOffset;
    private int daylightSaving;
    private boolean forceStandardTime;
    private Locale locale;

    protected TCalendarDate() {
        this(TTimeZone.getDefault());
    }

    protected TCalendarDate(TTimeZone var1) {
        this.dayOfWeek = -2147483648;
        this.zoneinfo = var1;
    }

    public TEra getEra() {
        return this.era;
    }

    public TCalendarDate setEra(TEra var1) {
        if(this.era == var1) {
            return this;
        } else {
            this.era = var1;
            this.normalized = false;
            return this;
        }
    }

    public int getYear() {
        return this.year;
    }

    public TCalendarDate setYear(int var1) {
        if(this.year != var1) {
            this.year = var1;
            this.normalized = false;
        }

        return this;
    }

    public TCalendarDate addYear(int var1) {
        if(var1 != 0) {
            this.year += var1;
            this.normalized = false;
        }

        return this;
    }

    public boolean isLeapYear() {
        return this.leapYear;
    }

    void setLeapYear(boolean var1) {
        this.leapYear = var1;
    }

    public int getMonth() {
        return this.month;
    }

    public TCalendarDate setMonth(int var1) {
        if(this.month != var1) {
            this.month = var1;
            this.normalized = false;
        }

        return this;
    }

    public TCalendarDate addMonth(int var1) {
        if(var1 != 0) {
            this.month += var1;
            this.normalized = false;
        }

        return this;
    }

    public int getDayOfMonth() {
        return this.dayOfMonth;
    }

    public TCalendarDate setDayOfMonth(int var1) {
        if(this.dayOfMonth != var1) {
            this.dayOfMonth = var1;
            this.normalized = false;
        }

        return this;
    }

    public TCalendarDate addDayOfMonth(int var1) {
        if(var1 != 0) {
            this.dayOfMonth += var1;
            this.normalized = false;
        }

        return this;
    }

    public int getDayOfWeek() {
        if(!this.isNormalized()) {
            this.dayOfWeek = -2147483648;
        }

        return this.dayOfWeek;
    }

    public int getHours() {
        return this.hours;
    }

    public TCalendarDate setHours(int var1) {
        if(this.hours != var1) {
            this.hours = var1;
            this.normalized = false;
        }

        return this;
    }

    public TCalendarDate addHours(int var1) {
        if(var1 != 0) {
            this.hours += var1;
            this.normalized = false;
        }

        return this;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public TCalendarDate setMinutes(int var1) {
        if(this.minutes != var1) {
            this.minutes = var1;
            this.normalized = false;
        }

        return this;
    }

    public TCalendarDate addMinutes(int var1) {
        if(var1 != 0) {
            this.minutes += var1;
            this.normalized = false;
        }

        return this;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public TCalendarDate setSeconds(int var1) {
        if(this.seconds != var1) {
            this.seconds = var1;
            this.normalized = false;
        }

        return this;
    }

    public TCalendarDate addSeconds(int var1) {
        if(var1 != 0) {
            this.seconds += var1;
            this.normalized = false;
        }

        return this;
    }

    public int getMillis() {
        return this.millis;
    }

    public TCalendarDate setMillis(int var1) {
        if(this.millis != var1) {
            this.millis = var1;
            this.normalized = false;
        }

        return this;
    }

    public TCalendarDate addMillis(int var1) {
        if(var1 != 0) {
            this.millis += var1;
            this.normalized = false;
        }

        return this;
    }

    public long getTimeOfDay() {
        return !this.isNormalized()?(this.fraction = -9223372036854775808L):this.fraction;
    }

    public TCalendarDate setDate(int var1, int var2, int var3) {
        this.setYear(var1);
        this.setMonth(var2);
        this.setDayOfMonth(var3);
        return this;
    }

    public TCalendarDate addDate(int var1, int var2, int var3) {
        this.addYear(var1);
        this.addMonth(var2);
        this.addDayOfMonth(var3);
        return this;
    }

    public TCalendarDate setTimeOfDay(int var1, int var2, int var3, int var4) {
        this.setHours(var1);
        this.setMinutes(var2);
        this.setSeconds(var3);
        this.setMillis(var4);
        return this;
    }

    public TCalendarDate addTimeOfDay(int var1, int var2, int var3, int var4) {
        this.addHours(var1);
        this.addMinutes(var2);
        this.addSeconds(var3);
        this.addMillis(var4);
        return this;
    }

    protected void setTimeOfDay(long var1) {
        this.fraction = var1;
    }

    public boolean isNormalized() {
        return this.normalized;
    }

    public boolean isStandardTime() {
        return this.forceStandardTime;
    }

    public void setStandardTime(boolean var1) {
        this.forceStandardTime = var1;
    }

    public boolean isDaylightTime() {
        return this.isStandardTime()?false:this.daylightSaving != 0;
    }

    protected void setLocale(Locale var1) {
        this.locale = var1;
    }

    public TTimeZone getZone() {
        return this.zoneinfo;
    }

    public TCalendarDate setZone(TTimeZone var1) {
        this.zoneinfo = var1;
        return this;
    }

    public boolean isSameDate(TCalendarDate var1) {
        return this.getDayOfWeek() == var1.getDayOfWeek() && this.getMonth() == var1.getMonth() && this.getYear() == var1.getYear() && this.getEra() == var1.getEra();
    }

    public boolean equals(Object var1) {
        if(!(var1 instanceof TCalendarDate)) {
            return false;
        } else {
            TCalendarDate var2 = (TCalendarDate)var1;
            if(this.isNormalized() != var2.isNormalized()) {
                return false;
            } else {
                boolean var3 = this.zoneinfo != null;
                boolean var4 = var2.zoneinfo != null;
                return var3 != var4?false:(var3 && !this.zoneinfo.equals(var2.zoneinfo)?false:this.getEra() == var2.getEra() && this.year == var2.year && this.month == var2.month && this.dayOfMonth == var2.dayOfMonth && this.hours == var2.hours && this.minutes == var2.minutes && this.seconds == var2.seconds && this.millis == var2.millis && this.zoneOffset == var2.zoneOffset);
            }
        }
    }

    public int hashCode() {
        long var1 = ((((long)this.year - 1970L) * 12L + (long)(this.month - 1)) * 30L + (long)this.dayOfMonth) * 24L;
        var1 = (((var1 + (long)this.hours) * 60L + (long)this.minutes) * 60L + (long)this.seconds) * 1000L + (long)this.millis;
        var1 -= (long)this.zoneOffset;
        int var3 = this.isNormalized()?1:0;
        int var4 = 0;
        TEra var5 = this.getEra();
        if(var5 != null) {
            var4 = var5.hashCode();
        }

        int var6 = this.zoneinfo != null?this.zoneinfo.hashCode():0;
        return (int)var1 * (int)(var1 >> 32) ^ var4 ^ var3 ^ var6;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException var2) {
            throw new InternalError(var2);
        }
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        CalendarUtils.sprintf0d(var1, this.year, 4).append('-');
        CalendarUtils.sprintf0d(var1, this.month, 2).append('-');
        CalendarUtils.sprintf0d(var1, this.dayOfMonth, 2).append('T');
        CalendarUtils.sprintf0d(var1, this.hours, 2).append(':');
        CalendarUtils.sprintf0d(var1, this.minutes, 2).append(':');
        CalendarUtils.sprintf0d(var1, this.seconds, 2).append('.');
        CalendarUtils.sprintf0d(var1, this.millis, 3);
        if(this.zoneOffset == 0) {
            var1.append('Z');
        } else if(this.zoneOffset != -2147483648) {
            int var2;
            char var3;
            if(this.zoneOffset > 0) {
                var2 = this.zoneOffset;
                var3 = 43;
            } else {
                var2 = -this.zoneOffset;
                var3 = 45;
            }

            var2 /= '\uea60';
            var1.append(var3);
            CalendarUtils.sprintf0d(var1, var2 / 60, 2);
            CalendarUtils.sprintf0d(var1, var2 % 60, 2);
        } else {
            var1.append(" local time");
        }

        return var1.toString();
    }

    protected void setDayOfWeek(int var1) {
        this.dayOfWeek = var1;
    }

    protected void setNormalized(boolean var1) {
        this.normalized = var1;
    }

    public int getZoneOffset() {
        return this.zoneOffset;
    }

    protected void setZoneOffset(int var1) {
        this.zoneOffset = var1;
    }

    public int getDaylightSaving() {
        return this.daylightSaving;
    }

    protected void setDaylightSaving(int var1) {
        this.daylightSaving = var1;
    }
}
