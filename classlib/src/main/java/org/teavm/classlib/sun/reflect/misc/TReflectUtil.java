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
package org.teavm.classlib.sun.reflect.misc;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TClassLoader;
import org.teavm.classlib.java.lang.TIllegalAccessException;
import org.teavm.classlib.java.lang.TInstantiationException;
import org.teavm.classlib.java.lang.TObject;
import org.teavm.classlib.sun.reflect.TReflection;
import org.teavm.classlib.sun.security.util.TSecurityConstants;

/**
 * Created by vasek on 5. 7. 2016.
 */
public final class TReflectUtil {
    public static final String PROXY_PACKAGE = "com.sun.proxy";

    private TReflectUtil() {
    }

    public static Class<?> forName(String var0) throws ClassNotFoundException {
        checkPackageAccess(var0);
        return Class.forName(var0);
    }

    public static Object newInstance(TClass<?> var0) throws TInstantiationException, TIllegalAccessException {
        checkPackageAccess(var0);
        return var0.newInstance();
    }

    public static void ensureMemberAccess(TClass<?> var0, TClass<?> var1, TObject var2, int var3) throws IllegalAccessException {
        if(var2 == null && Modifier.isProtected(var3)) {
            int var4 = var3 & -5;
            var4 |= 1;
            TReflection.ensureMemberAccess(var0, var1, var2, var4);

            try {
                var4 &= -2;
                TReflection.ensureMemberAccess(var0, var1, var2, var4);
            } catch (IllegalAccessException var6) {
                if(!isSubclassOf(var0, var1)) {
                    throw var6;
                }
            }
        } else {
            TReflection.ensureMemberAccess(var0, var1, var2, var3);
        }
    }

    private static boolean isSubclassOf(TClass<?> var0, TClass<?> var1) {
        while(var0 != null) {
            if(var0 == var1) {
                return true;
            }

            var0 = var0.getSuperclass();
        }

        return false;
    }

    public static void conservativeCheckMemberAccess(Member var0) throws SecurityException {
        SecurityManager var1 = System.getSecurityManager();
        if(var1 != null) {
            Class var2 = var0.getDeclaringClass();
            checkPackageAccess(var2);
            if(!Modifier.isPublic(var0.getModifiers()) || !Modifier.isPublic(var2.getModifiers())) {
                var1.checkPermission(TSecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
            }
        }
    }

    public static void checkPackageAccess(TClass<?> var0) {
        checkPackageAccess(var0.getName());
        if(isNonPublicProxyClass(var0)) {
            checkProxyPackageAccess(var0);
        }

    }

    public static void checkPackageAccess(String var0) {
        SecurityManager var1 = System.getSecurityManager();
        if(var1 != null) {
            String var2 = var0.replace('/', '.');
            int var3;
            if(var2.startsWith("[")) {
                var3 = var2.lastIndexOf(91) + 2;
                if(var3 > 1 && var3 < var2.length()) {
                    var2 = var2.substring(var3);
                }
            }

            var3 = var2.lastIndexOf(46);
            if(var3 != -1) {
                var1.checkPackageAccess(var2.substring(0, var3));
            }
        }

    }

    public static boolean isPackageAccessible(Class<?> var0) {
        try {
            checkPackageAccess(var0);
            return true;
        } catch (SecurityException var2) {
            return false;
        }
    }

    private static boolean isAncestor(TClassLoader var0, TClassLoader var1) {
        TClassLoader var2 = var1;

        do {
            var2 = var2.getParent();
            if(var0 == var2) {
                return true;
            }
        } while(var2 != null);

        return false;
    }

    public static boolean needsPackageAccessCheck(TClassLoader var0, TClassLoader var1) {
        return var0 != null && var0 != var1?(var1 == null?true:!isAncestor(var0, var1)):false;
    }

    public static void checkProxyPackageAccess(Class<?> var0) {
        SecurityManager var1 = System.getSecurityManager();
        if(var1 != null && Proxy.isProxyClass(var0)) {
            Class[] var2 = var0.getInterfaces();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Class var5 = var2[var4];
                checkPackageAccess(var5);
            }
        }

    }

    public static void checkProxyPackageAccess(TClassLoader var0, Class... var1) {
        SecurityManager var2 = System.getSecurityManager();
        if(var2 != null) {
            Class[] var3 = var1;
            int var4 = var1.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Class var6 = var3[var5];
                TClassLoader var7 = var6.getClassLoader();
                if(needsPackageAccessCheck(var0, var7)) {
                    checkPackageAccess(var6);
                }
            }
        }

    }

    public static boolean isNonPublicProxyClass(Class<?> var0) {
        String var1 = var0.getName();
        int var2 = var1.lastIndexOf(46);
        String var3 = var2 != -1?var1.substring(0, var2):"";
        return Proxy.isProxyClass(var0) && !var3.equals("com.sun.proxy");
    }

    public static void checkProxyMethod(Object var0, Method var1) {
        if(var0 != null && Proxy.isProxyClass(var0.getClass())) {
            if(Modifier.isStatic(var1.getModifiers())) {
                throw new IllegalArgumentException("Can\'t handle static method");
            } else {
                Class var2 = var1.getDeclaringClass();
                if(var2 == Object.class) {
                    String var3 = var1.getName();
                    if(var3.equals("hashCode") || var3.equals("equals") || var3.equals("toString")) {
                        return;
                    }
                }

                if(!isSuperInterface(var0.getClass(), var2)) {
                    throw new IllegalArgumentException("Can\'t handle: " + var1);
                }
            }
        } else {
            throw new IllegalArgumentException("Not a Proxy instance");
        }
    }

    private static boolean isSuperInterface(Class<?> var0, Class<?> var1) {
        Class[] var2 = var0.getInterfaces();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Class var5 = var2[var4];
            if(var5 == var1) {
                return true;
            }

            if(isSuperInterface(var5, var1)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isVMAnonymousClass(Class<?> var0) {
        return var0.getName().indexOf("/") > -1;
    }
}
