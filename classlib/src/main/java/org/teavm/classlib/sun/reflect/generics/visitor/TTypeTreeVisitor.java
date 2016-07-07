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
package org.teavm.classlib.sun.reflect.generics.visitor;

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
import org.teavm.classlib.sun.reflect.generics.tree.TTypeVariableSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TVoidDescriptor;
import org.teavm.classlib.sun.reflect.generics.tree.TWildcard;

/**
 * @author: Vaclav Tolar, (vaclav_tolar@kb.cz, vaclav.tolar@cleverlance.com, vaclav.tolar@gmail.com)
 * Date: 2016-07-07
 */
public interface TTypeTreeVisitor<T> {
    T getResult();

    void visitFormalTypeParameter(TFormalTypeParameter var1);

    void visitClassTypeSignature(TClassTypeSignature var1);

    void visitArrayTypeSignature(TArrayTypeSignature var1);

    void visitTypeVariableSignature(TTypeVariableSignature var1);

    void visitWildcard(TWildcard var1);

    void visitSimpleClassTypeSignature(TSimpleClassTypeSignature var1);

    void visitBottomSignature(TBottomSignature var1);

    void visitByteSignature(TByteSignature var1);

    void visitBooleanSignature(TBooleanSignature var1);

    void visitShortSignature(TShortSignature var1);

    void visitCharSignature(TCharSignature var1);

    void visitIntSignature(TIntSignature var1);

    void visitLongSignature(TLongSignature var1);

    void visitFloatSignature(TFloatSignature var1);

    void visitDoubleSignature(TDoubleSignature var1);

    void visitVoidDescriptor(TVoidDescriptor var1);
}
