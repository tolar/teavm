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

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.teavm.classlib.java.util.TTimeZone;

public abstract class TCalendarSystem {
    private static volatile boolean initialized = false;
    private static ConcurrentMap<String, String> names;
    private static ConcurrentMap<String, sun.util.calendar.CalendarSystem> calendars;
    private static final String PACKAGE_NAME = "sun.util.calendar.";
    private static final String[] namePairs = new String[]{"gregorian", "Gregorian", "japanese", "LocalGregorianCalendar", "julian", "JulianCalendar"};
    private static final TGregorian GREGORIAN_INSTANCE = new TGregorian();

    public TCalendarSystem() {
    }

    private static void initNames() {
        ConcurrentHashMap var0 = new ConcurrentHashMap();
        StringBuilder var1 = new StringBuilder();

        for(int var2 = 0; var2 < namePairs.length; var2 += 2) {
            var1.setLength(0);
            String var3 = var1.append("sun.util.calendar.").append(namePairs[var2 + 1]).toString();
            var0.put(namePairs[var2], var3);
        }

        Class var6 = sun.util.calendar.CalendarSystem.class;
        synchronized(sun.util.calendar.CalendarSystem.class) {
            if(!initialized) {
                names = var0;
                calendars = new ConcurrentHashMap();
                initialized = true;
            }

        }
    }

    public static TGregorian getGregorianCalendar() {
        return GREGORIAN_INSTANCE;
    }

    public static sun.util.calendar.CalendarSystem forName(String var0) {
        if("gregorian".equals(var0)) {
            return GREGORIAN_INSTANCE;
        } else {
            if(!initialized) {
                initNames();
            }

            sun.util.calendar.CalendarSystem var1 = (sun.util.calendar.CalendarSystem)calendars.get(var0);
            if(var1 != null) {
                return var1;
            } else {
                String var2 = (String)names.get(var0);
                if(var2 == null) {
                    return null;
                } else {
                    Object var5;
                    if(var2.endsWith("LocalGregorianCalendar")) {
                        var5 = TLocalGregorianCalendar.getLocalGregorianCalendar(var0);
                    } else {
                        try {
                            Class var3 = Class.forName(var2);
                            var5 = (sun.util.calendar.CalendarSystem)var3.newInstance();
                        } catch (Exception var4) {
                            throw new InternalError(var4);
                        }
                    }

                    if(var5 == null) {
                        return null;
                    } else {
                        sun.util.calendar.CalendarSystem
                                var6 = (sun.util.calendar.CalendarSystem)calendars.putIfAbsent(var0, var5);
                        return (sun.util.calendar.CalendarSystem)(var6 == null?var5:var6);
                    }
                }
            }
        }
    }

    public static Properties getCalendarProperties() throws IOException {
        return null;
    }

    public abstract String getName();

    public abstract TCalendarDate getCalendarDate();

    public abstract TCalendarDate getCalendarDate(long var1);

    public abstract TCalendarDate getCalendarDate(long var1, TCalendarDate var3);

    public abstract TCalendarDate getCalendarDate(long var1, TTimeZone var3);

    public abstract TCalendarDate newCalendarDate();

    public abstract TCalendarDate newCalendarDate(TTimeZone var1);

    public abstract long getTime(TCalendarDate var1);

    public abstract int getYearLength(TCalendarDate var1);

    public abstract int getYearLengthInMonths(TCalendarDate var1);

    public abstract int getMonthLength(TCalendarDate var1);

    public abstract int getWeekLength();

    public abstract TEra getEra(String var1);

    public abstract TEra[] getEras();

    public abstract void setEra(TCalendarDate var1, String var2);

    public abstract TCalendarDate getNthDayOfWeek(int var1, int var2, TCalendarDate var3);

    public abstract TCalendarDate setTimeOfDay(TCalendarDate var1, int var2);

    public abstract boolean validate(TCalendarDate var1);

    public abstract boolean normalize(TCalendarDate var1);
}
