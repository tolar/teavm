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
package org.teavm.classlib.sun.security.x509;

import org.teavm.classlib.java.util.TComparator;

/**
 * Created by vasek on 18. 8. 2016.
 */
class TAVAComparator implements TComparator<TAVA> {
    private static final TComparator<TAVA> INSTANCE = new TAVAComparator();

    private TAVAComparator() {
    }

    static TComparator<TAVA> getInstance() {
        return INSTANCE;
    }

    public int compare(TAVA var1, TAVA var2) {
        boolean var3 = var1.hasRFC2253Keyword();
        boolean var4 = var2.hasRFC2253Keyword();
        return var3 == var4?var1.toRFC2253CanonicalString().compareTo(var2.toRFC2253CanonicalString()):(var3?-1:1);
    }
}
