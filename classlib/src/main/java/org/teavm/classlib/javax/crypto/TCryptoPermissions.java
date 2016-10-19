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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import org.teavm.classlib.java.io.TBufferedReader;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.io.TInputStreamReader;
import org.teavm.classlib.java.lang.TClassNotFoundException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TPermission;
import org.teavm.classlib.java.security.TPermissionCollection;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.java.util.concurrent.TConcurrentHashMap;

final class TCryptoPermissions extends TPermissionCollection implements Serializable {
    private static final long serialVersionUID = 4946547168093391015L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("perms", Hashtable.class)};
    private transient TConcurrentHashMap<TString, TPermissionCollection> perms = new TConcurrentHashMap(7);

    TCryptoPermissions() {
    }

    void load(TInputStream var1) throws TIOException, TCryptoPolicyParser.ParsingException {
        TCryptoPolicyParser var2 = new TCryptoPolicyParser();
        var2.read(new TBufferedReader(new TInputStreamReader(var1, TString.wrap("UTF-8"))));
        TCryptoPermission[] var3 = var2.getPermissions();

        for(int var4 = 0; var4 < var3.length; ++var4) {
            this.add(var3[var4]);
        }

    }

    boolean isEmpty() {
        return this.perms.isEmpty();
    }

    public void add(TPermission var1) {
        if(this.isReadOnly()) {
            throw new SecurityException("Attempt to add a Permission to a readonly CryptoPermissions object");
        } else if(var1 instanceof TCryptoPermission) {
            TCryptoPermission var2 = (TCryptoPermission)var1;
            TPermissionCollection var3 = this.getPermissionCollection(var2);
            var3.add(var2);
            TString var4 = var2.getAlgorithm();
            this.perms.putIfAbsent(var4, var3);
        }
    }

    public boolean implies(TPermission var1) {
        if(!(var1 instanceof TCryptoPermission)) {
            return false;
        } else {
            TCryptoPermission var2 = (TCryptoPermission)var1;
            TPermissionCollection var3 = this.getPermissionCollection(var2.getAlgorithm());
            return var3.implies(var2);
        }
    }

    public TEnumeration<TPermission> elements() {
        return new TPermissionsEnumerator(this.perms.elements());
    }

    TCryptoPermissions getMinimum(TCryptoPermissions var1) {
        if(var1 == null) {
            return null;
        } else if(this.perms.containsKey("CryptoAllPermission")) {
            return var1;
        } else if(var1.perms.containsKey("CryptoAllPermission")) {
            return this;
        } else {
            TCryptoPermissions var2 = new TCryptoPermissions();
            TPermissionCollection var3 = (TPermissionCollection)var1.perms.get("*");
            int var4 = 0;
            if(var3 != null) {
                var4 = ((TCryptoPermission)var3.elements().nextElement()).getMaxKeySize();
            }

            TEnumeration var5 = this.perms.keys();

            while(true) {
                TCryptoPermission[] var9;
                while(true) {
                    if(!var5.hasMoreElements()) {
                        TPermissionCollection var12 = (TPermissionCollection)this.perms.get("*");
                        if(var12 == null) {
                            return var2;
                        }

                        var4 = ((TCryptoPermission)var12.elements().nextElement()).getMaxKeySize();
                        TEnumeration var13 = var1.perms.keys();

                        while(true) {
                            String var14;
                            do {
                                if(!var13.hasMoreElements()) {
                                    return var2;
                                }

                                var14 = (String)var13.nextElement();
                            } while(this.perms.containsKey(var14));

                            TPermissionCollection var15 = (TPermissionCollection)var1.perms.get(var14);
                            TCryptoPermission[] var16 = this.getMinimum(var4, var15);

                            for(int var11 = 0; var11 < var16.length; ++var11) {
                                var2.add(var16[var11]);
                            }
                        }
                    }

                    String var6 = (String)var5.nextElement();
                    TPermissionCollection var7 = (TPermissionCollection)this.perms.get(var6);
                    TPermissionCollection var8 = (TPermissionCollection)var1.perms.get(var6);
                    if(var8 == null) {
                        if(var3 == null) {
                            continue;
                        }

                        var9 = this.getMinimum(var4, var7);
                        break;
                    }

                    var9 = this.getMinimum(var7, var8);
                    break;
                }

                for(int var10 = 0; var10 < var9.length; ++var10) {
                    var2.add(var9[var10]);
                }
            }
        }
    }

    private TCryptoPermission[] getMinimum(TPermissionCollection var1, TPermissionCollection var2) {
        Vector var3 = new Vector(2);
        TEnumeration var4 = var1.elements();

        while(true) {
            while(var4.hasMoreElements()) {
                TCryptoPermission var5 = (TCryptoPermission)var4.nextElement();
                TEnumeration var6 = var2.elements();

                while(var6.hasMoreElements()) {
                    TCryptoPermission var7 = (TCryptoPermission)var6.nextElement();
                    if(var7.implies(var5)) {
                        var3.addElement(var5);
                        break;
                    }

                    if(var5.implies(var7)) {
                        var3.addElement(var7);
                    }
                }
            }

            TCryptoPermission[] var8 = new TCryptoPermission[var3.size()];
            var3.copyInto(var8);
            return var8;
        }
    }

    private TCryptoPermission[] getMinimum(int var1, TPermissionCollection var2) {
        Vector var3 = new Vector(1);
        TEnumeration var4 = var2.elements();

        while(var4.hasMoreElements()) {
            TCryptoPermission var5 = (TCryptoPermission)var4.nextElement();
            if(var5.getMaxKeySize() <= var1) {
                var3.addElement(var5);
            } else if(var5.getCheckParam()) {
                var3.addElement(new TCryptoPermission(var5.getAlgorithm(), var1, var5.getAlgorithmParameterSpec(), var5.getExemptionMechanism()));
            } else {
                var3.addElement(new TCryptoPermission(var5.getAlgorithm(), var1, var5.getExemptionMechanism()));
            }
        }

        TCryptoPermission[] var6 = new TCryptoPermission[var3.size()];
        var3.copyInto(var6);
        return var6;
    }

    TPermissionCollection getPermissionCollection(TString var1) {
        TPermissionCollection var2 = (TPermissionCollection)this.perms.get("CryptoAllPermission");
        if(var2 == null) {
            var2 = (TPermissionCollection)this.perms.get(var1);
            if(var2 == null) {
                var2 = (TPermissionCollection)this.perms.get("*");
            }
        }

        return var2;
    }

    private TPermissionCollection getPermissionCollection(TCryptoPermission var1) {
        TString var2 = var1.getAlgorithm();
        TPermissionCollection var3 = (TPermissionCollection)this.perms.get(var2);
        if(var3 == null) {
            var3 = var1.newPermissionCollection();
        }

        return var3;
    }

    private void readObject(ObjectInputStream var1) throws TIOException, TClassNotFoundException {
        ObjectInputStream.GetField var2 = var1.readFields();
        Hashtable var3 = (Hashtable)((Hashtable)var2.get("perms", (Object)null));
        if(var3 != null) {
            this.perms = new TConcurrentHashMap(var3);
        } else {
            this.perms = new TConcurrentHashMap();
        }

    }

    private void writeObject(ObjectOutputStream var1) throws IOException {
        Hashtable var2 = new Hashtable(this.perms);
        ObjectOutputStream.PutField var3 = var1.putFields();
        var3.put("perms", var2);
        var1.writeFields();
    }
}
