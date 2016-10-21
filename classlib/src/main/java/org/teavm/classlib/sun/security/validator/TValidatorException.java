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

import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.security.cert.TX509Certificate;

/**
 * Created by vasek on 21. 10. 2016.
 */
public class TValidatorException extends TCertificateException {
    private static final long serialVersionUID = -2836879718282292155L;
    public static final Object T_NO_TRUST_ANCHOR = "No trusted certificate found";
    public static final Object T_EE_EXTENSIONS = "End entity certificate extension check failed";
    public static final Object T_CA_EXTENSIONS = "CA certificate extension check failed";
    public static final Object T_CERT_EXPIRED = "Certificate expired";
    public static final Object T_SIGNATURE_ERROR = "Certificate signature validation failed";
    public static final Object T_NAME_CHAINING = "Certificate chaining error";
    public static final Object T_ALGORITHM_DISABLED = "Certificate signature algorithm disabled";
    public static final Object T_UNTRUSTED_CERT = "Untrusted certificate";
    private Object type;
    private TX509Certificate cert;

    public TValidatorException(TString var1) {
        super(var1);
    }

    public TValidatorException(TString var1, Throwable var2) {
        super(var1);
        this.initCause(var2);
    }

    public TValidatorException(Object var1) {
        this((Object)var1, (TX509Certificate)null);
    }

    public TValidatorException(Object var1, TX509Certificate var2) {
        super((TString)var1);
        this.type = var1;
        this.cert = var2;
    }

    public TValidatorException(Object var1, TX509Certificate var2, Throwable var3) {
        this(var1, var2);
        this.initCause(var3);
    }

    public TValidatorException(TString var1, Object var2, TX509Certificate var3) {
        super(var1);
        this.type = var2;
        this.cert = var3;
    }

    public TValidatorException(TString var1, Object var2, TX509Certificate var3, Throwable var4) {
        this(var1, var2, var3);
        this.initCause(var4);
    }

    public Object getErrorType() {
        return this.type;
    }

    public TX509Certificate getErrorCertificate() {
        return this.cert;
    }
}
