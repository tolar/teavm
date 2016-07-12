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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TClassLoader;
import org.teavm.classlib.java.lang.TObject;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.misc.TCallerSensitive;

/**
 * Created by vasek on 4. 7. 2016.
 */
public class TReflection {
    private static volatile Map<Class<?>, String[]> fieldFilterMap;
    private static volatile Map<Class<?>, String[]> methodFilterMap;

    public TReflection() {
    }

    @TCallerSensitive
    public static native TClass<?> getCallerClass();

    /** @deprecated */
    @Deprecated
    public static native TClass<?> getCallerClass(int var0);

    public static native int getClassAccessFlags(TClass<?> var0);

    public static boolean quickCheckMemberAccess(TClass<?> var0, int var1) {
        return Modifier.isPublic(getClassAccessFlags(var0) & var1);
    }

    public static void ensureMemberAccess(TClass<?> var0, TClass<?> var1, TObject var2, int var3) throws IllegalAccessException {
        if(var0 != null && var1 != null) {
            if(!verifyMemberAccess(var0, var1, var2, var3)) {
                throw new IllegalAccessException("Class " + var0.getName() + " can not access a member of class " + var1.getName() + " with modifiers \"" + Modifier.toString(var3) + "\"");
            }
        } else {
            throw new InternalError();
        }
    }

    public static boolean verifyMemberAccess(TClass<?> var0, TClass<?> var1, TObject var2, int var3) {
        return true;
    }

    private static boolean isSameClassPackage(TClass<?> var0, TClass<?> var1) {
        return isSameClassPackage(var0.getClassLoader(), var0.getName(), var1.getClassLoader(), var1.getName());
    }

    private static boolean isSameClassPackage(TClassLoader var0, TString var1, TClassLoader var2, TString var3) {
        if(var0 != var2) {
            return false;
        } else {
            int var4 = var1.lastIndexOf(46);
            int var5 = var3.lastIndexOf(46);
            if(var4 != -1 && var5 != -1) {
                int var6 = 0;
                int var7 = 0;
                if(var1.charAt(var6) == 91) {
                    do {
                        ++var6;
                    } while(var1.charAt(var6) == 91);

                    if(var1.charAt(var6) != 76) {
                        throw new InternalError("Illegal class name " + var1);
                    }
                }

                if(var3.charAt(var7) == 91) {
                    do {
                        ++var7;
                    } while(var3.charAt(var7) == 91);

                    if(var3.charAt(var7) != 76) {
                        throw new InternalError("Illegal class name " + var3);
                    }
                }

                int var8 = var4 - var6;
                int var9 = var5 - var7;
                return var8 != var9?false:var1.regionMatches(false, var6, var3, var7, var8);
            } else {
                return var4 == var5;
            }
        }
    }

    static boolean isSubclassOf(TClass<?> var0, TClass<?> var1) {
        while(var0 != null) {
            if(var0 == var1) {
                return true;
            }

            var0 = var0.getSuperclass();
        }

        return false;
    }

    public static synchronized void registerFieldsToFilter(Class<?> var0, String... var1) {
        fieldFilterMap = registerFilter(fieldFilterMap, var0, var1);
    }

    public static synchronized void registerMethodsToFilter(Class<?> var0, String... var1) {
        methodFilterMap = registerFilter(methodFilterMap, var0, var1);
    }

    private static Map<Class<?>, String[]> registerFilter(Map<Class<?>, String[]> var0, Class<?> var1, String... var2) {
        if(var0.get(var1) != null) {
            throw new IllegalArgumentException("Filter already registered: " + var1);
        } else {
            HashMap var3 = new HashMap(var0);
            var3.put(var1, var2);
            return var3;
        }
    }

    public static Field[] filterFields(Class<?> var0, Field[] var1) {
        return fieldFilterMap == null?var1:(Field[])((Field[])filter(var1, (String[])fieldFilterMap.get(var0)));
    }

    public static Method[] filterMethods(Class<?> var0, Method[] var1) {
        return methodFilterMap == null?var1:(Method[])((Method[])filter(var1, (String[])methodFilterMap.get(var0)));
    }

    private static Member[] filter(Member[] var0, String[] var1) {
        if(var1 != null && var0.length != 0) {
            int var2 = 0;
            Member[] var3 = var0;
            int var4 = var0.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Member var6 = var3[var5];
                boolean var7 = false;
                String[] var8 = var1;
                int var9 = var1.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    String var11 = var8[var10];
                    if(var6.getName() == var11) {
                        var7 = true;
                        break;
                    }
                }

                if(!var7) {
                    ++var2;
                }
            }

            var3 = (Member[])((Member[]) Array.newInstance(var0[0].getClass(), var2));
            var4 = 0;
            Member[] var14 = var0;
            int var15 = var0.length;

            for(int var16 = 0; var16 < var15; ++var16) {
                Member var17 = var14[var16];
                boolean var18 = false;
                String[] var19 = var1;
                int var20 = var1.length;

                for(int var12 = 0; var12 < var20; ++var12) {
                    String var13 = var19[var12];
                    if(var17.getName() == var13) {
                        var18 = true;
                        break;
                    }
                }

                if(!var18) {
                    var3[var4++] = var17;
                }
            }

            return var3;
        } else {
            return var0;
        }
    }

    public static boolean isCallerSensitive(Method var0) {
        return false;
    }

    private static boolean isExtClassLoader(ClassLoader var0) {
        for(ClassLoader var1 = ClassLoader.getSystemClassLoader(); var1 != null; var1 = var1.getParent()) {
            if(var1.getParent() == null && var1 == var0) {
                return true;
            }
        }

        return false;
    }

    static {
        HashMap var0 = new HashMap();
        var0.put(TReflection.class, new String[]{"fieldFilterMap", "methodFilterMap"});
        var0.put(System.class, new String[]{"security"});
        var0.put(Class.class, new String[]{"classLoader"});
        fieldFilterMap = var0;
        methodFilterMap = new HashMap();
    }
}
