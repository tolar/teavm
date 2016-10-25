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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by vasek on 25. 10. 2016.
 */
public class TAlgorithmDecomposer {
    private static final Pattern transPattern = Pattern.compile("/");
    private static final Pattern pattern = Pattern.compile("with|and", 2);

    public TAlgorithmDecomposer() {
    }

    public Set<String> decompose(String var1) {
        if(var1 != null && var1.length() != 0) {
            String[] var2 = transPattern.split(var1);
            HashSet var3 = new HashSet();
            String[] var4 = var2;
            int var5 = var2.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String var7 = var4[var6];
                if(var7 != null && var7.length() != 0) {
                    String[] var8 = pattern.split(var7);
                    String[] var9 = var8;
                    int var10 = var8.length;

                    for(int var11 = 0; var11 < var10; ++var11) {
                        String var12 = var9[var11];
                        if(var12 != null && var12.length() != 0) {
                            var3.add(var12);
                        }
                    }
                }
            }

            if(var3.contains("SHA1") && !var3.contains("SHA-1")) {
                var3.add("SHA-1");
            }

            if(var3.contains("SHA-1") && !var3.contains("SHA1")) {
                var3.add("SHA1");
            }

            if(var3.contains("SHA224") && !var3.contains("SHA-224")) {
                var3.add("SHA-224");
            }

            if(var3.contains("SHA-224") && !var3.contains("SHA224")) {
                var3.add("SHA224");
            }

            if(var3.contains("SHA256") && !var3.contains("SHA-256")) {
                var3.add("SHA-256");
            }

            if(var3.contains("SHA-256") && !var3.contains("SHA256")) {
                var3.add("SHA256");
            }

            if(var3.contains("SHA384") && !var3.contains("SHA-384")) {
                var3.add("SHA-384");
            }

            if(var3.contains("SHA-384") && !var3.contains("SHA384")) {
                var3.add("SHA384");
            }

            if(var3.contains("SHA512") && !var3.contains("SHA-512")) {
                var3.add("SHA-512");
            }

            if(var3.contains("SHA-512") && !var3.contains("SHA512")) {
                var3.add("SHA512");
            }

            return var3;
        } else {
            return new HashSet();
        }
    }
}
