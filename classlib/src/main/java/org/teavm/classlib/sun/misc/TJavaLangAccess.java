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
package org.teavm.classlib.sun.misc;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TRunnable;
import org.teavm.classlib.java.lang.TThread;
import org.teavm.classlib.java.lang.reflect.TExecutable;
import org.teavm.classlib.java.security.TAccessControlContext;
import org.teavm.classlib.sun.nio.ch.TInterruptible;
import org.teavm.classlib.sun.reflect.TConstantPool;
import org.teavm.classlib.sun.reflect.annotation.TAnnotationType;

/**
 * Created by vasek on 9. 7. 2016.
 */
public interface TJavaLangAccess {
    TConstantPool getConstantPool(TClass<?> var1);

    boolean casAnnotationType(TClass<?> var1, TAnnotationType var2, TAnnotationType var3);

    TAnnotationType getAnnotationType(TClass<?> var1);

    Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap(TClass<?> var1);

    byte[] getRawClassAnnotations(TClass<?> var1);

    byte[] getRawClassTypeAnnotations(TClass<?> var1);

    byte[] getRawExecutableTypeAnnotations(TExecutable var1);

    <E extends Enum<E>> E[] getEnumConstantsShared(TClass<E> var1);

    void blockedOn(TThread var1, TInterruptible var2);

    void registerShutdownHook(int var1, boolean var2, TRunnable var3);

    int getStackTraceDepth(Throwable var1);

    StackTraceElement getStackTraceElement(Throwable var1, int var2);

    String newStringUnsafe(char[] var1);

    Thread newThreadWithAcc(TRunnable var1, TAccessControlContext var2);

    void invokeFinalize(Object var1) throws Throwable;
}
