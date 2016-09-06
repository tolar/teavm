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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.teavm.classlib.java.io.TIOException;

public class TAlgorithmParameters {

    public final void init(byte[] params, String format) throws TIOException {
    }

    public final void init(byte[] params) throws TIOException {
    }

    public static TAlgorithmParameters getInstance(String algorithm,
            String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException
    {
        return null;
    }

    public static TAlgorithmParameters getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        return null;
    }

    public final byte[] getEncoded(String format) throws IOException
    {
        return null;
    }

    public final byte[] getEncoded() throws IOException
    {
        return null;
    }
}
