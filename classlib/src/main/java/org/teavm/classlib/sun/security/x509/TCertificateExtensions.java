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
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.teavm.classlib.java.util.TCollections;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.java.util.TMap;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TCertificateExtensions implements TCertAttrSet<TExtension> {
    public static final String IDENT = "x509.info.extensions";
    public static final String NAME = "extensions";
    private TMap<String, TExtension> map = TCollections.synchronizedMap(new TreeMap());
    private boolean unsupportedCritExt = false;
    private TMap<String, TExtension> unparseableExtensions;
    private static Class[] PARAMS = new Class[]{Boolean.class, Object.class};

    public TCertificateExtensions() {
    }

    public TCertificateExtensions(TDerInputStream var1) throws IOException {
        this.init(var1);
    }

    private void init(TDerInputStream var1) throws IOException {
        TDerValue[] var2 = var1.getSequence(5);

        for(int var3 = 0; var3 < var2.length; ++var3) {
            TExtension var4 = new TExtension(var2[var3]);
            this.parseExtension(var4);
        }

    }

    private void parseExtension(TExtension var1) throws IOException {
        try {
            Class var2 = TOIDMap.getClass(var1.getExtensionId());
            if(var2 == null) {
                if(var1.isCritical()) {
                    this.unsupportedCritExt = true;
                }

                if(this.map.put(var1.getExtensionId().toString(), var1) != null) {
                    throw new IOException("Duplicate extensions not allowed");
                }
            } else {
                Constructor var9 = var2.getConstructor(PARAMS);
                Object[] var10 = new Object[]{Boolean.valueOf(var1.isCritical()), var1.getExtensionValue()};
                TCertAttrSet var5 = (TCertAttrSet)var9.newInstance(var10);
                if(this.map.put(var5.getName(), (TExtension)var5) != null) {
                    throw new IOException("Duplicate extensions not allowed");
                }
            }
        } catch (InvocationTargetException var6) {
            Throwable var3 = var6.getTargetException();
            if(!var1.isCritical()) {
                if(this.unparseableExtensions == null) {
                    this.unparseableExtensions = new TreeMap();
                }

                this.unparseableExtensions.put(var1.getExtensionId().toString(), new UnparseableExtension(var1, var3));

            } else if(var3 instanceof IOException) {
                throw (IOException)var3;
            } else {
                throw new IOException(var3);
            }
        } catch (IOException var7) {
            throw var7;
        } catch (Exception var8) {
            throw new IOException(var8);
        }
    }

    public void encode(OutputStream var1) throws CertificateException, IOException {
        this.encode(var1, false);
    }

    public void encode(OutputStream var1, boolean var2) throws CertificateException, IOException {
        TDerOutputStream var3 = new TDerOutputStream();
        Collection var4 = this.map.values();
        Object[] var5 = var4.toArray();

        for(int var6 = 0; var6 < var5.length; ++var6) {
            if(var5[var6] instanceof TCertAttrSet) {
                ((TCertAttrSet)var5[var6]).encode(var3);
            } else {
                if(!(var5[var6] instanceof TExtension)) {
                    throw new CertificateException("Illegal extension object");
                }

                ((TExtension)var5[var6]).encode(var3);
            }
        }

        TDerOutputStream var8 = new TDerOutputStream();
        var8.write(48, var3);
        TDerOutputStream var7;
        if(!var2) {
            var7 = new TDerOutputStream();
            var7.write(TDerValue.createTag(-128, true, 3), var8);
        } else {
            var7 = var8;
        }

        var1.write(var7.toByteArray());
    }

    public void set(String var1, Object var2) throws IOException {
        if(var2 instanceof TExtension) {
            this.map.put(var1, (TExtension)var2);
        } else {
            throw new IOException("Unknown extension type.");
        }
    }

    public TExtension get(String var1) throws IOException {
        TExtension var2 = (TExtension)this.map.get(var1);
        if(var2 == null) {
            throw new IOException("No extension found with name " + var1);
        } else {
            return var2;
        }
    }

    TExtension getExtension(String var1) {
        return (TExtension)this.map.get(var1);
    }

    public void delete(String var1) throws IOException {
        Object var2 = this.map.get(var1);
        if(var2 == null) {
            throw new IOException("No extension found with name " + var1);
        } else {
            this.map.remove(var1);
        }
    }

    public String getNameByOid(TObjectIdentifier var1) throws IOException {
        Iterator var2 = this.map.keySet().iterator();

        String var3;
        do {
            if(!var2.hasNext()) {
                return null;
            }

            var3 = (String)var2.next();
        } while(!((TExtension)this.map.get(var3)).getExtensionId().equals(var1));

        return var3;
    }

    public TEnumeration<TExtension> getElements() {
        return TCollections.enumeration(this.map.values());
    }

    public Collection<TExtension> getAllExtensions() {
        return this.map.values();
    }

    public Map<String, TExtension> getUnparseableExtensions() {
        return this.unparseableExtensions == null?Collections.emptyMap():this.unparseableExtensions;
    }

    public String getName() {
        return "extensions";
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.unsupportedCritExt;
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof sun.security.x509.CertificateExtensions)) {
            return false;
        } else {
            Collection var2 = ((sun.security.x509.CertificateExtensions)var1).getAllExtensions();
            Object[] var3 = var2.toArray();
            int var4 = var3.length;
            if(var4 != this.map.size()) {
                return false;
            } else {
                String var7 = null;

                for(int var8 = 0; var8 < var4; ++var8) {
                    if(var3[var8] instanceof CertAttrSet) {
                        var7 = ((CertAttrSet)var3[var8]).getName();
                    }

                    Extension var5 = (Extension)var3[var8];
                    if(var7 == null) {
                        var7 = var5.getExtensionId().toString();
                    }

                    Extension var6 = (Extension)this.map.get(var7);
                    if(var6 == null) {
                        return false;
                    }

                    if(!var6.equals(var5)) {
                        return false;
                    }
                }

                return this.getUnparseableExtensions().equals(((sun.security.x509.CertificateExtensions)var1).getUnparseableExtensions());
            }
        }
    }

    public int hashCode() {
        return this.map.hashCode() + this.getUnparseableExtensions().hashCode();
    }

    public String toString() {
        return this.map.toString();
    }
}
