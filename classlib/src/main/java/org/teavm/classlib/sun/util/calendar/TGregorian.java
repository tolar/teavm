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

import java.util.TimeZone;

import org.teavm.classlib.java.util.TTimeZone;

import sun.util.calendar.CalendarDate;

public class TGregorian extends TBaseCalendar {
    TGregorian() {
    }

    public String getName() {
        return "gregorian";
    }

    public TGregorian.Date getCalendarDate() {
        return this.getCalendarDate(System.currentTimeMillis(), (CalendarDate)this.newCalendarDate());
    }

    public TGregorian.Date getCalendarDate(long var1) {
        return this.getCalendarDate(var1, (TCalendarDate)this.newCalendarDate());
    }

    public TGregorian.Date getCalendarDate(long var1, CalendarDate var3) {
        return (TGregorian.Date)super.getCalendarDate(var1, var3);
    }

    public TGregorian.Date getCalendarDate(long var1, TimeZone var3) {
        return this.getCalendarDate(var1, (TCalendarDate)this.newCalendarDate(var3));
    }

    public TGregorian.Date newCalendarDate() {
        return new TGregorian.Date();
    }

    public TGregorian.Date newCalendarDate(TTimeZone var1) {
        return new TGregorian.Date(var1);
    }

    static class Date extends TBaseCalendar.Date {
        protected Date() {
        }

        protected Date(TimeZone var1) {
            super(var1);
        }

        public int getNormalizedYear() {
            return this.getYear();
        }

        public void setNormalizedYear(int var1) {
            this.setYear(var1);
        }
    }
}
