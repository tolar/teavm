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
package org.teavm.classlib.java.lang.reflect;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.reflect.TConstructorAccessor;
import org.teavm.classlib.sun.reflect.TMethodAccessor;

/**
 * Created by vasek on 4. 7. 2016.
 */
class TReflectAccess implements org.teavm.classlib.sun.reflect.TLangReflectAccess {
    public TField newField(TClass<?> declaringClass,
            String name,
            TClass<?> type,
            int modifiers,
            int slot,
            String signature,
            byte[] annotations)
    {
        return new TField(declaringClass,
                name,
                type,
                modifiers,
                slot,
                signature,
                annotations);
    }

    @Override
    public TField newField(TClass<?> var1, String var2, Class<?> var3, int var4, int var5, String var6, byte[] var7) {
        return null;
    }

    @Override
    public TMethod newMethod(TClass<?> var1, String var2, Class<?>[] var3, Class<?> var4, Class<?>[] var5, int var6,
            int var7, String var8, byte[] var9, byte[] var10, byte[] var11) {
        return null;
    }

    @Override
    public <T> TConstructor<T> newConstructor(Class<T> var1, Class<?>[] var2, Class<?>[] var3, int var4, int var5,
            String var6, byte[] var7, byte[] var8) {
        return null;
    }

    public TMethod newMethod(TClass<?> declaringClass,
            TString name,
            TClass<?>[] parameterTypes,
            TClass<?> returnType,
            TClass<?>[] checkedExceptions,
            int modifiers,
            int slot,
            String signature,
            byte[] annotations,
            byte[] parameterAnnotations,
            byte[] annotationDefault)
    {
        return new TMethod(declaringClass,
                name,
                parameterTypes,
                returnType,
                checkedExceptions,
                modifiers,
                slot,
                signature,
                annotations,
                parameterAnnotations,
                annotationDefault);
    }

    public <T> TConstructor<T> newConstructor(TClass<T> declaringClass,
            TClass<?>[] parameterTypes,
            TClass<?>[] checkedExceptions,
            int modifiers,
            int slot,
            String signature,
            byte[] annotations,
            byte[] parameterAnnotations)
    {
        return new TConstructor(declaringClass,
                parameterTypes,
                checkedExceptions,
                modifiers,
                slot,
                signature,
                annotations,
                parameterAnnotations);
    }

    public TMethodAccessor getMethodAccessor(TMethod m) {
        return m.getMethodAccessor();
    }

    public void setMethodAccessor(TMethod m, TMethodAccessor accessor) {
        m.setMethodAccessor(accessor);
    }

    public TConstructorAccessor getConstructorAccessor(TConstructor<?> c) {
        return c.getConstructorAccessor();
    }

    public void setConstructorAccessor(TConstructor<?> c,
            TConstructorAccessor accessor)
    {
        c.setConstructorAccessor(accessor);
    }

    @Override
    public byte[] getExecutableTypeAnnotationBytes(TExecutable var1) {
        return new byte[0];
    }

    @Override
    public int getConstructorSlot(TConstructor<?> var1) {
        return 0;
    }

    @Override
    public String getConstructorSignature(TConstructor<?> var1) {
        return null;
    }

    @Override
    public byte[] getConstructorAnnotations(TConstructor<?> var1) {
        return new byte[0];
    }

    @Override
    public byte[] getConstructorParameterAnnotations(TConstructor<?> var1) {
        return new byte[0];
    }

    //
    // Copying routines, needed to quickly fabricate new Field,
    // Method, and Constructor objects from templates
    //
    public TMethod      copyMethod(TMethod arg) {
        return arg.copy();
    }

    public TField       copyField(TField arg) {
        return arg.copy();
    }

    public <T> TConstructor<T> copyConstructor(TConstructor<T> arg) {
        return arg.copy();
    }
}
