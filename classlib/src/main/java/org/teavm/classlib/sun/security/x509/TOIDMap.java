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
package org.teavm.classlib.sun.security.x509;

import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

import sun.security.util.ObjectIdentifier;

public class TOIDMap {
    private static final String ROOT = "x509.info.extensions";
    private static final String AUTH_KEY_IDENTIFIER = "x509.info.extensions.AuthorityKeyIdentifier";
    private static final String SUB_KEY_IDENTIFIER = "x509.info.extensions.SubjectKeyIdentifier";
    private static final String KEY_USAGE = "x509.info.extensions.KeyUsage";
    private static final String PRIVATE_KEY_USAGE = "x509.info.extensions.PrivateKeyUsage";
    private static final String POLICY_MAPPINGS = "x509.info.extensions.PolicyMappings";
    private static final String SUB_ALT_NAME = "x509.info.extensions.SubjectAlternativeName";
    private static final String ISSUER_ALT_NAME = "x509.info.extensions.IssuerAlternativeName";
    private static final String BASIC_CONSTRAINTS = "x509.info.extensions.BasicConstraints";
    private static final String NAME_CONSTRAINTS = "x509.info.extensions.NameConstraints";
    private static final String POLICY_CONSTRAINTS = "x509.info.extensions.PolicyConstraints";
    private static final String CRL_NUMBER = "x509.info.extensions.CRLNumber";
    private static final String CRL_REASON = "x509.info.extensions.CRLReasonCode";
    private static final String NETSCAPE_CERT = "x509.info.extensions.NetscapeCertType";
    private static final String CERT_POLICIES = "x509.info.extensions.CertificatePolicies";
    private static final String EXT_KEY_USAGE = "x509.info.extensions.ExtendedKeyUsage";
    private static final String INHIBIT_ANY_POLICY = "x509.info.extensions.InhibitAnyPolicy";
    private static final String CRL_DIST_POINTS = "x509.info.extensions.CRLDistributionPoints";
    private static final String CERT_ISSUER = "x509.info.extensions.CertificateIssuer";
    private static final String SUBJECT_INFO_ACCESS = "x509.info.extensions.SubjectInfoAccess";
    private static final String AUTH_INFO_ACCESS = "x509.info.extensions.AuthorityInfoAccess";
    private static final String ISSUING_DIST_POINT = "x509.info.extensions.IssuingDistributionPoint";
    private static final String DELTA_CRL_INDICATOR = "x509.info.extensions.DeltaCRLIndicator";
    private static final String FRESHEST_CRL = "x509.info.extensions.FreshestCRL";
    private static final String OCSPNOCHECK = "x509.info.extensions.OCSPNoCheck";
    private static final int[] NetscapeCertType_data = new int[]{2, 16, 840, 1, 113730, 1, 1};
    private static final Map<TObjectIdentifier, TOIDMap.OIDInfo> oidMap = new HashMap();
    private static final Map<String, TOIDMap.OIDInfo> nameMap = new HashMap();

    private TOIDMap() {
    }

    private static void addInternal(String var0, TObjectIdentifier var1, String var2) {
        TOIDMap.OIDInfo var3 = new TOIDMap.OIDInfo(var0, var1, var2);
        oidMap.put(var1, var3);
        nameMap.put(var0, var3);
    }

    public static void addAttribute(String var0, String var1, Class<?> var2) throws TCertificateException {
        TObjectIdentifier var3;
        try {
            var3 = new TObjectIdentifier(var1);
        } catch (TIOException var5) {
            throw new TCertificateException(TString.wrap("Invalid Object identifier: " + var1));
        }

        TOIDMap.OIDInfo var4 = new TOIDMap.OIDInfo(var0, var3, var2);
        if(oidMap.put(var3, var4) != null) {
            throw new TCertificateException(TString.wrap("Object identifier already exists: " + var1));
        } else if(nameMap.put(var0, var4) != null) {
            throw new TCertificateException(TString.wrap("Name already exists: " + var0));
        }
    }

    public static String getName(ObjectIdentifier var0) {
        TOIDMap.OIDInfo var1 = (TOIDMap.OIDInfo)oidMap.get(var0);
        return var1 == null?null:var1.name;
    }

    public static TObjectIdentifier getOID(String var0) {
        TOIDMap.OIDInfo var1 = (TOIDMap.OIDInfo)nameMap.get(var0);
        return var1 == null?null:var1.oid;
    }

    public static Class<?> getClass(String var0) throws CertificateException {
        TOIDMap.OIDInfo var1 = (TOIDMap.OIDInfo)nameMap.get(var0);
        return var1 == null?null:var1.getClazz();
    }

    public static Class<?> getClass(TObjectIdentifier var0) throws CertificateException {
        TOIDMap.OIDInfo var1 = (TOIDMap.OIDInfo)oidMap.get(var0);
        return var1 == null?null:var1.getClazz();
    }

