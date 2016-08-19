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

import org.teavm.classlib.sun.security.util.TDerOutputStream;

import sun.security.util.DerValue;
import sun.security.x509.DNSName;
import sun.security.x509.EDIPartyName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.IPAddressName;
import sun.security.x509.OIDName;
import sun.security.x509.RFC822Name;
import sun.security.x509.URIName;
import sun.security.x509.X500Name;

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

    public TGeneralName(DerValue var1) throws IOException {
        this(var1, false);
    }

    public TGeneralName(DerValue var1, boolean var2) throws IOException {
        this.name = null;
        short var3 = (short)((byte)(var1.tag & 31));
        switch(var3) {
            case 0:
                if(!var1.isContextSpecific() || !var1.isConstructed()) {
                    throw new IOException("Invalid encoding of Other-Name");
                }

                var1.resetTag((byte) 48);
                this.name = new TOtherName(var1);
                break;
            case 1:
                if(var1.isContextSpecific() && !var1.isConstructed()) {
                    var1.resetTag(22);
                    this.name = new RFC822Name(var1);
                    break;
                }

                throw new IOException("Invalid encoding of RFC822 name");
            case 2:
                if(var1.isContextSpecific() && !var1.isConstructed()) {
                    var1.resetTag(22);
                    this.name = new DNSName(var1);
                    break;
                }

                throw new IOException("Invalid encoding of DNS name");
            case 3:
            default:
                throw new IOException("Unrecognized GeneralName tag, (" + var3 + ")");
            case 4:
                if(!var1.isContextSpecific() || !var1.isConstructed()) {
                    throw new IOException("Invalid encoding of Directory name");
                }

                this.name = new X500Name(var1.getData());
                break;
            case 5:
                if(!var1.isContextSpecific() || !var1.isConstructed()) {
                    throw new IOException("Invalid encoding of EDI name");
                }

                var1.resetTag(48);
                this.name = new EDIPartyName(var1);
                break;
            case 6:
                if(!var1.isContextSpecific() || var1.isConstructed()) {
                    throw new IOException("Invalid encoding of URI");
                }

                var1.resetTag(22);
                this.name = var2? URIName.nameConstraint(var1):new URIName(var1);
                break;
            case 7:
                if(var1.isContextSpecific() && !var1.isConstructed()) {
                    var1.resetTag(4);
                    this.name = new IPAddressName(var1);
                    break;
                }

                throw new IOException("Invalid encoding of IP address");
            case 8:
                if(!var1.isContextSpecific() || var1.isConstructed()) {
                    throw new IOException("Invalid encoding of OID name");
                }

                var1.resetTag(6);
                this.name = new OIDName(var1);
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
        } else if(!(var1 instanceof sun.security.x509.GeneralName)) {
            return false;
        } else {
            GeneralNameInterface var2 = ((sun.security.x509.GeneralName)var1).name;

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

    public void encode(TDerOutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();
        this.name.encode(var2);
        int var3 = this.name.getType();
        if(var3 != 0 && var3 != 3 && var3 != 5) {
            if(var3 == 4) {
                var1.write(DerValue.createTag((byte) -128, true, (byte)var3), var2);
            } else {
                var1.writeImplicit(DerValue.createTag((byte) -128, false, (byte)var3), var2);
            }
        } else {
            var1.writeImplicit(DerValue.createTag((byte) -128, true, (byte)var3), var2);
        }

    }
}
