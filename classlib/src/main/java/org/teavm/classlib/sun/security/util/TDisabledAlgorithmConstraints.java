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

import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vasek on 25. 10. 2016.
 */
public class TDisabledAlgorithmConstraints extends TAbstractAlgorithmConstraints {
    public static final String PROPERTY_CERTPATH_DISABLED_ALGS = "jdk.certpath.disabledAlgorithms";
    public static final String PROPERTY_TLS_DISABLED_ALGS = "jdk.tls.disabledAlgorithms";
    private static final Map<String, String[]> disabledAlgorithmsMap = new HashMap();
    private static final Map<String, TDisabledAlgorithmConstraints.KeySizeConstraints> keySizeConstraintsMap = new HashMap();
    private final String[] disabledAlgorithms;
    private final TDisabledAlgorithmConstraints.KeySizeConstraints keySizeConstraints;

    public TDisabledAlgorithmConstraints(String var1) {
        this(var1, new TAlgorithmDecomposer());
    }

    public TDisabledAlgorithmConstraints(String var1, TAlgorithmDecomposer var2) {
        super(var2);
        this.disabledAlgorithms = getAlgorithms(disabledAlgorithmsMap, var1);
        this.keySizeConstraints = getKeySizeConstraints(this.disabledAlgorithms, var1);
    }

    public final boolean permits(Set<CryptoPrimitive> var1, String var2, AlgorithmParameters var3) {
        if(var1 != null && !var1.isEmpty()) {
            return checkAlgorithm(this.disabledAlgorithms, var2, this.decomposer);
        } else {
            throw new IllegalArgumentException("No cryptographic primitive specified");
        }
    }

    public final boolean permits(Set<CryptoPrimitive> var1, Key var2) {
        return this.checkConstraints(var1, "", var2, (AlgorithmParameters)null);
    }

    public final boolean permits(Set<CryptoPrimitive> var1, String var2, Key var3, AlgorithmParameters var4) {
        if(var2 != null && var2.length() != 0) {
            return this.checkConstraints(var1, var2, var3, var4);
        } else {
            throw new IllegalArgumentException("No algorithm name specified");
        }
    }

    private boolean checkConstraints(Set<CryptoPrimitive> var1, String var2, Key var3, AlgorithmParameters var4) {
        if(var3 == null) {
            throw new IllegalArgumentException("The key cannot be null");
        } else {
            return var2 != null && var2.length() != 0 && !this.permits(var1, var2, var4)?false:(!this.permits(var1, var3.getAlgorithm(), (AlgorithmParameters)null)?false:!this.keySizeConstraints.disables(var3));
        }
    }

    private static TDisabledAlgorithmConstraints.KeySizeConstraints getKeySizeConstraints(String[] var0, String var1) {
        Map var2 = keySizeConstraintsMap;
        synchronized(keySizeConstraintsMap) {
            if(!keySizeConstraintsMap.containsKey(var1)) {
                TDisabledAlgorithmConstraints.KeySizeConstraints var3 = new TDisabledAlgorithmConstraints.KeySizeConstraints(var0);
                keySizeConstraintsMap.put(var1, var3);
            }

            return (TDisabledAlgorithmConstraints.KeySizeConstraints)keySizeConstraintsMap.get(var1);
        }
    }

    private static class KeySizeConstraint {
        private int minSize;
        private int maxSize;
        private int prohibitedSize = -1;

        public KeySizeConstraint(TDisabledAlgorithmConstraints.KeySizeConstraint.Operator var1, int var2) {
            switch(null.$SwitchMap$sun$security$util$DisabledAlgorithmConstraints$KeySizeConstraint$Operator[var1.ordinal()]) {
                case 1:
                    this.minSize = 0;
                    this.maxSize = 2147483647;
                    this.prohibitedSize = var2;
                    break;
                case 2:
                    this.minSize = var2;
                    this.maxSize = var2;
                    break;
                case 3:
                    this.minSize = var2;
                    this.maxSize = 2147483647;
                    break;
                case 4:
                    this.minSize = var2 + 1;
                    this.maxSize = 2147483647;
                    break;
                case 5:
                    this.minSize = 0;
                    this.maxSize = var2;
                    break;
                case 6:
                    this.minSize = 0;
                    this.maxSize = var2 > 1?var2 - 1:0;
                    break;
                default:
                    this.minSize = 2147483647;
                    this.maxSize = -1;
            }

        }

