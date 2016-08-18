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

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.util.Enumeration;

/**
 * Created by vasek on 18. 8. 2016.
 */
public interface TCertAttrSet<T> {
    String toString();

    void encode(OutputStream var1) throws CertificateException, IOException;

    void set(String var1, Object var2) throws CertificateException, IOException;

    Object get(String var1) throws CertificateException, IOException;

    void delete(String var1) throws CertificateException, IOException;

    Enumeration<T> getElements();

    String getName();
}
