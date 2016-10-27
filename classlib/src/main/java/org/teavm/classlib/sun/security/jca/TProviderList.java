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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TProvider;
import org.teavm.classlib.java.util.TAbstractList;
import org.teavm.classlib.java.util.TArrayList;
import org.teavm.classlib.java.util.TIterator;
import org.teavm.classlib.java.util.TList;

public final class TProviderList {
    private static final TProviderConfig[] PC0 = new TProviderConfig[0];
    private static final Provider[] P0 = new Provider[0];
    static final TProviderList EMPTY;
    private static final TProvider EMPTY_PROVIDER;
    private final TProviderConfig[] configs;
    private volatile boolean allLoaded;
    private final TList<TProvider> userList;

    static TProviderList fromSecurityProperties() {
        return (TProviderList) AccessController.doPrivileged(new PrivilegedAction() {
            public TProviderList run() {
                return new TProviderList(null);
            }
        });
    }

    public static TProviderList add(TProviderList var0, Provider var1) {
        return insertAt(var0, var1, -1);
    }

    public static TProviderList insertAt(TProviderList var0, TProvider var1, int var2) {
        if(var0.getProvider(var1.getName()) != null) {
            return var0;
        } else {
            ArrayList var3 = new ArrayList(Arrays.asList(var0.configs));
            int var4 = var3.size();
            if(var2 < 0 || var2 > var4) {
                var2 = var4;
            }

            var3.add(var2, new TProviderConfig(var1));
            return new TProviderList((TProviderConfig[])var3.toArray(PC0), true);
        }
    }

