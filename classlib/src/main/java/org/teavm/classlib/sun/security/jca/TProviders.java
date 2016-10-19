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
package org.teavm.classlib.sun.security.jca;

import org.teavm.classlib.java.security.TProvider;

public class TProviders {
    private static final ThreadLocal<TProviderList> threadLists = new InheritableThreadLocal();
    private static volatile int threadListsUsed;
    private static volatile TProviderList providerList;
    private static final String BACKUP_PROVIDER_CLASSNAME = "sun.security.provider.VerificationProvider";
    private static final String[] jarVerificationProviders;

    private TProviders() {
    }

    public static TProvider getSunProvider() {
        try {
            Class var0 = Class.forName(jarVerificationProviders[0]);
            return (TProvider)var0.newInstance();
        } catch (Exception var3) {
            try {
                Class var1 = Class.forName("sun.security.provider.VerificationProvider");
                return (TProvider)var1.newInstance();
            } catch (Exception var2) {
                throw new RuntimeException("Sun provider not found", var3);
            }
        }
    }

    public static Object startJarVerification() {
        TProviderList var0 = getProviderList();
        TProviderList var1 = var0.getJarList(jarVerificationProviders);
        return beginThreadProviderList(var1);
    }

    public static void stopJarVerification(Object var0) {
        endThreadProviderList((TProviderList)var0);
    }

    public static TProviderList getProviderList() {
        TProviderList var0 = getThreadProviderList();
        if(var0 == null) {
            var0 = getSystemProviderList();
        }

        return var0;
    }

    public static void setProviderList(TProviderList var0) {
        if(getThreadProviderList() == null) {
            setSystemProviderList(var0);
        } else {
            changeThreadProviderList(var0);
        }

    }

    public static TProviderList getFullProviderList() {
        Class var1 = sun.security.jca.Providers.class;
        TProviderList var0;
        synchronized(sun.security.jca.Providers.class) {
            var0 = getThreadProviderList();
            if(var0 != null) {
                TProviderList var2 = var0.removeInvalid();
                if(var2 != var0) {
                    changeThreadProviderList(var2);
                    var0 = var2;
                }

                return var0;
            }
        }

        var0 = getSystemProviderList();
        TProviderList var5 = var0.removeInvalid();
        if(var5 != var0) {
            setSystemProviderList(var5);
            var0 = var5;
        }

        return var0;
    }

    private static TProviderList getSystemProviderList() {
        return providerList;
    }

    private static void setSystemProviderList(TProviderList var0) {
        providerList = var0;
    }

    public static TProviderList getThreadProviderList() {
        return threadListsUsed == 0?null:(TProviderList)threadLists.get();
    }

    private static void changeThreadProviderList(TProviderList var0) {
        threadLists.set(var0);
    }

    public static synchronized TProviderList beginThreadProviderList(TProviderList var0) {
        if(TProviderList.debug != null) {
            TProviderList.debug.println("ThreadLocal providers: " + var0);
        }

        TProviderList var1 = (TProviderList)threadLists.get();
        ++threadListsUsed;
        threadLists.set(var0);
        return var1;
    }

    public static synchronized void endThreadProviderList(TProviderList var0) {
        if(var0 == null) {
            if(TProviderList.debug != null) {
                TProviderList.debug.println("Disabling ThreadLocal providers");
            }

            threadLists.remove();
        } else {
            if(TProviderList.debug != null) {
                TProviderList.debug.println("Restoring previous ThreadLocal providers: " + var0);
            }

            threadLists.set(var0);
        }

        --threadListsUsed;
    }

    static {
        providerList = TProviderList.EMPTY;
        providerList = TProviderList.fromSecurityProperties();
        jarVerificationProviders = new String[]{"sun.security.provider.Sun", "sun.security.rsa.SunRsaSign", "sun.security.ec.SunEC", "sun.security.provider.VerificationProvider"};
    }
}
