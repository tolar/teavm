/*
 *  Copyright 2016 vtolar.
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
package org.teavm.classlib.java.lang.reflect.factory;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TClassLoader;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.lang.reflect.TConstructor;
import org.teavm.classlib.java.lang.reflect.TGenericDeclaration;
import org.teavm.classlib.java.lang.reflect.TMethod;
import org.teavm.classlib.java.lang.reflect.TParameterizedType;
import org.teavm.classlib.java.lang.reflect.TType;
import org.teavm.classlib.java.lang.reflect.TTypeVariable;
import org.teavm.classlib.java.lang.reflect.TWildcardType;
import org.teavm.classlib.sun.reflect.generics.scope.TScope;
import org.teavm.classlib.sun.reflect.generics.tree.TFieldTypeSignature;

/**
 * @author: Vaclav Tolar, (vaclav_tolar@kb.cz, vaclav.tolar@cleverlance.com, vaclav.tolar@gmail.com)
 * Date: 2016-07-07
 */
public class TCoreReflectionFactory implements TGenericsFactory {
    private final TGenericDeclaration decl;
    private final TScope scope;

    private TCoreReflectionFactory(TGenericDeclaration var1, TScope var2) {
        this.decl = var1;
        this.scope = var2;
    }

    private TGenericDeclaration getDecl() {
        return this.decl;
    }

    private TScope getScope() {
        return this.scope;
    }

    private TClassLoader getDeclsLoader() {
        if(this.decl instanceof TClass) {
            return ((TClass)this.decl).getClassLoader();
        } else if(this.decl instanceof TMethod) {
            return ((TMethod)this.decl).getDeclaringClass().getClassLoader();
        } else {
            assert this.decl instanceof TConstructor : "Constructor expected";

            return ((TConstructor)this.decl).getDeclaringClass().getClassLoader();
        }
    }

    public static TCoreReflectionFactory make(TGenericDeclaration var0, TScope var1) {
        return new TCoreReflectionFactory(var0, var1);
    }

    public TTypeVariable<?> makeTypeVariable(String var1, TFieldTypeSignature[] var2) {
        return null;
    }

    public TWildcardType makeWildcard(TFieldTypeSignature[] var1, TFieldTypeSignature[] var2) {
        return null;
    }

    @Override
    public TType makeNamedType(String var1) {
        return null;
    }

    public TParameterizedType makeParameterizedType(TType var1, TType[] var2, TType var3) {
        return null;
    }

    public TTypeVariable<?> findTypeVariable(String var1) {
        return this.getScope().lookup(var1);
    }

    public TType makeNamedType(TString var1) {
        return TClass.forName(var1, false, this.getDeclsLoader());
    }

    public TType makeArrayType(TType var1) {
        return null;
    }

    public TType makeByte() {
        return null;
    }

    public TType makeBool() {
        return null;
    }

    public TType makeShort() {
        return null;
    }

    public TType makeChar() {
        return null;
    }

    public TType makeInt() {
        return null;
    }

    public TType makeLong() {
        return null;
    }

    public TType makeFloat() {
        return null;
    }

    public TType makeDouble() {
        return null;
    }

    public TType makeVoid() {
        return null;
    }
}
