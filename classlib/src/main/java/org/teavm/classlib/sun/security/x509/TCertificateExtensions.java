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
import java.util.Map;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.java.util.TCollection;
import org.teavm.classlib.java.util.TCollections;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.java.util.TIterator;
import org.teavm.classlib.java.util.TMap;
import org.teavm.classlib.java.util.TTreeMap;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TCertificateExtensions implements TCertAttrSet<TExtension> {
    public static final String IDENT = "x509.info.extensions";
    public static final String NAME = "extensions";
    private TMap<TString, TExtension> map = TCollections.synchronizedMap(new TTreeMap());
    private boolean unsupportedCritExt = false;
    private TMap<TString, TExtension> unparseableExtensions;
    //private static TClass[] PARAMS = new TClass[]{TBoolean.class, TObject.class};

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
            TClass var2 = TOIDMap.getClass(var1.getExtensionId());
            if(var2 == null) {
                if(var1.isCritical()) {
                    this.unsupportedCritExt = true;
                }

                if(this.map.put(TString.wrap(var1.getExtensionId().toString()), var1) != null) {
                    throw new IOException("Duplicate extensions not allowed");
                }
            } else {
//                Constructor var9 = var2.getConstructor(PARAMS);
//                Object[] var10 = new Object[]{Boolean.valueOf(var1.isCritical()), var1.getExtensionValue()};
//                TCertAttrSet var5 = (TCertAttrSet)var9.newInstance(var10);
//                if(this.map.put(var5.getName(), (TExtension)var5) != null) {
//                    throw new IOException("Duplicate extensions not allowed");
//                }
            }
//        } catch (InvocationTargetException var6) {
//            Throwable var3 = var6.getTargetException();
//            if(!var1.isCritical()) {
//                if(this.unparseableExtensions == null) {
//                    this.unparseableExtensions = new TTreeMap();
//                }
//
//                this.unparseableExtensions.put(var1.getExtensionId().toString(), new TUnparseableExtension(var1, var3));
//
//            } else if(var3 instanceof IOException) {
//                throw (IOException)var3;
//            } else {
//                throw new IOException(var3);
//            }
        } catch (IOException var7) {
            throw var7;
        } catch (Exception var8) {
            throw new IOException(var8);
        }
    }

    public void encode(TOutputStream var1) throws TCertificateException, TIOException {
        this.encode(var1, false);
    }

    public void encode(TOutputStream var1, boolean var2) throws TCertificateException, TIOException {
        TDerOutputStream var3 = new TDerOutputStream();
        TCollection var4 = this.map.values();
        Object[] var5 = var4.toArray();

        for(int var6 = 0; var6 < var5.length; ++var6) {
            if(var5[var6] instanceof TCertAttrSet) {
                ((TCertAttrSet)var5[var6]).encode(var3);
            } else {
                if(!(var5[var6] instanceof TExtension)) {
                    throw new TCertificateException(TString.wrap("Illegal extension object"));
                }

                ((TExtension)var5[var6]).encode(var3);
            }
        }

        TDerOutputStream var8 = new TDerOutputStream();
        var8.write((byte) 48, var3);
        TDerOutputStream var7;
        if(!var2) {
            var7 = new TDerOutputStream();
            var7.write(TDerValue.createTag((byte)-128, true, (byte) 3), var8);
        } else {
            var7 = var8;
        }

        var1.write(var7.toByteArray());
    }

    public void set(TString var1, Object var2) throws TIOException {
        if(var2 instanceof TExtension) {
            this.map.put(var1, (TExtension)var2);
        } else {
            throw new TIOException(TString.wrap("Unknown extension type."));
        }
    }

    public TExtension get(TString var1) throws TIOException {
        TExtension var2 = (TExtension)this.map.get(var1);
        if(var2 == null) {
            throw new TIOException(TString.wrap("No extension found with name " + var1));
        } else {
            return var2;
        }
    }

    TExtension getExtension(String var1) {
        return (TExtension)this.map.get(var1);
    }

    public void delete(TString var1) throws TIOException {
        Object var2 = this.map.get(var1);
        if(var2 == null) {
            throw new TIOException(TString.wrap("No extension found with name " + var1));
        } else {
            this.map.remove(var1);
        }
    }

    public String getNameByOid(TObjectIdentifier var1) throws IOException {
        TIterator var2 = this.map.keySet().iterator();

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

    public TCollection<TExtension> getAllExtensions() {
        return this.map.values();
    }

    public Map<String, TExtension> getUnparseableExtensions() {
        //return this.unparseableExtensions == null?TCollections.emptyMap():this.unparseableExtensions;
        return null;
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
        } else if(!(var1 instanceof TCertificateExtensions)) {
            return false;
        } else {
            TCollection var2 = ((TCertificateExtensions)var1).getAllExtensions();
            Object[] var3 = var2.toArray();
            int var4 = var3.length;
            if(var4 != this.map.size()) {
                return false;
            } else {
                String var7 = null;

                for(int var8 = 0; var8 < var4; ++var8) {
                    if(var3[var8] instanceof TCertAttrSet) {
                        var7 = ((TCertAttrSet)var3[var8]).getName();
                    }

                    TExtension var5 = (TExtension)var3[var8];
                    if(var7 == null) {
                        var7 = var5.getExtensionId().toString();
                    }

                    TExtension var6 = (TExtension)this.map.get(var7);
                    if(var6 == null) {
                        return false;
                    }

                    if(!var6.equals(var5)) {
                        return false;
                    }
                }

                return this.getUnparseableExtensions().equals(((TCertificateExtensions)var1).getUnparseableExtensions());
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
