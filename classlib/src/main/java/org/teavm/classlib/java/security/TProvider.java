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
package org.teavm.classlib.java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TClassLoader;
import org.teavm.classlib.java.lang.TObject;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.lang.ref.TReference;
import org.teavm.classlib.java.lang.ref.TWeakReference;
import org.teavm.classlib.java.lang.reflect.TConstructor;
import org.teavm.classlib.java.util.TArrayList;
import org.teavm.classlib.java.util.TCollection;
import org.teavm.classlib.java.util.TCollections;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.java.util.THashMap;
import org.teavm.classlib.java.util.TIterator;
import org.teavm.classlib.java.util.TLinkedHashMap;
import org.teavm.classlib.java.util.TLinkedHashSet;
import org.teavm.classlib.java.util.TList;
import org.teavm.classlib.java.util.TLocale;
import org.teavm.classlib.java.util.TMap;
import org.teavm.classlib.java.util.TProperties;
import org.teavm.classlib.java.util.TSet;

/**
 * Created by vasek on 29. 6. 2016.
 */
public abstract class TProvider extends TProperties {
    static final long serialVersionUID = -4298000515446427739L;
    private TString name;
    private TString info;
    private double version;
    private transient TSet<TMap.Entry<Object, Object>> entrySet = null;
    private transient int entrySetCallCount = 0;
    private transient boolean initialized;
    private transient boolean legacyChanged;
    private transient boolean servicesChanged;
    private transient TMap<TString, TString> legacyStrings;
    private transient TMap<org.teavm.classlib.java.security.TProvider.ServiceKey, org.teavm.classlib.java.security.TProvider.Service> serviceMap;
    private transient TMap<org.teavm.classlib.java.security.TProvider.ServiceKey, org.teavm.classlib.java.security.TProvider.Service> legacyMap;
    private transient TSet<org.teavm.classlib.java.security.TProvider.Service> serviceSet;
    private static final String ALIAS_PREFIX = "Alg.Alias.";
    private static final String ALIAS_PREFIX_LOWER = "alg.alias.";
    private static final int ALIAS_LENGTH = "Alg.Alias.".length();
    private static volatile org.teavm.classlib.java.security.TProvider.ServiceKey previousKey = new org.teavm.classlib.java.security.TProvider.ServiceKey("", "", false);
    private static final TMap<TString, org.teavm.classlib.java.security.TProvider.EngineDescription> knownEngines = new THashMap();

    protected TProvider(TString var1, double var2, TString var4) {
        this.name = var1;
        this.version = var2;
        this.info = var4;
        this.putId();
        this.initialized = true;
    }

    public TString getName() {
        return this.name;
    }

    public double getVersion() {
        return this.version;
    }

    public TString getInfo() {
        return this.info;
    }

    public String toString() {
        return this.name + " version " + this.version;
    }

    public synchronized void clear() {
        this.check("clearProviderProperties." + this.name);
        this.implClear();
    }

    public synchronized void load(InputStream var1) throws IOException {
        this.check("putProviderProperty." + this.name);

        Properties var2 = new Properties();
        var2.load(var1);
        this.implPutAll(var2);
    }

    public synchronized void putAll(Map<?, ?> var1) {
        this.check("putProviderProperty." + this.name);

        this.implPutAll(var1);
    }

    public synchronized TSet<TMap.Entry<Object, Object>> entrySet() {
        this.checkInitialized();
        if(this.entrySet == null) {
            if(this.entrySetCallCount++ != 0) {
                return super.entrySet();
            }

            this.entrySet = TCollections.unmodifiableMap(this).entrySet();
        }

        if(this.entrySetCallCount != 2) {
            throw new RuntimeException("Internal error.");
        } else {
            return this.entrySet;
        }
    }

    public TSet<Object> keySet() {
        this.checkInitialized();
        return TCollections.unmodifiableSet(super.keySet());
    }

    public TCollection<Object> values() {
        this.checkInitialized();
        return TCollections.unmodifiableCollection(super.values());
    }

    public synchronized Object put(Object var1, Object var2) {
        this.check("putProviderProperty." + this.name);

        return this.implPut(var1, var2);
    }

    public synchronized Object putIfAbsent(Object var1, Object var2) {
        this.check("putProviderProperty." + this.name);

        return this.implPutIfAbsent(var1, var2);
    }

    public synchronized Object remove(Object var1) {
        this.check("removeProviderProperty." + this.name);

        return this.implRemove(var1);
    }

    public synchronized boolean remove(Object var1, Object var2) {
        this.check("removeProviderProperty." + this.name);

        return this.implRemove(var1, var2);
    }

    public synchronized boolean replace(Object var1, Object var2, Object var3) {
        this.check("putProviderProperty." + this.name);

        return this.implReplace(var1, var2, var3);
    }

