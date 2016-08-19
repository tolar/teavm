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

import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TPKIXExtensions {
    private static final int[] AuthorityKey_data = new int[]{2, 5, 29, 35};
    private static final int[] SubjectKey_data = new int[]{2, 5, 29, 14};
    private static final int[] KeyUsage_data = new int[]{2, 5, 29, 15};
    private static final int[] PrivateKeyUsage_data = new int[]{2, 5, 29, 16};
    private static final int[] CertificatePolicies_data = new int[]{2, 5, 29, 32};
    private static final int[] PolicyMappings_data = new int[]{2, 5, 29, 33};
    private static final int[] SubjectAlternativeName_data = new int[]{2, 5, 29, 17};
    private static final int[] IssuerAlternativeName_data = new int[]{2, 5, 29, 18};
    private static final int[] SubjectDirectoryAttributes_data = new int[]{2, 5, 29, 9};
    private static final int[] BasicConstraints_data = new int[]{2, 5, 29, 19};
    private static final int[] NameConstraints_data = new int[]{2, 5, 29, 30};
    private static final int[] PolicyConstraints_data = new int[]{2, 5, 29, 36};
    private static final int[] CRLDistributionPoints_data = new int[]{2, 5, 29, 31};
    private static final int[] CRLNumber_data = new int[]{2, 5, 29, 20};
    private static final int[] IssuingDistributionPoint_data = new int[]{2, 5, 29, 28};
    private static final int[] DeltaCRLIndicator_data = new int[]{2, 5, 29, 27};
    private static final int[] ReasonCode_data = new int[]{2, 5, 29, 21};
    private static final int[] HoldInstructionCode_data = new int[]{2, 5, 29, 23};
    private static final int[] InvalidityDate_data = new int[]{2, 5, 29, 24};
    private static final int[] ExtendedKeyUsage_data = new int[]{2, 5, 29, 37};
    private static final int[] InhibitAnyPolicy_data = new int[]{2, 5, 29, 54};
    private static final int[] CertificateIssuer_data = new int[]{2, 5, 29, 29};
    private static final int[] AuthInfoAccess_data = new int[]{1, 3, 6, 1, 5, 5, 7, 1, 1};
    private static final int[] SubjectInfoAccess_data = new int[]{1, 3, 6, 1, 5, 5, 7, 1, 11};
    private static final int[] FreshestCRL_data = new int[]{2, 5, 29, 46};
    private static final int[] OCSPNoCheck_data = new int[]{1, 3, 6, 1, 5, 5, 7, 48, 1, 5};
    public static final TObjectIdentifier AuthorityKey_Id;
    public static final TObjectIdentifier SubjectKey_Id;
    public static final TObjectIdentifier KeyUsage_Id;
    public static final TObjectIdentifier PrivateKeyUsage_Id;
    public static final TObjectIdentifier CertificatePolicies_Id;
    public static final TObjectIdentifier PolicyMappings_Id;
    public static final TObjectIdentifier SubjectAlternativeName_Id;
    public static final TObjectIdentifier IssuerAlternativeName_Id;
    public static final TObjectIdentifier SubjectDirectoryAttributes_Id;
    public static final TObjectIdentifier BasicConstraints_Id;
    public static final TObjectIdentifier NameConstraints_Id;
    public static final TObjectIdentifier PolicyConstraints_Id;
    public static final TObjectIdentifier CRLDistributionPoints_Id;
    public static final TObjectIdentifier CRLNumber_Id;
    public static final TObjectIdentifier IssuingDistributionPoint_Id;
    public static final TObjectIdentifier DeltaCRLIndicator_Id;
    public static final TObjectIdentifier ReasonCode_Id;
    public static final TObjectIdentifier HoldInstructionCode_Id;
    public static final TObjectIdentifier InvalidityDate_Id;
    public static final TObjectIdentifier ExtendedKeyUsage_Id;
    public static final TObjectIdentifier InhibitAnyPolicy_Id;
    public static final TObjectIdentifier CertificateIssuer_Id;
    public static final TObjectIdentifier AuthInfoAccess_Id;
    public static final TObjectIdentifier SubjectInfoAccess_Id;
    public static final TObjectIdentifier FreshestCRL_Id;
    public static final TObjectIdentifier OCSPNoCheck_Id;

    public TPKIXExtensions() {
    }

    static {
        AuthorityKey_Id = TObjectIdentifier.newInternal(AuthorityKey_data);
        SubjectKey_Id = TObjectIdentifier.newInternal(SubjectKey_data);
        KeyUsage_Id = TObjectIdentifier.newInternal(KeyUsage_data);
        PrivateKeyUsage_Id = TObjectIdentifier.newInternal(PrivateKeyUsage_data);
        CertificatePolicies_Id = TObjectIdentifier.newInternal(CertificatePolicies_data);
        PolicyMappings_Id = TObjectIdentifier.newInternal(PolicyMappings_data);
        SubjectAlternativeName_Id = TObjectIdentifier.newInternal(SubjectAlternativeName_data);
        IssuerAlternativeName_Id = TObjectIdentifier.newInternal(IssuerAlternativeName_data);
        ExtendedKeyUsage_Id = TObjectIdentifier.newInternal(ExtendedKeyUsage_data);
        InhibitAnyPolicy_Id = TObjectIdentifier.newInternal(InhibitAnyPolicy_data);
        SubjectDirectoryAttributes_Id = TObjectIdentifier.newInternal(SubjectDirectoryAttributes_data);
        BasicConstraints_Id = TObjectIdentifier.newInternal(BasicConstraints_data);
        ReasonCode_Id = TObjectIdentifier.newInternal(ReasonCode_data);
        HoldInstructionCode_Id = TObjectIdentifier.newInternal(HoldInstructionCode_data);
        InvalidityDate_Id = TObjectIdentifier.newInternal(InvalidityDate_data);
        NameConstraints_Id = TObjectIdentifier.newInternal(NameConstraints_data);
        PolicyConstraints_Id = TObjectIdentifier.newInternal(PolicyConstraints_data);
        CRLDistributionPoints_Id = TObjectIdentifier.newInternal(CRLDistributionPoints_data);
        CRLNumber_Id = TObjectIdentifier.newInternal(CRLNumber_data);
        IssuingDistributionPoint_Id = TObjectIdentifier.newInternal(IssuingDistributionPoint_data);
        DeltaCRLIndicator_Id = TObjectIdentifier.newInternal(DeltaCRLIndicator_data);
        CertificateIssuer_Id = TObjectIdentifier.newInternal(CertificateIssuer_data);
        AuthInfoAccess_Id = TObjectIdentifier.newInternal(AuthInfoAccess_data);
        SubjectInfoAccess_Id = TObjectIdentifier.newInternal(SubjectInfoAccess_data);
        FreshestCRL_Id = TObjectIdentifier.newInternal(FreshestCRL_data);
        OCSPNoCheck_Id = TObjectIdentifier.newInternal(OCSPNoCheck_data);
    }
}
