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

import java.lang.reflect.Field;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.sun.misc.THexDumpEncoder;

class TUnparseableExtension extends TExtension {
    private String name = "";
    private Throwable why;

    public TUnparseableExtension(TExtension var1, Throwable var2) {
        super(var1);

        try {
            TClass var3 = TOIDMap.getClass(var1.getExtensionId());
            if(var3 != null) {
                Field var4 = var3.getDeclaredField("NAME");
                this.name = (String)((String)var4.get((Object)null)) + " ";
            }
        } catch (Exception var5) {
            ;
        }

        this.why = var2;
    }

    public String toString() {
        return super.toString() + "Unparseable " + this.name + "extension due to\n" + this.why + "\n\n" + (new THexDumpEncoder()).encodeBuffer(this.getExtensionValue());
    }
}
