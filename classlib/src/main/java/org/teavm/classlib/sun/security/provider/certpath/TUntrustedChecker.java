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
package org.teavm.classlib.sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXCertPathChecker;
import java.util.Collection;
import java.util.Set;
import org.teavm.classlib.java.security.cert.TCertificate;
import org.teavm.classlib.java.security.cert.TX509Certificate;

/**
 * Created by vasek on 29. 10. 2016.
 */
public final class TUntrustedChecker extends PKIXCertPathChecker {

    public TUntrustedChecker() {
    }

    public void init(boolean var1) throws CertPathValidatorException {
    }

    public boolean isForwardCheckingSupported() {
        return true;
    }

    public Set<String> getSupportedExtensions() {
        return null;
    }

    public void check(TCertificate var1, Collection<String> var2) throws CertPathValidatorException {
        TX509Certificate var3 = (TX509Certificate)var1;
        if(UntrustedCertificates.isUntrusted(var3)) {

            throw new CertPathValidatorException("Untrusted certificate: " + var3.getSubjectX500Principal());
        }
    }
}
