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
package org.teavm.classlib.javax.crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.teavm.classlib.java.io.TBufferedReader;
import org.teavm.classlib.java.io.TReader;
import org.teavm.classlib.java.io.TStreamTokenizer;
import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.lang.reflect.TConstructor;
import org.teavm.classlib.java.security.spec.TAlgorithmParameterSpec;

final class TCryptoPolicyParser {
    private Vector<TCryptoPolicyParser.GrantEntry> grantEntries = new Vector();
    private TStreamTokenizer st;
    private int lookahead;

    TCryptoPolicyParser() {
    }

    void read(TReader var1) throws TCryptoPolicyParser.ParsingException, IOException {
        if(!(var1 instanceof TBufferedReader)) {
            var1 = new TBufferedReader((TReader)var1);
        }

        this.st = new TStreamTokenizer((TReader)var1);
        this.st.resetSyntax();
        this.st.wordChars(97, 122);
        this.st.wordChars(65, 90);
        this.st.wordChars(46, 46);
        this.st.wordChars(48, 57);
        this.st.wordChars(95, 95);
        this.st.wordChars(36, 36);
        this.st.wordChars(160, 255);
        this.st.whitespaceChars(0, 32);
        this.st.commentChar(47);
        this.st.quoteChar(39);
        this.st.quoteChar(34);
        this.st.lowerCaseMode(false);
        this.st.ordinaryChar(47);
        this.st.slashSlashComments(true);
        this.st.slashStarComments(true);
        this.st.parseNumbers();
        Object var2 = null;

        for(this.lookahead = this.st.nextToken(); this.lookahead != -1; this.match(";")) {
            if(!this.peek("grant")) {
                throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), "expected grant statement");
            }

