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
package org.teavm.classlib.java.io;

import java.io.IOException;

public interface TObjectOutput extends TDataOutput, AutoCloseable {
    /**
     * Write an object to the underlying storage or stream.  The
     * class that implements this interface defines how the object is
     * written.
     *
     * @param obj the object to be written
     * @exception IOException Any of the usual Input/Output related exceptions.
     */
    public void writeObject(Object obj)
            throws IOException;

    /**
     * Writes a byte. This method will block until the byte is actually
     * written.
     * @param b the byte
     * @exception IOException If an I/O error has occurred.
     */
    public void write(int b) throws TIOException;

    /**
     * Writes an array of bytes. This method will block until the bytes
     * are actually written.
     * @param b the data to be written
     * @exception IOException If an I/O error has occurred.
     */
    public void write(byte b[]) throws TIOException;

    /**
     * Writes a sub array of bytes.
     * @param b the data to be written
     * @param off       the start offset in the data
     * @param len       the number of bytes that are written
     * @exception IOException If an I/O error has occurred.
     */
    public void write(byte b[], int off, int len) throws TIOException;

    /**
     * Flushes the stream. This will write any buffered
     * output bytes.
     * @exception IOException If an I/O error has occurred.
     */
    public void flush() throws TIOException;

    /**
     * Closes the stream. This method must be called
     * to release any resources associated with the
     * stream.
     * @exception IOException If an I/O error has occurred.
     */
    public void close() throws TIOException;
}
