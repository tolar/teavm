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

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TAlgorithmParameters;
import org.teavm.classlib.java.security.TNoSuchAlgorithmException;
import org.teavm.classlib.sun.security.util.TDerEncoder;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TAlgorithmId implements TDerEncoder {

    private static final long serialVersionUID = 7205873507486557157L;
    private TObjectIdentifier algid;
    private TAlgorithmParameters algParams;
    private boolean constructedFromDer = true;
    protected TDerValue params;
    private static boolean initOidTable = false;
    private static Map<String, TObjectIdentifier> oidTable;
    private static final Map<TObjectIdentifier, TString> nameTable;
    public static final TObjectIdentifier MD2_oid = TObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 2, 2});
    public static final TObjectIdentifier MD5_oid = TObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 2, 5});
    public static final TObjectIdentifier SHA_oid = TObjectIdentifier.newInternal(new int[]{1, 3, 14, 3, 2, 26});
    public static final TObjectIdentifier SHA224_oid = TObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 4});
    public static final TObjectIdentifier SHA256_oid = TObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 1});
    public static final TObjectIdentifier SHA384_oid = TObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 2});
    public static final TObjectIdentifier SHA512_oid = TObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 3});
    private static final int[] DH_data = new int[]{1, 2, 840, 113549, 1, 3, 1};
    private static final int[] DH_PKIX_data = new int[]{1, 2, 840, 10046, 2, 1};
    private static final int[] DSA_OIW_data = new int[]{1, 3, 14, 3, 2, 12};
    private static final int[] DSA_PKIX_data = new int[]{1, 2, 840, 10040, 4, 1};
    private static final int[] RSA_data = new int[]{2, 5, 8, 1, 1};
    private static final int[] RSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 1};
    public static final TObjectIdentifier DH_oid;
    public static final TObjectIdentifier DH_PKIX_oid;
    public static final TObjectIdentifier DSA_oid;
    public static final TObjectIdentifier DSA_OIW_oid;
    public static final TObjectIdentifier EC_oid = oid(new int[]{1, 2, 840, 10045, 2, 1});
    public static final TObjectIdentifier ECDH_oid = oid(new int[]{1, 3, 132, 1, 12});
    public static final TObjectIdentifier RSA_oid;
    public static final TObjectIdentifier RSAEncryption_oid;
    public static final TObjectIdentifier AES_oid = oid(new int[]{2, 16, 840, 1, 101, 3, 4, 1});
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
    public static final TObjectIdentifier md2WithRSAEncryption_oid;
    public static final TObjectIdentifier md5WithRSAEncryption_oid;
    public static final TObjectIdentifier sha1WithRSAEncryption_oid;
    public static final TObjectIdentifier sha1WithRSAEncryption_OIW_oid;
    public static final TObjectIdentifier sha224WithRSAEncryption_oid;
    public static final TObjectIdentifier sha256WithRSAEncryption_oid;
    public static final TObjectIdentifier sha384WithRSAEncryption_oid;
    public static final TObjectIdentifier sha512WithRSAEncryption_oid;
    public static final TObjectIdentifier shaWithDSA_OIW_oid;
    public static final TObjectIdentifier sha1WithDSA_OIW_oid;
    public static final TObjectIdentifier sha1WithDSA_oid;
    public static final TObjectIdentifier sha224WithDSA_oid = oid(new int[]{2, 16, 840, 1, 101, 3, 4, 3, 1});
    public static final TObjectIdentifier sha256WithDSA_oid = oid(new int[]{2, 16, 840, 1, 101, 3, 4, 3, 2});
    public static final TObjectIdentifier sha1WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 1});
    public static final TObjectIdentifier sha224WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3, 1});
    public static final TObjectIdentifier sha256WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3, 2});
    public static final TObjectIdentifier sha384WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3, 3});
    public static final TObjectIdentifier sha512WithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3, 4});
    public static final TObjectIdentifier specifiedWithECDSA_oid = oid(new int[]{1, 2, 840, 10045, 4, 3});
    public static final TObjectIdentifier pbeWithMD5AndDES_oid = TObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 3});
    public static final TObjectIdentifier pbeWithMD5AndRC2_oid = TObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 6});
    public static final TObjectIdentifier pbeWithSHA1AndDES_oid = TObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 10});
    public static final TObjectIdentifier pbeWithSHA1AndRC2_oid = TObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 11});
    public static TObjectIdentifier pbeWithSHA1AndDESede_oid = TObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 12, 1, 3});
    public static TObjectIdentifier pbeWithSHA1AndRC2_40_oid = TObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 12, 1, 6});

    static {
        DH_oid = TObjectIdentifier.newInternal(DH_data);
        DH_PKIX_oid = TObjectIdentifier.newInternal(DH_PKIX_data);
        DSA_OIW_oid = TObjectIdentifier.newInternal(DSA_OIW_data);
        DSA_oid = TObjectIdentifier.newInternal(DSA_PKIX_data);
        RSA_oid = TObjectIdentifier.newInternal(RSA_data);
        RSAEncryption_oid = TObjectIdentifier.newInternal(RSAEncryption_data);
        md2WithRSAEncryption_oid = TObjectIdentifier.newInternal(md2WithRSAEncryption_data);
        md5WithRSAEncryption_oid = TObjectIdentifier.newInternal(md5WithRSAEncryption_data);
        sha1WithRSAEncryption_oid = TObjectIdentifier.newInternal(sha1WithRSAEncryption_data);
        sha1WithRSAEncryption_OIW_oid = TObjectIdentifier.newInternal(sha1WithRSAEncryption_OIW_data);
        sha224WithRSAEncryption_oid = TObjectIdentifier.newInternal(sha224WithRSAEncryption_data);
        sha256WithRSAEncryption_oid = TObjectIdentifier.newInternal(sha256WithRSAEncryption_data);
        sha384WithRSAEncryption_oid = TObjectIdentifier.newInternal(sha384WithRSAEncryption_data);
        sha512WithRSAEncryption_oid = TObjectIdentifier.newInternal(sha512WithRSAEncryption_data);
        shaWithDSA_OIW_oid = TObjectIdentifier.newInternal(shaWithDSA_OIW_data);
        sha1WithDSA_OIW_oid = TObjectIdentifier.newInternal(sha1WithDSA_OIW_data);
        sha1WithDSA_oid = TObjectIdentifier.newInternal(dsaWithSHA1_PKIX_data);
        nameTable = new HashMap();
        nameTable.put(MD5_oid, TString.wrap("MD5"));
        nameTable.put(MD2_oid, TString.wrap("MD2"));
        nameTable.put(SHA_oid, TString.wrap("SHA-1"));
        nameTable.put(SHA224_oid, TString.wrap("SHA-224"));
        nameTable.put(SHA256_oid, TString.wrap("SHA-256"));
        nameTable.put(SHA384_oid, TString.wrap("SHA-384"));
        nameTable.put(SHA512_oid, TString.wrap("SHA-512"));
        nameTable.put(RSAEncryption_oid, TString.wrap("RSA"));
        nameTable.put(RSA_oid, TString.wrap("RSA"));
        nameTable.put(DH_oid, TString.wrap("Diffie-Hellman"));
        nameTable.put(DH_PKIX_oid, TString.wrap("Diffie-Hellman"));
        nameTable.put(DSA_oid, TString.wrap("DSA"));
        nameTable.put(DSA_OIW_oid, TString.wrap("DSA"));
        nameTable.put(EC_oid, TString.wrap("EC"));
        nameTable.put(ECDH_oid, TString.wrap("ECDH"));
        nameTable.put(AES_oid, TString.wrap("AES"));
        nameTable.put(sha1WithECDSA_oid, TString.wrap("SHA1withECDSA"));
        nameTable.put(sha224WithECDSA_oid, TString.wrap("SHA224withECDSA"));
        nameTable.put(sha256WithECDSA_oid, TString.wrap("SHA256withECDSA"));
        nameTable.put(sha384WithECDSA_oid, TString.wrap("SHA384withECDSA"));
        nameTable.put(sha512WithECDSA_oid, TString.wrap("SHA512withECDSA"));
        nameTable.put(md5WithRSAEncryption_oid, TString.wrap("MD5withRSA"));
        nameTable.put(md2WithRSAEncryption_oid, TString.wrap("MD2withRSA"));
        nameTable.put(sha1WithDSA_oid, TString.wrap("SHA1withDSA"));
        nameTable.put(sha1WithDSA_OIW_oid, TString.wrap("SHA1withDSA"));
        nameTable.put(shaWithDSA_OIW_oid, TString.wrap("SHA1withDSA"));
        nameTable.put(sha224WithDSA_oid, TString.wrap("SHA224withDSA"));
        nameTable.put(sha256WithDSA_oid, TString.wrap("SHA256withDSA"));
        nameTable.put(sha1WithRSAEncryption_oid, TString.wrap("SHA1withRSA"));
        nameTable.put(sha1WithRSAEncryption_OIW_oid, TString.wrap("SHA1withRSA"));
        nameTable.put(sha224WithRSAEncryption_oid, TString.wrap("SHA224withRSA"));
        nameTable.put(sha256WithRSAEncryption_oid, TString.wrap("SHA256withRSA"));
        nameTable.put(sha384WithRSAEncryption_oid, TString.wrap("SHA384withRSA"));
        nameTable.put(sha512WithRSAEncryption_oid, TString.wrap("SHA512withRSA"));
        nameTable.put(pbeWithMD5AndDES_oid, TString.wrap("PBEWithMD5AndDES"));
        nameTable.put(pbeWithMD5AndRC2_oid, TString.wrap("PBEWithMD5AndRC2"));
        nameTable.put(pbeWithSHA1AndDES_oid, TString.wrap("PBEWithSHA1AndDES"));
        nameTable.put(pbeWithSHA1AndRC2_oid, TString.wrap("PBEWithSHA1AndRC2"));
        nameTable.put(pbeWithSHA1AndDESede_oid, TString.wrap("PBEWithSHA1AndDESede"));
        nameTable.put(pbeWithSHA1AndRC2_40_oid, TString.wrap("PBEWithSHA1AndRC2_40"));
    }


    private TAlgorithmId(TObjectIdentifier var1, TDerValue var2) throws TIOException {
        this.algid = var1;
        this.params = var2;
        if(this.params != null) {
            this.decodeParams();
        }

    }

    public TAlgorithmId(TObjectIdentifier var1) {
        this.algid = var1;
    }

    protected void decodeParams() throws TIOException {
        String var1 = this.algid.toString();

        try {
            this.algParams = TAlgorithmParameters.getInstance(var1);
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
        TObjectIdentifier var1;
        try {
            var1 = algOID(var0);
        } catch (TIOException var3) {
            throw new TNoSuchAlgorithmException(TString.wrap("Invalid ObjectIdentifier " + var0));
        }

        if(var1 == null) {
            throw new TNoSuchAlgorithmException(TString.wrap("unrecognized algorithm name: " + var0));
        } else {
            return new TAlgorithmId(var1);
        }
    }

    private static TObjectIdentifier algOID(String var0) throws TIOException {
        if(var0.indexOf(46) != -1) {
            return var0.startsWith("OID.")?new TObjectIdentifier(TString.wrap(var0.substring("OID.".length()))):new TObjectIdentifier(TString.wrap(var0));
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
                                                                            oidTable.put(var8, new TObjectIdentifier(TString.wrap(var1)));
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            if(oidTable == null) {
                                                                oidTable = Collections.emptyMap();
                                                            }

                                                            initOidTable = true;
                                                        }

                                                        return (TObjectIdentifier)oidTable.get(var0.toUpperCase(Locale.ENGLISH));
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

    private static TObjectIdentifier oid(int... var0) {
        return TObjectIdentifier.newInternal(var0);
    }

    public final void encode(TDerOutputStream var1) throws TIOException {
        this.derEncode(var1);
    }

    public void derEncode(TOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        TDerOutputStream var3 = new TDerOutputStream();
        var2.putOID(this.algid);
        if(!this.constructedFromDer) {
            if(this.algParams != null) {
                this.params = new TDerValue(this.algParams.getEncoded());
            } else {
                this.params = null;
            }
        }

        if(this.params == null) {
            var2.putNull();
        } else {
            var2.putDerValue(this.params);
        }

        var3.write((byte) 48, var2);
        var1.write(var3.toByteArray());
    }

    public TString getName() {
        TString var1 = (TString)nameTable.get(this.algid);
        if(var1 != null) {
            return var1;
        } else {
            if(this.params != null && this.algid.equals(specifiedWithECDSA_oid)) {
                try {
                    TAlgorithmId var2 = parse(new TDerValue(this.getEncodedParams()));
                    TString var3 = var2.getName();
                    var1 = makeSigAlg(var3, TString.wrap("EC"));
                } catch (TIOException var4) {
                    ;
                }
            }

            return var1 == null?TString.wrap(this.algid.toString()):var1;
        }
    }

    public byte[] getEncodedParams() throws TIOException {
        return this.params == null?null:this.params.toByteArray();
    }

    public static TString makeSigAlg(TString var0, TString var1) {
        var0 = var0.replace(TString.wrap("-"), TString.wrap(""));
        if(var1.equalsIgnoreCase(TString.wrap("EC"))) {
            var1 = TString.wrap("ECDSA");
        }

        return TString.wrap(var0 + "with" + var1);
    }

    public final TObjectIdentifier getOID() {
        return this.algid;
    }

    public static String getEncAlgFromSigAlg(String var0) {
        var0 = var0.toUpperCase(Locale.ENGLISH);
        int var1 = var0.indexOf("WITH");
        String var2 = null;
        if(var1 > 0) {
            int var3 = var0.indexOf("AND", var1 + 4);
            if(var3 > 0) {
                var2 = var0.substring(var1 + 4, var3);
            } else {
                var2 = var0.substring(var1 + 4);
            }

            if(var2.equalsIgnoreCase("ECDSA")) {
                var2 = "EC";
            }
        }

        return var2;
    }

    public static String getDigAlgFromSigAlg(String var0) {
        var0 = var0.toUpperCase(Locale.ENGLISH);
        int var1 = var0.indexOf("WITH");
        return var1 > 0?var0.substring(0, var1):null;
    }
}
