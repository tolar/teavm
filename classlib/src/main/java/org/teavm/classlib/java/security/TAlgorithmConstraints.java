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
package org.teavm.classlib.java.security;

import java.security.CryptoPrimitive;
import java.security.Key;
import java.util.Set;
import org.teavm.classlib.java.lang.TString;

/**
 * Created by vasek on 21. 10. 2016.
 */
public interface TAlgorithmConstraints {

    /**
     * Determines whether an algorithm is granted permission for the
     * specified cryptographic primitives.
     *
     * @param primitives a set of cryptographic primitives
     * @param algorithm the algorithm name
     * @param parameters the algorithm parameters, or null if no additional
     *     parameters
     *
     * @return true if the algorithm is permitted and can be used for all
     *     of the specified cryptographic primitives
     *
     * @throws IllegalArgumentException if primitives or algorithm is null
     *     or empty
     */
    public boolean permits(Set<CryptoPrimitive> primitives,
            TString algorithm, TAlgorithmParameters parameters);

    /**
     * Determines whether a key is granted permission for the specified
     * cryptographic primitives.
     * <p>
     * This method is usually used to check key size and key usage.
     *
     * @param primitives a set of cryptographic primitives
     * @param key the key
     *
     * @return true if the key can be used for all of the specified
     *     cryptographic primitives
     *
     * @throws IllegalArgumentException if primitives is null or empty,
     *     or the key is null
     */
    public boolean permits(Set<CryptoPrimitive> primitives, Key key);

    /**
     * Determines whether an algorithm and the corresponding key are granted
     * permission for the specified cryptographic primitives.
     *
     * @param primitives a set of cryptographic primitives
     * @param algorithm the algorithm name
     * @param key the key
     * @param parameters the algorithm parameters, or null if no additional
     *     parameters
     *
     * @return true if the key and the algorithm can be used for all of the
     *     specified cryptographic primitives
     *
     * @throws IllegalArgumentException if primitives or algorithm is null
     *     or empty, or the key is null
     */
    public boolean permits(Set<CryptoPrimitive> primitives,
            TString algorithm, TKey key, TAlgorithmParameters parameters);

}
