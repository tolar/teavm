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

import java.lang.reflect.InvocationTargetException;
import java.security.cert.CRLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TOutputStream;
import org.teavm.classlib.java.lang.TBoolean;
import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TObject;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.lang.reflect.TConstructor;
import org.teavm.classlib.java.security.cert.TCRLException;
import org.teavm.classlib.java.security.cert.TCertificateException;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TCRLExtensions {
    private Map<String, TExtension> map = Collections.synchronizedMap(new TreeMap());
    private boolean unsupportedCritExt = false;
    private static final TClass[] PARAMS = new TClass[]{TBoolean.TYPE, TObject.class};

    public TCRLExtensions() {
    }

    public TCRLExtensions(TDerInputStream var1) throws TCRLException {
        this.init(var1);
    }

    private void init(TDerInputStream var1) throws TCRLException {
        try {
            TDerInputStream var2 = var1;
            byte var3 = (byte)var1.peekByte();
            if((var3 & 192) == 128 && (var3 & 31) == 0) {
                TDerValue var4 = var1.getDerValue();
                var2 = var4.data;
            }

            TDerValue[] var8 = var2.getSequence(5);

            for(int var5 = 0; var5 < var8.length; ++var5) {
                TExtension var6 = new TExtension(var8[var5]);
                this.parseExtension(var6);
            }

        } catch (TIOException var7) {
            throw new TCRLException(TString.wrap("Parsing error: " + var7.toString()));
        }
    }

    private void parseExtension(TExtension var1) throws TCRLException {
        try {
            TClass var2 = TOIDMap.getClass(var1.getExtensionId());
            if(var2 == null) {
                if(var1.isCritical()) {
                    this.unsupportedCritExt = true;
                }

                if(this.map.put(var1.getExtensionId().toString(), var1) != null) {
                    throw new CRLException("Duplicate extensions not allowed");
                }
            } else {
                TConstructor var3 = var2.getConstructor(PARAMS);
                Object[] var4 = new Object[]{Boolean.valueOf(var1.isCritical()), var1.getExtensionValue()};
                TCertAttrSet var5 = (TCertAttrSet)var3.newInstance(var4);
                if(this.map.put(var5.getName(), (TExtension)var5) != null) {
                    throw new CRLException("Duplicate extensions not allowed");
                }
            }
        } catch (InvocationTargetException var6) {
            throw new TCRLException(TString.wrap(var6.getTargetException().getMessage()));
        } catch (Exception var7) {
            throw new TCRLException(TString.wrap(var7.toString()));
        }
    }

    public void encode(TOutputStream var1, boolean var2) throws TCRLException {
        try {
            TDerOutputStream var3 = new TDerOutputStream();
            Collection var4 = this.map.values();
            Object[] var5 = var4.toArray();

            for(int var6 = 0; var6 < var5.length; ++var6) {
                if(var5[var6] instanceof TCertAttrSet) {
                    ((TCertAttrSet)var5[var6]).encode(var3);
                } else {
                    if(!(var5[var6] instanceof TExtension)) {
                        throw new TCRLException(TString.wrap("Illegal extension object"));
                    }

                    ((TExtension)var5[var6]).encode(var3);
                }
            }

            TDerOutputStream var10 = new TDerOutputStream();
            var10.write((byte)48, var3);
            TDerOutputStream var7 = new TDerOutputStream();
            if(var2) {
                var7.write(TDerValue.createTag((byte)-128, true, (byte)0), var10);
            } else {
                var7 = var10;
            }

            var1.write(var7.toByteArray());
        } catch (TIOException var8) {
            throw new TCRLException(TString.wrap("Encoding error: " + var8.toString()));
        } catch (TCertificateException var9) {
            throw new TCRLException(TString.wrap("Encoding error: " + var9.toString()));
        }
    }

    public TExtension get(TString var1) {
        TX509AttributeName var2 = new TX509AttributeName(var1);
        TString var4 = var2.getPrefix();
        TString var3;
        if(var4.equalsIgnoreCase(TString.wrap("x509"))) {
            int var5 = var1.lastIndexOf(TString.wrap("."));
            var3 = var1.substring(var5 + 1);
        } else {
            var3 = var1;
        }

        return (TExtension)this.map.get(var3);
    }

    public void set(String var1, Object var2) {
        this.map.put(var1, (TExtension)var2);
    }

    public void delete(String var1) {
        this.map.remove(var1);
    }

    public Enumeration<TExtension> getElements() {
        return Collections.enumeration(this.map.values());
    }

    public Collection<TExtension> getAllExtensions() {
        return this.map.values();
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.unsupportedCritExt;
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TCRLExtensions)) {
            return false;
        } else {
            Collection var2 = ((TCRLExtensions)var1).getAllExtensions();
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

                return true;
            }
        }
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    public String toString() {
        return this.map.toString();
    }
}
