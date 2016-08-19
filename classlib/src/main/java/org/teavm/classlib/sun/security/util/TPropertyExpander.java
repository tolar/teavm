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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import org.teavm.classlib.java.lang.TException;
import org.teavm.classlib.java.lang.TString;

public class TPropertyExpander {
    public TPropertyExpander() {
    }

    public static String expand(String var0) throws TPropertyExpander.ExpandException {
        return expand(var0, false);
    }

    public static String expand(String var0, boolean var1) throws TPropertyExpander.ExpandException {
        if(var0 == null) {
            return null;
        } else {
            int var2 = var0.indexOf("${", 0);
            if(var2 == -1) {
                return var0;
            } else {
                StringBuffer var3 = new StringBuffer(var0.length());
                int var4 = var0.length();
                int var5 = 0;

                while(var2 < var4) {
                    if(var2 > var5) {
                        var3.append(var0.substring(var5, var2));
                    }

                    int var6 = var2 + 2;
                    if(var6 < var4 && var0.charAt(var6) == 123) {
                        var6 = var0.indexOf("}}", var6);
                        if(var6 == -1 || var6 + 2 == var4) {
                            var3.append(var0.substring(var2));
                            break;
                        }

                        ++var6;
                        var3.append(var0.substring(var2, var6 + 1));
                    } else {
                        while(true) {
                            if(var6 >= var4 || var0.charAt(var6) == 125) {
                                if(var6 == var4) {
                                    var3.append(var0.substring(var2, var6));
                                    return var3.toString();
                                }

                                String var7 = var0.substring(var2 + 2, var6);
                                if(var7.equals("/")) {
                                    var3.append(File.separatorChar);
                                } else {
                                    String var8 = System.getProperty(var7);
                                    if(var8 == null) {
                                        throw new TPropertyExpander.ExpandException("unable to expand property " + var7);
                                    }

                                    if(var1) {
                                        try {
                                            if(var3.length() > 0 || !(new URI(var8)).isAbsolute()) {
                                                throw new TException(TString.wrap("Not implemented"));
                                                //var8 = ParseUtil.encodePath(var8);
                                            }
                                        } catch (URISyntaxException var10) {
                                            throw new TException(TString.wrap("Not implemented"));
                                            //var8 = ParseUtil.encodePath(var8);
                                        }
                                    }

                                    var3.append(var8);
                                }
                                break;
                            }

                            ++var6;
                        }
                    }

                    var5 = var6 + 1;
                    var2 = var0.indexOf("${", var5);
                    if(var2 == -1) {
                        if(var5 < var4) {
                            var3.append(var0.substring(var5, var4));
                        }
                        break;
                    }
                }

                return var3.toString();
            }
        }
    }

    public static class ExpandException extends GeneralSecurityException {
        private static final long serialVersionUID = -7941948581406161702L;

        public ExpandException(String var1) {
            super(var1);
        }
    }
}
