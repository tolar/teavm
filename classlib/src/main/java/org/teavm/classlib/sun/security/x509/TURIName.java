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
import org.teavm.classlib.java.net.TURI;
import org.teavm.classlib.java.net.TURISyntaxException;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;

public class TURIName implements TGeneralNameInterface {
    private TURI uri;
    private TString host;
    private TDNSName hostDNS;
    private TIPAddressName hostIP;

    public TURIName(TDerValue var1) throws TIOException {
        this(var1.getIA5String());
    }

    public TURIName(TString var1) throws TIOException {
        try {
            this.uri = new TURI(var1);
        } catch (TURISyntaxException var7) {
            throw new TIOException(TString.wrap("invalid URI name:" + var1), var7);
        }

        if(this.uri.getScheme() == null) {
            throw new TIOException(TString.wrap("URI name must include scheme:" + var1));
        } else {
            this.host = this.uri.getHost();
            if(this.host != null) {
                if(this.host.charAt(0) == 91) {
                    TString var2 = this.host.substring(1, this.host.length() - 1);

                    try {
                        this.hostIP = new TIPAddressName(var2);
                    } catch (IOException var6) {
                        throw new TIOException(TString.wrap("invalid URI name (host portion is not a valid IPv6 address):" + var1));
                    }
                } else {
                    try {
                        this.hostDNS = new TDNSName(this.host);
                    } catch (TIOException var5) {
                        try {
                            this.hostIP = new TIPAddressName(this.host);
                        } catch (Exception var4) {
                            throw new TIOException(TString.wrap("invalid URI name (host portion is not a valid DNS name, IPv4 address, or IPv6 address):" + var1));
                        }
                    }
                }
            }

        }
    }

    public static TURIName nameConstraint(TDerValue var0) throws TIOException {
        TString var2 = var0.getIA5String();

        TURI var1;
        try {
            var1 = new TURI(var2);
        } catch (TURISyntaxException var6) {
            throw new TIOException(TString.wrap("invalid URI name constraint:" + var2), var6);
        }

        if(var1.getScheme() == null) {
            TString var3 = var1.getSchemeSpecificPart();

            try {
                TDNSName var4;
                if(var3.startsWith(TString.wrap("."))) {
                    var4 = new TDNSName(var3.substring(1));
                } else {
                    var4 = new TDNSName(var3);
                }

                return new TURIName(var1, var3, var4);
            } catch (TIOException var5) {
                throw new TIOException(TString.wrap("invalid URI name constraint:" + var2), var5);
            }
        } else {
            throw new TIOException(TString.wrap("invalid URI name constraint (should not include scheme):" + var2));
        }
    }

    TURIName(TURI var1, TString var2, TDNSName var3) {
        this.uri = var1;
        this.host = var2;
        this.hostDNS = var3;
    }

    public int getType() {
        return 6;
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        var1.putIA5String(this.uri.toASCIIString());
    }

    public String toString() {
        return "URIName: " + this.uri.toString();
    }

    public boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof TURIName)) {
            return false;
        } else {
            TURIName var2 = (TURIName)var1;
            return this.uri.equals(var2.getURI());
        }
    }

    public TURI getURI() {
        return this.uri;
    }

    public String getName() {
        return this.uri.toString();
    }

    public TString getScheme() {
        return this.uri.getScheme();
    }

    public TString getHost() {
        return this.host;
    }

    public Object getHostObject() {
        return this.hostIP != null?this.hostIP:this.hostDNS;
    }

    public int hashCode() {
        return this.uri.hashCode();
    }

    public int constrains(TGeneralNameInterface var1) throws UnsupportedOperationException {
        int var2;
        if(var1 == null) {
            var2 = -1;
        } else if(var1.getType() != 6) {
            var2 = -1;
        } else {
            TString var3 = ((TURIName)var1).getHost();
            if(var3.equalsIgnoreCase(this.host)) {
                var2 = 0;
            } else {
                Object var4 = ((TURIName)var1).getHostObject();
                if(this.hostDNS != null && var4 instanceof TDNSName) {
                    boolean var5 = this.host.charAt(0) == 46;
                    boolean var6 = var3.charAt(0) == 46;
                    TDNSName var7 = (TDNSName)var4;
                    var2 = this.hostDNS.constrains(var7);
                    if(!var5 && !var6 && (var2 == 2 || var2 == 1)) {
                        var2 = 3;
                    }

                    if(var5 != var6 && var2 == 0) {
                        if(var5) {
                            var2 = 2;
                        } else {
                            var2 = 1;
                        }
                    }
                } else {
                    var2 = 3;
                }
            }
        }

        return var2;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        TDNSName var1 = null;

        try {
            var1 = new TDNSName(this.host);
        } catch (TIOException var3) {
            throw new UnsupportedOperationException(var3.getMessage());
        }

        return var1.subtreeDepth();
    }
}
