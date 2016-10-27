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
package org.teavm.classlib.java.security.cert;

import java.util.Set;

import org.teavm.classlib.java.util.TDate;
import org.teavm.classlib.sun.security.provider.certpath.TCertPathHelper;
import org.teavm.classlib.sun.security.x509.TGeneralNameInterface;

class TCertPathHelperImpl extends TCertPathHelper {

    private TCertPathHelperImpl() {
        // empty
    }

    /**
     * Initialize the helper framework. This method must be called from
     * the static initializer of each class that is the target of one of
     * the methods in this class. This ensures that the helper is initialized
     * prior to a tunneled call from the Sun provider.
     */
    synchronized static void initialize() {
        if (TCertPathHelper.instance == null) {
            TCertPathHelper.instance = new TCertPathHelperImpl();
        }
    }

    protected void implSetPathToNames(TX509CertSelector sel,
            Set<TGeneralNameInterface> names) {
        sel.setPathToNamesInternal(names);
    }

    protected void implSetDateAndTime(TX509CRLSelector sel, TDate date, long skew) {
        sel.setDateAndTime(date, skew);
    }
}
