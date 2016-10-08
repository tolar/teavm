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

import java.net.URL;
import java.security.AccessController;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class TJceSecurityManager extends SecurityManager {
    private static final TCryptoPermissions defaultPolicy = TJceSecurity.getDefaultPolicy();
    private static final TCryptoPermissions exemptPolicy = TJceSecurity.getExemptPolicy();
    private static final TCryptoAllPermission allPerm;
    private static final Vector<Class<?>> TrustedCallersCache = new Vector(2);
    private static final ConcurrentMap<URL, TCryptoPermissions> exemptCache = new ConcurrentHashMap();
    private static final TCryptoPermissions CACHE_NULL_MARK = new TCryptoPermissions();
    static final TJceSecurityManager INSTANCE;

    private TJceSecurityManager() {
    }

    TCryptoPermission getCryptoPermission(String var1) {
        var1 = var1.toUpperCase(Locale.ENGLISH);
        TCryptoPermission var2 = this.getDefaultPermission(var1);
        if(var2 == TCryptoAllPermission.INSTANCE) {
            return var2;
        } else {
            Class[] var3 = this.getClassContext();
            URL var4 = null;

            int var5;
            for(var5 = 0; var5 < var3.length; ++var5) {
                Class var6 = var3[var5];
                var4 = TJceSecurity.getCodeBase(var6);
                if(var4 != null) {
                    break;
                }

                if(!var6.getName().startsWith("javax.crypto.")) {
                    return var2;
                }
            }

            if(var5 == var3.length) {
                return var2;
            } else {
                TCryptoPermissions var14 = (TCryptoPermissions)exemptCache.get(var4);
                if(var14 == null) {
                    synchronized(this.getClass()) {
                        var14 = (TCryptoPermissions)exemptCache.get(var4);
                        if(var14 == null) {
                            var14 = getAppPermissions(var4);
                            exemptCache.putIfAbsent(var4, var14 == null?CACHE_NULL_MARK:var14);
                        }
                    }
                }

                if(var14 != null && var14 != CACHE_NULL_MARK) {
                    if(var14.implies(allPerm)) {
                        return allPerm;
                    } else {
                        PermissionCollection var7 = var14.getPermissionCollection(var1);
                        if(var7 == null) {
                            return var2;
                        } else {
                            Enumeration var8 = var7.elements();

                            while(var8.hasMoreElements()) {
                                TCryptoPermission var9 = (TCryptoPermission)var8.nextElement();
                                if(var9.getExemptionMechanism() == null) {
                                    return var9;
                                }
                            }

                            PermissionCollection var15 = exemptPolicy.getPermissionCollection(var1);
                            if(var15 == null) {
                                return var2;
                            } else {
                                var8 = var15.elements();

                                while(var8.hasMoreElements()) {
                                    TCryptoPermission var10 = (TCryptoPermission)var8.nextElement();

                                    try {
                                        TExemptionMechanism.getInstance(var10.getExemptionMechanism());
                                        if(var10.getAlgorithm().equals("*")) {
                                            TCryptoPermission var11;
                                            if(var10.getCheckParam()) {
                                                var11 = new TCryptoPermission(var1, var10.getMaxKeySize(), var10.getAlgorithmParameterSpec(), var10.getExemptionMechanism());
                                            } else {
                                                var11 = new TCryptoPermission(var1, var10.getMaxKeySize(), var10.getExemptionMechanism());
                                            }

                                            if(var14.implies(var11)) {
                                                return var11;
                                            }
                                        }

                                        if(var14.implies(var10)) {
                                            return var10;
                                        }
                                    } catch (Exception var12) {
                                        ;
                                    }
                                }

                                return var2;
                            }
                        }
                    }
                } else {
                    return var2;
                }
            }
        }
    }

    private static TCryptoPermissions getAppPermissions(URL var0) {
        try {
            return TJceSecurity.verifyExemptJar(var0);
        } catch (Exception var2) {
            return null;
        }
    }

    private TCryptoPermission getDefaultPermission(String var1) {
        Enumeration var2 = defaultPolicy.getPermissionCollection(var1).elements();
        return (TCryptoPermission)var2.nextElement();
    }

    boolean isCallerTrusted() {
        Class[] var1 = this.getClassContext();
        URL var2 = null;

        int var3;
        for(var3 = 0; var3 < var1.length; ++var3) {
            var2 = TJceSecurity.getCodeBase(var1[var3]);
            if(var2 != null) {
                break;
            }
        }

        if(var3 == var1.length) {
            return true;
        } else if(TrustedCallersCache.contains(var1[var3])) {
            return true;
        } else {
            try {
                TJceSecurity.verifyProviderJar(var2);
            } catch (Exception var5) {
                return false;
            }

            TrustedCallersCache.addElement(var1[var3]);
            return true;
        }
    }

    static {
        allPerm = TCryptoAllPermission.INSTANCE;
        INSTANCE = (TJceSecurityManager) AccessController.doPrivileged(new PrivilegedAction() {
            public TJceSecurityManager run() {
                return new TJceSecurityManager(null);
            }
        });
    }
}
