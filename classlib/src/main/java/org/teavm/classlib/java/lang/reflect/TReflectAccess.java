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


/**
 * Created by vasek on 4. 7. 2016.
 */
class TReflectAccess implements org.teavm.classlib.sun.reflect.TLangReflectAccess {
    public TField newField(Class<?> declaringClass,
            String name,
            Class<?> type,
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

    public Method newMethod(Class<?> declaringClass,
            String name,
            Class<?>[] parameterTypes,
            Class<?> returnType,
            Class<?>[] checkedExceptions,
            int modifiers,
            int slot,
            String signature,
            byte[] annotations,
            byte[] parameterAnnotations,
            byte[] annotationDefault)
    {
        return new Method(declaringClass,
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

    public <T> Constructor<T> newConstructor(Class<T> declaringClass,
            Class<?>[] parameterTypes,
            Class<?>[] checkedExceptions,
            int modifiers,
            int slot,
            String signature,
            byte[] annotations,
            byte[] parameterAnnotations)
    {
        return new Constructor<>(declaringClass,
                parameterTypes,
                checkedExceptions,
                modifiers,
                slot,
                signature,
                annotations,
                parameterAnnotations);
    }

    public MethodAccessor getMethodAccessor(Method m) {
        return m.getMethodAccessor();
    }

    public void setMethodAccessor(Method m, MethodAccessor accessor) {
        m.setMethodAccessor(accessor);
    }

    public ConstructorAccessor getConstructorAccessor(Constructor<?> c) {
        return c.getConstructorAccessor();
    }

    public void setConstructorAccessor(Constructor<?> c,
            ConstructorAccessor accessor)
    {
        c.setConstructorAccessor(accessor);
    }

    public int getConstructorSlot(Constructor<?> c) {
        return c.getSlot();
    }

    public String getConstructorSignature(Constructor<?> c) {
        return c.getSignature();
    }

    public byte[] getConstructorAnnotations(Constructor<?> c) {
        return c.getRawAnnotations();
    }

    public byte[] getConstructorParameterAnnotations(Constructor<?> c) {
        return c.getRawParameterAnnotations();
    }

    public byte[] getExecutableTypeAnnotationBytes(java.lang.reflect.Executable ex) {
        return ex.getTypeAnnotationBytes();
    }

    //
    // Copying routines, needed to quickly fabricate new Field,
    // Method, and Constructor objects from templates
    //
    public Method      copyMethod(Method arg) {
        return arg.copy();
    }

    public Field       copyField(Field arg) {
        return arg.copy();
    }

    public <T> Constructor<T> copyConstructor(Constructor<T> arg) {
        return arg.copy();
    }
}