        public boolean disables(Key var1) {
            int var2 = TKeyUtil.getKeySize(var1);
            return var2 == 0?true:(var2 <= 0?false:var2 < this.minSize || var2 > this.maxSize || this.prohibitedSize == var2);
        }

        static enum Operator {
            EQ,
            NE,
            LT,
            LE,
            GT,
            GE;

            private Operator() {
            }

            static TDisabledAlgorithmConstraints.KeySizeConstraint.Operator of(String var0) {
                byte var2 = -1;
                switch(var0.hashCode()) {
                    case 60:
                        if(var0.equals("<")) {
                            var2 = 2;
                        }
                        break;
                    case 62:
                        if(var0.equals(">")) {
                            var2 = 4;
                        }
                        break;
                    case 1084:
                        if(var0.equals("!=")) {
                            var2 = 1;
                        }
                        break;
                    case 1921:
                        if(var0.equals("<=")) {
                            var2 = 3;
                        }
                        break;
                    case 1952:
                        if(var0.equals("==")) {
                            var2 = 0;
                        }
                        break;
                    case 1983:
                        if(var0.equals(">=")) {
                            var2 = 5;
                        }
                }

                switch(var2) {
                    case 0:
                        return EQ;
                    case 1:
                        return NE;
                    case 2:
                        return LT;
                    case 3:
                        return LE;
                    case 4:
                        return GT;
                    case 5:
                        return GE;
                    default:
                        throw new IllegalArgumentException(var0 + " is not a legal Operator");
                }
            }
        }
    }

    private static class KeySizeConstraints {
        private static final Pattern pattern = Pattern.compile("(\\S+)\\s+keySize\\s*(<=|<|==|!=|>|>=)\\s*(\\d+)");
        private Map<String, Set<TDisabledAlgorithmConstraints.KeySizeConstraint>> constraintsMap = Collections
                .synchronizedMap(new HashMap());

        public KeySizeConstraints(String[] var1) {
            String[] var2 = var1;
            int var3 = var1.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String var5 = var2[var4];
                if(var5 != null && !var5.isEmpty()) {
                    Matcher var6 = pattern.matcher(var5);
                    if(var6.matches()) {
                        String var7 = var6.group(1);
                        TDisabledAlgorithmConstraints.KeySizeConstraint.Operator var8 = TDisabledAlgorithmConstraints.KeySizeConstraint.Operator.of(var6.group(2));
                        int var9 = Integer.parseInt(var6.group(3));
                        var7 = var7.toLowerCase(Locale.ENGLISH);
                        Map var10 = this.constraintsMap;
                        synchronized(this.constraintsMap) {
                            if(!this.constraintsMap.containsKey(var7)) {
                                this.constraintsMap.put(var7, new HashSet());
                            }

                            Set var11 = (Set)this.constraintsMap.get(var7);
                            TDisabledAlgorithmConstraints.KeySizeConstraint var12 = new TDisabledAlgorithmConstraints.KeySizeConstraint(var8, var9);
                            var11.add(var12);
                        }
                    }
                }
            }

        }

        public boolean disables(Key var1) {
            String var2 = var1.getAlgorithm().toLowerCase(Locale.ENGLISH);
            Map var3 = this.constraintsMap;
            synchronized(this.constraintsMap) {
                if(this.constraintsMap.containsKey(var2)) {
                    Set var4 = (Set)this.constraintsMap.get(var2);
                    Iterator var5 = var4.iterator();

                    while(var5.hasNext()) {
                        TDisabledAlgorithmConstraints.KeySizeConstraint var6 = (TDisabledAlgorithmConstraints.KeySizeConstraint)var5.next();
                        if(var6.disables(var1)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }
    }
}
