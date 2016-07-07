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

import org.teavm.classlib.java.lang.reflect.TParameterizedType;
import org.teavm.classlib.java.lang.reflect.TType;
import org.teavm.classlib.java.lang.reflect.TTypeVariable;
import org.teavm.classlib.java.lang.reflect.TWildcardType;
import org.teavm.classlib.sun.reflect.generics.tree.TFieldTypeSignature;

/**
 * @author: Vaclav Tolar, (vaclav_tolar@kb.cz, vaclav.tolar@cleverlance.com, vaclav.tolar@gmail.com)
 * Date: 2016-07-07
 */
public interface TGenericsFactory {
    TTypeVariable<?> makeTypeVariable(String var1, TFieldTypeSignature[] var2);

    TParameterizedType makeParameterizedType(TType var1, TType[] var2, TType var3);

    TTypeVariable<?> findTypeVariable(String var1);

    TWildcardType makeWildcard(TFieldTypeSignature[] var1, TFieldTypeSignature[] var2);

    TType makeNamedType(String var1);

    TType makeArrayType(TType var1);

    TType makeByte();

    TType makeBool();

    TType makeShort();

    TType makeChar();

    TType makeInt();

    TType makeLong();

    TType makeFloat();

    TType makeDouble();

    TType makeVoid();
}
