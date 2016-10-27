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

import java.security.cert.CRL;

public interface TCRLSelector extends Cloneable {

    /**
     * Decides whether a {@code CRL} should be selected.
     *
     * @param   crl     the {@code CRL} to be checked
     * @return  {@code true} if the {@code CRL} should be selected,
     * {@code false} otherwise
     */
    boolean match(CRL crl);

    /**
     * Makes a copy of this {@code CRLSelector}. Changes to the
     * copy will not affect the original and vice versa.
     *
     * @return a copy of this {@code CRLSelector}
     */
    Object clone();
}
