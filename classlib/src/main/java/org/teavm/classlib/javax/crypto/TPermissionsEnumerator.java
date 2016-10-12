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

import java.util.NoSuchElementException;

import org.teavm.classlib.java.security.TPermission;
import org.teavm.classlib.java.security.TPermissionCollection;
import org.teavm.classlib.java.util.TEnumeration;

final class TPermissionsEnumerator implements TEnumeration<TPermission> {
    private TEnumeration<TPermissionCollection> perms;
    private TEnumeration<TPermission> permset;

    TPermissionsEnumerator(TEnumeration<TPermissionCollection> var1) {
        this.perms = var1;
        this.permset = this.getNextEnumWithMore();
    }

    public synchronized boolean hasMoreElements() {
        if(this.permset == null) {
            return false;
        } else if(this.permset.hasMoreElements()) {
            return true;
        } else {
            this.permset = this.getNextEnumWithMore();
            return this.permset != null;
        }
    }

    public synchronized TPermission nextElement() {
        if(this.hasMoreElements()) {
            return (TPermission)this.permset.nextElement();
        } else {
            throw new NoSuchElementException("PermissionsEnumerator");
        }
    }

    private TEnumeration<TPermission> getNextEnumWithMore() {
        while(true) {
            if(this.perms.hasMoreElements()) {
                TPermissionCollection var1 = (TPermissionCollection)this.perms.nextElement();
                TEnumeration var2 = var1.elements();
                if(!var2.hasMoreElements()) {
                    continue;
                }

                return var2;
            }

            return null;
        }
    }
}
