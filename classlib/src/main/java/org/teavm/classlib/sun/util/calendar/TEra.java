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

public final class TEra {
    private final String name;
    private final String abbr;
    private final long since;
    private final TCalendarDate sinceDate;
    private final boolean localTime;
    private int hash = 0;

    public TEra(String var1, String var2, long var3, boolean var5) {
        this.name = var1;
        this.abbr = var2;
        this.since = var3;
        this.localTime = var5;
        TGregorian var6 = TCalendarSystem.getGregorianCalendar();
        TGregorian.Date var7 = var6.newCalendarDate((TimeZone)null);
        var6.getCalendarDate(var3, var7);
        this.sinceDate = new TImmutableGregorianDate(var7);
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName(Locale var1) {
        return this.name;
    }

    public String getAbbreviation() {
        return this.abbr;
    }

    public String getDiaplayAbbreviation(Locale var1) {
        return this.abbr;
    }

    public long getSince(TimeZone var1) {
        if(var1 != null && this.localTime) {
            int var2 = var1.getOffset(this.since);
            return this.since - (long)var2;
        } else {
            return this.since;
        }
    }

    public CalendarDate getSinceDate() {
        return this.sinceDate;
    }

    public boolean isLocalTime() {
        return this.localTime;
    }

    public boolean equals(Object var1) {
        if(!(var1 instanceof sun.util.calendar.Era)) {
            return false;
        } else {
            sun.util.calendar.Era var2 = (sun.util.calendar.Era)var1;
            return this.name.equals(var2.name) && this.abbr.equals(var2.abbr) && this.since == var2.since && this.localTime == var2.localTime;
        }
    }

    public int hashCode() {
        if(this.hash == 0) {
            this.hash = this.name.hashCode() ^ this.abbr.hashCode() ^ (int)this.since ^ (int)(this.since >> 32) ^ (this.localTime?1:0);
        }

        return this.hash;
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        var1.append('[');
        var1.append(this.getName()).append(" (");
        var1.append(this.getAbbreviation()).append(')');
        var1.append(" since ").append(this.getSinceDate());
        if(this.localTime) {
            var1.setLength(var1.length() - 1);
            var1.append(" local time");
        }

        var1.append(']');
        return var1.toString();
    }
}
