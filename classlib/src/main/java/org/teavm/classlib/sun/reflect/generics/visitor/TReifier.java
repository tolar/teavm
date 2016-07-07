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
package org.teavm.classlib.sun.reflect.generics.visitor;

import java.util.Iterator;
import java.util.List;
import org.teavm.classlib.java.lang.reflect.TParameterizedType;
import org.teavm.classlib.java.lang.reflect.TType;
import org.teavm.classlib.java.lang.reflect.factory.TGenericsFactory;
import org.teavm.classlib.sun.reflect.generics.tree.TArrayTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TBooleanSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TBottomSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TByteSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TCharSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TClassTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TDoubleSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TFloatSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TFormalTypeParameter;
import org.teavm.classlib.sun.reflect.generics.tree.TIntSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TLongSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TShortSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TSimpleClassTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TTypeArgument;
import org.teavm.classlib.sun.reflect.generics.tree.TTypeVariableSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TVoidDescriptor;
import org.teavm.classlib.sun.reflect.generics.tree.TWildcard;

/**
 * Created by vasek on 7. 7. 2016.
 */
public class TReifier implements TTypeTreeVisitor<TType> {
    private TType resultType;
    private TGenericsFactory factory;

    private TReifier(TGenericsFactory var1) {
        this.factory = var1;
    }

    private TGenericsFactory getFactory() {
        return this.factory;
    }

    public static TReifier make(TGenericsFactory var0) {
        return new TReifier(var0);
    }

    private TType[] reifyTypeArguments(TTypeArgument[] var1) {
        TType[] var2 = new TType[var1.length];

        for(int var3 = 0; var3 < var1.length; ++var3) {
            var1[var3].accept(this);
            var2[var3] = this.resultType;
        }

        return var2;
    }

    public TType getResult() {
        assert this.resultType != null;

        return this.resultType;
    }

    public void visitFormalTypeParameter(TFormalTypeParameter var1) {
        this.resultType = this.getFactory().makeTypeVariable(var1.getName(), var1.getBounds());
    }

    public void visitClassTypeSignature(TClassTypeSignature var1) {
        List var2 = var1.getPath();

        assert !var2.isEmpty();

        Iterator var3 = var2.iterator();
        TSimpleClassTypeSignature var4 = (TSimpleClassTypeSignature)var3.next();
        StringBuilder var5 = new StringBuilder(var4.getName());
        boolean var6 = var4.getDollar();

        while(var3.hasNext() && var4.getTypeArguments().length == 0) {
            var4 = (TSimpleClassTypeSignature)var3.next();
            var6 = var4.getDollar();
            var5.append(var6?"$":".").append(var4.getName());
        }

        assert !var3.hasNext() || var4.getTypeArguments().length > 0;

        TType var7 = this.getFactory().makeNamedType(var5.toString());
        if(var4.getTypeArguments().length == 0) {
            assert !var3.hasNext();

            this.resultType = var7;
        } else {
            assert var4.getTypeArguments().length > 0;

            TType[] var8 = this.reifyTypeArguments(var4.getTypeArguments());
            TParameterizedType var9 = this.getFactory().makeParameterizedType(var7, var8, (TType)null);

            for(var6 = false; var3.hasNext(); var9 = this.getFactory().makeParameterizedType(var7, var8, var9)) {
                var4 = (TSimpleClassTypeSignature)var3.next();
                var6 = var4.getDollar();
                var5.append(var6?"$":".").append(var4.getName());
                var7 = this.getFactory().makeNamedType(var5.toString());
                var8 = this.reifyTypeArguments(var4.getTypeArguments());
            }

            this.resultType = var9;
        }

    }

    public void visitArrayTypeSignature(TArrayTypeSignature var1) {
        var1.getComponentType().accept(this);
        TType var2 = this.resultType;
        this.resultType = this.getFactory().makeArrayType(var2);
    }

    public void visitTypeVariableSignature(TTypeVariableSignature var1) {
        this.resultType = this.getFactory().findTypeVariable(var1.getIdentifier());
    }

    public void visitWildcard(TWildcard var1) {
        this.resultType = this.getFactory().makeWildcard(var1.getUpperBounds(), var1.getLowerBounds());
    }

    public void visitSimpleClassTypeSignature(TSimpleClassTypeSignature var1) {
        this.resultType = this.getFactory().makeNamedType(var1.getName());
    }

    public void visitBottomSignature(TBottomSignature var1) {
    }

    public void visitByteSignature(TByteSignature var1) {
        this.resultType = this.getFactory().makeByte();
    }

    public void visitBooleanSignature(TBooleanSignature var1) {
        this.resultType = this.getFactory().makeBool();
    }

    public void visitShortSignature(TShortSignature var1) {
        this.resultType = this.getFactory().makeShort();
    }

    public void visitCharSignature(TCharSignature var1) {
        this.resultType = this.getFactory().makeChar();
    }

    public void visitIntSignature(TIntSignature var1) {
        this.resultType = this.getFactory().makeInt();
    }

    public void visitLongSignature(TLongSignature var1) {
        this.resultType = this.getFactory().makeLong();
    }

    public void visitFloatSignature(TFloatSignature var1) {
        this.resultType = this.getFactory().makeFloat();
    }

    public void visitDoubleSignature(TDoubleSignature var1) {
        this.resultType = this.getFactory().makeDouble();
    }

    public void visitVoidDescriptor(TVoidDescriptor var1) {
        this.resultType = this.getFactory().makeVoid();
    }
}