    static {
        addInternal("x509.info.extensions.SubjectKeyIdentifier", TPKIXExtensions.SubjectKey_Id, "sun.security.x509.SubjectKeyIdentifierExtension");
        addInternal("x509.info.extensions.KeyUsage", TPKIXExtensions.KeyUsage_Id, "sun.security.x509.KeyUsageExtension");
        addInternal("x509.info.extensions.PrivateKeyUsage", TPKIXExtensions.PrivateKeyUsage_Id, "sun.security.x509.PrivateKeyUsageExtension");
        addInternal("x509.info.extensions.SubjectAlternativeName", TPKIXExtensions.SubjectAlternativeName_Id, "sun.security.x509.SubjectAlternativeNameExtension");
        addInternal("x509.info.extensions.IssuerAlternativeName", TPKIXExtensions.IssuerAlternativeName_Id, "sun.security.x509.IssuerAlternativeNameExtension");
        addInternal("x509.info.extensions.BasicConstraints", TPKIXExtensions.BasicConstraints_Id, "sun.security.x509.BasicConstraintsExtension");
        addInternal("x509.info.extensions.CRLNumber", TPKIXExtensions.CRLNumber_Id, "sun.security.x509.CRLNumberExtension");
        addInternal("x509.info.extensions.CRLReasonCode", TPKIXExtensions.ReasonCode_Id, "sun.security.x509.CRLReasonCodeExtension");
        addInternal("x509.info.extensions.NameConstraints", TPKIXExtensions.NameConstraints_Id, "sun.security.x509.NameConstraintsExtension");
        addInternal("x509.info.extensions.PolicyMappings", TPKIXExtensions.PolicyMappings_Id, "sun.security.x509.PolicyMappingsExtension");
        addInternal("x509.info.extensions.AuthorityKeyIdentifier", TPKIXExtensions.AuthorityKey_Id, "sun.security.x509.AuthorityKeyIdentifierExtension");
        addInternal("x509.info.extensions.PolicyConstraints", TPKIXExtensions.PolicyConstraints_Id, "sun.security.x509.PolicyConstraintsExtension");
        addInternal("x509.info.extensions.NetscapeCertType", TObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 113730, 1, 1}), "sun.security.x509.NetscapeCertTypeExtension");
        addInternal("x509.info.extensions.CertificatePolicies", TPKIXExtensions.CertificatePolicies_Id, "sun.security.x509.CertificatePoliciesExtension");
        addInternal("x509.info.extensions.ExtendedKeyUsage", TPKIXExtensions.ExtendedKeyUsage_Id, "sun.security.x509.ExtendedKeyUsageExtension");
        addInternal("x509.info.extensions.InhibitAnyPolicy", TPKIXExtensions.InhibitAnyPolicy_Id, "sun.security.x509.InhibitAnyPolicyExtension");
        addInternal("x509.info.extensions.CRLDistributionPoints", TPKIXExtensions.CRLDistributionPoints_Id, "sun.security.x509.CRLDistributionPointsExtension");
        addInternal("x509.info.extensions.CertificateIssuer", TPKIXExtensions.CertificateIssuer_Id, "sun.security.x509.CertificateIssuerExtension");
        addInternal("x509.info.extensions.SubjectInfoAccess", TPKIXExtensions.SubjectInfoAccess_Id, "sun.security.x509.SubjectInfoAccessExtension");
        addInternal("x509.info.extensions.AuthorityInfoAccess", TPKIXExtensions.AuthInfoAccess_Id, "sun.security.x509.AuthorityInfoAccessExtension");
        addInternal("x509.info.extensions.IssuingDistributionPoint", TPKIXExtensions.IssuingDistributionPoint_Id, "sun.security.x509.IssuingDistributionPointExtension");
        addInternal("x509.info.extensions.DeltaCRLIndicator", TPKIXExtensions.DeltaCRLIndicator_Id, "sun.security.x509.DeltaCRLIndicatorExtension");
        addInternal("x509.info.extensions.FreshestCRL", TPKIXExtensions.FreshestCRL_Id, "sun.security.x509.FreshestCRLExtension");
        addInternal("x509.info.extensions.OCSPNoCheck", TPKIXExtensions.OCSPNoCheck_Id, "sun.security.x509.OCSPNoCheckExtension");
    }

    private static class OIDInfo {
        final TObjectIdentifier oid;
        final String name;
        final String className;
        private volatile Class<?> clazz;

        OIDInfo(String var1, TObjectIdentifier var2, String var3) {
            this.name = var1;
            this.oid = var2;
            this.className = var3;
        }

        OIDInfo(String var1, TObjectIdentifier var2, Class<?> var3) {
            this.name = var1;
            this.oid = var2;
            this.className = var3.getName();
            this.clazz = var3;
        }

        Class<?> getClazz() throws CertificateException {
            try {
                Class var1 = this.clazz;
                if(var1 == null) {
                    var1 = Class.forName(this.className);
                    this.clazz = var1;
                }

                return var1;
            } catch (ClassNotFoundException var2) {
                throw new CertificateException("Could not load class: " + var2, var2);
            }
        }
    }
}
