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
package org.teavm.classlib.sun.security.x509;

import org.teavm.classlib.java.lang.TString;

public class TX509AttributeName {
    private static final char SEPARATOR = '.';
    private TString prefix = null;
    private TString suffix = null;

    public TX509AttributeName(TString var1) {
        int var2 = var1.indexOf(46);
        if(var2 < 0) {
            this.prefix = var1;
        } else {
            this.prefix = var1.substring(0, var2);
            this.suffix = var1.substring(var2 + 1);
        }

    }

    public TString getPrefix() {
        return this.prefix;
    }

    public TString getSuffix() {
        return this.suffix;
    }
}
