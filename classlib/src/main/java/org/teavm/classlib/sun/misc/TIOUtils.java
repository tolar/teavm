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
package org.teavm.classlib.sun.misc;

import java.util.Arrays;

import org.teavm.classlib.java.io.TEOFException;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.lang.TString;

public class TIOUtils {
    public TIOUtils() {
    }

    public static byte[] readFully(TInputStream var0, int var1, boolean var2) throws TIOException {
        byte[] var3 = new byte[0];
        if(var1 == -1) {
            var1 = 2147483647;
        }

        int var6;
        for(int var4 = 0; var4 < var1; var4 += var6) {
            int var5;
            if(var4 >= var3.length) {
                var5 = Math.min(var1 - var4, var3.length + 1024);
                if(var3.length < var4 + var5) {
                    var3 = Arrays.copyOf(var3, var4 + var5);
                }
            } else {
                var5 = var3.length - var4;
            }

            var6 = var0.read(var3, var4, var5);
            if(var6 < 0) {
                if(var2 && var1 != 2147483647) {
                    throw new TEOFException(TString.wrap("Detect premature EOF"));
                }

                if(var3.length != var4) {
                    var3 = Arrays.copyOf(var3, var4);
                }
                break;
            }
        }

        return var3;
    }
}
