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
import org.teavm.classlib.java.lang.reflect.TField;

/**
 * Created by vasek on 4. 7. 2016.
 */
public interface TLangReflectAccess {
    TField newField(Class<?> var1, String var2, Class<?> var3, int var4, int var5, String var6, byte[] var7);

    TMethod newMethod(Class<?> var1, String var2, Class<?>[] var3, Class<?> var4, Class<?>[] var5, int var6, int var7, String var8, byte[] var9, byte[] var10, byte[] var11);

    <T> TConstructor<T> newConstructor(Class<T> var1, Class<?>[] var2, Class<?>[] var3, int var4, int var5, String var6, byte[] var7, byte[] var8);

    TMethodAccessor getMethodAccessor(TMethod var1);

    void setMethodAccessor(Method var1, TMethodAccessor var2);

    TConstructorAccessor getConstructorAccessor(Constructor<?> var1);

    void setConstructorAccessor(Constructor<?> var1, TConstructorAccessor var2);

    byte[] getExecutableTypeAnnotationBytes(Executable var1);

    int getConstructorSlot(Constructor<?> var1);

    String getConstructorSignature(Constructor<?> var1);

    byte[] getConstructorAnnotations(Constructor<?> var1);

    byte[] getConstructorParameterAnnotations(Constructor<?> var1);

    Method copyMethod(Method var1);

    Field copyField(Field var1);

    <T> Constructor<T> copyConstructor(Constructor<T> var1);
}
