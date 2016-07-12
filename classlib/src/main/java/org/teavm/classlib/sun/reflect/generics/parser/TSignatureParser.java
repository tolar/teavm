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
package org.teavm.classlib.sun.reflect.generics.parser;

import java.lang.reflect.GenericSignatureFormatError;
import java.util.ArrayList;
import java.util.List;

import org.teavm.classlib.sun.reflect.generics.tree.TArrayTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TBaseType;
import org.teavm.classlib.sun.reflect.generics.tree.TBooleanSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TBottomSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TByteSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TCharSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TClassSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TClassTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TDoubleSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TFieldTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TFloatSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TFormalTypeParameter;
import org.teavm.classlib.sun.reflect.generics.tree.TIntSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TLongSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TMethodTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TReturnType;
import org.teavm.classlib.sun.reflect.generics.tree.TShortSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TSimpleClassTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TTypeArgument;
import org.teavm.classlib.sun.reflect.generics.tree.TTypeSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TTypeVariableSignature;
import org.teavm.classlib.sun.reflect.generics.tree.TVoidDescriptor;
import org.teavm.classlib.sun.reflect.generics.tree.TWildcard;

/**
 * @author: Vaclav Tolar, (vaclav_tolar@kb.cz, vaclav.tolar@cleverlance.com, vaclav.tolar@gmail.com)
 * Date: 2016-07-11
 */
public class TSignatureParser {
    private char[] input;
    private int index = 0;
    private static final char EOI = ':';
    private static final boolean DEBUG = false;

    private TSignatureParser() {
    }

    private char getNext() {
        assert this.index <= this.input.length;

        try {
            return this.input[this.index++];
        } catch (ArrayIndexOutOfBoundsException var2) {
            return ':';
        }
    }

    private char current() {
        assert this.index <= this.input.length;

        try {
            return this.input[this.index];
        } catch (ArrayIndexOutOfBoundsException var2) {
            return ':';
        }
    }

    private void advance() {
        assert this.index <= this.input.length;

        ++this.index;
    }

    private String remainder() {
        return new String(this.input, this.index, this.input.length - this.index);
    }

    private boolean matches(char var1, char... var2) {
        char[] var3 = var2;
        int var4 = var2.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            char var6 = var3[var5];
            if(var1 == var6) {
                return true;
            }
        }

