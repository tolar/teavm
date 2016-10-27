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
package org.teavm.classlib.sun.security.cert;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.teavm.classlib.sun.security.util.TDerInputStream;
import org.teavm.classlib.sun.security.util.TDerOutputStream;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.x509.TCertificatePolicyId;

public class TCertificatePolicySet {
    private final Vector<TCertificatePolicyId> ids;

    public TCertificatePolicySet(Vector<TCertificatePolicyId> var1) {
        this.ids = var1;
    }

    public TCertificatePolicySet(TDerInputStream var1) throws IOException {
        this.ids = new Vector();
        TDerValue[] var2 = var1.getSequence(5);

        for(int var3 = 0; var3 < var2.length; ++var3) {
            TCertificatePolicyId var4 = new TCertificatePolicyId(var2[var3]);
            this.ids.addElement(var4);
        }

    }

    public String toString() {
        String var1 = "CertificatePolicySet:[\n" + this.ids.toString() + "]\n";
        return var1;
    }

    public void encode(TDerOutputStream var1) throws IOException {
        TDerOutputStream var2 = new TDerOutputStream();

        for(int var3 = 0; var3 < this.ids.size(); ++var3) {
            ((TCertificatePolicyId)this.ids.elementAt(var3)).encode(var2);
        }

        var1.write((byte)48, var2);
    }

    public List<TCertificatePolicyId> getCertPolicyIds() {
        return Collections.unmodifiableList(this.ids);
    }
}
