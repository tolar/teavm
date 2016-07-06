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


/**
 * Created by vasek on 6. 7. 2016.
 */
public class FieldRepository extends AbstractRepository<TypeSignature> {
    private Type genericType;

    protected FieldRepository(String var1, GenericsFactory var2) {
        super(var1, var2);
    }

    protected TypeSignature parse(String var1) {
        return SignatureParser.make().parseTypeSig(var1);
    }

    public static sun.reflect.generics.repository.FieldRepository make(String var0, GenericsFactory var1) {
        return new sun.reflect.generics.repository.FieldRepository(var0, var1);
    }

    public Type getGenericType() {
        if(this.genericType == null) {
            Reifier var1 = this.getReifier();
            ((TypeSignature)this.getTree()).accept(var1);
            this.genericType = var1.getResult();
        }

        return this.genericType;
    }
}
