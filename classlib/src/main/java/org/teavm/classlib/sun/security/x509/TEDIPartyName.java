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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TEDIPartyName implements TGeneralNameInterface {
    private static final byte TAG_ASSIGNER = 0;
    private static final byte TAG_PARTYNAME = 1;
    private TString assigner = null;
    private TString party = null;
    private int myhash = -1;

    public TEDIPartyName(TString var1, TString var2) {
        this.assigner = var1;
        this.party = var2;
    }

    public TEDIPartyName(TString var1) {
        this.party = var1;
    }

    public TEDIPartyName(TDerValue var1) throws IOException {
        TDerInputStream var2 = new TDerInputStream(var1.toByteArray());
        TDerValue[] var3 = var2.getSequence(2);
        int var4 = var3.length;
        if(var4 >= 1 && var4 <= 2) {
            for(int var5 = 0; var5 < var4; ++var5) {
                TDerValue var6 = var3[var5];
                if(var6.isContextSpecific((byte) 0) && !var6.isConstructed()) {
                    if(this.assigner != null) {
                        throw new IOException("Duplicate nameAssigner found in EDIPartyName");
                    }

                    var6 = var6.data.getDerValue();
                    this.assigner = var6.getAsString();
                }

                if(var6.isContextSpecific((byte) 1) && !var6.isConstructed()) {
                    if(this.party != null) {
                        throw new IOException("Duplicate partyName found in EDIPartyName");
                    }

                    var6 = var6.data.getDerValue();
                    this.party = var6.getAsString();
                }
            }

        } else {
            throw new IOException("Invalid encoding of EDIPartyName");
        }
    }

    public int getType() {
        return 5;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        TDerOutputStream var3 = new TDerOutputStream();
        if(this.assigner != null) {
            TDerOutputStream var4 = new TDerOutputStream();
            var4.putPrintableString(this.assigner);
            var2.write(TDerValue.createTag((byte)-128, false, (byte) 0), var4);
        }

        if(this.party == null) {
            throw new TIOException(TString.wrap("Cannot have null partyName"));
        } else {
            var3.putPrintableString(this.party);
            var2.write(TDerValue.createTag((byte)-128, false, (byte) 1), var3);
            var1.write((byte) 48, var2);
        }
    }

    public TString getAssignerName() {
        return this.assigner;
    }

    public TString getPartyName() {
        return this.party;
    }

    public boolean equals(Object var1) {
        if(!(var1 instanceof TEDIPartyName)) {
            return false;
        } else {
            TString var2 = ((TEDIPartyName)var1).assigner;
            if(this.assigner == null) {
                if(var2 != null) {
                    return false;
                }
            } else if(!this.assigner.equals(var2)) {
                return false;
            }

            TString var3 = ((TEDIPartyName)var1).party;
            if(this.party == null) {
                if(var3 != null) {
                    return false;
                }
            } else if(!this.party.equals(var3)) {
                return false;
            }

            return true;
        }
    }

    public int hashCode() {
        if(this.myhash == -1) {
            this.myhash = 37 + (this.party == null?1:this.party.hashCode());
            if(this.assigner != null) {
                this.myhash = 37 * this.myhash + this.assigner.hashCode();
            }
        }

        return this.myhash;
    }

    public String toString() {
        return "EDIPartyName: " + (this.assigner == null?"":"  nameAssigner = " + this.assigner + ",") + "  partyName = " + this.party;
    }

    public int constrains(TGeneralNameInterface var1) throws UnsupportedOperationException {
        byte var2;
        if(var1 == null) {
            var2 = -1;
        } else {
            if(var1.getType() == 5) {
                throw new UnsupportedOperationException("Narrowing, widening, and matching of names not supported for EDIPartyName");
            }

            var2 = -1;
        }

        return var2;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth() not supported for EDIPartyName");
    }
}
