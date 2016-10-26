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
package org.teavm.classlib.java.security.cert;

import java.security.cert.Certificate;

/**
 * Created by vasek on 26. 10. 2016.
 */
public interface TCertSelector extends Cloneable {

    /**
     * Decides whether a {@code Certificate} should be selected.
     *
     * @param   cert    the {@code Certificate} to be checked
     * @return  {@code true} if the {@code Certificate}
     * should be selected, {@code false} otherwise
     */
    boolean match(Certificate cert);

    /**
     * Makes a copy of this {@code TCertSelector}. Changes to the
     * copy will not affect the original and vice versa.
     *
     * @return a copy of this {@code TCertSelector}
     */
    Object clone();
}