        return false;
    }

    private Error error(String var1) {
        return new GenericSignatureFormatError("Signature Parse error: " + var1 + "\n\tRemaining input: " + this.remainder());
    }

    private void progress(int var1) {
        if(this.index <= var1) {
            throw this.error("Failure to make progress!");
        }
    }

    public static TSignatureParser make() {
        return new TSignatureParser();
    }

    public TClassSignature parseClassSig(String var1) {
        this.input = var1.toCharArray();
        return this.parseClassSignature();
    }

    public TMethodTypeSignature parseMethodSig(String var1) {
        this.input = var1.toCharArray();
        return this.parseMethodTypeSignature();
    }

    public TTypeSignature parseTypeSig(String var1) {
        this.input = var1.toCharArray();
        return this.parseTypeSignature();
    }

    private TClassSignature parseClassSignature() {
        assert this.index == 0;

        return TClassSignature.make(this.parseZeroOrMoreFormalTypeParameters(), this.parseClassTypeSignature(), this.parseSuperInterfaces());
    }

    private TFormalTypeParameter[] parseZeroOrMoreFormalTypeParameters() {
        return this.current() == 60?this.parseFormalTypeParameters():new TFormalTypeParameter[0];
    }

    private TFormalTypeParameter[] parseFormalTypeParameters() {
        ArrayList var1 = new ArrayList(3);

        assert this.current() == 60;

        if(this.current() != 60) {
            throw this.error("expected \'<\'");
        } else {
            this.advance();
            var1.add(this.parseFormalTypeParameter());

            while(this.current() != 62) {
                int var2 = this.index;
                var1.add(this.parseFormalTypeParameter());
                this.progress(var2);
            }

            this.advance();
            return (TFormalTypeParameter[])var1.toArray(new TFormalTypeParameter[var1.size()]);
        }
    }

    private TFormalTypeParameter parseFormalTypeParameter() {
        String var1 = this.parseIdentifier();
        TFieldTypeSignature[] var2 = this.parseBounds();
        return TFormalTypeParameter.make(var1, var2);
    }

    private String parseIdentifier() {
        StringBuilder var1 = new StringBuilder();

        while(!Character.isWhitespace(this.current())) {
            char var2 = this.current();
            switch(var2) {
                case '.':
                case '/':
                case ':':
                case ';':
                case '<':
                case '>':
                case '[':
                    return var1.toString();
                default:
                    var1.append(var2);
                    this.advance();
            }
        }

        return var1.toString();
    }

    private TFieldTypeSignature parseFieldTypeSignature() {
        return this.parseFieldTypeSignature(true);
    }

    private TFieldTypeSignature parseFieldTypeSignature(boolean var1) {
        switch(this.current()) {
            case 'L':
                return this.parseClassTypeSignature();
            case 'T':
                return this.parseTypeVariableSignature();
            case '[':
                if(var1) {
                    return this.parseArrayTypeSignature();
                }

                throw this.error("Array signature not allowed here.");
            default:
                throw this.error("Expected Field Type Signature");
        }
    }

    private TClassTypeSignature parseClassTypeSignature() {
        assert this.current() == 76;

        if(this.current() != 76) {
            throw this.error("expected a class type");
        } else {
            this.advance();
            ArrayList var1 = new ArrayList(5);
            var1.add(this.parsePackageNameAndSimpleClassTypeSignature());
            this.parseClassTypeSignatureSuffix(var1);
            if(this.current() != 59) {
                throw this.error("expected \';\' got \'" + this.current() + "\'");
            } else {
                this.advance();
                return TClassTypeSignature.make(var1);
            }
        }
    }

    private TSimpleClassTypeSignature parsePackageNameAndSimpleClassTypeSignature() {
        String var1 = this.parseIdentifier();
        if(this.current() == 47) {
            StringBuilder var2 = new StringBuilder(var1);

            while(this.current() == 47) {
                this.advance();
                var2.append(".");
                var2.append(this.parseIdentifier());
            }

            var1 = var2.toString();
        }

        switch(this.current()) {
            case ';':
                return TSimpleClassTypeSignature.make(var1, false, new TTypeArgument[0]);
            case '<':
                return TSimpleClassTypeSignature.make(var1, false, this.parseTypeArguments());
            default:
                throw this.error("expected \'<\' or \';\' but got " + this.current());
        }
    }

    private TSimpleClassTypeSignature parseSimpleClassTypeSignature(boolean var1) {
        String var2 = this.parseIdentifier();
        char var3 = this.current();
        switch(var3) {
            case '.':
            case ';':
                return TSimpleClassTypeSignature.make(var2, var1, new TTypeArgument[0]);
            case '<':
                return TSimpleClassTypeSignature.make(var2, var1, this.parseTypeArguments());
            default:
                throw this.error("expected \'<\' or \';\' or \'.\', got \'" + var3 + "\'.");
        }
    }

    private void parseClassTypeSignatureSuffix(List<TSimpleClassTypeSignature> var1) {
        while(this.current() == 46) {
            this.advance();
            var1.add(this.parseSimpleClassTypeSignature(true));
        }

    }

    private TTypeArgument[] parseTypeArgumentsOpt() {
        return this.current() == 60?this.parseTypeArguments():new TTypeArgument[0];
    }

    private TTypeArgument[] parseTypeArguments() {
        ArrayList var1 = new ArrayList(3);

        assert this.current() == 60;

        if(this.current() != 60) {
            throw this.error("expected \'<\'");
        } else {
            this.advance();
            var1.add(this.parseTypeArgument());

            while(this.current() != 62) {
                var1.add(this.parseTypeArgument());
            }

            this.advance();
            return (TTypeArgument[])var1.toArray(new TTypeArgument[var1.size()]);
        }
    }

    private TTypeArgument parseTypeArgument() {
        TFieldTypeSignature[] var1 = new TFieldTypeSignature[1];
        TFieldTypeSignature[] var2 = new TFieldTypeSignature[1];
        TTypeArgument[] var3 = new TTypeArgument[0];
        char var4 = this.current();
        switch(var4) {
            case '*':
                this.advance();
                var1[0] = TSimpleClassTypeSignature.make("java.lang.Object", false, var3);
                var2[0] = TBottomSignature.make();
                return TWildcard.make(var1, var2);
            case '+':
                this.advance();
                var1[0] = this.parseFieldTypeSignature();
                var2[0] = TBottomSignature.make();
                return TWildcard.make(var1, var2);
            case ',':
            default:
                return this.parseFieldTypeSignature();
            case '-':
                this.advance();
                var2[0] = this.parseFieldTypeSignature();
                var1[0] = TSimpleClassTypeSignature.make("java.lang.Object", false, var3);
                return TWildcard.make(var1, var2);
        }
    }

    private TTypeVariableSignature parseTypeVariableSignature() {
        assert this.current() == 84;

        if(this.current() != 84) {
            throw this.error("expected a type variable usage");
        } else {
            this.advance();
            TTypeVariableSignature var1 = TTypeVariableSignature.make(this.parseIdentifier());
            if(this.current() != 59) {
                throw this.error("; expected in signature of type variable named" + var1.getIdentifier());
            } else {
                this.advance();
                return var1;
            }
        }
    }

    private TArrayTypeSignature parseArrayTypeSignature() {
        if(this.current() != 91) {
            throw this.error("expected array type signature");
        } else {
            this.advance();
            return TArrayTypeSignature.make(this.parseTypeSignature());
        }
    }

    private TTypeSignature parseTypeSignature() {
        switch(this.current()) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
                return this.parseBaseType();
            case 'E':
            case 'G':
            case 'H':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            default:
                return this.parseFieldTypeSignature();
        }
    }

    private TBaseType parseBaseType() {
        switch(this.current()) {
            case 'B':
                this.advance();
                return TByteSignature.make();
            case 'C':
                this.advance();
                return TCharSignature.make();
            case 'D':
                this.advance();
                return TDoubleSignature.make();
            case 'E':
            case 'G':
            case 'H':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            default:
                assert false;

                throw this.error("expected primitive type");
            case 'F':
                this.advance();
                return TFloatSignature.make();
            case 'I':
                this.advance();
                return TIntSignature.make();
            case 'J':
                this.advance();
                return TLongSignature.make();
            case 'S':
                this.advance();
                return TShortSignature.make();
            case 'Z':
                this.advance();
                return TBooleanSignature.make();
        }
    }

    private TFieldTypeSignature[] parseBounds() {
        ArrayList var1 = new ArrayList(3);
        if(this.current() == 58) {
            this.advance();
            switch(this.current()) {
                case ':':
                    break;
                default:
                    var1.add(this.parseFieldTypeSignature());
            }

            while(this.current() == 58) {
                this.advance();
                var1.add(this.parseFieldTypeSignature());
            }
        } else {
            this.error("Bound expected");
        }

        return (TFieldTypeSignature[])var1.toArray(new TFieldTypeSignature[var1.size()]);
    }

    private TClassTypeSignature[] parseSuperInterfaces() {
        ArrayList var1 = new ArrayList(5);

        while(this.current() == 76) {
            var1.add(this.parseClassTypeSignature());
        }

        return (TClassTypeSignature[])var1.toArray(new TClassTypeSignature[var1.size()]);
    }

    private TMethodTypeSignature parseMethodTypeSignature() {
        assert this.index == 0;

        return TMethodTypeSignature.make(this.parseZeroOrMoreFormalTypeParameters(), this.parseFormalParameters(), this.parseReturnType(), this.parseZeroOrMoreThrowsSignatures());
    }

    private TTypeSignature[] parseFormalParameters() {
        if(this.current() != 40) {
            throw this.error("expected \'(\'");
        } else {
            this.advance();
            TTypeSignature[] var1 = this.parseZeroOrMoreTypeSignatures();
            if(this.current() != 41) {
                throw this.error("expected \')\'");
            } else {
                this.advance();
                return var1;
            }
        }
    }

    private TTypeSignature[] parseZeroOrMoreTypeSignatures() {
        ArrayList var1 = new ArrayList();
        boolean var2 = false;

        while(!var2) {
            switch(this.current()) {
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'L':
                case 'S':
                case 'T':
                case 'Z':
                case '[':
                    var1.add(this.parseTypeSignature());
                    break;
                case 'E':
                case 'G':
                case 'H':
                case 'K':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                default:
                    var2 = true;
            }
        }

        return (TTypeSignature[])var1.toArray(new TTypeSignature[var1.size()]);
    }

    private TReturnType parseReturnType() {
        if(this.current() == 86) {
            this.advance();
            return TVoidDescriptor.make();
        } else {
            return this.parseTypeSignature();
        }
    }

    private TFieldTypeSignature[] parseZeroOrMoreThrowsSignatures() {
        ArrayList var1 = new ArrayList(3);

        while(this.current() == 94) {
            var1.add(this.parseThrowsSignature());
        }

        return (TFieldTypeSignature[])var1.toArray(new TFieldTypeSignature[var1.size()]);
    }

    private TFieldTypeSignature parseThrowsSignature() {
        assert this.current() == 94;

        if(this.current() != 94) {
            throw this.error("expected throws signature");
        } else {
            this.advance();
            return this.parseFieldTypeSignature(false);
        }
    }
}