            TCryptoPolicyParser.GrantEntry var3 = this.parseGrantEntry((Hashtable)var2);
            if(var3 != null) {
                this.grantEntries.addElement(var3);
            }
        }

    }

    private TCryptoPolicyParser.GrantEntry parseGrantEntry(Hashtable<String, Vector<String>> var1) throws TCryptoPolicyParser.ParsingException, IOException {
        TCryptoPolicyParser.GrantEntry var2 = new TCryptoPolicyParser.GrantEntry();
        this.match("grant");
        this.match("{");

        while(!this.peek("}")) {
            if(!this.peek("Permission")) {
                throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), "expected permission entry");
            }

            TCryptoPolicyParser.CryptoPermissionEntry var3 = this.parsePermissionEntry(var1);
            var2.add(var3);
            this.match(";");
        }

        this.match("}");
        return var2;
    }

    private TCryptoPolicyParser.CryptoPermissionEntry parsePermissionEntry(Hashtable<TString, Vector<TString>> var1) throws TCryptoPolicyParser.ParsingException, IOException {
        TCryptoPolicyParser.CryptoPermissionEntry var2 = new TCryptoPolicyParser.CryptoPermissionEntry();
        this.match("Permission");
        var2.cryptoPermission = this.match("permission type");
        if(var2.cryptoPermission.equals("javax.crypto.CryptoAllPermission")) {
            var2.alg = TString.wrap("CryptoAllPermission");
            var2.maxKeySize = 2147483647;
            return var2;
        } else {
            if(this.peek("\"")) {
                var2.alg = this.match("quoted string").toUpperCase(Locale.ENGLISH);
            } else {
                if(!this.peek("*")) {
                    throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), "Missing the algorithm name");
                }

                this.match("*");
                var2.alg = TString.wrap("*");
            }

            this.peekAndMatch(",");
            if(this.peek("\"")) {
                var2.exemptionMechanism = this.match("quoted string").toUpperCase(Locale.ENGLISH);
            }

            this.peekAndMatch(",");
            if(!this.isConsistent(var2.alg, var2.exemptionMechanism, var1)) {
                throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), "Inconsistent policy");
            } else {
                if(this.peek("number")) {
                    var2.maxKeySize = this.match();
                } else if(this.peek("*")) {
                    this.match("*");
                    var2.maxKeySize = 2147483647;
                } else {
                    if(!this.peek(";")) {
                        throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), "Missing the maximum allowable key size");
                    }

                    var2.maxKeySize = 2147483647;
                }

                this.peekAndMatch(",");
                if(this.peek("\"")) {
                    TString var3 = this.match("quoted string");
                    Vector var4 = new Vector(1);

                    while(this.peek(",")) {
                        this.match(",");
                        if(this.peek("number")) {
                            var4.addElement(new Integer(this.match()));
                        } else {
                            if(!this.peek("*")) {
                                throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), "Expecting an integer");
                            }

                            this.match("*");
                            var4.addElement(new Integer(2147483647));
                        }
                    }

                    Integer[] var5 = new Integer[var4.size()];
                    var4.copyInto(var5);
                    var2.checkParam = true;
                    var2.algParamSpec = getInstance(var3, var5);
                }

                return var2;
            }
        }
    }

    private static final TAlgorithmParameterSpec getInstance(TString var0, Integer[] var1) throws TCryptoPolicyParser.ParsingException {
        TAlgorithmParameterSpec var2 = null;

        try {
            TClass var3 = TClass.forName(var0);
            TClass[] var4 = new TClass[var1.length];

            for(int var5 = 0; var5 < var1.length; ++var5) {
                var4[var5] = Integer.TYPE;
            }

            TConstructor var7 = var3.getConstructor(var4);
            var2 = (TAlgorithmParameterSpec)var7.newInstance((Object[])var1);
            return var2;
        } catch (Exception var6) {
            throw new TCryptoPolicyParser.ParsingException("Cannot call the constructor of " + var0 + var6);
        }
    }

    private boolean peekAndMatch(String var1) throws TCryptoPolicyParser.ParsingException, IOException {
        if(this.peek(var1)) {
            this.match(var1);
            return true;
        } else {
            return false;
        }
    }

    private boolean peek(String var1) {
        boolean var2 = false;
        switch(this.lookahead) {
            case -3:
                if(var1.equalsIgnoreCase(this.st.sval)) {
                    var2 = true;
                }
                break;
            case -2:
                if(var1.equalsIgnoreCase("number")) {
                    var2 = true;
                }
                break;
            case 34:
                if(var1.equals("\"")) {
                    var2 = true;
                }
                break;
            case 42:
                if(var1.equals("*")) {
                    var2 = true;
                }
                break;
            case 44:
                if(var1.equals(",")) {
                    var2 = true;
                }
                break;
            case 59:
                if(var1.equals(";")) {
                    var2 = true;
                }
                break;
            case 123:
                if(var1.equals("{")) {
                    var2 = true;
                }
                break;
            case 125:
                if(var1.equals("}")) {
                    var2 = true;
                }
        }

        return var2;
    }

    private int match() throws TCryptoPolicyParser.ParsingException, IOException {
        int var1 = -1;
        int var2 = this.st.lineno();
        String var3 = null;
        switch(this.lookahead) {
            case -2:
                var1 = (int)this.st.nval;
                if(var1 < 0) {
                    var3 = String.valueOf(this.st.nval);
                }

                this.lookahead = this.st.nextToken();
                break;
            default:
                var3 = this.st.sval;
        }

        if(var1 <= 0) {
            throw new TCryptoPolicyParser.ParsingException(var2, "a non-negative number", var3);
        } else {
            return var1;
        }
    }

    private TString match(String var1) throws TCryptoPolicyParser.ParsingException, IOException {
        TString var2 = null;
        switch(this.lookahead) {
            case -3:
                if(var1.equalsIgnoreCase(this.st.sval)) {
                    this.lookahead = this.st.nextToken();
                } else {
                    if(!var1.equalsIgnoreCase("permission type")) {
                        throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), var1, this.st.sval);
                    }

                    var2 = this.st.sval;
                    this.lookahead = this.st.nextToken();
                }
                break;
            case -2:
                throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), var1, "number " + String.valueOf(this.st.nval));
            case -1:
                throw new TCryptoPolicyParser.ParsingException("expected " + var1 + ", read end of file");
            case 34:
                if(var1.equalsIgnoreCase("quoted string")) {
                    var2 = this.st.sval;
                    this.lookahead = this.st.nextToken();
                } else {
                    if(!var1.equalsIgnoreCase("permission type")) {
                        throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), var1, this.st.sval);
                    }

                    var2 = this.st.sval;
                    this.lookahead = this.st.nextToken();
                }
                break;
            case 42:
                if(!var1.equals("*")) {
                    throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), var1, "*");
                }

                this.lookahead = this.st.nextToken();
                break;
            case 44:
                if(!var1.equals(",")) {
                    throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), var1, ",");
                }

                this.lookahead = this.st.nextToken();
                break;
            case 59:
                if(!var1.equals(";")) {
                    throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), var1, ";");
                }

                this.lookahead = this.st.nextToken();
                break;
            case 123:
                if(!var1.equals("{")) {
                    throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), var1, "{");
                }

                this.lookahead = this.st.nextToken();
                break;
            case 125:
                if(!var1.equals("}")) {
                    throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), var1, "}");
                }

                this.lookahead = this.st.nextToken();
                break;
            default:
                throw new TCryptoPolicyParser.ParsingException(this.st.lineno(), var1, new String(new char[]{(char)this.lookahead}));
        }

        return var2;
    }

    TCryptoPermission[] getPermissions() {
        Vector var1 = new Vector();
        Enumeration var2 = this.grantEntries.elements();

        while(var2.hasMoreElements()) {
            TCryptoPolicyParser.GrantEntry var3 = (TCryptoPolicyParser.GrantEntry)var2.nextElement();
            Enumeration var4 = var3.permissionElements();

            while(var4.hasMoreElements()) {
                TCryptoPolicyParser.CryptoPermissionEntry var5 = (TCryptoPolicyParser.CryptoPermissionEntry)var4.nextElement();
                if(var5.cryptoPermission.equals("javax.crypto.CryptoAllPermission")) {
                    var1.addElement(TCryptoAllPermission.INSTANCE);
                } else if(var5.checkParam) {
                    var1.addElement(new TCryptoPermission(var5.alg, var5.maxKeySize, var5.algParamSpec, var5.exemptionMechanism));
                } else {
                    var1.addElement(new TCryptoPermission(var5.alg, var5.maxKeySize, var5.exemptionMechanism));
                }
            }
        }

        TCryptoPermission[] var6 = new TCryptoPermission[var1.size()];
        var1.copyInto(var6);
        return var6;
    }

    private boolean isConsistent(TString var1, TString var2, Hashtable<TString, Vector<TString>> var3) {
        TString var4 = var2 == null?TString.wrap("none"):var2;
        Vector var5;
        if(var3 == null) {
            var3 = new Hashtable();
            var5 = new Vector(1);
            var5.addElement(var4);
            var3.put(var1, var5);
            return true;
        } else if(var3.containsKey("CryptoAllPermission")) {
            return false;
        } else {
            if(var3.containsKey(var1)) {
                var5 = (Vector)var3.get(var1);
                if(var5.contains(var4)) {
                    return false;
                }
            } else {
                var5 = new Vector(1);
            }

            var5.addElement(var4);
            var3.put(var1, var5);
            return true;
        }
    }

    static final class ParsingException extends GeneralSecurityException {
        private static final long serialVersionUID = 7147241245566588374L;

        ParsingException(String var1) {
            super(var1);
        }

        ParsingException(int var1, String var2) {
            super("line " + var1 + ": " + var2);
        }

        ParsingException(int var1, String var2, String var3) {
            super("line " + var1 + ": expected \'" + var2 + "\', found \'" + var3 + "\'");
        }
    }

    private static class CryptoPermissionEntry {
        TString cryptoPermission;
        TString alg = null;
        TString exemptionMechanism = null;
        int maxKeySize = 0;
        boolean checkParam = false;
        TAlgorithmParameterSpec algParamSpec = null;

        CryptoPermissionEntry() {
        }

        public int hashCode() {
            int var1 = this.cryptoPermission.hashCode();
            if(this.alg != null) {
                var1 ^= this.alg.hashCode();
            }

            if(this.exemptionMechanism != null) {
                var1 ^= this.exemptionMechanism.hashCode();
            }

            var1 ^= this.maxKeySize;
            if(this.checkParam) {
                var1 ^= 100;
            }

            if(this.algParamSpec != null) {
                var1 ^= this.algParamSpec.hashCode();
            }

            return var1;
        }

        public boolean equals(Object var1) {
            if(var1 == this) {
                return true;
            } else if(!(var1 instanceof TCryptoPolicyParser.CryptoPermissionEntry)) {
                return false;
            } else {
                TCryptoPolicyParser.CryptoPermissionEntry var2 = (TCryptoPolicyParser.CryptoPermissionEntry)var1;
                if(this.cryptoPermission == null) {
                    if(var2.cryptoPermission != null) {
                        return false;
                    }
                } else if(!this.cryptoPermission.equals(var2.cryptoPermission)) {
                    return false;
                }

                if(this.alg == null) {
                    if(var2.alg != null) {
                        return false;
                    }
                } else if(!this.alg.equalsIgnoreCase(var2.alg)) {
                    return false;
                }

                if(this.maxKeySize != var2.maxKeySize) {
                    return false;
                } else if(this.checkParam != var2.checkParam) {
                    return false;
                } else {
                    if(this.algParamSpec == null) {
                        if(var2.algParamSpec != null) {
                            return false;
                        }
                    } else if(!this.algParamSpec.equals(var2.algParamSpec)) {
                        return false;
                    }

                    return true;
                }
            }
        }
    }

    private static class GrantEntry {
        private Vector<TCryptoPolicyParser.CryptoPermissionEntry> permissionEntries = new Vector();

        GrantEntry() {
        }

        void add(TCryptoPolicyParser.CryptoPermissionEntry var1) {
            this.permissionEntries.addElement(var1);
        }

        boolean remove(TCryptoPolicyParser.CryptoPermissionEntry var1) {
            return this.permissionEntries.removeElement(var1);
        }

        boolean contains(TCryptoPolicyParser.CryptoPermissionEntry var1) {
            return this.permissionEntries.contains(var1);
        }

        Enumeration<TCryptoPolicyParser.CryptoPermissionEntry> permissionElements() {
            return this.permissionEntries.elements();
        }
    }
}
