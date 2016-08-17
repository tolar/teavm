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
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.SimpleTimeZone;

import org.teavm.classlib.java.util.TDate;
import org.teavm.classlib.java.util.TSimpleTimeZone;
import org.teavm.classlib.java.util.TTimeZone;

public class TZoneInfo extends TTimeZone {
    private static final int UTC_TIME = 0;
    private static final int STANDARD_TIME = 1;
    private static final int WALL_TIME = 2;
    private static final long OFFSET_MASK = 15L;
    private static final long DST_MASK = 240L;
    private static final int DST_NSHIFT = 4;
    private static final long ABBR_MASK = 3840L;
    private static final int TRANSITION_NSHIFT = 12;
    private static final TCalendarSystem gcal = TCalendarSystem.getGregorianCalendar();
    private int rawOffset;
    private int rawOffsetDiff;
    private int checksum;
    private int dstSavings;
    private long[] transitions;
    private int[] offsets;
    private int[] simpleTimeZoneParams;
    private boolean willGMTOffsetChange;
    private transient boolean dirty;
    private static final long serialVersionUID = 2653134537216586139L;
    private transient SimpleTimeZone lastRule;

    public TZoneInfo() {
        this.rawOffsetDiff = 0;
        this.willGMTOffsetChange = false;
        this.dirty = false;
    }

    public TZoneInfo(String var1, int var2) {
        this(var1, var2, 0, 0, (long[])null, (int[])null, (int[])null, false);
    }

    TZoneInfo(String var1, int var2, int var3, int var4, long[] var5, int[] var6, int[] var7, boolean var8) {
        this.rawOffsetDiff = 0;
        this.willGMTOffsetChange = false;
        this.dirty = false;
        this.setID(var1);
        this.rawOffset = var2;
        this.dstSavings = var3;
        this.checksum = var4;
        this.transitions = var5;
        this.offsets = var6;
        this.simpleTimeZoneParams = var7;
        this.willGMTOffsetChange = var8;
    }

    public int getOffset(long var1) {
        return this.getOffsets(var1, (int[])null, 0);
    }

    public int getOffsets(long var1, int[] var3) {
        return this.getOffsets(var1, var3, 0);
    }

    public int getOffsetsByStandard(long var1, int[] var3) {
        return this.getOffsets(var1, var3, 1);
    }

    public int getOffsetsByWall(long var1, int[] var3) {
        return this.getOffsets(var1, var3, 2);
    }

    private int getOffsets(long var1, int[] var3, int var4) {
        int var5;
        if(this.transitions == null) {
            var5 = this.getLastRawOffset();
            if(var3 != null) {
                var3[0] = var5;
                var3[1] = 0;
            }

            return var5;
        } else {
            var1 -= (long)this.rawOffsetDiff;
            var5 = this.getTransitionIndex(var1, var4);
            if(var5 < 0) {
                int var12 = this.getLastRawOffset();
                if(var3 != null) {
                    var3[0] = var12;
                    var3[1] = 0;
                }

                return var12;
            } else {
                int var10;
                if(var5 < this.transitions.length) {
                    long var11 = this.transitions[var5];
                    int var13 = this.offsets[(int)(var11 & 15L)] + this.rawOffsetDiff;
                    if(var3 != null) {
                        int var9 = (int)(var11 >>> 4 & 15L);
                        var10 = var9 == 0?0:this.offsets[var9];
                        var3[0] = var13 - var10;
                        var3[1] = var10;
                    }

                    return var13;
                } else {
                    TSimpleTimeZone var6 = this.getLastRule();
                    int var7;
                    if(var6 != null) {
                        var7 = var6.getRawOffset();
                        long var8 = var1;
                        if(var4 != 0) {
                            var8 = var1 - (long)this.rawOffset;
                        }

                        var10 = var6.getOffset(var8) - this.rawOffset;
                        if(var10 > 0 && var6.getOffset(var8 - (long)var10) == var7) {
                            var10 = 0;
                        }

                        if(var3 != null) {
                            var3[0] = var7;
                            var3[1] = var10;
                        }

                        return var7 + var10;
                    } else {
                        var7 = this.getLastRawOffset();
                        if(var3 != null) {
                            var3[0] = var7;
                            var3[1] = 0;
                        }

                        return var7;
                    }
                }
            }
        }
    }

    private int getTransitionIndex(long var1, int var3) {
        int var4 = 0;
        int var5 = this.transitions.length - 1;

        while(var4 <= var5) {
            int var6 = (var4 + var5) / 2;
            long var7 = this.transitions[var6];
            long var9 = var7 >> 12;
            if(var3 != 0) {
                var9 += (long)this.offsets[(int)(var7 & 15L)];
            }

            if(var3 == 1) {
                int var11 = (int)(var7 >>> 4 & 15L);
                if(var11 != 0) {
                    var9 -= (long)this.offsets[var11];
                }
            }

            if(var9 < var1) {
                var4 = var6 + 1;
            } else {
                if(var9 <= var1) {
                    return var6;
                }

                var5 = var6 - 1;
            }
        }

        if(var4 >= this.transitions.length) {
            return var4;
        } else {
            return var4 - 1;
        }
    }

