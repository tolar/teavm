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
package org.teavm.classlib.sun.security.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.teavm.classlib.java.math.TBigInteger;

public class TDebug {
    private String prefix;
    //private static String args = (String) AccessController.doPrivileged(new TGetPropertyAction(TString.wrap("java.security.debug")));
    private static final char[] hexDigits;

    public TDebug() {
    }

    public static void Help() {
        System.err.println();
        System.err.println("all           turn on all debugging");
        System.err.println("access        print all checkPermission results");
        System.err.println("certpath      PKIX CertPathBuilder and");
        System.err.println("              CertPathValidator debugging");
        System.err.println("combiner      SubjectDomainCombiner debugging");
        System.err.println("gssloginconfig");
        System.err.println("              GSS LoginConfigImpl debugging");
        System.err.println("configfile    JAAS ConfigFile loading");
        System.err.println("configparser  JAAS ConfigFile parsing");
        System.err.println("jar           jar verification");
        System.err.println("logincontext  login context results");
        System.err.println("jca           JCA engine class debugging");
        System.err.println("policy        loading and granting");
        System.err.println("provider      security provider debugging");
        System.err.println("pkcs11        PKCS11 session manager debugging");
        System.err.println("pkcs11keystore");
        System.err.println("              PKCS11 TKeyStore debugging");
        System.err.println("sunpkcs11     SunPKCS11 provider debugging");
        System.err.println("scl           permissions SecureClassLoader assigns");
        System.err.println("ts            timestamping");
        System.err.println();
        System.err.println("The following can be used with access:");
        System.err.println();
        System.err.println("stack         include stack trace");
        System.err.println("domain        dump all domains in context");
        System.err.println("failure       before throwing exception, dump stack");
        System.err.println("              and domain that didn\'t have permission");
        System.err.println();
        System.err.println("The following can be used with stack and domain:");
        System.err.println();
        System.err.println("permission=<classname>");
        System.err.println("              only dump output if specified permission");
        System.err.println("              is being checked");
        System.err.println("codebase=<URL>");
        System.err.println("              only dump output if specified codebase");
        System.err.println("              is being checked");
        System.err.println();
        System.err.println("The following can be used with provider:");
        System.err.println();
        System.err.println("engine=<engines>");
        System.err.println("              only dump output for the specified list");
        System.err.println("              of JCA engines. Supported values:");
        System.err.println("              TCipher, KeyAgreement, KeyGenerator,");
        System.err.println("              KeyPairGenerator, TKeyStore, Mac,");
        System.err.println("              MessageDigest, SecureRandom, Signature.");
        System.err.println();
        System.err.println("Note: Separate multiple options with a comma");
        System.exit(0);
    }

    public static TDebug getInstance(String var0) {
        return getInstance(var0, var0);
    }

    public static TDebug getInstance(String var0, String var1) {
        if(isOn(var0)) {
            TDebug var2 = new TDebug();
            var2.prefix = var1;
            return var2;
        } else {
            return null;
        }
    }

    public static boolean isOn(String var0) {
        return false;
    }

    public void println(String var1) {
        System.err.println(this.prefix + ": " + var1);
    }

    public void println() {
        System.err.println(this.prefix + ":");
    }

    public static void println(String var0, String var1) {
        System.err.println(var0 + ": " + var1);
    }

    public static String toHexString(TBigInteger var0) {
        String var1 = var0.toString(16);
        StringBuffer var2 = new StringBuffer(var1.length() * 2);
        if(var1.startsWith("-")) {
            var2.append("   -");
            var1 = var1.substring(1);
        } else {
            var2.append("    ");
        }

        if(var1.length() % 2 != 0) {
            var1 = "0" + var1;
        }

        int var3 = 0;

        while(var3 < var1.length()) {
            var2.append(var1.substring(var3, var3 + 2));
            var3 += 2;
            if(var3 != var1.length()) {
                if(var3 % 64 == 0) {
                    var2.append("\n    ");
                } else if(var3 % 8 == 0) {
                    var2.append(" ");
                }
            }
        }

        return var2.toString();
    }

    private static String marshal(String var0) {
        if(var0 == null) {
            return null;
        } else {
            StringBuffer var1 = new StringBuffer();
            StringBuffer var2 = new StringBuffer(var0);
            String var3 = "[Pp][Ee][Rr][Mm][Ii][Ss][Ss][Ii][Oo][Nn]=";
            String var4 = "permission=";
            String var5 = var3 + "[a-zA-Z_$][a-zA-Z0-9_$]*([.][a-zA-Z_$][a-zA-Z0-9_$]*)*";
            Pattern var6 = Pattern.compile(var5);
            Matcher var7 = var6.matcher(var2);
            StringBuffer var8 = new StringBuffer();

            String var9;
            while(var7.find()) {
                var9 = var7.group();
                var1.append(var9.replaceFirst(var3, var4));
                var1.append("  ");
                var7.appendReplacement(var8, "");
            }

            var7.appendTail(var8);
            var3 = "[Cc][Oo][Dd][Ee][Bb][Aa][Ss][Ee]=";
            var4 = "codebase=";
            var5 = var3 + "[^, ;]*";
            var6 = Pattern.compile(var5);
            var7 = var6.matcher(var8);
            var8 = new StringBuffer();

            while(var7.find()) {
                var9 = var7.group();
                var1.append(var9.replaceFirst(var3, var4));
                var1.append("  ");
                var7.appendReplacement(var8, "");
            }

            var7.appendTail(var8);
            var1.append(var8.toString().toLowerCase(Locale.ENGLISH));
            return var1.toString();
        }
    }

    public static String toString(byte[] var0) {
        if(var0 == null) {
            return "(null)";
        } else {
            StringBuilder var1 = new StringBuilder(var0.length * 3);

            for(int var2 = 0; var2 < var0.length; ++var2) {
                int var3 = var0[var2] & 255;
                if(var2 != 0) {
                    var1.append(':');
                }

                var1.append(hexDigits[var3 >>> 4]);
                var1.append(hexDigits[var3 & 15]);
            }

            return var1.toString();
        }
    }

    static {
        hexDigits = "0123456789abcdef".toCharArray();
    }
}
