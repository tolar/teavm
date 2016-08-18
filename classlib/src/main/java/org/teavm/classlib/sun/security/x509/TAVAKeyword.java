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
package org.teavm.classlib.sun.security.x509;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.X500Name;

/**
 * Created by vasek on 18. 8. 2016.
 */
class TAVAKeyword {
    private static final Map<ObjectIdentifier, TAVAKeyword> oidMap = new HashMap();
    private static final Map<String, TAVAKeyword> keywordMap = new HashMap();
    private String keyword;
    private ObjectIdentifier oid;
    private boolean rfc1779Compliant;
    private boolean rfc2253Compliant;

    private TAVAKeyword(String var1, ObjectIdentifier var2, boolean var3, boolean var4) {
        this.keyword = var1;
        this.oid = var2;
        this.rfc1779Compliant = var3;
        this.rfc2253Compliant = var4;
        oidMap.put(var2, this);
        keywordMap.put(var1, this);
    }

    private boolean isCompliant(int var1) {
        switch(var1) {
            case 1:
                return true;
            case 2:
                return this.rfc1779Compliant;
            case 3:
                return this.rfc2253Compliant;
            default:
                throw new IllegalArgumentException("Invalid standard " + var1);
        }
    }

    static ObjectIdentifier getOID(String var0, int var1, Map<String, String> var2) throws IOException {
        var0 = var0.toUpperCase(Locale.ENGLISH);
        if(var1 == 3) {
            if(var0.startsWith(" ") || var0.endsWith(" ")) {
                throw new IOException("Invalid leading or trailing space in keyword \"" + var0 + "\"");
            }
        } else {
            var0 = var0.trim();
        }

        String var3 = (String)var2.get(var0);
        if(var3 == null) {
            TAVAKeyword var4 = (TAVAKeyword)keywordMap.get(var0);
            if(var4 != null && var4.isCompliant(var1)) {
                return var4.oid;
            } else {
                if(var1 == 1 && var0.startsWith("OID.")) {
                    var0 = var0.substring(4);
                }

                boolean var6 = false;
                if(var0.length() != 0) {
                    char var5 = var0.charAt(0);
                    if(var5 >= 48 && var5 <= 57) {
                        var6 = true;
                    }
                }

                if(!var6) {
                    throw new IOException("Invalid keyword \"" + var0 + "\"");
                } else {
                    return new ObjectIdentifier(var0);
                }
            }
        } else {
            return new ObjectIdentifier(var3);
        }
    }

    static String getKeyword(TObjectIdentifier var0, int var1) {
        return getKeyword(var0, var1, Collections.emptyMap());
    }

    static String getKeyword(TObjectIdentifier var0, int var1, Map<String, String> var2) {
        String var3 = var0.toString();
        String var4 = (String)var2.get(var3);
        if(var4 == null) {
            TAVAKeyword var7 = (TAVAKeyword)oidMap.get(var0);
            return var7 != null && var7.isCompliant(var1)?var7.keyword:(var1 == 3?var3:"OID." + var3);
        } else if(var4.length() == 0) {
            throw new IllegalArgumentException("keyword cannot be empty");
        } else {
            var4 = var4.trim();
            char var5 = var4.charAt(0);
            if(var5 < 65 || var5 > 122 || var5 > 90 && var5 < 97) {
                throw new IllegalArgumentException("keyword does not start with letter");
            } else {
                for(int var6 = 1; var6 < var4.length(); ++var6) {
                    var5 = var4.charAt(var6);
                    if((var5 < 65 || var5 > 122 || var5 > 90 && var5 < 97) && (var5 < 48 || var5 > 57) && var5 != 95) {
                        throw new IllegalArgumentException("keyword character is not a letter, digit, or underscore");
                    }
                }

                return var4;
            }
        }
    }

    static boolean hasKeyword(ObjectIdentifier var0, int var1) {
        TAVAKeyword var2 = (TAVAKeyword)oidMap.get(var0);
        return var2 == null?false:var2.isCompliant(var1);
    }

    static {
        new TAVAKeyword("CN", X500Name.commonName_oid, true, true);
        new TAVAKeyword("C", X500Name.countryName_oid, true, true);
        new TAVAKeyword("L", X500Name.localityName_oid, true, true);
        new TAVAKeyword("S", X500Name.stateName_oid, false, false);
        new TAVAKeyword("ST", X500Name.stateName_oid, true, true);
        new TAVAKeyword("O", X500Name.orgName_oid, true, true);
        new TAVAKeyword("OU", X500Name.orgUnitName_oid, true, true);
        new TAVAKeyword("T", X500Name.title_oid, false, false);
        new TAVAKeyword("IP", X500Name.ipAddress_oid, false, false);
        new TAVAKeyword("STREET", X500Name.streetAddress_oid, true, true);
        new TAVAKeyword("DC", X500Name.DOMAIN_COMPONENT_OID, false, true);
        new TAVAKeyword("DNQUALIFIER", X500Name.DNQUALIFIER_OID, false, false);
        new TAVAKeyword("DNQ", X500Name.DNQUALIFIER_OID, false, false);
        new TAVAKeyword("SURNAME", X500Name.SURNAME_OID, false, false);
        new TAVAKeyword("GIVENNAME", X500Name.GIVENNAME_OID, false, false);
        new TAVAKeyword("INITIALS", X500Name.INITIALS_OID, false, false);
        new TAVAKeyword("GENERATION", X500Name.GENERATIONQUALIFIER_OID, false, false);
        new TAVAKeyword("EMAIL", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
        new TAVAKeyword("EMAILADDRESS", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
        new TAVAKeyword("UID", X500Name.userid_oid, false, true);
        new TAVAKeyword("SERIALNUMBER", X500Name.SERIALNUMBER_OID, false, false);
    }
}
