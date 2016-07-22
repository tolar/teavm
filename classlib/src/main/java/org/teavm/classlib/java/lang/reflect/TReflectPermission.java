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
package org.teavm.classlib.java.lang.reflect;

import org.teavm.classlib.java.security.TBasicPermission;
import org.teavm.classlib.java.security.TPermission;

public class TReflectPermission extends TBasicPermission {
    public TReflectPermission(String suppressAccessChecks) {
        super(suppressAccessChecks);
    }

    @Override
    public boolean implies(TPermission permission) {
        return false;
    }

    @Override
    public String getActions() {
        return null;
    }
}