    public static TProviderList remove(TProviderList var0, String var1) {
        if(var0.getProvider(var1) == null) {
            return var0;
        } else {
            TProviderConfig[] var2 = new TProviderConfig[var0.size() - 1];
            int var3 = 0;
            TProviderConfig[] var4 = var0.configs;
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                TProviderConfig var7 = var4[var6];
                if(!var7.getProvider().getName().equals(var1)) {
                    var2[var3++] = var7;
                }
            }

            return new TProviderList(var2, true);
        }
    }

    public static TProviderList newList(Provider... var0) {
        TProviderConfig[] var1 = new TProviderConfig[var0.length];

        for(int var2 = 0; var2 < var0.length; ++var2) {
            var1[var2] = new TProviderConfig(var0[var2]);
        }

        return new TProviderList(var1, true);
    }

    private TProviderList(TProviderConfig[] var1, boolean var2) {
        this.userList = new TAbstractList() {
            public int size() {
                return TProviderList.this.configs.length;
            }

            public TProvider get(int var1) {
                return TProviderList.this.getProvider(var1);
            }
        };
        this.configs = var1;
        this.allLoaded = var2;
    }

    private TProviderList() {
        this.userList = new TAbstractList() {
            public int size() {
                return TProviderList.this.configs.length;
            }

            public TProvider get(int var1) {
                return TProviderList.this.getProvider(var1);
            }
        };
        ArrayList var1 = new ArrayList();
        int var2 = 1;

        while(true) {
            String var3 = Security.getProperty("security.provider." + var2);
            if(var3 == null) {
                break;
            }

            var3 = var3.trim();
            if(var3.length() == 0) {
                System.err.println("invalid entry for security.provider." + var2);
                break;
            }

            int var4 = var3.indexOf(32);
            TProviderConfig var5;
            if(var4 == -1) {
                var5 = new TProviderConfig(var3);
            } else {
                String var6 = var3.substring(0, var4);
                String var7 = var3.substring(var4 + 1).trim();
                var5 = new TProviderConfig(var6, var7);
            }

            if(!var1.contains(var5)) {
                var1.add(var5);
            }

            ++var2;
        }

        this.configs = (TProviderConfig[])var1.toArray(PC0);

    }

    TProviderList getJarList(String[] var1) {
        ArrayList var2 = new ArrayList();
        String[] var3 = var1;
        int var4 = var1.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String var6 = var3[var5];
            TProviderConfig var7 = new TProviderConfig(var6);
            TProviderConfig[] var8 = this.configs;
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                TProviderConfig var11 = var8[var10];
                if(var11.equals(var7)) {
                    var7 = var11;
                    break;
                }
            }

            var2.add(var7);
        }

        TProviderConfig[] var12 = (TProviderConfig[])var2.toArray(PC0);
        return new TProviderList(var12, false);
    }

    public int size() {
        return this.configs.length;
    }

    TProvider getProvider(int var1) {
        TProvider var2 = this.configs[var1].getProvider();
        return var2 != null?var2:EMPTY_PROVIDER;
    }

    public TList<TProvider> providers() {
        return this.userList;
    }

    private TProviderConfig getProviderConfig(TString var1) {
        int var2 = this.getIndex(var1);
        return var2 != -1?this.configs[var2]:null;
    }

    public TProvider getProvider(TString var1) {
        TProviderConfig var2 = this.getProviderConfig(var1);
        return var2 == null?null:var2.getProvider();
    }

    public int getIndex(TString var1) {
        for(int var2 = 0; var2 < this.configs.length; ++var2) {
            TProvider var3 = this.getProvider(var2);
            if(var3.getName().equals(var1)) {
                return var2;
            }
        }

        return -1;
    }

    private int loadAll() {
        if(this.allLoaded) {
            return this.configs.length;
        } else {

            int var1 = 0;

            for(int var2 = 0; var2 < this.configs.length; ++var2) {
                TProvider var3 = this.configs[var2].getProvider();
                if(var3 != null) {
                    ++var1;
                }
            }

            if(var1 == this.configs.length) {
                this.allLoaded = true;
            }

            return var1;
        }
    }

    TProviderList removeInvalid() {
        int var1 = this.loadAll();
        if(var1 == this.configs.length) {
            return this;
        } else {
            TProviderConfig[] var2 = new TProviderConfig[var1];
            int var3 = 0;

            for(int var4 = 0; var3 < this.configs.length; ++var3) {
                TProviderConfig var5 = this.configs[var3];
                if(var5.isLoaded()) {
                    var2[var4++] = var5;
                }
            }

            return new TProviderList(var2, true);
        }
    }

    public TProvider[] toArray() {
        return (TProvider[])this.providers().toArray(P0);
    }

    public String toString() {
        return Arrays.asList(this.configs).toString();
    }

    public TProvider.Service getService(TString var1, TString var2) {
        for(int var3 = 0; var3 < this.configs.length; ++var3) {
            TProvider var4 = this.getProvider(var3);
            TProvider.Service var5 = var4.getService(var1, var2);
            if(var5 != null) {
                return var5;
            }
        }

        return null;
    }

    public TList<TProvider.Service> getServices(TString var1, TString var2) {
        return new TProviderList.ServiceList(var1, var2);
    }

    /** @deprecated */
    @Deprecated
    public TList<Provider.Service> getServices(TString var1, TList<TString> var2) {
        TArrayList var3 = new TArrayList();
        TIterator var4 = var2.iterator();

        while(var4.hasNext()) {
            TString var5 = (TString)var4.next();
            var3.add(new TServiceId(var1, var5));
        }

        return this.getServices(var3);
    }

    public TList<TProvider.Service> getServices(List<TServiceId> var1) {
        return new TProviderList.ServiceList(var1);
    }

    static {
        EMPTY = new TProviderList(PC0, true);
        EMPTY_PROVIDER = new TProvider("##Empty##", 1.0D, var4) {
            private static final long serialVersionUID = 1151354171352296389L;

            public Service getService(String var1, String var2) {
                return null;
            }
        };
    }

    private final class ServiceList extends TAbstractList<TProvider.Service> {
        private final TString type;
        private final TString algorithm;
        private final List<TServiceId> ids;
        private TProvider.Service firstService;
        private TList<TProvider.Service> services;
        private int providerIndex;

        ServiceList(TString var2, TString var3) {
            this.type = var2;
            this.algorithm = var3;
            this.ids = null;
        }

        ServiceList(List<TServiceId> var1) {
            this.type = null;
            this.algorithm = null;
            this.ids = var2;
        }

        private void addService(TProvider.Service var1) {
            if(this.firstService == null) {
                this.firstService = var1;
            } else {
                if(this.services == null) {
                    this.services = new TArrayList(4);
                    this.services.add(this.firstService);
                }

                this.services.add(var1);
            }

        }

        private TProvider.Service tryGet(int var1) {
            while(var1 != 0 || this.firstService == null) {
                if(this.services != null && this.services.size() > var1) {
                    return (TProvider.Service)this.services.get(var1);
                }

                if(this.providerIndex >= TProviderList.this.configs.length) {
                    return null;
                }

                TProvider var2 = TProviderList.this.getProvider(this.providerIndex++);
                if(this.type != null) {
                    TProvider.Service var6 = var2.getService(this.type, this.algorithm);
                    if(var6 != null) {
                        this.addService(var6);
                    }
                } else {
                    Iterator var3 = this.ids.iterator();

                    while(var3.hasNext()) {
                        TServiceId var4 = (TServiceId)var3.next();
                        TProvider.Service var5 = var2.getService(var4.type, var4.algorithm);
                        if(var5 != null) {
                            this.addService(var5);
                        }
                    }
                }
            }

            return this.firstService;
        }

        public TProvider.Service get(int var1) {
            TProvider.Service var2 = this.tryGet(var1);
            if(var2 == null) {
                throw new IndexOutOfBoundsException();
            } else {
                return var2;
            }
        }

        public int size() {
            int var1;
            if(this.services != null) {
                var1 = this.services.size();
            } else {
                var1 = this.firstService != null?1:0;
            }

            while(this.tryGet(var1) != null) {
                ++var1;
            }

            return var1;
        }

        public boolean isEmpty() {
            return this.tryGet(0) == null;
        }

        public TIterator<TProvider.Service> iterator() {
            return new TIterator() {
                int index;

                public boolean hasNext() {
                    return TProviderList.ServiceList.this.tryGet(this.index) != null;
                }

                public TProvider.Service next() {
                    TProvider.Service var1 = TProviderList.ServiceList.this.tryGet(this.index);
                    if(var1 == null) {
                        throw new NoSuchElementException();
                    } else {
                        ++this.index;
                        return var1;
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
