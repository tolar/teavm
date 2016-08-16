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

/**
 * Created by vasek on 14. 7. 2016.
 */
public abstract class TX509Certificate extends TCertificate {

    /**
     * Creates a certificate of the specified type.
     *
     * @param type the standard name of the certificate type.
     *             See the CertificateFactory section in the <a href=
     *             "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     *             Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     *             for information about standard certificate types.
     */
    protected TX509Certificate(String type) {
        super(type);
    }

    public abstract byte[] getTBSCertificate();
}
