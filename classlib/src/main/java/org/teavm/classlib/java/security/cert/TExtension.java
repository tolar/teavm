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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;

public interface TExtension {

    /**
     * Gets the extensions's object identifier.
     *
     * @return the object identifier as a String
     */
    String getId();

    /**
     * Gets the extension's criticality setting.
     *
     * @return true if this is a critical extension.
     */
    boolean isCritical();

    /**
     * Gets the extensions's DER-encoded value. Note, this is the bytes
     * that are encoded as an OCTET STRING. It does not include the OCTET
     * STRING tag and length.
     *
     * @return a copy of the extension's value, or {@code null} if no
     *    extension value is present.
     */
    byte[] getValue();

    /**
     * Generates the extension's DER encoding and writes it to the output
     * stream.
     *
     * @param out the output stream
     * @exception TIOException on encoding or output error.
     * @exception NullPointerException if {@code out} is {@code null}.
     */
    void encode(TOutputStream out) throws TIOException;
}
