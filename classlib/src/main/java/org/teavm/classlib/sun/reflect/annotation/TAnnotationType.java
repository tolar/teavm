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
package org.teavm.classlib.sun.reflect.annotation;

import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.annotation.TAnnotation;
import org.teavm.classlib.sun.misc.TJavaLangAccess;
import org.teavm.classlib.sun.misc.TSharedSecrets;

/**
 * Created by vasek on 9. 7. 2016.
 */
public class TAnnotationType {
    private final Map<String, Class<?>> memberTypes;
    private final Map<String, Object> memberDefaults;
    private final Map<String, Method> members;

    public static TAnnotationType getInstance(TClass<? extends TAnnotation> var0) {
        TJavaLangAccess var1 = TSharedSecrets.getJavaLangAccess();
        org.teavm.classlib.sun.reflect.annotation.TAnnotationType var2 = var1.getAnnotationType(var0);
        if(var2 == null) {
            var2 = new org.teavm.classlib.sun.reflect.annotation.TAnnotationType(var0);
            if(!var1.casAnnotationType(var0, (TAnnotationType)null, var2)) {
                var2 = var1.getAnnotationType(var0);

                assert var2 != null;
            }
        }

        return var2;
    }

    private TAnnotationType(final TClass<? extends TAnnotation> var1) {
        if(!var1.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type");
        } else {
            Method[] var2 = (Method[]) AccessController.doPrivileged(new PrivilegedAction() {
                public Method[] run() {
                    return var1.getDeclaredMethods();
                }
            });
            this.memberTypes = new HashMap(var2.length + 1, 1.0F);
            this.memberDefaults = new HashMap(0);
            this.members = new HashMap(var2.length + 1, 1.0F);
            Method[] var3 = var2;
            int var4 = var2.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Method var6 = var3[var5];
                if(var6.getParameterTypes().length != 0) {
                    throw new IllegalArgumentException(var6 + " has params");
                }

                String var7 = var6.getName();
                Class var8 = var6.getReturnType();
                this.memberTypes.put(var7, invocationHandlerReturnType(var8));
                this.members.put(var7, var6);
                Object var9 = var6.getDefaultValue();
                if(var9 != null) {
                    this.memberDefaults.put(var7, var9);
                }
            }


        }
    }

    public static Class<?> invocationHandlerReturnType(Class<?> var0) {
        return var0 == Byte.TYPE?Byte.class:(var0 == Character.TYPE?Character.class:(var0 == Double.TYPE?Double.class:(var0 == Float.TYPE?Float.class:(var0 == Integer.TYPE?Integer.class:(var0 == Long.TYPE?Long.class:(var0 == Short.TYPE?Short.class:(var0 == Boolean.TYPE?Boolean.class:var0)))))));
    }

    public Map<String, Class<?>> memberTypes() {
        return this.memberTypes;
    }

    public Map<String, Method> members() {
        return this.members;
    }

    public Map<String, Object> memberDefaults() {
        return this.memberDefaults;
    }

    public RetentionPolicy retention() {
        return null;
    }

    public boolean isInherited() {
        return false;
    }

    public String toString() {
        return "Annotation Type:\n   Member types: " + this.memberTypes + "\n" + "   Member defaults: " + this.memberDefaults + "\n" + "   Retention policy: " + null + "\n" + "   Inherited: " + false;
    }
}