    public int getOffset(int var1, int var2, int var3, int var4, int var5, int var6) {
        if(var6 >= 0 && var6 < 86400000) {
            if(var1 == 0) {
                var2 = 1 - var2;
            } else if(var1 != 1) {
                throw new IllegalArgumentException();
            }

            TCalendarDate var7 = gcal.newCalendarDate((TTimeZone)null);
            var7.setDate(var2, var3 + 1, var4);
            if(!gcal.validate(var7)) {
                throw new IllegalArgumentException();
            } else if(var5 >= 1 && var5 <= 7) {
                if(this.transitions == null) {
                    return this.getLastRawOffset();
                } else {
                    long var8 = gcal.getTime(var7) + (long)var6;
                    var8 -= (long)this.rawOffset;
                    return this.getOffsets(var8, (int[])null, 0);
                }
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public synchronized void setRawOffset(int var1) {
        if(var1 != this.rawOffset + this.rawOffsetDiff) {
            this.rawOffsetDiff = var1 - this.rawOffset;
            if(this.lastRule != null) {
                this.lastRule.setRawOffset(var1);
            }

            this.dirty = true;
        }
    }

    public int getRawOffset() {
        if(!this.willGMTOffsetChange) {
            return this.rawOffset + this.rawOffsetDiff;
        } else {
            int[] var1 = new int[2];
            this.getOffsets(System.currentTimeMillis(), var1, 0);
            return var1[0];
        }
    }

    public boolean isDirty() {
        return this.dirty;
    }

    private int getLastRawOffset() {
        return this.rawOffset + this.rawOffsetDiff;
    }

    public boolean useDaylightTime() {
        return this.simpleTimeZoneParams != null;
    }

    public boolean observesDaylightTime() {
        if(this.simpleTimeZoneParams != null) {
            return true;
        } else if(this.transitions == null) {
            return false;
        } else {
            long var1 = System.currentTimeMillis() - (long)this.rawOffsetDiff;
            int var3 = this.getTransitionIndex(var1, 0);
            if(var3 < 0) {
                return false;
            } else {
                for(int var4 = var3; var4 < this.transitions.length; ++var4) {
                    if((this.transitions[var4] & 240L) != 0L) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public boolean inDaylightTime(TDate var1) {
        if(var1 == null) {
            throw new NullPointerException();
        } else if(this.transitions == null) {
            return false;
        } else {
            long var2 = var1.getTime() - (long)this.rawOffsetDiff;
            int var4 = this.getTransitionIndex(var2, 0);
            if(var4 < 0) {
                return false;
            } else if(var4 < this.transitions.length) {
                return (this.transitions[var4] & 240L) != 0L;
            } else {
                TSimpleTimeZone var5 = this.getLastRule();
                return var5 != null?var5.inDaylightTime(var1):false;
            }
        }
    }

    public int getDSTSavings() {
        return this.dstSavings;
    }

    public String toString() {
        return this.getClass().getName() + "[id=\"" + this.getID() + "\"" + ",offset=" + this.getLastRawOffset() + ",dstSavings=" + this.dstSavings + ",useDaylight=" + this.useDaylightTime() + ",transitions=" + (this.transitions != null?this.transitions.length:0) + ",lastRule=" + (this.lastRule == null?this.getLastRuleInstance():this.lastRule) + "]";
    }

    public static String[] getAvailableIDs() {
        return TZoneInfoFile.getZoneIds();
    }

    public static String[] getAvailableIDs(int var0) {
        return TZoneInfoFile.getZoneIds(var0);
    }

    public static TTimeZone getTimeZone(String var0) {
        return TZoneInfoFile.getZoneInfo(var0);
    }

    private synchronized TSimpleTimeZone getLastRule() {
        if(this.lastRule == null) {
            this.lastRule = this.getLastRuleInstance();
        }

        return this.lastRule;
    }

    public TSimpleTimeZone getLastRuleInstance() {
        return this.simpleTimeZoneParams == null?null:(this.simpleTimeZoneParams.length == 10?new SimpleTimeZone(this.getLastRawOffset(), this.getID(), this.simpleTimeZoneParams[0], this.simpleTimeZoneParams[1], this.simpleTimeZoneParams[2], this.simpleTimeZoneParams[3], this.simpleTimeZoneParams[4], this.simpleTimeZoneParams[5], this.simpleTimeZoneParams[6], this.simpleTimeZoneParams[7], this.simpleTimeZoneParams[8], this.simpleTimeZoneParams[9], this.dstSavings):new SimpleTimeZone(this.getLastRawOffset(), this.getID(), this.simpleTimeZoneParams[0], this.simpleTimeZoneParams[1], this.simpleTimeZoneParams[2], this.simpleTimeZoneParams[3], this.simpleTimeZoneParams[4], this.simpleTimeZoneParams[5], this.simpleTimeZoneParams[6], this.simpleTimeZoneParams[7], this.dstSavings));
    }

    public Object clone() {
        TZoneInfo var1 = (TZoneInfo)super.clone();
        var1.lastRule = null;
        return var1;
    }

    public int hashCode() {
        return this.getLastRawOffset() ^ this.checksum;
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof sun.util.calendar.ZoneInfo)) {
            return false;
        } else {
            TZoneInfo var2 = (TZoneInfo)var1;
            return this.getID().equals(var2.getID()) && this.getLastRawOffset() == var2.getLastRawOffset() && this.checksum == var2.checksum;
        }
    }

    public boolean hasSameRules(TTimeZone var1) {
        return this == var1?true:(var1 == null?false:(!(var1 instanceof TZoneInfo)?(this.getRawOffset() != var1.getRawOffset()?false:this.transitions == null && !this.useDaylightTime() && !var1.useDaylightTime()):(this.getLastRawOffset() != ((TZoneInfo)var1).getLastRawOffset()?false:this.checksum == ((TZoneInfo)var1).checksum)));
    }

    public static Map<String, String> getAliasTable() {
        return TZoneInfoFile.getAliasMap();
    }

    private void readObject(ObjectInputStream var1) throws IOException, ClassNotFoundException {
        var1.defaultReadObject();
        this.dirty = true;
    }
}