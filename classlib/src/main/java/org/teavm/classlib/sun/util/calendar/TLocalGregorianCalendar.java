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
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.teavm.classlib.java.util.TTimeZone;

public class TLocalGregorianCalendar extends TBaseCalendar {
    private String name;
    private TEra[] eras;

    static TLocalGregorianCalendar getLocalGregorianCalendar(String var0) {
        Properties var1;
        try {
            var1 = TCalendarSystem.getCalendarProperties();
        } catch (IllegalArgumentException | IOException var16) {
            throw new InternalError(var16);
        }

        String var2 = var1.getProperty("calendar." + var0 + ".eras");
        if(var2 == null) {
            return null;
        } else {
            ArrayList var3 = new ArrayList();
            StringTokenizer var4 = new StringTokenizer(var2, ";");

            while(var4.hasMoreTokens()) {
                String var5 = var4.nextToken().trim();
                StringTokenizer var6 = new StringTokenizer(var5, ",");
                String var7 = null;
                boolean var8 = true;
                long var9 = 0L;
                String var11 = null;

                while(var6.hasMoreTokens()) {
                    String var12 = var6.nextToken();
                    int var13 = var12.indexOf(61);
                    if(var13 == -1) {
                        return null;
                    }

                    String var14 = var12.substring(0, var13);
                    String var15 = var12.substring(var13 + 1);
                    if("name".equals(var14)) {
                        var7 = var15;
                    } else if("since".equals(var14)) {
                        if(var15.endsWith("u")) {
                            var8 = false;
                            var9 = Long.parseLong(var15.substring(0, var15.length() - 1));
                        } else {
                            var9 = Long.parseLong(var15);
                        }
                    } else {
                        if(!"abbr".equals(var14)) {
                            throw new RuntimeException("Unknown key word: " + var14);
                        }

                        var11 = var15;
                    }
                }

                TEra var18 = new TEra(var7, var11, var9, var8);
                var3.add(var18);
            }

            TEra[] var17 = new TEra[var3.size()];
            var3.toArray(var17);
            return new TLocalGregorianCalendar(var0, var17);
        }
    }

    private TLocalGregorianCalendar(String var1, TEra[] var2) {
        this.name = var1;
        this.eras = var2;
        this.setEras(var2);
    }

    public String getName() {
        return this.name;
    }

    public TLocalGregorianCalendar.Date getCalendarDate() {
        return this.getCalendarDate(System.currentTimeMillis(), (TCalendarDate)this.newCalendarDate());
    }

    public TLocalGregorianCalendar.Date getCalendarDate(long var1) {
        return this.getCalendarDate(var1, (TCalendarDate)this.newCalendarDate());
    }

    public TLocalGregorianCalendar.Date getCalendarDate(long var1, TTimeZone var3) {
        return this.getCalendarDate(var1, (TCalendarDate)this.newCalendarDate(var3));
    }

    public TLocalGregorianCalendar.Date getCalendarDate(long var1, TCalendarDate var3) {
        TLocalGregorianCalendar.Date var4 = (TLocalGregorianCalendar.Date)super.getCalendarDate(var1, var3);
        return this.adjustYear(var4, var1, var4.getZoneOffset());
    }

    private TLocalGregorianCalendar.Date adjustYear(TLocalGregorianCalendar.Date var1, long var2, int var4) {
        int var5;
        for(var5 = this.eras.length - 1; var5 >= 0; --var5) {
            TEra var6 = this.eras[var5];
            long var7 = var6.getSince((TTimeZone)null);
            if(var6.isLocalTime()) {
                var7 -= (long)var4;
            }

            if(var2 >= var7) {
                var1.setLocalEra(var6);
                int var9 = var1.getNormalizedYear() - var6.getSinceDate().getYear() + 1;
                var1.setLocalYear(var9);
                break;
            }
        }

        if(var5 < 0) {
            var1.setLocalEra((TEra)null);
            var1.setLocalYear(var1.getNormalizedYear());
        }

        var1.setNormalized(true);
        return var1;
    }

    public TLocalGregorianCalendar.Date newCalendarDate() {
        return new TLocalGregorianCalendar.Date();
    }

    public TLocalGregorianCalendar.Date newCalendarDate(TTimeZone var1) {
        return new TLocalGregorianCalendar.Date(var1);
    }

    public boolean validate(TCalendarDate var1) {
        TLocalGregorianCalendar.Date var2 = (TLocalGregorianCalendar.Date)var1;
        TEra var3 = var2.getEra();
        if(var3 != null) {
            if(!this.validateEra(var3)) {
                return false;
            }

            var2.setNormalizedYear(var3.getSinceDate().getYear() + var2.getYear() - 1);
            TLocalGregorianCalendar.Date var4 = this.newCalendarDate(var1.getZone());
            var4.setEra(var3).setDate(var1.getYear(), var1.getMonth(), var1.getDayOfMonth());
            this.normalize(var4);
            if(var4.getEra() != var3) {
                return false;
            }
        } else {
            if(var1.getYear() >= this.eras[0].getSinceDate().getYear()) {
                return false;
            }

            var2.setNormalizedYear(var2.getYear());
        }

        return super.validate(var2);
    }