    public synchronized Object replace(Object var1, Object var2) {
        this.check("putProviderProperty." + this.name);

        return this.implReplace(var1, var2);
    }

    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ? extends Object> var1) {
        this.check("putProviderProperty." + this.name);

        this.implReplaceAll(var1);
    }

    public synchronized Object compute(Object var1, BiFunction<? super Object, ? super Object, ? extends Object> var2) {
        this.check("putProviderProperty." + this.name);
        this.check("removeProviderProperty" + this.name);

        return this.implCompute(var1, var2);
    }

    public synchronized Object computeIfAbsent(Object var1, Function<? super Object, ? extends Object> var2) {
        this.check("putProviderProperty." + this.name);
        this.check("removeProviderProperty" + this.name);

        return this.implComputeIfAbsent(var1, var2);
    }

    public synchronized Object computeIfPresent(Object var1, BiFunction<? super Object, ? super Object, ? extends Object> var2) {
        this.check("putProviderProperty." + this.name);
        this.check("removeProviderProperty" + this.name);

        return this.implComputeIfPresent(var1, var2);
    }

    public synchronized Object merge(Object var1, Object var2, BiFunction<? super Object, ? super Object, ? extends Object> var3) {
        this.check("putProviderProperty." + this.name);
        this.check("removeProviderProperty" + this.name);

        return this.implMerge(var1, var2, var3);
    }

    public Object get(Object var1) {
        this.checkInitialized();
        return super.get(var1);
    }

    public synchronized Object getOrDefault(Object var1, Object var2) {
        this.checkInitialized();
        return super.getOrDefault(var1, var2);
    }

    public synchronized void forEach(BiConsumer<? super Object, ? super Object> var1) {
        this.checkInitialized();
        super.forEach(var1);
    }

    public TEnumeration<Object> keys() {
        this.checkInitialized();
        return super.keys();
    }

    public TEnumeration<Object> elements() {
        this.checkInitialized();
        return super.elements();
    }

    public String getProperty(String var1) {
        this.checkInitialized();
        return super.getProperty(var1);
    }

    private void checkInitialized() {
        if(!this.initialized) {
            throw new IllegalStateException();
        }
    }

    private void check(String var1) {
        this.checkInitialized();
        SecurityManager var2 = System.getSecurityManager();
        if(var2 != null) {
            var2.checkSecurityAccess(var1);
        }

    }

    private void putId() {
        super.put("Provider.id name", String.valueOf(this.name));
        super.put("Provider.id version", String.valueOf(this.version));
        super.put("Provider.id info", String.valueOf(this.info));
        super.put("Provider.id className", this.getClass().getName());
    }

    private void readObject(ObjectInputStream var1) throws IOException, ClassNotFoundException {
        HashMap var2 = new HashMap();
        TIterator var3 = super.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry var4 = (Map.Entry)var3.next();
            var2.put(var4.getKey(), var4.getValue());
        }

        this.defaults = null;
        var1.defaultReadObject();
        this.implClear();
        this.initialized = true;
        this.putAll(var2);
    }

    private boolean checkLegacy(Object var1) {
        TString var2 = (TString)var1;
        if(var2.startsWith(TString.wrap("Provider."))) {
            return false;
        } else {
            this.legacyChanged = true;
            if(this.legacyStrings == null) {
                this.legacyStrings = new TLinkedHashMap();
            }

            return true;
        }
    }

    private void implPutAll(Map<?, ?> var1) {
        Iterator var2 = var1.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry var3 = (Map.Entry)var2.next();
            this.implPut(var3.getKey(), var3.getValue());
        }

    }

    private Object implRemove(Object var1) {
        if(var1 instanceof String) {
            if(!this.checkLegacy(var1)) {
                return null;
            }

            this.legacyStrings.remove((String)var1);
        }

        return super.remove(var1);
    }

    private boolean implRemove(Object var1, Object var2) {
        if(var1 instanceof String && var2 instanceof String) {
            if(!this.checkLegacy(var1)) {
                return false;
            }

            this.legacyStrings.remove((String)var1, var2);
        }

        return super.remove(var1, var2);
    }

    private boolean implReplace(Object var1, Object var2, Object var3) {
        if(var1 instanceof TString && var2 instanceof TString && var3 instanceof TString) {
            if(!this.checkLegacy(var1)) {
                return false;
            }

            this.legacyStrings.replace((TString)var1, (TString)var2, (TString)var3);
        }

        return super.replace(var1, var2, var3);
    }

    private Object implReplace(Object var1, Object var2) {
        if(var1 instanceof String && var2 instanceof String) {
            if(!this.checkLegacy(var1)) {
                return null;
            }

            this.legacyStrings.replace((String)var1, (String)var2);
        }

        return super.replace(var1, var2);
    }

    private void implReplaceAll(BiFunction<? super Object, ? super Object, ? extends Object> var1) {
        this.legacyChanged = true;
        if(this.legacyStrings == null) {
            this.legacyStrings = new TLinkedHashMap();
        } else {
            this.legacyStrings.replaceAll(var1);
        }

        super.replaceAll(var1);
    }

    private Object implMerge(Object var1, Object var2, BiFunction<? super Object, ? super Object, ? extends Object> var3) {
        if(var1 instanceof String && var2 instanceof String) {
            if(!this.checkLegacy(var1)) {
                return null;
            }

            this.legacyStrings.merge((String)var1, (String)var2, var3);
        }

        return super.merge(var1, var2, var3);
    }

    private Object implCompute(Object var1, BiFunction<? super Object, ? super Object, ? extends Object> var2) {
        if(var1 instanceof String) {
            if(!this.checkLegacy(var1)) {
                return null;
            }

            this.legacyStrings.computeIfAbsent((String)var1, (Function)var2);
        }

        return super.compute(var1, var2);
    }

    private Object implComputeIfAbsent(Object var1, Function<? super Object, ? extends Object> var2) {
        if(var1 instanceof String) {
            if(!this.checkLegacy(var1)) {
                return null;
            }

            this.legacyStrings.computeIfAbsent((String)var1, var2);
        }

        return super.computeIfAbsent(var1, var2);
    }

    private Object implComputeIfPresent(Object var1, BiFunction<? super Object, ? super Object, ? extends Object> var2) {
        if(var1 instanceof String) {
            if(!this.checkLegacy(var1)) {
                return null;
            }

            this.legacyStrings.computeIfPresent((String)var1, var2);
        }

        return super.computeIfPresent(var1, var2);
    }

    private Object implPut(Object var1, Object var2) {
        if(var1 instanceof String && var2 instanceof String) {
            if(!this.checkLegacy(var1)) {
                return null;
            }

            this.legacyStrings.put((String)var1, (String)var2);
        }

        return super.put(var1, var2);
    }

    private Object implPutIfAbsent(Object var1, Object var2) {
        if(var1 instanceof String && var2 instanceof String) {
            if(!this.checkLegacy(var1)) {
                return null;
            }

            this.legacyStrings.putIfAbsent((String)var1, (String)var2);
        }

        return super.putIfAbsent(var1, var2);
    }

    private void implClear() {
        if(this.legacyStrings != null) {
            this.legacyStrings.clear();
        }

        if(this.legacyMap != null) {
            this.legacyMap.clear();
        }

        if(this.serviceMap != null) {
            this.serviceMap.clear();
        }

        this.legacyChanged = false;
        this.servicesChanged = false;
        this.serviceSet = null;
        super.clear();
        this.putId();
    }

    private void ensureLegacyParsed() {
        if(this.legacyChanged && this.legacyStrings != null) {
            this.serviceSet = null;
            if(this.legacyMap == null) {
                this.legacyMap = new TLinkedHashMap();
            } else {
                this.legacyMap.clear();
            }

            Iterator var1 = this.legacyStrings.entrySet().iterator();

            while(var1.hasNext()) {
                Map.Entry var2 = (Map.Entry)var1.next();
                this.parseLegacyPut((String)var2.getKey(), (String)var2.getValue());
            }

            this.removeInvalidServices(this.legacyMap);
            this.legacyChanged = false;
        }
    }

    private void removeInvalidServices(Map<org.teavm.classlib.java.security.TProvider.ServiceKey, org.teavm.classlib.java.security.TProvider.Service> var1) {
        Iterator var2 = var1.entrySet().iterator();

        while(var2.hasNext()) {
            org.teavm.classlib.java.security.TProvider.Service var3 = (org.teavm.classlib.java.security.TProvider.Service)((Map.Entry)var2.next()).getValue();
            if(!var3.isValid()) {
                var2.remove();
            }
        }

    }

    private String[] getTypeAndAlgorithm(String var1) {
        int var2 = var1.indexOf(".");
        if(var2 < 1) {
            if(debug != null) {
                debug.println("Ignoring invalid entry in provider " + this.name + ":" + var1);
            }

            return null;
        } else {
            String var3 = var1.substring(0, var2);
            String var4 = var1.substring(var2 + 1);
            return new String[]{var3, var4};
        }
    }

    private void parseLegacyPut(String var1, String var2) {
        String var6;
        String var7;
        org.teavm.classlib.java.security.TProvider.ServiceKey var8;
        org.teavm.classlib.java.security.TProvider.Service var9;
        if(var1.toLowerCase(Locale.ENGLISH).startsWith("alg.alias.")) {
            String var4 = var1.substring(ALIAS_LENGTH);
            String[] var5 = this.getTypeAndAlgorithm(var4);
            if(var5 == null) {
                return;
            }

            var6 = getEngineName(var5[0]);
            var7 = var5[1].intern();
            var8 = new org.teavm.classlib.java.security.TProvider.ServiceKey(var6, var2, true);
            var9 = (org.teavm.classlib.java.security.TProvider.Service)this.legacyMap.get(var8);
            if(var9 == null) {
                var9 = new org.teavm.classlib.java.security.TProvider.Service(this);
                var9.type = var6;
                var9.algorithm = var2;
                this.legacyMap.put(var8, var9);
            }

            this.legacyMap.put(new org.teavm.classlib.java.security.TProvider.ServiceKey(var6, var7, true), var9);
            var9.addAlias(var7);
        } else {
            String[] var3 = this.getTypeAndAlgorithm(var1);
            if(var3 == null) {
                return;
            }

            int var12 = var3[1].indexOf(32);
            if(var12 == -1) {
                String var13 = getEngineName(var3[0]);
                var6 = var3[1].intern();
                var8 = new org.teavm.classlib.java.security.TProvider.ServiceKey(var13, var6, true);
                var9 = (org.teavm.classlib.java.security.TProvider.Service)this.legacyMap.get(var8);
                if(var9 == null) {
                    var9 = new org.teavm.classlib.java.security.TProvider.Service(this);
                    var9.type = var13;
                    var9.algorithm = var6;
                    this.legacyMap.put(var8, var9);
                }

                var9.className = var2;
            } else {
                var6 = getEngineName(var3[0]);
                var7 = var3[1];
                String var14 = var7.substring(0, var12).intern();

                String var15;
                for(var15 = var7.substring(var12 + 1); var15.startsWith(" "); var15 = var15.substring(1)) {
                    ;
                }

                var15 = var15.intern();
                org.teavm.classlib.java.security.TProvider.ServiceKey var10 = new org.teavm.classlib.java.security.TProvider.ServiceKey(var6, var14, true);
                org.teavm.classlib.java.security.TProvider.Service var11 = (org.teavm.classlib.java.security.TProvider.Service)this.legacyMap.get(var10);
                if(var11 == null) {
                    var11 = new org.teavm.classlib.java.security.TProvider.Service(this);
                    var11.type = var6;
                    var11.algorithm = var14;
                    this.legacyMap.put(var10, var11);
                }

                var11.addAttribute(var15, var2);
            }
        }

    }

    public synchronized org.teavm.classlib.java.security.TProvider.Service getService(TString var1, TString var2) {
        this.checkInitialized();
        org.teavm.classlib.java.security.TProvider.ServiceKey var3 = previousKey;
        if(!var3.matches(var1, var2)) {
            var3 = new org.teavm.classlib.java.security.TProvider.ServiceKey(var1, var2, false);
            previousKey = var3;
        }

        if(this.serviceMap != null) {
            org.teavm.classlib.java.security.TProvider.Service var4 = (org.teavm.classlib.java.security.TProvider.Service)this.serviceMap.get(var3);
            if(var4 != null) {
                return var4;
            }
        }

        this.ensureLegacyParsed();
        return this.legacyMap != null?(org.teavm.classlib.java.security.TProvider.Service)this.legacyMap.get(var3):null;
    }

    public synchronized TSet<org.teavm.classlib.java.security.TProvider.Service> getServices() {
        this.checkInitialized();
        if(this.legacyChanged || this.servicesChanged) {
            this.serviceSet = null;
        }

        if(this.serviceSet == null) {
            this.ensureLegacyParsed();
            TLinkedHashSet var1 = new TLinkedHashSet();
            if(this.serviceMap != null) {
                var1.addAll(this.serviceMap.values());
            }

            if(this.legacyMap != null) {
                var1.addAll(this.legacyMap.values());
            }

            this.serviceSet = TCollections.unmodifiableSet(var1);
            this.servicesChanged = false;
        }

        return this.serviceSet;
    }

    protected synchronized void putService(org.teavm.classlib.java.security.TProvider.Service var1) {
        this.check("putProviderProperty." + this.name);

        if(var1 == null) {
            throw new NullPointerException();
        } else if(var1.getProvider() != this) {
            throw new IllegalArgumentException("service.getProvider() must match this Provider object");
        } else {
            if(this.serviceMap == null) {
                this.serviceMap = new TLinkedHashMap();
            }

            this.servicesChanged = true;
            String var2 = var1.getType();
            String var3 = var1.getAlgorithm();
            org.teavm.classlib.java.security.TProvider.ServiceKey var4 = new org.teavm.classlib.java.security.TProvider.ServiceKey(var2, var3, true);
            this.implRemoveService((org.teavm.classlib.java.security.TProvider.Service)this.serviceMap.get(var4));
            this.serviceMap.put(var4, var1);
            Iterator var5 = var1.getAliases().iterator();

            while(var5.hasNext()) {
                String var6 = (String)var5.next();
                this.serviceMap.put(new org.teavm.classlib.java.security.TProvider.ServiceKey(var2, var6, true), var1);
            }

            this.putPropertyStrings(var1);
        }
    }

    private void putPropertyStrings(org.teavm.classlib.java.security.TProvider.Service var1) {
        String var2 = var1.getType();
        String var3 = var1.getAlgorithm();
        super.put(var2 + "." + var3, var1.getClassName());
        Iterator var4 = var1.getAliases().iterator();

        while(var4.hasNext()) {
            String var5 = (String)var4.next();
            super.put("Alg.Alias." + var2 + "." + var5, var3);
        }

        var4 = var1.attributes.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry var7 = (Map.Entry)var4.next();
            String var6 = var2 + "." + var3 + " " + var7.getKey();
            super.put(var6, var7.getValue());
        }

    }

    private void removePropertyStrings(org.teavm.classlib.java.security.TProvider.Service var1) {
        TString var2 = var1.getType();
        TString var3 = var1.getAlgorithm();
        super.remove(var2 + "." + var3);
        TIterator var4 = var1.getAliases().iterator();

        while(var4.hasNext()) {
            String var5 = (String)var4.next();
            super.remove("Alg.Alias." + var2 + "." + var5);
        }

        var4 = var1.attributes.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry var7 = (Map.Entry)var4.next();
            String var6 = var2 + "." + var3 + " " + var7.getKey();
            super.remove(var6);
        }

    }

    protected synchronized void removeService(org.teavm.classlib.java.security.TProvider.Service var1) {
        this.check("removeProviderProperty." + this.name);

        if(var1 == null) {
            throw new NullPointerException();
        } else {
            this.implRemoveService(var1);
        }
    }

    private void implRemoveService(org.teavm.classlib.java.security.TProvider.Service var1) {
        if(var1 != null && this.serviceMap != null) {
            TString var2 = var1.getType();
            TString var3 = var1.getAlgorithm();
            org.teavm.classlib.java.security.TProvider.ServiceKey var4 = new org.teavm.classlib.java.security.TProvider.ServiceKey(var2, var3, false);
            org.teavm.classlib.java.security.TProvider.Service var5 = (org.teavm.classlib.java.security.TProvider.Service)this.serviceMap.get(var4);
            if(var1 == var5) {
                this.servicesChanged = true;
                this.serviceMap.remove(var4);
                TIterator var6 = var1.getAliases().iterator();

                while(var6.hasNext()) {
                    TString var7 = (TString)var6.next();
                    this.serviceMap.remove(new org.teavm.classlib.java.security.TProvider.ServiceKey(var2, var7, false));
                }

                this.removePropertyStrings(var1);
            }
        }
    }

    private static void addEngine(TString var0, boolean var1, TString var2) {
        org.teavm.classlib.java.security.TProvider.EngineDescription var3 = new org.teavm.classlib.java.security.TProvider.EngineDescription(var0, var1, var2);
        knownEngines.put(var0.toLowerCase(), var3);
        knownEngines.put(var0, var3);
    }

    private static TString getEngineName(TString var0) {
        org.teavm.classlib.java.security.TProvider.EngineDescription var1 = (org.teavm.classlib.java.security.TProvider.EngineDescription)knownEngines.get(var0);
        if(var1 == null) {
            var1 = (org.teavm.classlib.java.security.TProvider.EngineDescription)knownEngines.get(var0.toLowerCase());
        }

        return var1 == null?var0:var1.name;
    }

    static {
        addEngine(TString.wrap("AlgorithmParameterGenerator"), false, (TString)null);
        addEngine(TString.wrap("AlgorithmParameters"), false, (TString)null);
        addEngine(TString.wrap("KeyFactory"), false, (TString)null);
        addEngine(TString.wrap("KeyPairGenerator"), false, (TString)null);
        addEngine(TString.wrap("KeyStore"), false, (TString)null);
        addEngine(TString.wrap("MessageDigest"), false, (TString)null);
        addEngine(TString.wrap("SecureRandom"), false, (TString)null);
        addEngine(TString.wrap("Signature"), true, (TString)null);
        addEngine(TString.wrap("CertificateFactory"), false, (TString)null);
        addEngine(TString.wrap("CertPathBuilder"), false, (TString)null);
        addEngine(TString.wrap("CertPathValidator"), false, (TString)null);
        addEngine(TString.wrap("CertStore"), false, TString.wrap("java.security.cert.CertStoreParameters"));
        addEngine(TString.wrap("Cipher"), true, (TString)null);
        addEngine(TString.wrap("ExemptionMechanism"), false, (TString)null);
        addEngine(TString.wrap("Mac"), true, (TString)null);
        addEngine(TString.wrap("KeyAgreement"), true, (TString)null);
        addEngine(TString.wrap("KeyGenerator"), false, (TString)null);
        addEngine(TString.wrap("SecretKeyFactory"), false, (TString)null);
        addEngine(TString.wrap("KeyManagerFactory"), false, (TString)null);
        addEngine(TString.wrap("SSLContext"), false, (TString)null);
        addEngine(TString.wrap("TrustManagerFactory"), false, (TString)null);
        addEngine(TString.wrap("GssApiMechanism"), false, (TString)null);
        addEngine(TString.wrap("SaslClientFactory"), false, (TString)null);
        addEngine(TString.wrap("SaslServerFactory"), false, (TString)null);
        addEngine(TString.wrap("Policy"), false, TString.wrap("java.security.Policy$Parameters"));
        addEngine(TString.wrap("Configuration"), false, TString.wrap("javax.security.auth.login.Configuration$Parameters"));
        addEngine(TString.wrap("XMLSignatureFactory"), false, (TString)null);
        addEngine(TString.wrap("KeyInfoFactory"), false, (TString)null);
        addEngine(TString.wrap("TransformService"), false, (TString)null);
        addEngine(TString.wrap("TerminalFactory"), false, TString.wrap("java.lang.Object"));
    }

    private static class EngineDescription {
        final TString name;
        final boolean supportsParameter;
        final TString constructorParameterClassName;
        private volatile TClass<?> constructorParameterClass;

        EngineDescription(TString var1, boolean var2, TString var3) {
            this.name = var1;
            this.supportsParameter = var2;
            this.constructorParameterClassName = var3;
        }

        Class<?> getConstructorParameterClass() throws ClassNotFoundException {
            Class var1 = this.constructorParameterClass;
            if(var1 == null) {
                var1 = Class.forName(this.constructorParameterClassName);
                this.constructorParameterClass = var1;
            }

            return var1;
        }
    }

    public static class Service {
        private TString type;
        private TString algorithm;
        private TString className;
        private final org.teavm.classlib.java.security.TProvider provider;
        private TList<TString> aliases;
        private TMap<org.teavm.classlib.java.security.TProvider.UString, TString> attributes;
        private volatile TReference<TClass<?>> classRef;
        private volatile Boolean hasKeyAttributes;
        private TString[] supportedFormats;
        private TClass[] supportedClasses;
        private boolean registered;
        private static final TClass<?>[] CLASS0 = new TClass[0];

        private Service(org.teavm.classlib.java.security.TProvider var1) {
            this.provider = var1;
            this.aliases = TCollections.emptyList();
            this.attributes = TCollections.emptyMap();
        }

        private boolean isValid() {
            return this.type != null && this.algorithm != null && this.className != null;
        }

        private void addAlias(String var1) {
            if(this.aliases.isEmpty()) {
                this.aliases = new TArrayList(2);
            }

            this.aliases.add(var1);
        }

        void addAttribute(TString var1, TString var2) {
            if(this.attributes.isEmpty()) {
                this.attributes = new THashMap(8);
            }

            this.attributes.put(new org.teavm.classlib.java.security.TProvider.UString(var1), var2);
        }

        public Service(org.teavm.classlib.java.security.TProvider var1, TString var2, TString var3, TString var4, List<TString> var5, Map<TString, TString> var6) {
            if(var1 != null && var2 != null && var3 != null && var4 != null) {
                this.provider = var1;
                this.type = org.teavm.classlib.java.security.TProvider.getEngineName(var2);
                this.algorithm = var3;
                this.className = var4;
                if(var5 == null) {
                    this.aliases = TCollections.emptyList();
                } else {
                    this.aliases = new TArrayList(var5);
                }

                if(var6 == null) {
                    this.attributes = Collections.emptyMap();
                } else {
                    this.attributes = new THashMap();
                    Iterator var7 = var6.entrySet().iterator();

                    while(var7.hasNext()) {
                        TMap.Entry var8 = (TMap.Entry)var7.next();
                        this.attributes.put(new org.teavm.classlib.java.security.TProvider.UString((TString)var8.getKey()), var8.getValue());
                    }
                }

            } else {
                throw new NullPointerException();
            }
        }

        public final TString getType() {
            return this.type;
        }

        public final TString getAlgorithm() {
            return this.algorithm;
        }

        public final org.teavm.classlib.java.security.TProvider getProvider() {
            return this.provider;
        }

        public final TString getClassName() {
            return this.className;
        }

        private final TList<TString> getAliases() {
            return this.aliases;
        }

        public final TString getAttribute(TString var1) {
            if(var1 == null) {
                throw new NullPointerException();
            } else {
                return (TString)this.attributes.get(new org.teavm.classlib.java.security.TProvider.UString(var1));
            }
        }

        public Object newInstance(Object var1) throws NoSuchAlgorithmException {
            if(!this.registered) {
                if(this.provider.getService(this.type, this.algorithm) != this) {
                    throw new NoSuchAlgorithmException("Service not registered with Provider " + this.provider.getName() + ": " + this);
                }

                this.registered = true;
            }

            try {
                org.teavm.classlib.java.security.TProvider.EngineDescription var2 = (org.teavm.classlib.java.security.TProvider.EngineDescription) org.teavm.classlib.java.security.TProvider.knownEngines.get(this.type);
                if(var2 == null) {
                    return this.newInstanceGeneric(var1);
                } else {
                    Class var3;
                    Constructor var5;
                    if(var2.constructorParameterClassName == null) {
                        if(var1 != null) {
                            throw new InvalidParameterException("constructorParameter not used with " + this.type + " engines");
                        } else {
                            var3 = this.getImplClass();
                            Class[] var9 = new Class[0];
                            var5 = var3.getConstructor(var9);
                            return var5.newInstance(new Object[0]);
                        }
                    } else {
                        var3 = var2.getConstructorParameterClass();
                        Class var4;
                        if(var1 != null) {
                            var4 = var1.getClass();
                            if(!var3.isAssignableFrom(var4)) {
                                throw new InvalidParameterException("constructorParameter must be instanceof " + var2.constructorParameterClassName.replace('$', '.') + " for engine type " + this.type);
                            }
                        }

                        var4 = this.getImplClass();
                        var5 = var4.getConstructor(new Class[]{var3});
                        return var5.newInstance(new Object[]{var1});
                    }
                }
            } catch (NoSuchAlgorithmException var6) {
                throw var6;
            } catch (InvocationTargetException var7) {
                throw new NoSuchAlgorithmException("Error constructing implementation (algorithm: " + this.algorithm + ", provider: " + this.provider.getName() + ", class: " + this.className + ")", var7.getCause());
            } catch (Exception var8) {
                throw new NoSuchAlgorithmException("Error constructing implementation (algorithm: " + this.algorithm + ", provider: " + this.provider.getName() + ", class: " + this.className + ")", var8);
            }
        }

        private TClass<?> getImplClass() throws NoSuchAlgorithmException {
            try {
                TReference var1 = this.classRef;
                TClass var2 = var1 == null?null:(TClass)var1.get();
                if(var2 == null) {
                    TClassLoader var3 = this.provider.getClass().getClassLoader();
                    if(var3 == null) {
                        var2 = TClass.forName(this.className);
                    } else {
                        var2 = var3.loadClass(this.className);
                    }

                    if(!Modifier.isPublic(var2.getModifiers())) {
                        throw new NoSuchAlgorithmException("class configured for " + this.type + " (provider: " + this.provider.getName() + ") is not public.");
                    }

                    this.classRef = new TWeakReference(var2);
                }

                return var2;
            } catch (ClassNotFoundException var4) {
                throw new NoSuchAlgorithmException("class configured for " + this.type + " (provider: " + this.provider.getName() + ") cannot be found.", var4);
            }
        }

        private TObject newInstanceGeneric(TObject var1) throws Exception {
            TClass var2 = this.getImplClass();
            if(var1 == null) {
                try {
                    TClass[] var11 = new TClass[0];
                    TConstructor var12 = var2.getConstructor(var11);
                    return var12.newInstance(new Object[0]);
                } catch (NoSuchMethodException var10) {
                    throw new NoSuchAlgorithmException("No public no-arg constructor found in class " + this.className);
                }
            } else {
                Class var3 = var1.getClass();
                Constructor[] var4 = var2.getConstructors();
                Constructor[] var5 = var4;
                int var6 = var4.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    Constructor var8 = var5[var7];
                    Class[] var9 = var8.getParameterTypes();
                    if(var9.length == 1 && var9[0].isAssignableFrom(var3)) {
                        return var8.newInstance(new Object[]{var1});
                    }
                }

                throw new NoSuchAlgorithmException("No public constructor matching " + var3.getName() + " found in class " + this.className);
            }
        }

        public boolean supportsParameter(Object var1) {
            org.teavm.classlib.java.security.TProvider.EngineDescription var2 = (org.teavm.classlib.java.security.TProvider.EngineDescription) org.teavm.classlib.java.security.TProvider.knownEngines.get(this.type);
            if(var2 == null) {
                return true;
            } else if(!var2.supportsParameter) {
                throw new InvalidParameterException("supportsParameter() not used with " + this.type + " engines");
            } else if(var1 != null && !(var1 instanceof Key)) {
                throw new InvalidParameterException("Parameter must be instanceof Key for engine " + this.type);
            } else if(!this.hasKeyAttributes()) {
                return true;
            } else if(var1 == null) {
                return false;
            } else {
                Key var3 = (Key)var1;
                return this.supportsKeyFormat(var3)?true:this.supportsKeyClass(var3);
            }
        }

        private boolean hasKeyAttributes() {
            Boolean var1 = this.hasKeyAttributes;
            if(var1 == null) {
                synchronized(this) {
                    String var3 = this.getAttribute("SupportedKeyFormats");
                    if(var3 != null) {
                        this.supportedFormats = var3.split("\\|");
                    }

                    var3 = this.getAttribute("SupportedKeyClasses");
                    if(var3 != null) {
                        String[] var4 = var3.split("\\|");
                        ArrayList var5 = new ArrayList(var4.length);
                        String[] var6 = var4;
                        int var7 = var4.length;

                        for(int var8 = 0; var8 < var7; ++var8) {
                            String var9 = var6[var8];
                            Class var10 = this.getKeyClass(var9);
                            if(var10 != null) {
                                var5.add(var10);
                            }
                        }

                        this.supportedClasses = (Class[])var5.toArray(CLASS0);
                    }

                    boolean var13 = this.supportedFormats != null || this.supportedClasses != null;
                    var1 = Boolean.valueOf(var13);
                    this.hasKeyAttributes = var1;
                }
            }

            return var1.booleanValue();
        }

        private Class<?> getKeyClass(String var1) {
            try {
                return Class.forName(var1);
            } catch (ClassNotFoundException var4) {
                try {
                    ClassLoader var2 = this.provider.getClass().getClassLoader();
                    if(var2 != null) {
                        return var2.loadClass(var1);
                    }
                } catch (ClassNotFoundException var3) {
                    ;
                }

                return null;
            }
        }

        private boolean supportsKeyFormat(Key var1) {
            if(this.supportedFormats == null) {
                return false;
            } else {
                String var2 = var1.getFormat();
                if(var2 == null) {
                    return false;
                } else {
                    String[] var3 = this.supportedFormats;
                    int var4 = var3.length;

                    for(int var5 = 0; var5 < var4; ++var5) {
                        String var6 = var3[var5];
                        if(var6.equals(var2)) {
                            return true;
                        }
                    }

                    return false;
                }
            }
        }

        private boolean supportsKeyClass(Key var1) {
            if(this.supportedClasses == null) {
                return false;
            } else {
                TClass var2 = var1.getClass();
                TClass[] var3 = this.supportedClasses;
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    TClass var6 = var3[var5];
                    if(var6.isAssignableFrom(var2)) {
                        return true;
                    }
                }

                return false;
            }
        }

        public String toString() {
            String var1 = this.aliases.isEmpty()?"":"\r\n  aliases: " + this.aliases.toString();
            String var2 = this.attributes.isEmpty()?"":"\r\n  attributes: " + this.attributes.toString();
            return this.provider.getName() + ": " + this.type + "." + this.algorithm + " -> " + this.className + var1 + var2 + "\r\n";
        }
    }

    private static class ServiceKey {
        private final TString type;
        private final TString algorithm;
        private final TString originalAlgorithm;

        private ServiceKey(TString var1, TString var2, boolean var3) {
            this.type = var1;
            this.originalAlgorithm = var2;
            var2 = var2.toUpperCase();
            this.algorithm = var3?var2.intern():var2;
        }

        public int hashCode() {
            return this.type.hashCode() + this.algorithm.hashCode();
        }

        public boolean equals(Object var1) {
            if(this == var1) {
                return true;
            } else if(!(var1 instanceof org.teavm.classlib.java.security.TProvider.ServiceKey)) {
                return false;
            } else {
                org.teavm.classlib.java.security.TProvider.ServiceKey var2 = (org.teavm.classlib.java.security.TProvider.ServiceKey)var1;
                return this.type.equals(var2.type) && this.algorithm.equals(var2.algorithm);
            }
        }

        boolean matches(TString var1, TString var2) {
            return this.type == var1 && this.originalAlgorithm == var2;
        }
    }

    private static class UString {
        final TString string;
        final TString lowerString;

        UString(TString var1) {
            this.string = var1;
            this.lowerString = var1.toLowerCase(TLocale.ENGLISH);
        }

        public int hashCode() {
            return this.lowerString.hashCode();
        }

        public boolean equals(Object var1) {
            if(this == var1) {
                return true;
            } else if(!(var1 instanceof org.teavm.classlib.java.security.TProvider.UString)) {
                return false;
            } else {
                org.teavm.classlib.java.security.TProvider.UString var2 = (org.teavm.classlib.java.security.TProvider.UString)var1;
                return this.lowerString.equals(var2.lowerString);
            }
        }

        public String toString() {
            return this.string;
        }
    }
}
