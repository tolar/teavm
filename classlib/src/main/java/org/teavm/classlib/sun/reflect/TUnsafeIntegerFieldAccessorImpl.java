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

import org.teavm.classlib.java.lang.reflect.TField;

/**
 * Created by vasek on 4. 7. 2016.
 */
class TUnsafeIntegerFieldAccessorImpl extends TUnsafeFieldAccessorImpl {
    TUnsafeIntegerFieldAccessorImpl(TField var1) {
        super(var1);
    }

    public Object get(Object var1) throws IllegalArgumentException {
        return new Integer(this.getInt(var1));
    }

    public boolean getBoolean(Object var1) throws IllegalArgumentException {
        throw this.newGetBooleanIllegalArgumentException();
    }

    public byte getByte(Object var1) throws IllegalArgumentException {
        throw this.newGetByteIllegalArgumentException();
    }

    public char getChar(Object var1) throws IllegalArgumentException {
        throw this.newGetCharIllegalArgumentException();
    }

    public short getShort(Object var1) throws IllegalArgumentException {
        throw this.newGetShortIllegalArgumentException();
    }

    public int getInt(Object var1) throws IllegalArgumentException {
        this.ensureObj(var1);
        return T_UNSAFE.getInt(var1, this.fieldOffset);
    }

    public long getLong(Object var1) throws IllegalArgumentException {
        return (long)this.getInt(var1);
    }

    public float getFloat(Object var1) throws IllegalArgumentException {
        return (float)this.getInt(var1);
    }

    public double getDouble(Object var1) throws IllegalArgumentException {
        return (double)this.getInt(var1);
    }

    public void set(Object var1, Object var2) throws IllegalArgumentException, IllegalAccessException {
        this.ensureObj(var1);
        if(this.isFinal) {
            this.throwFinalFieldIllegalAccessException(var2);
        }

        if(var2 == null) {
            this.throwSetIllegalArgumentException(var2);
        }

        if(var2 instanceof Byte) {
            T_UNSAFE.putInt(var1, this.fieldOffset, ((Byte)var2).byteValue());
        } else if(var2 instanceof Short) {
            T_UNSAFE.putInt(var1, this.fieldOffset, ((Short)var2).shortValue());
        } else if(var2 instanceof Character) {
            T_UNSAFE.putInt(var1, this.fieldOffset, ((Character)var2).charValue());
        } else if(var2 instanceof Integer) {
            T_UNSAFE.putInt(var1, this.fieldOffset, ((Integer)var2).intValue());
        } else {
            this.throwSetIllegalArgumentException(var2);
        }
    }

    public void setBoolean(Object var1, boolean var2) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(var2);
    }

    public void setByte(Object var1, byte var2) throws IllegalArgumentException, IllegalAccessException {
        this.setInt(var1, var2);
    }

    public void setChar(Object var1, char var2) throws IllegalArgumentException, IllegalAccessException {
        this.setInt(var1, var2);
    }

    public void setShort(Object var1, short var2) throws IllegalArgumentException, IllegalAccessException {
        this.setInt(var1, var2);
    }

    public void setInt(Object var1, int var2) throws IllegalArgumentException, IllegalAccessException {
        this.ensureObj(var1);
        if(this.isFinal) {
            this.throwFinalFieldIllegalAccessException(var2);
        }

        T_UNSAFE.putInt(var1, this.fieldOffset, var2);
    }

    public void setLong(Object var1, long var2) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(var2);
    }

    public void setFloat(Object var1, float var2) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(var2);
    }

    public void setDouble(Object var1, double var2) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(var2);
    }
}
