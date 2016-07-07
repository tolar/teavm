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
package org.teavm.classlib.sun.reflect.generics.repository;

import org.teavm.classlib.java.lang.reflect.factory.TGenericsFactory;
import org.teavm.classlib.sun.reflect.generics.tree.TTree;


/**
 * @author: Vaclav Tolar, (vaclav_tolar@kb.cz, vaclav.tolar@cleverlance.com, vaclav.tolar@gmail.com)
 * Date: 2016-07-07
 */
public abstract class TAbstractRepository<T extends TTree> {
    private final TGenericsFactory factory;
    private final T tree;

    private TGenericsFactory getFactory() {
        return this.factory;
    }

    protected T getTree() {
        return this.tree;
    }

    protected Reifier getReifier() {
        return Reifier.make(this.getFactory());
    }

    protected TAbstractRepository(String var1, GenericsFactory var2) {
        this.tree = this.parse(var1);
        this.factory = var2;
    }

    protected abstract T parse(String var1);
}
