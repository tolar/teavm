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

import java.lang.reflect.Modifier;
import java.security.Permission;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.reflect.TConstructor;
import org.teavm.classlib.java.lang.reflect.TExecutable;
import org.teavm.classlib.java.lang.reflect.TField;
import org.teavm.classlib.java.lang.reflect.TMethod;
import org.teavm.classlib.java.security.TPrivilegedAction;

/**
 * Created by vasek on 4. 7. 2016.
 */
public class TReflectionFactory {
    private static boolean initted = false;
    private static final Permission reflectionFactoryAccessPerm = new RuntimePermission("reflectionFactoryAccess");
    private static final org.teavm.classlib.sun.reflect.TReflectionFactory soleInstance = new org.teavm.classlib.sun.reflect.TReflectionFactory();
    private static volatile TLangReflectAccess langReflectAccess;
    private static boolean noInflation = false;
    private static int inflationThreshold = 15;

    private TReflectionFactory() {
    }

    public static org.teavm.classlib.sun.reflect.TReflectionFactory getReflectionFactory() {
        SecurityManager var0 = System.getSecurityManager();
        if(var0 != null) {
            var0.checkPermission(reflectionFactoryAccessPerm);
        }

        return soleInstance;
    }

    public void setLangReflectAccess(TLangReflectAccess var1) {
        langReflectAccess = var1;
    }

    public TFieldAccessor newFieldAccessor(TField var1, boolean var2) {
        checkInitted();
        return TUnsafeFieldAccessorFactory.newFieldAccessor(var1, var2);
    }

    public TMethodAccessor newMethodAccessor(TMethod var1) {
        return null;
    }

    public TConstructorAccessor newConstructorAccessor(TConstructor<?> var1) {
        return null;
    }

    public TField newField(TClass<?> var1, String var2, Class<?> var3, int var4, int var5, String var6, byte[] var7) {
        return langReflectAccess().newField(var1, var2, var3, var4, var5, var6, var7);
    }

    public TMethod newMethod(TClass<?> var1, String var2, Class<?>[] var3, Class<?> var4, Class<?>[] var5, int var6, int var7, String var8, byte[] var9, byte[] var10, byte[] var11) {
        return langReflectAccess().newMethod(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11);
    }

    public TConstructor<?> newConstructor(Class<?> var1, Class<?>[] var2, Class<?>[] var3, int var4, int var5, String var6, byte[] var7, byte[] var8) {
        return langReflectAccess().newConstructor(var1, var2, var3, var4, var5, var6, var7, var8);
    }

    public TMethodAccessor getMethodAccessor(TMethod var1) {
        return langReflectAccess().getMethodAccessor(var1);
    }

    public void setMethodAccessor(TMethod var1, TMethodAccessor var2) {
        langReflectAccess().setMethodAccessor(var1, var2);
    }

    public TConstructorAccessor getConstructorAccessor(TConstructor<?> var1) {
        return langReflectAccess().getConstructorAccessor(var1);
    }

    public void setConstructorAccessor(TConstructor<?> var1, TConstructorAccessor var2) {
        langReflectAccess().setConstructorAccessor(var1, var2);
    }

    public TMethod copyMethod(TMethod var1) {
        return langReflectAccess().copyMethod(var1);
    }

    public TField copyField(TField var1) {
        return langReflectAccess().copyField(var1);
    }

    public <T> TConstructor<T> copyConstructor(TConstructor<T> var1) {
        return langReflectAccess().copyConstructor(var1);
    }

    public byte[] getExecutableTypeAnnotationBytes(TExecutable var1) {
        return langReflectAccess().getExecutableTypeAnnotationBytes(var1);
    }

    public TConstructor<?> newConstructorForSerialization(TClass<?> var1, TConstructor<?> var2) {
        return null;
    }

    static int inflationThreshold() {
        return inflationThreshold;
    }

    private static void checkInitted() {
    }

    private static TLangReflectAccess langReflectAccess() {
        if(langReflectAccess == null) {
            Modifier.isPublic(1);
        }

        return langReflectAccess;
    }

    public static final class GetReflectionFactoryAction implements TPrivilegedAction<TReflectionFactory> {
        public GetReflectionFactoryAction() {
        }

        public org.teavm.classlib.sun.reflect.TReflectionFactory run() {
            return org.teavm.classlib.sun.reflect.TReflectionFactory.getReflectionFactory();
        }
    }
}
