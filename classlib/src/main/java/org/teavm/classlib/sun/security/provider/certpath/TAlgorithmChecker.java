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

import java.math.BigInteger;
import java.security.CryptoPrimitive;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TAlgorithmConstraints;
import org.teavm.classlib.java.security.TAlgorithmParameters;
import org.teavm.classlib.java.security.TPublicKey;
import org.teavm.classlib.java.security.cert.TCertificate;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.security.cert.TTrustAnchor;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.sun.security.util.TDisabledAlgorithmConstraints;
import org.teavm.classlib.sun.security.x509.TAlgorithmId;
import org.teavm.classlib.sun.security.x509.TX509CertImpl;

/**
 * Created by vasek on 22. 10. 2016.
 */
public final class TAlgorithmChecker extends PKIXCertPathChecker {
    private final TAlgorithmConstraints constraints;
    private final TPublicKey trustedPubKey;
    private TPublicKey prevPubKey;
    private static final Set<CryptoPrimitive> SIGNATURE_PRIMITIVE_SET;
    private static final Set<CryptoPrimitive> KU_PRIMITIVE_SET;
    private static final TDisabledAlgorithmConstraints certPathDefaultConstraints;

    public TAlgorithmChecker(TrustAnchor var1) {
        this(var1, certPathDefaultConstraints);
    }

    public TAlgorithmChecker(TAlgorithmConstraints var1) {
        this.prevPubKey = null;
        this.trustedPubKey = null;
        this.constraints = var1;
    }

    public TAlgorithmChecker(TTrustAnchor var1, TAlgorithmConstraints var2) {
        if(var1 == null) {
            throw new IllegalArgumentException("The trust anchor cannot be null");
        } else {
            if(var1.getTrustedCert() != null) {
                this.trustedPubKey = var1.getTrustedCert().getPublicKey();
            } else {
                this.trustedPubKey = var1.getCAPublicKey();
            }

            this.prevPubKey = this.trustedPubKey;
            this.constraints = var2;
        }
    }

    public void init(boolean var1) throws CertPathValidatorException {
        if(!var1) {
            if(this.trustedPubKey != null) {
                this.prevPubKey = this.trustedPubKey;
            } else {
                this.prevPubKey = null;
            }

        } else {
            throw new CertPathValidatorException("forward checking not supported");
        }
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        return null;
    }

