/*
 *  Copyright 2016 vasek.
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
package org.teavm.classlib.sun.reflect;

/**
 * Created by vasek on 6. 7. 2016.
 */
class TUTF8 {
    TUTF8() {
    }

    static byte[] encode(String var0) {
        int var1 = var0.length();
        byte[] var2 = new byte[utf8Length(var0)];
        int var3 = 0;

        try {
            for(int var4 = 0; var4 < var1; ++var4) {
                int var5 = var0.charAt(var4) & '\uffff';
                if(var5 >= 1 && var5 <= 127) {
                    var2[var3++] = (byte)var5;
                } else if(var5 != 0 && (var5 < 128 || var5 > 2047)) {
                    var2[var3++] = (byte)(224 + (var5 >> 12));
                    var2[var3++] = (byte)(128 + (var5 >> 6 & 63));
                    var2[var3++] = (byte)(128 + (var5 & 63));
                } else {
                    var2[var3++] = (byte)(192 + (var5 >> 6));
                    var2[var3++] = (byte)(128 + (var5 & 63));
                }
            }

            return var2;
        } catch (ArrayIndexOutOfBoundsException var6) {
            throw new InternalError("Bug in sun.reflect bootstrap UTF-8 encoder", var6);
        }
    }

    private static int utf8Length(String var0) {
        int var1 = var0.length();
        int var2 = 0;

        for(int var3 = 0; var3 < var1; ++var3) {
            int var4 = var0.charAt(var3) & '\uffff';
            if(var4 >= 1 && var4 <= 127) {
                ++var2;
            } else if(var4 != 0 && (var4 < 128 || var4 > 2047)) {
                var2 += 3;
            } else {
                var2 += 2;
            }
        }

        return var2;
    }
}
