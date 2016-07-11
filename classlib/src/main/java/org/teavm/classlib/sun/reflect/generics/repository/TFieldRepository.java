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
import org.teavm.classlib.sun.reflect.generics.tree.TTypeSignature;
import org.teavm.classlib.sun.reflect.generics.visitor.TReifier;

/**
 * Created by vasek on 6. 7. 2016.
 */
public class TFieldRepository extends TAbstractRepository<TTypeSignature> {
    private TType genericType;

    protected TFieldRepository(String var1, TGenericsFactory var2) {
        super(var1, var2);
    }

    protected TTypeSignature parse(String var1) {
        return TSignatureParser.make().parseTypeSig(var1);
    }

    public static TFieldRepository make(String var0, TGenericsFactory var1) {
        return new TFieldRepository(var0, var1);
    }

    public TType getGenericType() {
        if(this.genericType == null) {
            TReifier var1 = this.getReifier();
            ((TTypeSignature)this.getTree()).accept(var1);
            this.genericType = var1.getResult();
        }

        return this.genericType;
    }
}
