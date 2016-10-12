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

import java.io.Serializable;

import org.teavm.classlib.java.security.TPermission;
import org.teavm.classlib.java.security.TPermissionCollection;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.java.util.TVector;

final class TCryptoAllPermissionCollection extends TPermissionCollection implements Serializable {
    private static final long serialVersionUID = 7450076868380144072L;
    private boolean all_allowed = false;

    TCryptoAllPermissionCollection() {
    }

    public void add(TPermission var1) {
        if(this.isReadOnly()) {
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
        } else if(var1 == TCryptoAllPermission.INSTANCE) {
            this.all_allowed = true;
        }
    }

    public boolean implies(TPermission var1) {
        return !(var1 instanceof TCryptoPermission)?false:this.all_allowed;
    }

    public TEnumeration<TPermission> elements() {
        TVector var1 = new TVector(1);
        if(this.all_allowed) {
            var1.add(TCryptoAllPermission.INSTANCE);
        }

        return var1.elements();
    }
}
