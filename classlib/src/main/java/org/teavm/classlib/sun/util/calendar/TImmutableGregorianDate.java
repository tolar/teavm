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
import java.util.TimeZone;

import sun.util.calendar.CalendarDate;

class TImmutableGregorianDate extends TBaseCalendar.Date {
    private final TBaseCalendar.Date date;

    TImmutableGregorianDate(TBaseCalendar.Date var1) {
        if(var1 == null) {
            throw new NullPointerException();
        } else {
            this.date = var1;
        }
    }

    public TEra getEra() {
        return this.date.getEra();
    }

    public TCalendarDate setEra(TEra var1) {
        this.unsupported();
        return this;
    }

    public int getYear() {
        return this.date.getYear();
    }

    public TCalendarDate setYear(int var1) {
        this.unsupported();
        return this;
    }

    public TCalendarDate addYear(int var1) {
        this.unsupported();
        return this;
    }

    public boolean isLeapYear() {
        return this.date.isLeapYear();
    }

    void setLeapYear(boolean var1) {
        this.unsupported();
    }

    public int getMonth() {
        return this.date.getMonth();
    }

    public TCalendarDate setMonth(int var1) {
        this.unsupported();
        return this;
    }

    public TCalendarDate addMonth(int var1) {
        this.unsupported();
        return this;
    }

    public int getDayOfMonth() {
        return this.date.getDayOfMonth();
    }

    public TCalendarDate setDayOfMonth(int var1) {
        this.unsupported();
        return this;
    }

    public TCalendarDate addDayOfMonth(int var1) {
        this.unsupported();
        return this;
    }

    public int getDayOfWeek() {
        return this.date.getDayOfWeek();
    }

    public int getHours() {
        return this.date.getHours();
    }

    public TCalendarDate setHours(int var1) {
        this.unsupported();
        return this;
    }

    public CalendarDate addHours(int var1) {
        this.unsupported();
        return this;
    }

    public int getMinutes() {
        return this.date.getMinutes();
    }

    public TCalendarDate setMinutes(int var1) {
        this.unsupported();
        return this;
    }

    public TCalendarDate addMinutes(int var1) {
        this.unsupported();
        return this;
    }

    public int getSeconds() {
        return this.date.getSeconds();
    }

    public TCalendarDate setSeconds(int var1) {
        this.unsupported();
        return this;
    }

    public TCalendarDate addSeconds(int var1) {
        this.unsupported();
        return this;
    }

    public int getMillis() {
        return this.date.getMillis();
    }

    public TCalendarDate setMillis(int var1) {
        this.unsupported();
        return this;
    }

    public TCalendarDate addMillis(int var1) {
        this.unsupported();
        return this;
    }

    public long getTimeOfDay() {
        return this.date.getTimeOfDay();
    }

    public TCalendarDate setDate(int var1, int var2, int var3) {
        this.unsupported();
        return this;
    }

    public TCalendarDate addDate(int var1, int var2, int var3) {
        this.unsupported();
        return this;
    }

    public TCalendarDate setTimeOfDay(int var1, int var2, int var3, int var4) {
        this.unsupported();
        return this;
    }

    public TCalendarDate addTimeOfDay(int var1, int var2, int var3, int var4) {
        this.unsupported();
        return this;
    }

    protected void setTimeOfDay(long var1) {
        this.unsupported();
    }

    public boolean isNormalized() {
        return this.date.isNormalized();
    }

    public boolean isStandardTime() {
        return this.date.isStandardTime();
    }

    public void setStandardTime(boolean var1) {
        this.unsupported();
    }

    public boolean isDaylightTime() {
        return this.date.isDaylightTime();
    }

    protected void setLocale(Locale var1) {
        this.unsupported();
    }

    public TimeZone getZone() {
        return this.date.getZone();
    }

    public TCalendarDate setZone(TimeZone var1) {
        this.unsupported();
        return this;
    }

    public boolean isSameDate(TCalendarDate var1) {
        return var1.isSameDate(var1);
    }

    public boolean equals(Object var1) {
        return this == var1?true:(!(var1 instanceof TImmutableGregorianDate)?false:this.date.equals(((TImmutableGregorianDate)var1).date));
    }

    public int hashCode() {
        return this.date.hashCode();
    }

    public Object clone() {
        return super.clone();
    }

    public String toString() {
        return this.date.toString();
    }

    protected void setDayOfWeek(int var1) {
        this.unsupported();
    }

    protected void setNormalized(boolean var1) {
        this.unsupported();
    }

    public int getZoneOffset() {
        return this.date.getZoneOffset();
    }

    protected void setZoneOffset(int var1) {
        this.unsupported();
    }

    public int getDaylightSaving() {
        return this.date.getDaylightSaving();
    }

    protected void setDaylightSaving(int var1) {
        this.unsupported();
    }

    public int getNormalizedYear() {
        return this.date.getNormalizedYear();
    }

    public void setNormalizedYear(int var1) {
        this.unsupported();
    }

    private void unsupported() {
        throw new UnsupportedOperationException();
    }
}

