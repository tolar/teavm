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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by vasek on 4. 7. 2016.
 */
class TUnsafeFieldAccessorFactory {
    TUnsafeFieldAccessorFactory() {
    }

    static TFieldAccessor newFieldAccessor(Field var0, boolean var1) {
        Class var2 = var0.getType();
        boolean var3 = Modifier.isStatic(var0.getModifiers());
        boolean var4 = Modifier.isFinal(var0.getModifiers());
        boolean var5 = Modifier.isVolatile(var0.getModifiers());
        boolean var6 = var4 || var5;
        boolean var7 = var4 && (var3 || !var1);
        if(var3) {
            TUnsafeFieldAccessorImpl.T_UNSAFE.ensureClassInitialized(var0.getDeclaringClass());
            return (TFieldAccessor)(!var6?(var2 == Boolean.TYPE?new TUnsafeStaticBooleanFieldAccessorImpl(var0):
                    (var2 == Byte.TYPE?new TUnsafeStaticByteFieldAccessorImpl(var0):
                            (var2 == Short.TYPE?new TUnsafeStaticShortFieldAccessorImpl(var0):
                                    (var2 == Character.TYPE?new TUnsafeStaticCharacterFieldAccessorImpl(var0):
                                            (var2 == Integer.TYPE?new TUnsafeStaticIntegerFieldAccessorImpl(var0):
                                                    (var2 == Long.TYPE?new TUnsafeStaticLongFieldAccessorImpl(var0):
                                                            (var2 == Float.TYPE?new TUnsafeStaticFloatFieldAccessorImpl(var0):
                                                                    (var2 == Double.TYPE?new TUnsafeStaticDoubleFieldAccessorImpl(var0):
                                                                            new TUnsafeStaticObjectFieldAccessorImpl(var0))))))))):
                    (var2 == Boolean.TYPE?new TUnsafeQualifiedStaticBooleanFieldAccessorImpl(var0, var7):
                            (var2 == Byte.TYPE?new TUnsafeQualifiedStaticByteFieldAccessorImpl(var0, var7):
                                    (var2 == Short.TYPE?new TUnsafeQualifiedStaticShortFieldAccessorImpl(var0, var7):
                                            (var2 == Character.TYPE?new TUnsafeQualifiedStaticCharacterFieldAccessorImpl(var0, var7):
                                                    (var2 == Integer.TYPE?new TUnsafeQualifiedStaticIntegerFieldAccessorImpl(var0, var7):
                                                            (var2 == Long.TYPE?new TUnsafeQualifiedStaticLongFieldAccessorImpl(var0, var7):
                                                                    (var2 == Float.TYPE?new TUnsafeQualifiedStaticFloatFieldAccessorImpl(var0, var7):
                                                                            (var2 == Double.TYPE?new TUnsafeQualifiedStaticDoubleFieldAccessorImpl(var0, var7):
                                                                                    new TUnsafeQualifiedStaticObjectFieldAccessorImpl(var0, var7))))))))));
        } else {
            return (TFieldAccessor)(!var6?(var2 == Boolean.TYPE?new TUnsafeBooleanFieldAccessorImpl(var0):
                    (var2 == Byte.TYPE?new TUnsafeByteFieldAccessorImpl(var0):
                            (var2 == Short.TYPE?new TUnsafeShortFieldAccessorImpl(var0):
                                    (var2 == Character.TYPE?new TUnsafeCharacterFieldAccessorImpl(var0):
                                            (var2 == Integer.TYPE?new TUnsafeIntegerFieldAccessorImpl(var0):
                                                    (var2 == Long.TYPE?new TUnsafeLongFieldAccessorImpl(var0):
                                                            (var2 == Float.TYPE?new TUnsafeFloatFieldAccessorImpl(var0):
                                                                    (var2 == Double.TYPE?new TUnsafeDoubleFieldAccessorImpl(var0):
                                                                            new TUnsafeObjectFieldAccessorImpl(var0))))))))):
                    (var2 == Boolean.TYPE?new TUnsafeQualifiedBooleanFieldAccessorImpl(var0, var7):
                            (var2 == Byte.TYPE?new TUnsafeQualifiedByteFieldAccessorImpl(var0, var7):
                                    (var2 == Short.TYPE?new TUnsafeQualifiedShortFieldAccessorImpl(var0, var7):
                                            (var2 == Character.TYPE?new TUnsafeQualifiedCharacterFieldAccessorImpl(var0, var7):
                                                    (var2 == Integer.TYPE?new TUnsafeQualifiedIntegerFieldAccessorImpl(var0, var7):
                                                            (var2 == Long.TYPE?new TUnsafeQualifiedLongFieldAccessorImpl(var0, var7):
                                                                    (var2 == Float.TYPE?new TUnsafeQualifiedFloatFieldAccessorImpl(var0, var7):
                                                                            (var2 == Double.TYPE?new TUnsafeQualifiedDoubleFieldAccessorImpl(var0, var7):
                                                                                    new TUnsafeQualifiedObjectFieldAccessorImpl(var0, var7))))))))));
        }
    }
}
