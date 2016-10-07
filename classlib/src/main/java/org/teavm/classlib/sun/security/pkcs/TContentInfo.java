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
package org.teavm.classlib.sun.security.pkcs;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.util.TObjectIdentifier;

public class TContentInfo {
    private static int[] pkcs7 = new int[]{1, 2, 840, 113549, 1, 7};
    private static int[] data = new int[]{1, 2, 840, 113549, 1, 7, 1};
    private static int[] sdata = new int[]{1, 2, 840, 113549, 1, 7, 2};
    private static int[] edata = new int[]{1, 2, 840, 113549, 1, 7, 3};
    private static int[] sedata = new int[]{1, 2, 840, 113549, 1, 7, 4};
    private static int[] ddata = new int[]{1, 2, 840, 113549, 1, 7, 5};
    private static int[] crdata = new int[]{1, 2, 840, 113549, 1, 7, 6};
    private static int[] nsdata = new int[]{2, 16, 840, 1, 113730, 2, 5};
    private static int[] tstInfo = new int[]{1, 2, 840, 113549, 1, 9, 16, 1, 4};
    private static final int[] OLD_SDATA = new int[]{1, 2, 840, 1113549, 1, 7, 2};
    private static final int[] OLD_DATA = new int[]{1, 2, 840, 1113549, 1, 7, 1};
    public static TObjectIdentifier PKCS7_OID;
    public static TObjectIdentifier DATA_OID;
    public static TObjectIdentifier SIGNED_DATA_OID;
    public static TObjectIdentifier ENVELOPED_DATA_OID;
    public static TObjectIdentifier SIGNED_AND_ENVELOPED_DATA_OID;
    public static TObjectIdentifier DIGESTED_DATA_OID;
    public static TObjectIdentifier ENCRYPTED_DATA_OID;
    public static TObjectIdentifier OLD_SIGNED_DATA_OID;
    public static TObjectIdentifier OLD_DATA_OID;
    public static TObjectIdentifier NETSCAPE_CERT_SEQUENCE_OID;
    public static TObjectIdentifier TIMESTAMP_TOKEN_INFO_OID;
    TObjectIdentifier contentType;
    TDerValue content;

    public TContentInfo(TObjectIdentifier var1, TDerValue var2) {
        this.contentType = var1;
        this.content = var2;
    }

    public TContentInfo(byte[] var1) {
        TDerValue var2 = new TDerValue((byte)4, var1);
        this.contentType = DATA_OID;
        this.content = var2;
    }

    public TContentInfo(TDerInputStream var1) throws TIOException, TParsingException {
        this(var1, false);
    }

    public TContentInfo(TDerInputStream var1, boolean var2) throws TIOException, TParsingException {
        TDerValue[] var7 = var1.getSequence(2);
        TDerValue var5 = var7[0];
        TDerInputStream var3 = new TDerInputStream(var5.toByteArray());
        this.contentType = var3.getOID();
        if(var2) {
            this.content = var7[1];
        } else if(var7.length > 1) {
            TDerValue var6 = var7[1];
            TDerInputStream var4 = new TDerInputStream(var6.toByteArray());
            TDerValue[] var8 = var4.getSet(1, true);
            this.content = var8[0];
        }

    }

    public TDerValue getContent() {
        return this.content;
    }

    public TObjectIdentifier getContentType() {
        return this.contentType;
    }

    public byte[] getData() throws TIOException {
        if(!this.contentType.equals(DATA_OID) && !this.contentType.equals(OLD_DATA_OID) && !this.contentType.equals(TIMESTAMP_TOKEN_INFO_OID)) {
            throw new TIOException(TString.wrap("content type is not DATA: " + this.contentType));
        } else {
            return this.content == null?null:this.content.getOctetString();
        }
    }

    public void encode(TDerOutputStream var1) throws TIOException {
        TDerOutputStream var3 = new TDerOutputStream();
        var3.putOID(this.contentType);
        if(this.content != null) {
            TDerValue var4 = null;
            TDerOutputStream var2 = new TDerOutputStream();
            this.content.encode(var2);
            var4 = new TDerValue((byte)-96, var2.toByteArray());
            var3.putDerValue(var4);
        }

        var1.write((byte)48, var3);
    }

    public byte[] getContentBytes() throws TIOException {
        if(this.content == null) {
            return null;
        } else {
            TDerInputStream var1 = new TDerInputStream(this.content.toByteArray());
            return var1.getOctetString();
        }
    }

    public String toString() {
        String var1 = "";
        var1 = var1 + "Content Info Sequence\n\tContent type: " + this.contentType + "\n";
        var1 = var1 + "\tContent: " + this.content;
        return var1;
    }

    static {
        PKCS7_OID = TObjectIdentifier.newInternal(pkcs7);
        DATA_OID = TObjectIdentifier.newInternal(data);
        SIGNED_DATA_OID = TObjectIdentifier.newInternal(sdata);
        ENVELOPED_DATA_OID = TObjectIdentifier.newInternal(edata);
        SIGNED_AND_ENVELOPED_DATA_OID = TObjectIdentifier.newInternal(sedata);
        DIGESTED_DATA_OID = TObjectIdentifier.newInternal(ddata);
        ENCRYPTED_DATA_OID = TObjectIdentifier.newInternal(crdata);
        OLD_SIGNED_DATA_OID = TObjectIdentifier.newInternal(OLD_SDATA);
        OLD_DATA_OID = TObjectIdentifier.newInternal(OLD_DATA);
        NETSCAPE_CERT_SEQUENCE_OID = TObjectIdentifier.newInternal(nsdata);
        TIMESTAMP_TOKEN_INFO_OID = TObjectIdentifier.newInternal(tstInfo);
    }
}
