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
package org.teavm.classlib.sun.security.provider.certpath;

import java.util.Set;

import org.teavm.classlib.java.security.cert.TX509CRLSelector;
import org.teavm.classlib.java.security.cert.TX509CertSelector;
import org.teavm.classlib.java.util.TDate;
import org.teavm.classlib.sun.security.x509.TGeneralNameInterface;

public abstract class TCertPathHelper {
    protected static TCertPathHelper instance;

    protected TCertPathHelper() {
    }

    protected abstract void implSetPathToNames(TX509CertSelector var1, Set<TGeneralNameInterface> var2);

    protected abstract void implSetDateAndTime(TX509CRLSelector var1, TDate var2, long var3);

    static void setPathToNames(TX509CertSelector var0, Set<TGeneralNameInterface> var1) {
        instance.implSetPathToNames(var0, var1);
    }

    public static void setDateAndTime(TX509CRLSelector var0, TDate var1, long var2) {
        instance.implSetDateAndTime(var0, var1, var2);
    }
}