    private boolean validateEra(TEra var1) {
        for(int var2 = 0; var2 < this.eras.length; ++var2) {
            if(var1 == this.eras[var2]) {
                return true;
            }
        }

        return false;
    }

    public boolean normalize(TCalendarDate var1) {
        if(var1.isNormalized()) {
            return true;
        } else {
            this.normalizeYear(var1);
            TLocalGregorianCalendar.Date var2 = (TLocalGregorianCalendar.Date)var1;
            super.normalize(var2);
            boolean var3 = false;
            long var4 = 0L;
            int var6 = var2.getNormalizedYear();
            TEra var8 = null;

            int var7;
            for(var7 = this.eras.length - 1; var7 >= 0; --var7) {
                var8 = this.eras[var7];
                if(var8.isLocalTime()) {
                    TCalendarDate var9 = var8.getSinceDate();
                    int var10 = var9.getYear();
                    if(var6 > var10) {
                        break;
                    }

                    if(var6 == var10) {
                        int var11 = var2.getMonth();
                        int var12 = var9.getMonth();
                        if(var11 > var12) {
                            break;
                        }

                        if(var11 == var12) {
                            int var13 = var2.getDayOfMonth();
                            int var14 = var9.getDayOfMonth();
                            if(var13 > var14) {
                                break;
                            }

                            if(var13 == var14) {
                                long var15 = var2.getTimeOfDay();
                                long var17 = var9.getTimeOfDay();
                                if(var15 < var17) {
                                    --var7;
                                }
                                break;
                            }
                        }
                    }
                } else {
                    if(!var3) {
                        var4 = super.getTime(var1);
                        var3 = true;
                    }

                    long var19 = var8.getSince(var1.getZone());
                    if(var4 >= var19) {
                        break;
                    }
                }
            }

            if(var7 >= 0) {
                var2.setLocalEra(var8);
                int var20 = var2.getNormalizedYear() - var8.getSinceDate().getYear() + 1;
                var2.setLocalYear(var20);
            } else {
                var2.setEra((TEra)null);
                var2.setLocalYear(var6);
                var2.setNormalizedYear(var6);
            }

            var2.setNormalized(true);
            return true;
        }
    }

    void normalizeMonth(TCalendarDate var1) {
        this.normalizeYear(var1);
        super.normalizeMonth(var1);
    }

    void normalizeYear(TCalendarDate var1) {
        TLocalGregorianCalendar.Date var2 = (TLocalGregorianCalendar.Date)var1;
        TEra var3 = var2.getEra();
        if(var3 != null && this.validateEra(var3)) {
            var2.setNormalizedYear(var3.getSinceDate().getYear() + var2.getYear() - 1);
        } else {
            var2.setNormalizedYear(var2.getYear());
        }

    }

    public boolean isLeapYear(int var1) {
        return TCalendarUtils.isGregorianLeapYear(var1);
    }

    public boolean isLeapYear(TEra var1, int var2) {
        if(var1 == null) {
            return this.isLeapYear(var2);
        } else {
            int var3 = var1.getSinceDate().getYear() + var2 - 1;
            return this.isLeapYear(var3);
        }
    }

    public void getCalendarDateFromFixedDate(TCalendarDate var1, long var2) {
        TLocalGregorianCalendar.Date var4 = (TLocalGregorianCalendar.Date)var1;
        super.getCalendarDateFromFixedDate(var4, var2);
        this.adjustYear(var4, (var2 - 719163L) * 86400000L, 0);
    }

    public static class Date extends TBaseCalendar.Date {
        private int gregorianYear = -2147483648;

        protected Date() {
        }

        protected Date(TTimeZone var1) {
            super(var1);
        }

        public TLocalGregorianCalendar.Date setEra(TEra var1) {
            if(this.getEra() != var1) {
                super.setEra(var1);
                this.gregorianYear = -2147483648;
            }

            return this;
        }

        public TLocalGregorianCalendar.Date addYear(int var1) {
            super.addYear(var1);
            this.gregorianYear += var1;
            return this;
        }

        public TLocalGregorianCalendar.Date setYear(int var1) {
            if(this.getYear() != var1) {
                super.setYear(var1);
                this.gregorianYear = -2147483648;
            }

            return this;
        }

        public int getNormalizedYear() {
            return this.gregorianYear;
        }

        public void setNormalizedYear(int var1) {
            this.gregorianYear = var1;
        }

        void setLocalEra(TEra var1) {
            super.setEra(var1);
        }

        void setLocalYear(int var1) {
            super.setYear(var1);
        }

        public String toString() {
            String var1 = super.toString();
            var1 = var1.substring(var1.indexOf(84));
            StringBuffer var2 = new StringBuffer();
            TEra var3 = this.getEra();
            if(var3 != null) {
                String var4 = var3.getAbbreviation();
                if(var4 != null) {
                    var2.append(var4);
                }
            }

            var2.append(this.getYear()).append('.');
            TCalendarUtils.sprintf0d(var2, this.getMonth(), 2).append('.');
            TCalendarUtils.sprintf0d(var2, this.getDayOfMonth(), 2);
            var2.append(var1);
            return var2.toString();
        }
    }
}
