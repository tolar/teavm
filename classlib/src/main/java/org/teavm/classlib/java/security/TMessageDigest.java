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
package org.teavm.classlib.java.security;

import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class TMessageDigest {

    protected TMessageDigest(String algorithm) {
    }

    public static TMessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException {
        return null;
    }

    public static TMessageDigest getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException
    {
        return null;
    }


    public byte[] digest() {
        return null;
    }

    public int digest(byte[] buf, int offset, int len) throws DigestException {
        return -1;
    }

    public byte[] digest(byte[] input) {
        return digest();
    }

    public void update(byte[] input, int offset, int len) {
    }

}
