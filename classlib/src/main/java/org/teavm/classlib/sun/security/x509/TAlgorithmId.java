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

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TNoSuchAlgorithmException;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class TAlgorithmId {

    private static final long serialVersionUID = 7205873507486557157L;
    private ObjectIdentifier algid;
    private AlgorithmParameters algParams;
    private boolean constructedFromDer = true;
    protected DerValue params;
    private static boolean initOidTable = false;
    private static Map<String, ObjectIdentifier> oidTable;
    private static final Map<ObjectIdentifier, String> nameTable;
    public static final ObjectIdentifier MD2_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 2, 2});
    public static final ObjectIdentifier MD5_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 2, 5});
    public static final ObjectIdentifier SHA_oid = ObjectIdentifier.newInternal(new int[]{1, 3, 14, 3, 2, 26});
    public static final ObjectIdentifier SHA224_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 4});
    public static final ObjectIdentifier SHA256_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 1});
    public static final ObjectIdentifier SHA384_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 2});
    public static final ObjectIdentifier SHA512_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 3});
    private static final int[] DH_data = new int[]{1, 2, 840, 113549, 1, 3, 1};
    private static final int[] DH_PKIX_data = new int[]{1, 2, 840, 10046, 2, 1};
    private static final int[] DSA_OIW_data = new int[]{1, 3, 14, 3, 2, 12};
    private static final int[] DSA_PKIX_data = new int[]{1, 2, 840, 10040, 4, 1};
    private static final int[] RSA_data = new int[]{2, 5, 8, 1, 1};
    private static final int[] RSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 1};
    public static final ObjectIdentifier DH_oid;
    public static final ObjectIdentifier DH_PKIX_oid;
    public static final ObjectIdentifier DSA_oid;
    public static final ObjectIdentifier DSA_OIW_oid;
    public static final ObjectIdentifier EC_oid = oid(new int[]{1, 2, 840, 10045, 2, 1});
    public static final ObjectIdentifier ECDH_oid = oid(new int[]{1, 3, 132, 1, 12});
    public static final ObjectIdentifier RSA_oid;
    public static final ObjectIdentifier RSAEncryption_oid;
    public static final ObjectIdentifier AES_oid = oid(new int[]{2, 16, 840, 1, 101, 3, 4, 1});
    private static final int[] md2WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 2};
    private static final int[] md5WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 4};
    private static final int[] sha1WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 5};
    private static final int[] sha1WithRSAEncryption_OIW_data = new int[]{1, 3, 14, 3, 2, 29};
    private static final int[] sha224WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 14};
    private static final int[] sha256WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 11};
    private static final int[] sha384WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 12};
    private static final int[] sha512WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 13};
    private static final int[] shaWithDSA_OIW_data = new int[]{1, 3, 14, 3, 2, 13};
    private static final int[] sha1WithDSA_OIW_data = new int[]{1, 3, 14, 3, 2, 27};
    private static final int[] dsaWithSHA1_PKIX_data = new int[]{1, 2, 840, 10040, 4, 3};
    public static final ObjectIdentifier md2WithRSAEncryption_oid;
    public static final ObjectIdentifier md5WithRSAEncryption_oid;
    public static final ObjectIdentifier sha1WithRSAEncryption_oid;
    public static final ObjectIdentifier sha1WithRSAEncryption_OIW_oid;
    public static final ObjectIdentifier sha224WithRSAEncryption_oid;
    public static final ObjectIdentifier sha256WithRSAEncryption_oid;
    public static final ObjectIdentifier sha384WithRSAEncryption_oid;
    public static final ObjectIdentifier sha512WithRSAEncryption_oid;
    public static final ObjectIdentifier shaWithDSA_OIW_oid;
    public static final ObjectIdentifier sha1WithDSA_OIW_oid;
    public static final ObjectIdentifier sha1WithDSA_oid;
    public static final ObjectIdentifier sha224WithDSA_oid = oid(new int[]{2, 16, 840, 1, 101, 3, 4, 3, 1});
    public static final ObjectIdentifier sha256WithDSA_oid = oid(new int[]{2, 16, 840, 1, 101, 3, 4, 3, 2});
    public static final ObjectIdentifier sha1WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 1});
    public static final ObjectIdentifier sha224WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3, 1});
    public static final ObjectIdentifier sha256WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3, 2});
    public static final ObjectIdentifier sha384WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3, 3});
    public static final ObjectIdentifier sha512WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3, 4});
    public static final ObjectIdentifier specifiedWithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3});
    public static final ObjectIdentifier pbeWithMD5AndDES_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 3});
    public static final ObjectIdentifier pbeWithMD5AndRC2_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 6});
    public static final ObjectIdentifier pbeWithSHA1AndDES_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 10});
    public static final ObjectIdentifier pbeWithSHA1AndRC2_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 11});
    public static ObjectIdentifier pbeWithSHA1AndDESede_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 12, 1, 3});
    public static ObjectIdentifier pbeWithSHA1AndRC2_40_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 12, 1, 6});

    private TAlgorithmId(ObjectIdentifier var1, DerValue var2) throws TIOException {
        this.algid = var1;
        this.params = var2;
        if(this.params != null) {
            this.decodeParams();
        }

    }

    protected void decodeParams() throws TIOException {
        String var1 = this.algid.toString();

        try {
            this.algParams = AlgorithmParameters.getInstance(var1);
        } catch (NoSuchAlgorithmException var3) {
            this.algParams = null;
            return;
        }

        this.algParams.init(this.params.toByteArray());
    }

    public static TAlgorithmId parse(TDerValue var0) throws TIOException {
        if(var0.tag != 48) {
            throw new TIOException(TString.wrap("algid parse error, not a sequence"));
        } else {
            TDerInputStream var3 = var0.toDerInputStream();
            TObjectIdentifier var1 = var3.getOID();
            TDerValue var2;
            if(var3.available() == 0) {
                var2 = null;
            } else {
                var2 = var3.getDerValue();
                if(var2.tag == 5) {
                    if(var2.length() != 0) {
                        throw new TIOException(TString.wrap("invalid NULL"));
                    }

                    var2 = null;
                }

                if(var3.available() != 0) {
                    throw new TIOException(TString.wrap("Invalid AlgorithmIdentifier: extra data"));
                }
            }

            return new TAlgorithmId(var1, var2);
        }
    }

    public static TAlgorithmId get(String var0) throws TNoSuchAlgorithmException {
        ObjectIdentifier var1;
        try {
            var1 = algOID(var0);
        } catch (IOException var3) {
            throw new TNoSuchAlgorithmException("Invalid ObjectIdentifier " + var0);
        }

        if(var1 == null) {
            throw new TNoSuchAlgorithmException("unrecognized algorithm name: " + var0);
        } else {
            return new TAlgorithmId(var1);
        }
    }

    private static ObjectIdentifier algOID(String var0) throws TIOException {
        if(var0.indexOf(46) != -1) {
            return var0.startsWith("OID.")?new TObjectIdentifier(var0.substring("OID.".length())):new TObjectIdentifier(var0);
        } else if(var0.equalsIgnoreCase("MD5")) {
            return MD5_oid;
        } else if(var0.equalsIgnoreCase("MD2")) {
            return MD2_oid;
        } else if(!var0.equalsIgnoreCase("SHA") && !var0.equalsIgnoreCase("SHA1") && !var0.equalsIgnoreCase("SHA-1")) {
            if(!var0.equalsIgnoreCase("SHA-256") && !var0.equalsIgnoreCase("SHA256")) {
                if(!var0.equalsIgnoreCase("SHA-384") && !var0.equalsIgnoreCase("SHA384")) {
                    if(!var0.equalsIgnoreCase("SHA-512") && !var0.equalsIgnoreCase("SHA512")) {
                        if(!var0.equalsIgnoreCase("SHA-224") && !var0.equalsIgnoreCase("SHA224")) {
                            if(var0.equalsIgnoreCase("RSA")) {
                                return RSAEncryption_oid;
                            } else if(!var0.equalsIgnoreCase("Diffie-Hellman") && !var0.equalsIgnoreCase("DH")) {
                                if(var0.equalsIgnoreCase("DSA")) {
                                    return DSA_oid;
                                } else if(var0.equalsIgnoreCase("EC")) {
                                    return EC_oid;
                                } else if(var0.equalsIgnoreCase("ECDH")) {
                                    return ECDH_oid;
                                } else if(var0.equalsIgnoreCase("AES")) {
                                    return AES_oid;
                                } else if(!var0.equalsIgnoreCase("MD5withRSA") && !var0.equalsIgnoreCase("MD5/RSA")) {
                                    if(!var0.equalsIgnoreCase("MD2withRSA") && !var0.equalsIgnoreCase("MD2/RSA")) {
                                        if(!var0.equalsIgnoreCase("SHAwithDSA") && !var0.equalsIgnoreCase("SHA1withDSA") && !var0.equalsIgnoreCase("SHA/DSA") && !var0.equalsIgnoreCase("SHA1/DSA") && !var0.equalsIgnoreCase("DSAWithSHA1") && !var0.equalsIgnoreCase("DSS") && !var0.equalsIgnoreCase("SHA-1/DSA")) {
                                            if(var0.equalsIgnoreCase("SHA224WithDSA")) {
                                                return sha224WithDSA_oid;
                                            } else if(var0.equalsIgnoreCase("SHA256WithDSA")) {
                                                return sha256WithDSA_oid;
                                            } else if(!var0.equalsIgnoreCase("SHA1WithRSA") && !var0.equalsIgnoreCase("SHA1/RSA")) {
                                                if(!var0.equalsIgnoreCase("SHA1withECDSA") && !var0.equalsIgnoreCase("ECDSA")) {
                                                    if(var0.equalsIgnoreCase("SHA224withECDSA")) {
                                                        return sha224WithECDSA_oid;
                                                    } else if(var0.equalsIgnoreCase("SHA256withECDSA")) {
                                                        return sha256WithECDSA_oid;
                                                    } else if(var0.equalsIgnoreCase("SHA384withECDSA")) {
                                                        return sha384WithECDSA_oid;
                                                    } else if(var0.equalsIgnoreCase("SHA512withECDSA")) {
                                                        return sha512WithECDSA_oid;
                                                    } else {
                                                        if(!initOidTable) {
                                                            Provider[] var2 = Security.getProviders();

                                                            for(int var3 = 0; var3 < var2.length; ++var3) {
                                                                Enumeration var4 = var2[var3].keys();

                                                                while(var4.hasMoreElements()) {
                                                                    String var5 = (String)var4.nextElement();
                                                                    String var6 = var5.toUpperCase(Locale.ENGLISH);
                                                                    int var7;
                                                                    if(var6.startsWith("ALG.ALIAS") && (var7 = var6.indexOf("OID.", 0)) != -1) {
                                                                        var7 += "OID.".length();
                                                                        if(var7 == var5.length()) {
                                                                            break;
                                                                        }

                                                                        if(oidTable == null) {
                                                                            oidTable = new HashMap();
                                                                        }

                                                                        String var1 = var5.substring(var7);
                                                                        String var8 = var2[var3].getProperty(var5);
                                                                        if(var8 != null) {
                                                                            var8 = var8.toUpperCase(Locale.ENGLISH);
                                                                        }

                                                                        if(var8 != null && oidTable.get(var8) == null) {
                                                                            oidTable.put(var8, new ObjectIdentifier(var1));
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            if(oidTable == null) {
                                                                oidTable = Collections.emptyMap();
                                                            }

                                                            initOidTable = true;
                                                        }

                                                        return (ObjectIdentifier)oidTable.get(var0.toUpperCase(Locale.ENGLISH));
                                                    }
                                                } else {
                                                    return sha1WithECDSA_oid;
                                                }
                                            } else {
                                                return sha1WithRSAEncryption_oid;
                                            }
                                        } else {
                                            return sha1WithDSA_oid;
                                        }
                                    } else {
                                        return md2WithRSAEncryption_oid;
                                    }
                                } else {
                                    return md5WithRSAEncryption_oid;
                                }
                            } else {
                                return DH_oid;
                            }
                        } else {
                            return SHA224_oid;
                        }
                    } else {
                        return SHA512_oid;
                    }
                } else {
                    return SHA384_oid;
                }
            } else {
                return SHA256_oid;
            }
        } else {
            return SHA_oid;
        }
    }

    private static ObjectIdentifier oid(int... var0) {
        return ObjectIdentifier.newInternal(var0);
    }
}
