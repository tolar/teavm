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

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TClassLoader;
import org.teavm.classlib.java.lang.reflect.TConstructor;
import org.teavm.classlib.java.lang.reflect.TGenericDeclaration;
import org.teavm.classlib.java.lang.reflect.TMethod;
import org.teavm.classlib.java.lang.reflect.TType;
import org.teavm.classlib.java.lang.reflect.TTypeVariable;
import org.teavm.classlib.java.lang.reflect.TWildcardType;

import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;
import sun.reflect.generics.scope.Scope;
import sun.reflect.generics.tree.FieldTypeSignature;

/**
 * @author: Vaclav Tolar, (vaclav_tolar@kb.cz, vaclav.tolar@cleverlance.com, vaclav.tolar@gmail.com)
 * Date: 2016-07-07
 */
public class TCoreReflectionFactory implements TGenericsFactory {
    private final TGenericDeclaration decl;
    private final Scope scope;

    private TCoreReflectionFactory(TGenericDeclaration var1, Scope var2) {
        this.decl = var1;
        this.scope = var2;
    }

    private TGenericDeclaration getDecl() {
        return this.decl;
    }

    private Scope getScope() {
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

    public static TCoreReflectionFactory make(TGenericDeclaration var0, Scope var1) {
        return new TCoreReflectionFactory(var0, var1);
    }

    public TTypeVariable<?> makeTypeVariable(String var1, FieldTypeSignature[] var2) {
        return TTypeVariableImpl.make(this.getDecl(), var1, var2, this);
    }

    public TWildcardType makeWildcard(FieldTypeSignature[] var1, FieldTypeSignature[] var2) {
        return WildcardTypeImpl.make(var1, var2, this);
    }

    public ParameterizedType makeParameterizedType(Type var1, Type[] var2, Type var3) {
        return ParameterizedTypeImpl.make((Class)var1, var2, var3);
    }

    public TTypeVariable<?> findTypeVariable(String var1) {
        return this.getScope().lookup(var1);
    }

    public TType makeNamedType(String var1) {
        try {
            return Class.forName(var1, false, this.getDeclsLoader());
        } catch (ClassNotFoundException var3) {
            throw new TypeNotPresentException(var1, var3);
        }
    }

    public Type makeArrayType(Type var1) {
        return (Type)(var1 instanceof Class? Array.newInstance((Class)var1, 0).getClass(): GenericArrayTypeImpl.make(var1));
    }

    public Type makeByte() {
        return Byte.TYPE;
    }

    public Type makeBool() {
        return Boolean.TYPE;
    }

    public Type makeShort() {
        return Short.TYPE;
    }

    public Type makeChar() {
        return Character.TYPE;
    }

    public Type makeInt() {
        return Integer.TYPE;
    }

    public Type makeLong() {
        return Long.TYPE;
    }

    public Type makeFloat() {
        return Float.TYPE;
    }

    public Type makeDouble() {
        return Double.TYPE;
    }

    public Type makeVoid() {
        return Void.TYPE;
    }
}
