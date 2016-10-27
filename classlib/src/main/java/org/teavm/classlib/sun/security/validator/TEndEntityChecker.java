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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.security.cert.TX509Certificate;

/**
 * Created by vasek on 21. 10. 2016.
 */
class TEndEntityChecker {
    private static final String OID_EXTENDED_KEY_USAGE = "2.5.29.37";
    private static final String OID_EKU_TLS_SERVER = "1.3.6.1.5.5.7.3.1";
    private static final String OID_EKU_TLS_CLIENT = "1.3.6.1.5.5.7.3.2";
    private static final String OID_EKU_CODE_SIGNING = "1.3.6.1.5.5.7.3.3";
    private static final String OID_EKU_TIME_STAMPING = "1.3.6.1.5.5.7.3.8";
    private static final String OID_EKU_ANY_USAGE = "2.5.29.37.0";
    private static final String OID_EKU_NS_SGC = "2.16.840.1.113730.4.1";
    private static final String OID_EKU_MS_SGC = "1.3.6.1.4.1.311.10.3.3";
    private static final String OID_SUBJECT_ALT_NAME = "2.5.29.17";
    private static final String NSCT_SSL_CLIENT = "ssl_client";
    private static final String NSCT_SSL_SERVER = "ssl_server";
    private static final String NSCT_CODE_SIGNING = "object_signing";
    private static final int KU_SIGNATURE = 0;
    private static final int KU_KEY_ENCIPHERMENT = 2;
    private static final int KU_KEY_AGREEMENT = 4;
    private static final Collection<String> KU_SERVER_SIGNATURE = Arrays
            .asList(new String[]{"DHE_DSS", "DHE_RSA", "ECDHE_ECDSA", "ECDHE_RSA", "RSA_EXPORT", "UNKNOWN"});
    private static final Collection<String> KU_SERVER_ENCRYPTION = Arrays.asList(new String[]{"RSA"});
    private static final Collection<String> KU_SERVER_KEY_AGREEMENT = Arrays.asList(new String[]{"DH_DSS", "DH_RSA", "ECDH_ECDSA", "ECDH_RSA"});
    private final TString variant;
    private final TString type;

    private TEndEntityChecker(TString var1, TString var2) {
        this.type = var1;
        this.variant = var2;
    }

    static TEndEntityChecker getInstance(TString var0, TString var1) {
        return new TEndEntityChecker(var0, var1);
    }

    void check(TX509Certificate var1, Object var2) throws TCertificateException {
        if(!this.variant.equals("generic")) {
            if(this.variant.equals("tls server")) {
                this.checkTLSServer(var1, (String)var2);
            } else if(this.variant.equals("tls client")) {
                this.checkTLSClient(var1);
            } else if(this.variant.equals("code signing")) {
                this.checkCodeSigning(var1);
            } else if(this.variant.equals("jce signing")) {
                this.checkCodeSigning(var1);
            } else if(this.variant.equals("plugin code signing")) {
                this.checkCodeSigning(var1);
            } else {
                if(!this.variant.equals("tsa server")) {
                    throw new TCertificateException(TString.wrap("Unknown variant: " + this.variant));
                }

                this.checkTSAServer(var1);
            }

        }
    }

    private Set<String> getCriticalExtensions(TX509Certificate var1) {
        Set var2 = var1.getCriticalExtensionOIDs();
        if(var2 == null) {
            var2 = Collections.emptySet();
        }

        return var2;
    }

    private void checkRemainingExtensions(Set<String> var1) throws TCertificateException {
        var1.remove("2.5.29.19");
        var1.remove("2.5.29.17");
        if(!var1.isEmpty()) {
            throw new TCertificateException(TString.wrap("Certificate contains unsupported critical extensions: " + var1));
        }
    }

    private boolean checkEKU(TX509Certificate var1, Set<String> var2, String var3) throws TCertificateException {
        List var4 = var1.getExtendedKeyUsage();
        return var4 == null?true:var4.contains(var3) || var4.contains("2.5.29.37.0");
    }

    private boolean checkKeyUsage(TX509Certificate var1, int var2) throws TCertificateException {
        boolean[] var3 = var1.getKeyUsage();
        return var3 == null?true:var3.length > var2 && var3[var2];
    }

