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
package org.teavm.classlib.java.security.spec;

public abstract class TEncodedKeySpec implements TKeySpec {

    private byte[] encodedKey;

    /**
     * Creates a new EncodedKeySpec with the given encoded key.
     *
     * @param encodedKey the encoded key. The contents of the
     * array are copied to protect against subsequent modification.
     * @exception NullPointerException if {@code encodedKey}
     * is null.
     */
    public TEncodedKeySpec(byte[] encodedKey) {
        this.encodedKey = encodedKey.clone();
    }

    /**
     * Returns the encoded key.
     *
     * @return the encoded key. Returns a new array each time
     * this method is called.
     */
    public byte[] getEncoded() {
        return this.encodedKey.clone();
    }

    /**
     * Returns the name of the encoding format associated with this
     * key specification.
     *
     * <p>If the opaque representation of a key
     * (see {@link java.security.Key Key}) can be transformed
     * (see {@link java.security.KeyFactory KeyFactory})
     * into this key specification (or a subclass of it),
     * {@code getFormat} called
     * on the opaque key returns the same value as the
     * {@code getFormat} method
     * of this key specification.
     *
     * @return a string representation of the encoding format.
     */
    public abstract String getFormat();
}
