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


/**
 * Created by vasek on 4. 7. 2016.
 */
class ByteVectorImpl implements TByteVector {
    private byte[] data;
    private int pos;

    public ByteVectorImpl() {
        this(100);
    }

    public ByteVectorImpl(int var1) {
        this.data = new byte[var1];
        this.pos = -1;
    }

    public int getLength() {
        return this.pos + 1;
    }

    public byte get(int var1) {
        if(var1 >= this.data.length) {
            this.resize(var1);
            this.pos = var1;
        }

        return this.data[var1];
    }

    public void put(int var1, byte var2) {
        if(var1 >= this.data.length) {
            this.resize(var1);
            this.pos = var1;
        }

        this.data[var1] = var2;
    }

    public void add(byte var1) {
        if(++this.pos >= this.data.length) {
            this.resize(this.pos);
        }

        this.data[this.pos] = var1;
    }

    public void trim() {
        if(this.pos != this.data.length - 1) {
            byte[] var1 = new byte[this.pos + 1];
            System.arraycopy(this.data, 0, var1, 0, this.pos + 1);
            this.data = var1;
        }

    }

    public byte[] getData() {
        return this.data;
    }

    private void resize(int var1) {
        if(var1 <= 2 * this.data.length) {
            var1 = 2 * this.data.length;
        }

        byte[] var2 = new byte[var1];
        System.arraycopy(this.data, 0, var2, 0, this.data.length);
        this.data = var2;
    }
}
