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

import org.teavm.classlib.java.security.TAlgorithmConstraints;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.java.util.TCollection;
import org.teavm.classlib.java.util.TDate;

/**
 * Created by vasek on 21. 10. 2016.
 */
public abstract class TValidator {
    static final TX509Certificate[] CHAIN0 = new TX509Certificate[0];
    public static final String TYPE_SIMPLE = "Simple";
    public static final String TYPE_PKIX = "PKIX";
    public static final String VAR_GENERIC = "generic";
    public static final String VAR_CODE_SIGNING = "code signing";
    public static final String VAR_JCE_SIGNING = "jce signing";
    public static final String VAR_TLS_CLIENT = "tls client";
    public static final String VAR_TLS_SERVER = "tls server";
    public static final String VAR_TSA_SERVER = "tsa server";
    public static final String VAR_PLUGIN_CODE_SIGNING = "plugin code signing";
    final TEndEntityChecker endEntityChecker;
    final String variant;
    /** @deprecated */
    @Deprecated
    volatile TDate validationDate;

    TValidator(String var1, String var2) {
        this.variant = var2;
        this.endEntityChecker = TEndEntityChecker.getInstance(var1, var2);
    }

    public static sun.security.validator.Validator getInstance(String var0, String var1, TKeyStore var2) {
        return getInstance(var0, var1, (TCollection) KeyStores.getTrustedCerts(var2));
    }

    public static sun.security.validator.Validator getInstance(String var0, String var1, Collection<X509Certificate> var2) {
        if(var0.equals("Simple")) {
            return new TSimpleValidator(var1, var2);
        } else if(var0.equals("PKIX")) {
            return new PKIXValidator(var1, var2);
        } else {
            throw new IllegalArgumentException("Unknown validator type: " + var0);
        }
    }

    public static sun.security.validator.Validator getInstance(String var0, String var1, PKIXBuilderParameters var2) {
        if(!var0.equals("PKIX")) {
            throw new IllegalArgumentException("getInstance(PKIXBuilderParameters) can only be used with PKIX validator");
        } else {
            return new PKIXValidator(var1, var2);
        }
    }

    public final TX509Certificate[] validate(TX509Certificate[] var1) throws TCertificateException {
        return this.validate(var1, (TCollection)null, (Object)null);
    }

    public final TX509Certificate[] validate(TX509Certificate[] var1, TCollection<TX509Certificate> var2) throws TCertificateException {
        return this.validate(var1, var2, (Object)null);
    }

    public final TX509Certificate[] validate(TX509Certificate[] var1, TCollection<TX509Certificate> var2, Object var3) throws TCertificateException {
        return this.validate(var1, var2, (TAlgorithmConstraints)null, var3);
    }

    public final TX509Certificate[] validate(TX509Certificate[] var1, TCollection<TX509Certificate> var2, TAlgorithmConstraints var3, Object var4) throws CertificateException {
        var1 = this.engineValidate(var1, var2, var3, var4);
        if(var1.length > 1) {
            this.endEntityChecker.check(var1[0], var4);
        }

        return var1;
    }

    abstract TX509Certificate[] engineValidate(TX509Certificate[] var1, TCollection<TX509Certificate> var2, TAlgorithmConstraints var3, Object var4) throws TCertificateException;

    public abstract TCollection<TX509Certificate> getTrustedCertificates();

    /** @deprecated */
    @Deprecated
    public void setValidationDate(TDate var1) {
        this.validationDate = var1;
    }
}