    private void checkTLSClient(TX509Certificate var1) throws TCertificateException {
        Set var2 = this.getCriticalExtensions(var1);
        if(!this.checkKeyUsage(var1, 0)) {
            throw new TValidatorException(TString.wrap("KeyUsage does not allow digital signatures"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else if(!this.checkEKU(var1, var2, "1.3.6.1.5.5.7.3.2")) {
            throw new TValidatorException(TString.wrap("Extended key usage does not permit use for TLS client authentication"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else if(!TSimpleValidator.getNetscapeCertTypeBit(var1, "ssl_client")) {
            throw new TValidatorException(TString.wrap("Netscape cert type does not permit use for SSL client"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else {
            var2.remove("2.5.29.15");
            var2.remove("2.5.29.37");
            var2.remove("2.16.840.1.113730.1.1");
            this.checkRemainingExtensions(var2);
        }
    }

    private void checkTLSServer(TX509Certificate var1, String var2) throws TCertificateException {
        Set var3 = this.getCriticalExtensions(var1);
        if(KU_SERVER_ENCRYPTION.contains(var2)) {
            if(!this.checkKeyUsage(var1, 2)) {
                throw new TValidatorException(
                        TString.wrap("KeyUsage does not allow key encipherment"), TValidatorException.T_EE_EXTENSIONS, var1);
            }
        } else if(KU_SERVER_SIGNATURE.contains(var2)) {
            if(!this.checkKeyUsage(var1, 0)) {
                throw new TValidatorException(TString.wrap("KeyUsage does not allow digital signatures"), TValidatorException.T_EE_EXTENSIONS, var1);
            }
        } else {
            if(!KU_SERVER_KEY_AGREEMENT.contains(var2)) {
                throw new TCertificateException(TString.wrap("Unknown authType: " + var2));
            }

            if(!this.checkKeyUsage(var1, 4)) {
                throw new TValidatorException(TString.wrap("KeyUsage does not allow key agreement"), TValidatorException.T_EE_EXTENSIONS, var1);
            }
        }

        if(!this.checkEKU(var1, var3, "1.3.6.1.5.5.7.3.1") && !this.checkEKU(var1, var3, "1.3.6.1.4.1.311.10.3.3") && !this.checkEKU(var1, var3, "2.16.840.1.113730.4.1")) {
            throw new TValidatorException(TString.wrap("Extended key usage does not permit use for TLS server authentication"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else if(!TSimpleValidator.getNetscapeCertTypeBit(var1, "ssl_server")) {
            throw new TValidatorException(TString.wrap("Netscape cert type does not permit use for SSL server"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else {
            var3.remove("2.5.29.15");
            var3.remove("2.5.29.37");
            var3.remove("2.16.840.1.113730.1.1");
            this.checkRemainingExtensions(var3);
        }
    }

    private void checkCodeSigning(TX509Certificate var1) throws TCertificateException {
        Set var2 = this.getCriticalExtensions(var1);
        if(!this.checkKeyUsage(var1, 0)) {
            throw new TValidatorException(TString.wrap("KeyUsage does not allow digital signatures"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else if(!this.checkEKU(var1, var2, "1.3.6.1.5.5.7.3.3")) {
            throw new TValidatorException(TString.wrap("Extended key usage does not permit use for code signing"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else {
            if(!this.variant.equals("jce signing")) {
                if(!TSimpleValidator.getNetscapeCertTypeBit(var1, "object_signing")) {
                    throw new TValidatorException(TString.wrap("Netscape cert type does not permit use for code signing"), TValidatorException.T_EE_EXTENSIONS, var1);
                }

                var2.remove("2.16.840.1.113730.1.1");
            }

            var2.remove("2.5.29.15");
            var2.remove("2.5.29.37");
            this.checkRemainingExtensions(var2);
        }
    }

    private void checkTSAServer(TX509Certificate var1) throws TCertificateException {
        Set var2 = this.getCriticalExtensions(var1);
        if(!this.checkKeyUsage(var1, 0)) {
            throw new TValidatorException(TString.wrap("KeyUsage does not allow digital signatures"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else if(var1.getExtendedKeyUsage() == null) {
            throw new TValidatorException(TString.wrap("Certificate does not contain an extended key usage extension required for a TSA server"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else if(!this.checkEKU(var1, var2, "1.3.6.1.5.5.7.3.8")) {
            throw new TValidatorException(TString.wrap("Extended key usage does not permit use for TSA server"), TValidatorException.T_EE_EXTENSIONS, var1);
        } else {
            var2.remove("2.5.29.15");
            var2.remove("2.5.29.37");
            this.checkRemainingExtensions(var2);
        }
    }
}
