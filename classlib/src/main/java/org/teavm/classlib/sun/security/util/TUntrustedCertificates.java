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
package org.teavm.classlib.sun.security.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.sun.security.x509.TX509CertImpl;

/**
 * Created by vasek on 29. 10. 2016.
 */
public final class TUntrustedCertificates {
    private static final String ALGORITHM_KEY = "Algorithm";
    private static final Properties props = new Properties();
    private static final String algorithm;

    private static String stripColons(Object var0) {
        String var1 = (String)var0;
        char[] var2 = var1.toCharArray();
        int var3 = 0;

        for(int var4 = 0; var4 < var2.length; ++var4) {
            if(var2[var4] != 58) {
                if(var4 != var3) {
                    var2[var3] = var2[var4];
                }

                ++var3;
            }
        }

        if(var3 == var2.length) {
            return var1;
        } else {
            return new String(var2, 0, var3);
        }
    }

    public static boolean isUntrusted(TX509Certificate var0) {
        if(algorithm == null) {
            return false;
        } else {
            String var1;
            if(var0 instanceof TX509CertImpl) {
                var1 = ((TX509CertImpl)var0).getFingerprint(algorithm);
            } else {
                try {
                    var1 = (new TX509CertImpl(var0.getEncoded())).getFingerprint(algorithm);
                } catch (TCertificateException var3) {
                    return false;
                }
            }

            return props.containsKey(var1);
        }
    }

    private TUntrustedCertificates() {
    }

    static {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Void run() {
                File var1 = new File(System.getProperty("java.home"), "lib/security/blacklisted.certs");

                try {
                    FileInputStream var2 = new FileInputStream(var1);
                    Throwable var3 = null;

                    try {
                        TUntrustedCertificates.props.load(var2);
                        Iterator var4 = TUntrustedCertificates.props.entrySet().iterator();

                        while(var4.hasNext()) {
                            Map.Entry var5 = (Map.Entry)var4.next();
                            var5.setValue(TUntrustedCertificates.stripColons(var5.getValue()));
                        }
                    } catch (Throwable var14) {
                        var3 = var14;
                        throw var14;
                    } finally {
                        if(var2 != null) {
                            if(var3 != null) {
                                try {
                                    var2.close();
                                } catch (Throwable var13) {
                                    var3.addSuppressed(var13);
                                }
                            } else {
                                var2.close();
                            }
                        }

                    }
                } catch (IOException var16) {
                }

                return null;
            }
        });
        algorithm = props.getProperty("Algorithm");
    }
}
