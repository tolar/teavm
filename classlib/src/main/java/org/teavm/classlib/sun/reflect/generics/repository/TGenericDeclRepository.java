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

import java.lang.reflect.TypeVariable;
import org.teavm.classlib.java.lang.reflect.TTypeVariable;
import org.teavm.classlib.java.lang.reflect.factory.TGenericsFactory;
import org.teavm.classlib.sun.reflect.generics.tree.TFormalTypeParameter;
import org.teavm.classlib.sun.reflect.generics.tree.TSignature;
import org.teavm.classlib.sun.reflect.generics.visitor.TReifier;

/**
 * Created by vasek on 7. 7. 2016.
 */
public abstract class TGenericDeclRepository<S extends TSignature> extends TAbstractRepository<S> {
    private volatile TypeVariable<?>[] typeParams;

    protected TGenericDeclRepository(String var1, TGenericsFactory var2) {
        super(var1, var2);
    }

    public TTypeVariable<?>[] getTypeParameters() {
        TypeVariable[] var1 = this.typeParams;
        if(var1 == null) {
            TFormalTypeParameter[] var2 = ((TSignature)this.getTree()).getFormalTypeParameters();
            var1 = new TypeVariable[var2.length];

            for(int var3 = 0; var3 < var2.length; ++var3) {
                TReifier var4 = this.getReifier();
                var2[var3].accept(var4);
                var1[var3] = (TypeVariable)var4.getResult();
            }

            this.typeParams = var1;
        }

        return (TTypeVariable[])var1.clone();
    }
}
