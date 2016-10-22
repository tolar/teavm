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
package org.teavm.classlib.sun.security.validator;

import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.teavm.classlib.java.security.TKeyStore;
import org.teavm.classlib.java.security.cert.TCertificate;
import org.teavm.classlib.java.security.cert.TX509Certificate;

/**
 * Created by vasek on 22. 10. 2016.
 */
public class TKeyStores {
    private TKeyStores() {
    }

    public static Set<TX509Certificate> getTrustedCerts(TKeyStore var0) {
        HashSet var1 = new HashSet();

        try {
            Enumeration var2 = var0.aliases();

            while(var2.hasMoreElements()) {
                String var3 = (String)var2.nextElement();
                if(var0.isCertificateEntry(var3)) {
                    Certificate var4 = var0.getCertificate(var3);
                    if(var4 instanceof X509Certificate) {
                        var1.add((X509Certificate)var4);
                    }
                } else if(var0.isKeyEntry(var3)) {
                    TCertificate[] var6 = var0.getCertificateChain(var3);
                    if(var6 != null && var6.length > 0 && var6[0] instanceof TX509Certificate) {
                        var1.add((TX509Certificate)var6[0]);
                    }
                }
            }
        } catch (KeyStoreException var5) {
            ;
        }

        return var1;
    }
}