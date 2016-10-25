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
package org.teavm.classlib.sun.security.util;

import java.security.AccessController;
import java.security.Security;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.teavm.classlib.java.security.TAlgorithmConstraints;

/**
 * Created by vasek on 25. 10. 2016.
 */
public abstract class TAbstractAlgorithmConstraints implements TAlgorithmConstraints {
    protected final TAlgorithmDecomposer decomposer;

    protected TAbstractAlgorithmConstraints(TAlgorithmDecomposer var1) {
        this.decomposer = var1;
    }

    private static void loadAlgorithmsMap(Map<String, String[]> var0, String var1) {
        String var2 = (String) AccessController.doPrivileged(() -> {
            return Security.getProperty(var1);
        });
        String[] var3 = null;
        if(var2 != null && !var2.isEmpty()) {
            if(var2.length() >= 2 && var2.charAt(0) == 34 && var2.charAt(var2.length() - 1) == 34) {
                var2 = var2.substring(1, var2.length() - 1);
            }

            var3 = var2.split(",");

            for(int var4 = 0; var4 < var3.length; ++var4) {
                var3[var4] = var3[var4].trim();
            }
        }

        if(var3 == null) {
            var3 = new String[0];
        }

        var0.put(var1, var3);
    }

    static String[] getAlgorithms(Map<String, String[]> var0, String var1) {
        synchronized(var0) {
            if(!var0.containsKey(var1)) {
                loadAlgorithmsMap(var0, var1);
            }

            return (String[])var0.get(var1);
        }
    }

    static boolean checkAlgorithm(String[] var0, String var1, TAlgorithmDecomposer var2) {
        if(var1 != null && var1.length() != 0) {
            Set var3 = null;
            String[] var4 = var0;
            int var5 = var0.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String var7 = var4[var6];
                if(var7 != null && !var7.isEmpty()) {
                    if(var7.equalsIgnoreCase(var1)) {
                        return false;
                    }

                    if(var3 == null) {
                        var3 = var2.decompose(var1);
                    }

                    Iterator var8 = var3.iterator();

                    while(var8.hasNext()) {
                        String var9 = (String)var8.next();
                        if(var7.equalsIgnoreCase(var9)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        } else {
            throw new IllegalArgumentException("No algorithm name specified");
        }
    }
}
