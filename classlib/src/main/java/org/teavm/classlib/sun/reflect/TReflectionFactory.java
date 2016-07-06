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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;

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

    public TFieldAccessor newFieldAccessor(Field var1, boolean var2) {
        checkInitted();
        return TUnsafeFieldAccessorFactory.newFieldAccessor(var1, var2);
    }

    public TMethodAccessor newMethodAccessor(Method var1) {
        checkInitted();
        if(noInflation && !ReflectUtil.isVMAnonymousClass(var1.getDeclaringClass())) {
            return (new MethodAccessorGenerator()).generateMethod(var1.getDeclaringClass(), var1.getName(), var1.getParameterTypes(), var1.getReturnType(), var1.getExceptionTypes(), var1.getModifiers());
        } else {
            NativeMethodAccessorImpl var2 = new NativeMethodAccessorImpl(var1);
            DelegatingMethodAccessorImpl var3 = new DelegatingMethodAccessorImpl(var2);
            var2.setParent(var3);
            return var3;
        }
    }

    public TConstructorAccessor newConstructorAccessor(Constructor<?> var1) {
        checkInitted();
        Class var2 = var1.getDeclaringClass();
        if(Modifier.isAbstract(var2.getModifiers())) {
            return new InstantiationExceptionConstructorAccessorImpl((String)null);
        } else if(var2 == Class.class) {
            return new InstantiationExceptionConstructorAccessorImpl("Can not instantiate java.lang.Class");
        } else if(Reflection.isSubclassOf(var2, ConstructorAccessorImpl.class)) {
            return new BootstrapConstructorAccessorImpl(var1);
        } else if(noInflation && !ReflectUtil.isVMAnonymousClass(var1.getDeclaringClass())) {
            return (new MethodAccessorGenerator()).generateConstructor(var1.getDeclaringClass(), var1.getParameterTypes(), var1.getExceptionTypes(), var1.getModifiers());
        } else {
            NativeConstructorAccessorImpl var3 = new NativeConstructorAccessorImpl(var1);
            DelegatingConstructorAccessorImpl var4 = new DelegatingConstructorAccessorImpl(var3);
            var3.setParent(var4);
            return var4;
        }
    }

    public Field newField(Class<?> var1, String var2, Class<?> var3, int var4, int var5, String var6, byte[] var7) {
        return langReflectAccess().newField(var1, var2, var3, var4, var5, var6, var7);
    }

    public Method newMethod(Class<?> var1, String var2, Class<?>[] var3, Class<?> var4, Class<?>[] var5, int var6, int var7, String var8, byte[] var9, byte[] var10, byte[] var11) {
        return langReflectAccess().newMethod(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11);
    }

    public Constructor<?> newConstructor(Class<?> var1, Class<?>[] var2, Class<?>[] var3, int var4, int var5, String var6, byte[] var7, byte[] var8) {
        return langReflectAccess().newConstructor(var1, var2, var3, var4, var5, var6, var7, var8);
    }

    public TMethodAccessor getMethodAccessor(Method var1) {
        return langReflectAccess().getMethodAccessor(var1);
    }

    public void setMethodAccessor(Method var1, TMethodAccessor var2) {
        langReflectAccess().setMethodAccessor(var1, var2);
    }

    public TConstructorAccessor getConstructorAccessor(Constructor<?> var1) {
        return langReflectAccess().getConstructorAccessor(var1);
    }

    public void setConstructorAccessor(Constructor<?> var1, TConstructorAccessor var2) {
        langReflectAccess().setConstructorAccessor(var1, var2);
    }

    public Method copyMethod(Method var1) {
        return langReflectAccess().copyMethod(var1);
    }

    public Field copyField(Field var1) {
        return langReflectAccess().copyField(var1);
    }

    public <T> Constructor<T> copyConstructor(Constructor<T> var1) {
        return langReflectAccess().copyConstructor(var1);
    }

    public byte[] getExecutableTypeAnnotationBytes(Executable var1) {
        return langReflectAccess().getExecutableTypeAnnotationBytes(var1);
    }

    public Constructor<?> newConstructorForSerialization(Class<?> var1, Constructor<?> var2) {
        if(var2.getDeclaringClass() == var1) {
            return var2;
        } else {
            SerializationConstructorAccessorImpl
                    var3 = (new MethodAccessorGenerator()).generateSerializationConstructor(var1, var2.getParameterTypes(), var2.getExceptionTypes(), var2.getModifiers(), var2.getDeclaringClass());
            Constructor var4 = this.newConstructor(var2.getDeclaringClass(), var2.getParameterTypes(), var2.getExceptionTypes(), var2.getModifiers(), langReflectAccess().getConstructorSlot(var2), langReflectAccess().getConstructorSignature(var2), langReflectAccess().getConstructorAnnotations(var2), langReflectAccess().getConstructorParameterAnnotations(var2));
            this.setConstructorAccessor(var4, var3);
            return var4;
        }
    }

    static int inflationThreshold() {
        return inflationThreshold;
    }

    private static void checkInitted() {
        if(!initted) {
            AccessController.doPrivileged(new PrivilegedAction() {
                public Void run() {
                    if(System.out == null) {
                        return null;
                    } else {
                        String var1 = System.getProperty("sun.reflect.noInflation");
                        if(var1 != null && var1.equals("true")) {
                            sun.reflect.ReflectionFactory.noInflation = true;
                        }

                        var1 = System.getProperty("sun.reflect.inflationThreshold");
                        if(var1 != null) {
                            try {
                                sun.reflect.ReflectionFactory.inflationThreshold = Integer.parseInt(var1);
                            } catch (NumberFormatException var3) {
                                throw new RuntimeException("Unable to parse property sun.reflect.inflationThreshold", var3);
                            }
                        }

                        sun.reflect.ReflectionFactory.initted = true;
                        return null;
                    }
                }
            });
        }
    }

    private static TLangReflectAccess langReflectAccess() {
        if(langReflectAccess == null) {
            Modifier.isPublic(1);
        }

        return langReflectAccess;
    }

    public static final class GetReflectionFactoryAction implements PrivilegedAction<sun.reflect.ReflectionFactory> {
        public GetReflectionFactoryAction() {
        }

        public org.teavm.classlib.sun.reflect.TReflectionFactory run() {
            return org.teavm.classlib.sun.reflect.TReflectionFactory.getReflectionFactory();
        }
    }
}
