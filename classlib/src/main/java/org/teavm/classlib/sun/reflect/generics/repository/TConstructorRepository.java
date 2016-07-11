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
package org.teavm.classlib.sun.reflect.generics.repository;

import org.teavm.classlib.java.lang.reflect.TType;
import org.teavm.classlib.java.lang.reflect.factory.TGenericsFactory;
import org.teavm.classlib.sun.reflect.generics.parser.TSignatureParser;
import org.teavm.classlib.sun.reflect.generics.tree.TFieldTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TMethodTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TTypeSignature;
import org.teavm.classlib.sun.reflect.generics.visitor.TReifier;

/**
 * Created by vasek on 7. 7. 2016.
 */
public class TConstructorRepository extends TGenericDeclRepository<TMethodTypeSignature> {
    private TType[] paramTypes;
    private TType[] exceptionTypes;

    protected TConstructorRepository(String var1, TGenericsFactory var2) {
        super(var1, var2);
    }

    protected TMethodTypeSignature parse(String var1) {
        return TSignatureParser.make().parseMethodSig(var1);
    }

    public static TConstructorRepository make(String var0, TGenericsFactory var1) {
        return new TConstructorRepository(var0, var1);
    }

    public TType[] getParameterTypes() {
        if(this.paramTypes == null) {
            TTypeSignature[] var1 = ((TMethodTypeSignature)this.getTree()).getParameterTypes();
            TType[] var2 = new TType[var1.length];

            for(int var3 = 0; var3 < var1.length; ++var3) {
                TReifier var4 = this.getReifier();
                var1[var3].accept(var4);
                var2[var3] = var4.getResult();
            }

            this.paramTypes = var2;
        }

        return (TType[])this.paramTypes.clone();
    }

    public TType[] getExceptionTypes() {
        if(this.exceptionTypes == null) {
            TFieldTypeSignature[] var1 = ((TMethodTypeSignature)this.getTree()).getExceptionTypes();
            TType[] var2 = new TType[var1.length];

            for(int var3 = 0; var3 < var1.length; ++var3) {
                TReifier var4 = this.getReifier();
                var1[var3].accept(var4);
                var2[var3] = var4.getResult();
            }

            this.exceptionTypes = var2;
        }

        return (TType[])this.exceptionTypes.clone();
    }
}
