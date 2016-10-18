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

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TGeneralName {
    private TGeneralNameInterface name;

    public TGeneralName(TGeneralNameInterface var1) {
        this.name = null;
        if(var1 == null) {
            throw new NullPointerException("GeneralName must not be null");
        } else {
            this.name = var1;
        }
    }

    public TGeneralName(TDerValue var1) throws TIOException {
        this(var1, false);
    }

    public TGeneralName(TDerValue var1, boolean var2) throws TIOException {
        this.name = null;
        short var3 = (short)((byte)(var1.tag & 31));
        switch(var3) {
            case 0:
                if(!var1.isContextSpecific() || !var1.isConstructed()) {
                    throw new TIOException(TString.wrap("Invalid encoding of Other-Name"));
                }

                var1.resetTag((byte) 48);
                this.name = new TOtherName(var1);
                break;
            case 1:
                if(var1.isContextSpecific() && !var1.isConstructed()) {
                    var1.resetTag((byte) 22);
                    this.name = new TRFC822Name(var1);
                    break;
                }

                throw new TIOException(TString.wrap("Invalid encoding of RFC822 name"));
            case 2:
                if(var1.isContextSpecific() && !var1.isConstructed()) {
                    var1.resetTag((byte) 22);
                    this.name = new TDNSName(var1);
                    break;
                }

                throw new TIOException(TString.wrap("Invalid encoding of DNS name"));
            case 3:
            default:
                throw new TIOException(TString.wrap("Unrecognized GeneralName tag, (" + var3 + ")"));
            case 4:
                if(!var1.isContextSpecific() || !var1.isConstructed()) {
                    throw new TIOException(TString.wrap("Invalid encoding of Directory name"));
                }

                this.name = new TX500Name(var1.getData());
                break;
            case 5:
                if(!var1.isContextSpecific() || !var1.isConstructed()) {
                    throw new TIOException(TString.wrap("Invalid encoding of EDI name"));
                }

                var1.resetTag((byte) 48);
                this.name = new TEDIPartyName(var1);
                break;
            case 6:
                if(!var1.isContextSpecific() || var1.isConstructed()) {
                    throw new TIOException(TString.wrap("Invalid encoding of URI"));
                }

                var1.resetTag((byte) 22);
                this.name = var2? TURIName.nameConstraint(var1):new TURIName(var1);
                break;
            case 7:
                if(var1.isContextSpecific() && !var1.isConstructed()) {
                    var1.resetTag((byte) 4);
                    this.name = new TIPAddressName(var1);
                    break;
                }

                throw new TIOException(TString.wrap("Invalid encoding of IP address"));
            case 8:
                if(!var1.isContextSpecific() || var1.isConstructed()) {
                    throw new TIOException(TString.wrap("Invalid encoding of OID name"));
                }

                var1.resetTag((byte) 6);
                this.name = new TOIDName(var1);
        }

    }

    public int getType() {
        return this.name.getType();
    }

    public TGeneralNameInterface getName() {
        return this.name;
    }

    public String toString() {
        return this.name.toString();
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TGeneralName)) {
            return false;
        } else {
            TGeneralNameInterface var2 = ((TGeneralName)var1).name;

            try {
                return this.name.constrains(var2) == 0;
            } catch (UnsupportedOperationException var4) {
                return false;
            }
        }
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerOutputStream var2 = new TDerOutputStream();
        this.name.encode(var2);
        int var3 = this.name.getType();
        if(var3 != 0 && var3 != 3 && var3 != 5) {
            if(var3 == 4) {
                var1.write(TDerValue.createTag((byte) -128, true, (byte)var3), var2);
            } else {
                var1.writeImplicit(TDerValue.createTag((byte) -128, false, (byte)var3), var2);
            }
        } else {
            var1.writeImplicit(TDerValue.createTag((byte) -128, true, (byte)var3), var2);
        }

    }
}
