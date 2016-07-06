/*
 *  Copyright 2016 vasek.
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
package org.teavm.classlib.sun.reflect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vasek on 6. 7. 2016.
 */
class TLabel {
    private List<org.teavm.classlib.sun.reflect.TLabel.PatchInfo> patches = new ArrayList();

    public TLabel() {
    }

    void add(TClassFileAssembler var1, short var2, short var3, int var4) {
        this.patches.add(new org.teavm.classlib.sun.reflect.TLabel.PatchInfo(var1, var2, var3, var4));
    }

    public void bind() {
        Iterator var1 = this.patches.iterator();

        while(var1.hasNext()) {
            org.teavm.classlib.sun.reflect.TLabel.PatchInfo var2 = (org.teavm.classlib.sun.reflect.TLabel.PatchInfo)var1.next();
            short var3 = var2.asm.getLength();
            short var4 = (short)(var3 - var2.instrBCI);
            var2.asm.emitShort(var2.patchBCI, var4);
            var2.asm.setStack(var2.stackDepth);
        }

    }

    static class PatchInfo {
        final TClassFileAssembler asm;
        final short instrBCI;
        final short patchBCI;
        final int stackDepth;

        PatchInfo(TClassFileAssembler var1, short var2, short var3, int var4) {
            this.asm = var1;
            this.instrBCI = var2;
            this.patchBCI = var3;
            this.stackDepth = var4;
        }
    }
}