    public void check(TCertificate var1, Collection<String> var2) throws CertPathValidatorException {
        if(var1 instanceof TX509Certificate && this.constraints != null) {
            TX509CertImpl var3 = null;

            try {
                var3 = TX509CertImpl.toImpl((TX509Certificate)var1);
            } catch (TCertificateException var16) {
                throw new CertPathValidatorException(var16);
            }

            TPublicKey var4 = var3.getPublicKey();
            TString var5 = var3.getSigAlgName();
            TAlgorithmId var6 = null;

            try {
                var6 = (TAlgorithmId)var3.get(TString.wrap("x509.algorithm"));
            } catch (CertificateException var15) {
                throw new CertPathValidatorException(var15);
            }

            TAlgorithmParameters var7 = var6.getParameters();
            if(!this.constraints.permits(SIGNATURE_PRIMITIVE_SET, var5, var7)) {
                throw new CertPathValidatorException("Algorithm constraints check failed: " + var5, (Throwable)null, (CertPath)null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            } else {
                boolean[] var8 = var3.getKeyUsage();
                if(var8 != null && var8.length < 9) {
                    throw new CertPathValidatorException("incorrect KeyUsage extension", (Throwable)null, (CertPath)null, -1, PKIXReason.INVALID_KEY_USAGE);
                } else {
                    Object var9 = KU_PRIMITIVE_SET;
                    if(var8 != null) {
                        var9 = EnumSet.noneOf(CryptoPrimitive.class);
                        if(var8[0] || var8[1] || var8[5] || var8[6]) {
                            ((Set)var9).add(CryptoPrimitive.SIGNATURE);
                        }

                        if(var8[2]) {
                            ((Set)var9).add(CryptoPrimitive.KEY_ENCAPSULATION);
                        }

                        if(var8[3]) {
                            ((Set)var9).add(CryptoPrimitive.PUBLIC_KEY_ENCRYPTION);
                        }

                        if(var8[4]) {
                            ((Set)var9).add(CryptoPrimitive.KEY_AGREEMENT);
                        }

                        if(((Set)var9).isEmpty()) {
                            throw new CertPathValidatorException("incorrect KeyUsage extension", (Throwable)null, (CertPath)null, -1, PKIXReason.INVALID_KEY_USAGE);
                        }
                    }

                    if(!this.constraints.permits((Set)var9, var4)) {
                        throw new CertPathValidatorException("algorithm constraints check failed", (Throwable)null, (CertPath)null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
                    } else {
                        if(this.prevPubKey != null) {
                            if(var5 != null && !this.constraints.permits(SIGNATURE_PRIMITIVE_SET, var5, this.prevPubKey, var7)) {
                                throw new CertPathValidatorException("Algorithm constraints check failed: " + var5, (Throwable)null, (CertPath)null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
                            }

                            if(TPKIX.isDSAPublicKeyWithoutParams(var4)) {
                                if(!(this.prevPubKey instanceof DSAPublicKey)) {
                                    throw new CertPathValidatorException("Input key is not of a appropriate type for inheriting parameters");
                                }

                                DSAParams var10 = ((DSAPublicKey)this.prevPubKey).getParams();
                                if(var10 == null) {
                                    throw new CertPathValidatorException("Key parameters missing");
                                }

                                try {
                                    BigInteger var11 = ((DSAPublicKey)var4).getY();
                                    KeyFactory var12 = KeyFactory.getInstance("DSA");
                                    DSAPublicKeySpec
                                            var13 = new DSAPublicKeySpec(var11, var10.getP(), var10.getQ(), var10.getG());
                                    var4 = var12.generatePublic(var13);
                                } catch (GeneralSecurityException var14) {
                                    throw new CertPathValidatorException("Unable to generate key with inherited parameters: " + var14.getMessage(), var14);
                                }
                            }
                        }

                        this.prevPubKey = var4;
                    }
                }
            }
        }
    }

    void trySetTrustAnchor(TrustAnchor var1) {
        if(this.prevPubKey == null) {
            if(var1 == null) {
                throw new IllegalArgumentException("The trust anchor cannot be null");
            }

            if(var1.getTrustedCert() != null) {
                this.prevPubKey = var1.getTrustedCert().getPublicKey();
            } else {
                this.prevPubKey = var1.getCAPublicKey();
            }
        }

    }

    static void check(PublicKey var0, X509CRL var1) throws CertPathValidatorException {
        X509CRLImpl var2 = null;

        try {
            var2 = X509CRLImpl.toImpl(var1);
        } catch (CRLException var4) {
            throw new CertPathValidatorException(var4);
        }

        TAlgorithmId var3 = var2.getSigAlgId();
        check(var0, var3);
    }

    static void check(PublicKey var0, TAlgorithmId var1) throws CertPathValidatorException {
        TString var2 = var1.getName();
        TAlgorithmParameters var3 = var1.getParameters();
        if(!certPathDefaultConstraints.permits(SIGNATURE_PRIMITIVE_SET, var2, var0, var3)) {
            throw new CertPathValidatorException("algorithm check failed: " + var2 + " is disabled", (Throwable)null, (CertPath)null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
        }
    }

    static {
        SIGNATURE_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE));
        KU_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE, CryptoPrimitive.KEY_ENCAPSULATION, CryptoPrimitive.PUBLIC_KEY_ENCRYPTION, CryptoPrimitive.KEY_AGREEMENT));
        certPathDefaultConstraints = new DisabledAlgorithmConstraints("jdk.certpath.disabledAlgorithms");
    }
}
