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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TGeneralSubtrees implements Cloneable {
    private final List<TGeneralSubtree> trees;
    private static final int NAME_DIFF_TYPE = -1;
    private static final int NAME_MATCH = 0;
    private static final int NAME_NARROWS = 1;
    private static final int NAME_WIDENS = 2;
    private static final int NAME_SAME_TYPE = 3;

    public TGeneralSubtrees() {
        this.trees = new ArrayList();
    }

    private TGeneralSubtrees(TGeneralSubtrees var1) {
        this.trees = new ArrayList(var1.trees);
    }

    public TGeneralSubtrees(TDerValue var1) throws TIOException {
        this();
        if(var1.tag != 48) {
            throw new TIOException(TString.wrap("Invalid encoding of GeneralSubtrees."));
        } else {
            while(var1.data.available() != 0) {
                TDerValue var2 = var1.data.getDerValue();
                TGeneralSubtree var3 = new TGeneralSubtree(var2);
                this.add(var3);
            }

        }
    }

    public TGeneralSubtree get(int var1) {
        return (TGeneralSubtree)this.trees.get(var1);
    }

    public void remove(int var1) {
        this.trees.remove(var1);
    }

    public void add(TGeneralSubtree var1) {
        if(var1 == null) {
            throw new NullPointerException();
        } else {
            this.trees.add(var1);
        }
    }

    public boolean contains(TGeneralSubtree var1) {
        if(var1 == null) {
            throw new NullPointerException();
        } else {
            return this.trees.contains(var1);
        }
    }

    public int size() {
        return this.trees.size();
    }

    public Iterator<TGeneralSubtree> iterator() {
        return this.trees.iterator();
    }

    public List<TGeneralSubtree> trees() {
        return this.trees;
    }

    public Object clone() {
        return new TGeneralSubtrees(this);
    }

    public String toString() {
        String var1 = "   GeneralSubtrees:\n" + this.trees.toString() + "\n";
        return var1;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        int var3 = 0;

        for(int var4 = this.size(); var3 < var4; ++var3) {
            this.get(var3).encode(var2);
        }

        var1.write((byte) 48, var2);
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TGeneralSubtrees)) {
            return false;
        } else {
            TGeneralSubtrees var2 = (TGeneralSubtrees)var1;
            return this.trees.equals(var2.trees);
        }
    }

    public int hashCode() {
        return this.trees.hashCode();
    }

    private TGeneralNameInterface getGeneralNameInterface(int var1) {
        return getGeneralNameInterface(this.get(var1));
    }

    private static TGeneralNameInterface getGeneralNameInterface(TGeneralSubtree var0) {
        TGeneralName var1 = var0.getName();
        TGeneralNameInterface var2 = var1.getName();
        return var2;
    }

    private void minimize() {
        for(int var1 = 0; var1 < this.size() - 1; ++var1) {
            TGeneralNameInterface var2 = this.getGeneralNameInterface(var1);
            boolean var3 = false;
            int var4 = var1 + 1;

            label30:
            while(var4 < this.size()) {
                TGeneralNameInterface var5 = this.getGeneralNameInterface(var4);
                switch(var2.constrains(var5)) {
                    case 0:
                        var3 = true;
                        break label30;
                    case 1:
                        this.remove(var4);
                        --var4;
                    case -1:
                    case 3:
                        ++var4;
                        break;
                    case 2:
                        var3 = true;
                    default:
                        break label30;
                }
            }

            if(var3) {
                this.remove(var1);
                --var1;
            }
        }

    }

    private TGeneralSubtree createWidestSubtree(TGeneralNameInterface var1) {
        try {
            TGeneralName var2;
            switch(var1.getType()) {
                case 0:
                    TObjectIdentifier var3 = ((TOtherName)var1).getOID();
                    var2 = new TGeneralName(new TOtherName(var3, (byte[])null));
                    break;
                case 1:
                    var2 = new TGeneralName(new TRFC822Name(TString.wrap("")));
                    break;
                case 2:
                    var2 = new TGeneralName(new TDNSName(TString.wrap("")));
                    break;
                case 3:
                    var2 = new TGeneralName(new TX400Address((byte[])null));
                    break;
                case 4:
                    var2 = new TGeneralName(new TX500Name(TString.wrap("")));
                    break;
                case 5:
                    var2 = new TGeneralName(new TEDIPartyName(TString.wrap("")));
                    break;
                case 6:
                    var2 = new TGeneralName(new TURIName(TString.wrap("")));
                    break;
                case 7:
                    var2 = new TGeneralName(new TIPAddressName((byte[])null));
                    break;
                case 8:
                    var2 = new TGeneralName(new TOIDName(new TObjectIdentifier((int[])null)));
                    break;
                default:
                    throw new IOException("Unsupported GeneralNameInterface type: " + var1.getType());
            }

            return new TGeneralSubtree(var2, 0, -1);
        } catch (IOException var4) {
            throw new RuntimeException("Unexpected error: " + var4, var4);
        }
    }

    public TGeneralSubtrees intersect(TGeneralSubtrees var1) {
        if(var1 == null) {
            throw new NullPointerException("other GeneralSubtrees must not be null");
        } else {
            TGeneralSubtrees var2 = new TGeneralSubtrees();
            TGeneralSubtrees var3 = null;
            if(this.size() == 0) {
                this.union(var1);
                return null;
            } else {
                this.minimize();
                var1.minimize();

                int var4;
                boolean var7;
                int var8;
                for(var4 = 0; var4 < this.size(); ++var4) {
                    TGeneralNameInterface var5 = this.getGeneralNameInterface(var4);
                    boolean var6 = false;
                    var7 = false;
                    var8 = 0;

                    TGeneralSubtree var9;
                    TGeneralNameInterface var10;
                    label82:
                    while(var8 < var1.size()) {
                        var9 = var1.get(var8);
                        var10 = getGeneralNameInterface(var9);
                        switch(var5.constrains(var10)) {
                            case 0:
                            case 2:
                                var7 = false;
                                break label82;
                            case 1:
                                this.remove(var4);
                                --var4;
                                var2.add(var9);
                                var7 = false;
                                break label82;
                            case 3:
                                var7 = true;
                            case -1:
                            default:
                                ++var8;
                        }
                    }

                    if(var7) {
                        boolean var16 = false;

                        for(int var17 = 0; var17 < this.size(); ++var17) {
                            var10 = this.getGeneralNameInterface(var17);
                            if(var10.getType() == var5.getType()) {
                                for(int var11 = 0; var11 < var1.size(); ++var11) {
                                    TGeneralNameInterface var12 = var1.getGeneralNameInterface(var11);
                                    int var13 = var10.constrains(var12);
                                    if(var13 == 0 || var13 == 2 || var13 == 1) {
                                        var16 = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if(!var16) {
                            if(var3 == null) {
                                var3 = new TGeneralSubtrees();
                            }

                            var9 = this.createWidestSubtree(var5);
                            if(!var3.contains(var9)) {
                                var3.add(var9);
                            }
                        }

                        this.remove(var4);
                        --var4;
                    }
                }

                if(var2.size() > 0) {
                    this.union(var2);
                }

                for(var4 = 0; var4 < var1.size(); ++var4) {
                    TGeneralSubtree var14 = var1.get(var4);
                    TGeneralNameInterface var15 = getGeneralNameInterface(var14);
                    var7 = false;
                    var8 = 0;

                    label71:
                    while(var8 < this.size()) {
                        TGeneralNameInterface var18 = this.getGeneralNameInterface(var8);
                        switch(var18.constrains(var15)) {
                            case -1:
                                var7 = true;
                            default:
                                ++var8;
                                break;
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                                var7 = false;
                                break label71;
                        }
                    }

                    if(var7) {
                        this.add(var14);
                    }
                }

                return var3;
            }
        }
    }

    public void union(TGeneralSubtrees var1) {
        if(var1 != null) {
            int var2 = 0;

            for(int var3 = var1.size(); var2 < var3; ++var2) {
                this.add(var1.get(var2));
            }

            this.minimize();
        }

    }

    public void reduce(TGeneralSubtrees var1) {
        if(var1 != null) {
            int var2 = 0;

            for(int var3 = var1.size(); var2 < var3; ++var2) {
                TGeneralNameInterface var4 = var1.getGeneralNameInterface(var2);

                for(int var5 = 0; var5 < this.size(); ++var5) {
                    TGeneralNameInterface var6 = this.getGeneralNameInterface(var5);
                    switch(var4.constrains(var6)) {
                        case -1:
                        case 2:
                        case 3:
                        default:
                            break;
                        case 0:
                            this.remove(var5);
                            --var5;
                            break;
                        case 1:
                            this.remove(var5);
                            --var5;
                    }
                }
            }

        }
    }
}
