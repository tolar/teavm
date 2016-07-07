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

import java.lang.reflect.Type;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.repository.ConstructorRepository;
import sun.reflect.generics.tree.MethodTypeSignature;
import sun.reflect.generics.visitor.Reifier;

/**
 * Created by vasek on 7. 7. 2016.
 */
public class TMethodRepository extends ConstructorRepository {
    private Type returnType;

    private TMethodRepository(String var1, GenericsFactory var2) {
        super(var1, var2);
    }

    public static TMethodRepository make(String var0, GenericsFactory var1) {
        return new TMethodRepository(var0, var1);
    }

    public Type getReturnType() {
        if(this.returnType == null) {
            Reifier var1 = this.getReifier();
            ((MethodTypeSignature)this.getTree()).getReturnType().accept(var1);
            this.returnType = var1.getResult();
        }

        return this.returnType;
    }
}
