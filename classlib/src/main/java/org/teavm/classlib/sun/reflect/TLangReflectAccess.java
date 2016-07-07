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

import org.teavm.classlib.java.lang.reflect.TConstructor;
import org.teavm.classlib.java.lang.reflect.TExecutable;
import org.teavm.classlib.java.lang.reflect.TField;
import org.teavm.classlib.java.lang.reflect.TMethod;

/**
 * Created by vasek on 4. 7. 2016.
 */
public interface TLangReflectAccess {
    TField newField(Class<?> var1, String var2, Class<?> var3, int var4, int var5, String var6, byte[] var7);

    TMethod newMethod(Class<?> var1, String var2, Class<?>[] var3, Class<?> var4, Class<?>[] var5, int var6, int var7, String var8, byte[] var9, byte[] var10, byte[] var11);

    <T> TConstructor<T> newConstructor(Class<T> var1, Class<?>[] var2, Class<?>[] var3, int var4, int var5, String var6, byte[] var7, byte[] var8);

    TMethodAccessor getMethodAccessor(TMethod var1);

    void setMethodAccessor(TMethod var1, TMethodAccessor var2);

    TConstructorAccessor getConstructorAccessor(TConstructor<?> var1);

    void setConstructorAccessor(TConstructor<?> var1, TConstructorAccessor var2);

    byte[] getExecutableTypeAnnotationBytes(TExecutable var1);

    int getConstructorSlot(TConstructor<?> var1);

    String getConstructorSignature(TConstructor<?> var1);

    byte[] getConstructorAnnotations(TConstructor<?> var1);

    byte[] getConstructorParameterAnnotations(TConstructor<?> var1);

    TMethod copyMethod(TMethod var1);

    TField copyField(TField var1);

    <T> TConstructor<T> copyConstructor(TConstructor<T> var1);
}
