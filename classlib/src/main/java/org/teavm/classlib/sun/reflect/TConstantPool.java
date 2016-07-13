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

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.reflect.TField;
import org.teavm.classlib.java.lang.reflect.TMember;

/**
 * Created by vasek on 9. 7. 2016.
 */
public class TConstantPool {
    private Object constantPoolOop;

    public TConstantPool() {
    }

    public int getSize() {
        return this.getSize0(this.constantPoolOop);
    }

    public TClass<?> getClassAt(int var1) {
        return this.getClassAt0(this.constantPoolOop, var1);
    }

    public TClass<?> getClassAtIfLoaded(int var1) {
        return this.getClassAtIfLoaded0(this.constantPoolOop, var1);
    }

    public TMember getMethodAt(int var1) {
        return this.getMethodAt0(this.constantPoolOop, var1);
    }

    public TMember getMethodAtIfLoaded(int var1) {
        return this.getMethodAtIfLoaded0(this.constantPoolOop, var1);
    }

    public TField getFieldAt(int var1) {
        return this.getFieldAt0(this.constantPoolOop, var1);
    }

    public TField getFieldAtIfLoaded(int var1) {
        return this.getFieldAtIfLoaded0(this.constantPoolOop, var1);
    }

    public String[] getMemberRefInfoAt(int var1) {
        return this.getMemberRefInfoAt0(this.constantPoolOop, var1);
    }

    public int getIntAt(int var1) {
        return this.getIntAt0(this.constantPoolOop, var1);
    }

    public long getLongAt(int var1) {
        return this.getLongAt0(this.constantPoolOop, var1);
    }

    public float getFloatAt(int var1) {
        return this.getFloatAt0(this.constantPoolOop, var1);
    }

    public double getDoubleAt(int var1) {
        return this.getDoubleAt0(this.constantPoolOop, var1);
    }

    public String getStringAt(int var1) {
        return this.getStringAt0(this.constantPoolOop, var1);
    }

    public String getUTF8At(int var1) {
        return this.getUTF8At0(this.constantPoolOop, var1);
    }

    private native int getSize0(Object var1);

    private native TClass<?> getClassAt0(Object var1, int var2);

    private native TClass<?> getClassAtIfLoaded0(Object var1, int var2);

    private native TMember getMethodAt0(Object var1, int var2);

    private native TMember getMethodAtIfLoaded0(Object var1, int var2);

    private native TField getFieldAt0(Object var1, int var2);

    private native TField getFieldAtIfLoaded0(Object var1, int var2);

    private native String[] getMemberRefInfoAt0(Object var1, int var2);

    private native int getIntAt0(Object var1, int var2);

    private native long getLongAt0(Object var1, int var2);

    private native float getFloatAt0(Object var1, int var2);

    private native double getDoubleAt0(Object var1, int var2);

    private native String getStringAt0(Object var1, int var2);

    private native String getUTF8At0(Object var1, int var2);

    static {
        TReflection.registerFieldsToFilter(TConstantPool.class, new String[]{"constantPoolOop"});
    }
}
