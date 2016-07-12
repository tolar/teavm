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

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.annotation.Repeatable;
import java.util.Iterator;
import java.util.Map;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.annotation.TAnnotation;
import org.teavm.classlib.sun.misc.TJavaLangAccess;
import org.teavm.classlib.sun.misc.TSharedSecrets;

/**
 * Created by vasek on 9. 7. 2016.
 */
public final class TAnnotationSupport {
    private static final TJavaLangAccess LANG_ACCESS = TSharedSecrets.getJavaLangAccess();

    public TAnnotationSupport() {
    }

    public static <A extends TAnnotation> A[] getDirectlyAndIndirectlyPresent(Map<Class<? extends TAnnotation>, TAnnotation> var0, TClass<A> var1) {
        return null;
    }

    private static <A extends TAnnotation> A[] getIndirectlyPresent(Map<Class<? extends TAnnotation>, TAnnotation> var0, Class<A> var1) {
        return null;
    }

    private static <A extends TAnnotation> boolean containerBeforeContainee(Map<Class<? extends TAnnotation>, TAnnotation> var0, Class<A> var1) {
        Class var2 = ((Repeatable)var1.getDeclaredAnnotation(Repeatable.class)).value();
        Iterator var3 = var0.keySet().iterator();

        Class var4;
        do {
            if(!var3.hasNext()) {
                return false;
            }

            var4 = (Class)var3.next();
            if(var4 == var2) {
                return true;
            }
        } while(var4 != var1);

        return false;
    }

    public static <A extends Annotation> A[] getAssociatedAnnotations(Map<Class<? extends Annotation>, Annotation> var0, Class<?> var1, Class<A> var2) {
        return null;
    }

    private static <A extends TAnnotation> A[] getValueArray(TAnnotation var0) {
        return null;
    }

    private static AnnotationFormatError invalidContainerException(Annotation var0, Throwable var1) {
        return new AnnotationFormatError(var0 + " is an invalid container for repeating annotations", var1);
    }

    private static <A extends TAnnotation> void checkTypes(A[] var0, TAnnotation var1, Class<A> var2) {
        Annotation[] var3 = var0;
        int var4 = var0.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Annotation var6 = var3[var5];
            if(!var2.isInstance(var6)) {
                throw new AnnotationFormatError(String.format("%s is an invalid container for repeating annotations of type: %s", new Object[]{var1, var2}));
            }
        }

    }
}
