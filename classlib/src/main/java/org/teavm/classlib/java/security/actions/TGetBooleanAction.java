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
package org.teavm.classlib.java.security.actions;

import org.teavm.classlib.java.lang.TBoolean;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TPrivilegedAction;

/**
 * @author: Vaclav Tolar, (vaclav_tolar@kb.cz, vaclav.tolar@cleverlance.com, vaclav.tolar@gmail.com)
 * Date: 2016-07-12
 */
public class TGetBooleanAction implements TPrivilegedAction<TBoolean> {
    private TString theProp;

    public TGetBooleanAction(TString var1) {
        this.theProp = var1;
    }

    public TBoolean run() {
        return TBoolean.valueOf(TBoolean.getBoolean(this.theProp));
    }
}
