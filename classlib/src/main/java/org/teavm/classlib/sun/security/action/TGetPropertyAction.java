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
package org.teavm.classlib.sun.security.action;

import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TPrivilegedAction;

/**
 * Created by vasek on 24. 9. 2016.
 */
public class TGetPropertyAction implements TPrivilegedAction<TString> {
    private TString theProp;
    private TString defaultVal;

    public TGetPropertyAction(TString var1) {
        this.theProp = var1;
    }

    public TGetPropertyAction(TString var1, TString var2) {
        this.theProp = var1;
        this.defaultVal = var2;
    }

    public TString run() {
        TString var1 = TString.wrap(System.getProperty(this.theProp.toString()));
        return var1 == null?this.defaultVal:var1;
    }
}
