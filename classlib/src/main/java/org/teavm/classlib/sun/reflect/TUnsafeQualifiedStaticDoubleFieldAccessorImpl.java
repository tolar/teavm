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
class TUnsafeQualifiedStaticDoubleFieldAccessorImpl extends TUnsafeQualifiedStaticFieldAccessorImpl {
    TUnsafeQualifiedStaticDoubleFieldAccessorImpl(TField var1, boolean var2) {
        super(var1, var2);
    }

    public Object get(Object var1) throws IllegalArgumentException {
        return new Double(this.getDouble(var1));
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
        throw this.newGetIntIllegalArgumentException();
    }

    public long getLong(Object var1) throws IllegalArgumentException {
        throw this.newGetLongIllegalArgumentException();
    }

    public float getFloat(Object var1) throws IllegalArgumentException {
        throw this.newGetFloatIllegalArgumentException();
    }

    public double getDouble(Object var1) throws IllegalArgumentException {
        return T_UNSAFE.getDoubleVolatile(this.base, this.fieldOffset);
    }

    public void set(Object var1, Object var2) throws IllegalArgumentException, IllegalAccessException {
        if(this.isReadOnly) {
            this.throwFinalFieldIllegalAccessException(var2);
        }

        if(var2 == null) {
            this.throwSetIllegalArgumentException(var2);
        }

        if(var2 instanceof Byte) {
            T_UNSAFE.putDoubleVolatile(this.base, this.fieldOffset, (double)((Byte)var2).byteValue());
        } else if(var2 instanceof Short) {
            T_UNSAFE.putDoubleVolatile(this.base, this.fieldOffset, (double)((Short)var2).shortValue());
        } else if(var2 instanceof Character) {
            T_UNSAFE.putDoubleVolatile(this.base, this.fieldOffset, (double)((Character)var2).charValue());
        } else if(var2 instanceof Integer) {
            T_UNSAFE.putDoubleVolatile(this.base, this.fieldOffset, (double)((Integer)var2).intValue());
        } else if(var2 instanceof Long) {
            T_UNSAFE.putDoubleVolatile(this.base, this.fieldOffset, (double)((Long)var2).longValue());
        } else if(var2 instanceof Float) {
            T_UNSAFE.putDoubleVolatile(this.base, this.fieldOffset, (double)((Float)var2).floatValue());
        } else if(var2 instanceof Double) {
            T_UNSAFE.putDoubleVolatile(this.base, this.fieldOffset, ((Double)var2).doubleValue());
        } else {
            this.throwSetIllegalArgumentException(var2);
        }
    }

    public void setBoolean(Object var1, boolean var2) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(var2);
    }

    public void setByte(Object var1, byte var2) throws IllegalArgumentException, IllegalAccessException {
        this.setDouble(var1, (double)var2);
    }

    public void setChar(Object var1, char var2) throws IllegalArgumentException, IllegalAccessException {
        this.setDouble(var1, (double)var2);
    }

    public void setShort(Object var1, short var2) throws IllegalArgumentException, IllegalAccessException {
        this.setDouble(var1, (double)var2);
    }

    public void setInt(Object var1, int var2) throws IllegalArgumentException, IllegalAccessException {
        this.setDouble(var1, (double)var2);
    }

    public void setLong(Object var1, long var2) throws IllegalArgumentException, IllegalAccessException {
        this.setDouble(var1, (double)var2);
    }

    public void setFloat(Object var1, float var2) throws IllegalArgumentException, IllegalAccessException {
        this.setDouble(var1, (double)var2);
    }

    public void setDouble(Object var1, double var2) throws IllegalArgumentException, IllegalAccessException {
        if(this.isReadOnly) {
            this.throwFinalFieldIllegalAccessException(var2);
        }

        T_UNSAFE.putDoubleVolatile(this.base, this.fieldOffset, var2);
    }
}