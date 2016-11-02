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

import static org.teavm.classlib.java.io.TObjectStreamClass.processQueue;
import java.io.IOException;
import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TEnum;
import org.teavm.classlib.java.lang.TObject;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.lang.ref.TReferenceQueue;
import org.teavm.classlib.java.security.TAccessController;
import org.teavm.classlib.java.security.TPrivilegedAction;
import org.teavm.classlib.java.util.TArrayList;
import org.teavm.classlib.java.util.TArrays;
import org.teavm.classlib.java.util.TList;
import org.teavm.classlib.java.util.concurrent.TConcurrentHashMap;
import org.teavm.classlib.java.util.concurrent.TConcurrentMap;

public class TObjectOutputStream
        extends TOutputStream implements TObjectOutput, TObjectStreamConstants
{

    private static class Caches {
        /** cache of subclass security audit results */
        static final TConcurrentMap<TObjectStreamClass.WeakClassKey,Boolean> subclassAudits =
                new TConcurrentHashMap<>();

        /** queue for WeakReferences to audited subclasses */
        static final TReferenceQueue<Class<?>> subclassAuditsQueue =
                new TReferenceQueue<>();
    }

    /** filter stream for handling block data conversion */
    private final TObjectOutputStream.BlockDataOutputStream bout;
    /** obj -> wire handle map */
    private final TObjectOutputStream.HandleTable handles;
    /** obj -> replacement obj map */
    private final TObjectOutputStream.ReplaceTable subs;
    /** stream protocol version */
    private int protocol = PROTOCOL_VERSION_2;
    /** recursion depth */
    private int depth;

    /** buffer for writing primitive field values */
    private byte[] primVals;

    /** if true, invoke writeObjectOverride() instead of writeObject() */
    private final boolean enableOverride;
    /** if true, invoke replaceObject() */
    private boolean enableReplace;

    // values below valid only during upcalls to writeObject()/writeExternal()
    /**
     * Context during upcalls to class-defined writeObject methods; holds
     * object currently being serialized and descriptor for current class.
     * Null when not during writeObject upcall.
     */
    private TSerialCallbackContext curContext;
    /** current PutField object */
    private TObjectOutputStream.PutFieldImpl curPut;

    /** custom storage for debug trace info */
    private final TObjectOutputStream.DebugTraceInfoStack debugInfoStack;

    /**
     * value of "sun.io.serialization.extendedDebugInfo" property,
     * as true or false for extended information about exception's place
     */
    private static final boolean extendedDebugInfo =
            java.security.AccessController.doPrivileged(
                    new sun.security.action.GetBooleanAction(
                            "sun.io.serialization.extendedDebugInfo")).booleanValue();

    public TObjectOutputStream(TOutputStream out) throws TIOException {
        verifySubclass();
        bout = new TObjectOutputStream.BlockDataOutputStream(out);
        handles = new TObjectOutputStream.HandleTable(10, (float) 3.00);
        subs = new TObjectOutputStream.ReplaceTable(10, (float) 3.00);
        enableOverride = false;
        writeStreamHeader();
        bout.setBlockDataMode(true);
        if (extendedDebugInfo) {
            debugInfoStack = new TObjectOutputStream.DebugTraceInfoStack();
        } else {
            debugInfoStack = null;
        }
    }

    protected TObjectOutputStream() throws TIOException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        bout = null;
        handles = null;
        subs = null;
        enableOverride = true;
        debugInfoStack = null;
    }

    public void useProtocolVersion(int version) throws TIOException {
        if (handles.size() != 0) {
            // REMIND: implement better check for pristine stream?
            throw new IllegalStateException("stream non-empty");
        }
        switch (version) {
            case PROTOCOL_VERSION_1:
            case PROTOCOL_VERSION_2:
                protocol = version;
                break;

            default:
                throw new IllegalArgumentException(
                        "unknown version: " + version);
        }
    }

    public final void writeObject(TObject obj) throws TIOException {
        if (enableOverride) {
            writeObjectOverride(obj);
            return;
        }
        try {
            writeObject0(obj, false);
        } catch (TIOException ex) {
            if (depth == 0) {
                writeFatalException(ex);
            }
            throw ex;
        }
    }

    protected void writeObjectOverride(Object obj) throws TIOException {
    }

    public void writeUnshared(TObject obj) throws TIOException {
        try {
            writeObject0(obj, true);
        } catch (TIOException ex) {
            if (depth == 0) {
                writeFatalException(ex);
            }
            throw ex;
        }
    }

    public void defaultWriteObject() throws TIOException {
        TSerialCallbackContext ctx = curContext;
        if (ctx == null) {
            throw new TNotActiveException(TString.wrap("not in call to writeObject"));
        }
        TObject curObj = ctx.getObj();
        TObjectStreamClass curDesc = ctx.getDesc();
        bout.setBlockDataMode(false);
        defaultWriteFields(curObj, curDesc);
        bout.setBlockDataMode(true);
    }

    public TObjectOutputStream.PutField putFields() throws TIOException {
        if (curPut == null) {
            TSerialCallbackContext ctx = curContext;
            if (ctx == null) {
                throw new TNotActiveException(TString.wrap("not in call to writeObject"));
            }
            Object curObj = ctx.getObj();
            TObjectStreamClass curDesc = ctx.getDesc();
            curPut = new TObjectOutputStream.PutFieldImpl(curDesc);
        }
        return curPut;
    }

    public void writeFields() throws TIOException {
        if (curPut == null) {
            throw new TNotActiveException(TString.wrap("no current PutField object"));
        }
        bout.setBlockDataMode(false);
        curPut.writeFields();
        bout.setBlockDataMode(true);
    }


    public void reset() throws TIOException {
        if (depth != 0) {
            throw new TIOException(TString.wrap("stream active"));
        }
        bout.setBlockDataMode(false);
        bout.writeByte(TC_RESET);
        clear();
        bout.setBlockDataMode(true);
    }

    protected void annotateClass(Class<?> cl) throws TIOException {
    }

    protected void annotateProxyClass(Class<?> cl) throws TIOException {
    }

    protected TObject replaceObject(TObject obj) throws TIOException {
        return obj;
    }

    /**
     * Enable the stream to do replacement of objects in the stream.  When
     * enabled, the replaceObject method is called for every object being
     * serialized.
     *
     * <p>If <code>enable</code> is true, and there is a security manager
     * installed, this method first calls the security manager's
     * <code>checkPermission</code> method with a
     * <code>SerializablePermission("enableSubstitution")</code> permission to
     * ensure it's ok to enable the stream to do replacement of objects in the
     * stream.
     *
     * @param   enable boolean parameter to enable replacement of objects
     * @return  the previous setting before this method was invoked
     * @throws  SecurityException if a security manager exists and its
     *          <code>checkPermission</code> method denies enabling the stream
     *          to do replacement of objects in the stream.
     * @see SecurityManager#checkPermission
     * @see java.io.SerializablePermission
     */
    protected boolean enableReplaceObject(boolean enable)
            throws SecurityException
    {
        if (enable == enableReplace) {
            return enable;
        }
        if (enable) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        enableReplace = enable;
        return !enableReplace;
    }

    protected void writeStreamHeader() throws TIOException {
        bout.writeShort(STREAM_MAGIC);
        bout.writeShort(STREAM_VERSION);
    }

    protected void writeClassDescriptor(TObjectStreamClass desc)
            throws TIOException
    {
        desc.writeNonProxy(this);
    }

    public void write(int val) throws TIOException {
        bout.write(val);
    }

    public void write(byte[] buf) throws TIOException {
        bout.write(buf, 0, buf.length, false);
    }

    public void write(byte[] buf, int off, int len) throws TIOException {
        if (buf == null) {
            throw new NullPointerException();
        }
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        bout.write(buf, off, len, false);
    }

    public void flush() throws TIOException {
        bout.flush();
    }

    protected void drain() throws TIOException {
        bout.drain();
    }

    public void close() throws TIOException {
        flush();
        clear();
        bout.close();
    }

    public void writeBoolean(boolean val) throws TIOException {
        bout.writeBoolean(val);
    }

    public void writeByte(int val) throws TIOException  {
        bout.writeByte(val);
    }

    public void writeShort(int val)  throws TIOException {
        bout.writeShort(val);
    }

    public void writeChar(int val)  throws TIOException {
        bout.writeChar(val);
    }

    public void writeInt(int val)  throws TIOException {
        bout.writeInt(val);
    }

    public void writeLong(long val)  throws TIOException {
        bout.writeLong(val);
    }

    public void writeFloat(float val) throws TIOException {
        bout.writeFloat(val);
    }

    public void writeDouble(double val) throws TIOException {
        bout.writeDouble(val);
    }

    public void writeBytes(TString str) throws TIOException {
        bout.writeBytes(str);
    }

    public void writeChars(TString str) throws TIOException {
        bout.writeChars(str);
    }

    public void writeUTF(TString str) throws TIOException {
        bout.writeUTF(str);
    }

    /**
     * Provide programmatic access to the persistent fields to be written
     * to ObjectOutput.
     *
     * @since 1.2
     */
    public static abstract class PutField {

        /**
         * Put the value of the named boolean field into the persistent field.
         *
         * @param  name the name of the serializable field
         * @param  val the value to assign to the field
         * @throws IllegalArgumentException if <code>name</code> does not
         * match the name of a serializable field for the class whose fields
         * are being written, or if the type of the named field is not
         * <code>boolean</code>
         */
        public abstract void put(String name, boolean val);

        /**
         * Put the value of the named byte field into the persistent field.
         *
         * @param  name the name of the serializable field
         * @param  val the value to assign to the field
         * @throws IllegalArgumentException if <code>name</code> does not
         * match the name of a serializable field for the class whose fields
         * are being written, or if the type of the named field is not
         * <code>byte</code>
         */
        public abstract void put(String name, byte val);

        /**
         * Put the value of the named char field into the persistent field.
         *
         * @param  name the name of the serializable field
         * @param  val the value to assign to the field
         * @throws IllegalArgumentException if <code>name</code> does not
         * match the name of a serializable field for the class whose fields
         * are being written, or if the type of the named field is not
         * <code>char</code>
         */
        public abstract void put(String name, char val);

        /**
         * Put the value of the named short field into the persistent field.
         *
         * @param  name the name of the serializable field
         * @param  val the value to assign to the field
         * @throws IllegalArgumentException if <code>name</code> does not
         * match the name of a serializable field for the class whose fields
         * are being written, or if the type of the named field is not
         * <code>short</code>
         */
        public abstract void put(String name, short val);

        /**
         * Put the value of the named int field into the persistent field.
         *
         * @param  name the name of the serializable field
         * @param  val the value to assign to the field
         * @throws IllegalArgumentException if <code>name</code> does not
         * match the name of a serializable field for the class whose fields
         * are being written, or if the type of the named field is not
         * <code>int</code>
         */
        public abstract void put(String name, int val);

        /**
         * Put the value of the named long field into the persistent field.
         *
         * @param  name the name of the serializable field
         * @param  val the value to assign to the field
         * @throws IllegalArgumentException if <code>name</code> does not
         * match the name of a serializable field for the class whose fields
         * are being written, or if the type of the named field is not
         * <code>long</code>
         */
        public abstract void put(String name, long val);

        /**
         * Put the value of the named float field into the persistent field.
         *
         * @param  name the name of the serializable field
         * @param  val the value to assign to the field
         * @throws IllegalArgumentException if <code>name</code> does not
         * match the name of a serializable field for the class whose fields
         * are being written, or if the type of the named field is not
         * <code>float</code>
         */
        public abstract void put(String name, float val);

        /**
         * Put the value of the named double field into the persistent field.
         *
         * @param  name the name of the serializable field
         * @param  val the value to assign to the field
         * @throws IllegalArgumentException if <code>name</code> does not
         * match the name of a serializable field for the class whose fields
         * are being written, or if the type of the named field is not
         * <code>double</code>
         */
        public abstract void put(String name, double val);

        /**
         * Put the value of the named Object field into the persistent field.
         *
         * @param  name the name of the serializable field
         * @param  val the value to assign to the field
         *         (which may be <code>null</code>)
         * @throws IllegalArgumentException if <code>name</code> does not
         * match the name of a serializable field for the class whose fields
         * are being written, or if the type of the named field is not a
         * reference type
         */
        public abstract void put(String name, Object val);

        @Deprecated
        public abstract void write(TObjectOutput out) throws TIOException;
    }


    /**
     * Returns protocol version in use.
     */
    int getProtocolVersion() {
        return protocol;
    }

    /**
     * Writes string without allowing it to be replaced in stream.  Used by
     * ObjectStreamClass to write class descriptor type strings.
     */
    void writeTypeString(String str) throws TIOException {
        int handle;
        if (str == null) {
            writeNull();
        } else if ((handle = handles.lookup(str)) != -1) {
            writeHandle(handle);
        } else {
            writeString(str, false);
        }
    }

    /**
     * Verifies that this (possibly subclass) instance can be constructed
     * without violating security constraints: the subclass must not override
     * security-sensitive non-final methods, or else the
     * "enableSubclassImplementation" SerializablePermission is checked.
     */
    private void verifySubclass() {
        Class<?> cl = getClass();
        if (cl == TObjectOutputStream.class) {
            return;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return;
        }
        processQueue(TObjectOutputStream.Caches.subclassAuditsQueue, TObjectOutputStream.Caches.subclassAudits);
        TObjectStreamClass.WeakClassKey key = new TObjectStreamClass.WeakClassKey(cl, TObjectOutputStream.Caches.subclassAuditsQueue);
        Boolean result = TObjectOutputStream.Caches.subclassAudits.get(key);
        if (result == null) {
            result = Boolean.valueOf(auditSubclass(cl));
            TObjectOutputStream.Caches.subclassAudits.putIfAbsent(key, result);
        }
        if (result.booleanValue()) {
            return;
        }
        sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
    }

    /**
     * Performs reflective checks on given subclass to verify that it doesn't
     * override security-sensitive non-final methods.  Returns true if subclass
     * is "safe", false otherwise.
     */
    private static boolean auditSubclass(final Class<?> subcl) {
        Boolean result = TAccessController.doPrivileged(
                new TPrivilegedAction<Boolean>() {
                    public Boolean run() {
                        for (Class<?> cl = subcl;
                             cl != TObjectOutputStream.class;
                             cl = cl.getSuperclass())
                        {
                            try {
                                cl.getDeclaredMethod(
                                        "writeUnshared", new Class<?>[] { Object.class });
                                return Boolean.FALSE;
                            } catch (NoSuchMethodException ex) {
                            }
                            try {
                                cl.getDeclaredMethod("putFields", (Class<?>[]) null);
                                return Boolean.FALSE;
                            } catch (NoSuchMethodException ex) {
                            }
                        }
                        return Boolean.TRUE;
                    }
                }
        );
        return result.booleanValue();
    }

    /**
     * Clears internal data structures.
     */
    private void clear() {
        subs.clear();
        handles.clear();
    }

    /**
     * Underlying writeObject/writeUnshared implementation.
     */
    private void writeObject0(TObject obj, boolean unshared)
            throws TIOException
    {
        boolean oldMode = bout.setBlockDataMode(false);
        depth++;
        try {
            // handle previously written and non-replaceable objects
            int h;
            if ((obj = subs.lookup(obj)) == null) {
                writeNull();
                return;
            } else if (!unshared && (h = handles.lookup(obj)) != -1) {
                writeHandle(h);
                return;
            } else if (obj instanceof TClass) {
                writeClass((TClass) obj, unshared);
                return;
            } else if (obj instanceof TObjectStreamClass) {
                writeClassDesc((TObjectStreamClass) obj, unshared);
                return;
            }

            // check for replacement object
            Object orig = obj;
            Class<?> cl = obj.getClass();
            TObjectStreamClass desc;
            for (;;) {
                // REMIND: skip this check for strings/arrays?
                Class<?> repCl;
                desc = TObjectStreamClass.lookup(cl, true);
                if (!desc.hasWriteReplaceMethod() ||
                        (obj = desc.invokeWriteReplace(obj)) == null ||
                        (repCl = obj.getClass()) == cl)
                {
                    break;
                }
                cl = repCl;
            }
            if (enableReplace) {
                TObject rep = replaceObject(obj);
                if (rep != obj && rep != null) {
                    cl = rep.getClass();
                    desc = TObjectStreamClass.lookup(cl, true);
                }
                obj = rep;
            }

            // if object replaced, run through original checks a second time
            if (obj != orig) {
                subs.assign(orig, obj);
                if (obj == null) {
                    writeNull();
                    return;
                } else if (!unshared && (h = handles.lookup(obj)) != -1) {
                    writeHandle(h);
                    return;
                } else if (obj instanceof Class) {
                    writeClass((Class) obj, unshared);
                    return;
                } else if (obj instanceof TObjectStreamClass) {
                    writeClassDesc((TObjectStreamClass) obj, unshared);
                    return;
                }
            }

            // remaining cases
            if (obj instanceof TString) {
                writeString((TString) obj, unshared);
            } else if (cl.isArray()) {
                writeArray(obj, desc, unshared);
            } else if (obj instanceof TEnum) {
                writeEnum((TEnum<?>) obj, desc, unshared);
            } else if (obj instanceof TSerializable) {
                writeOrdinaryObject(obj, desc, unshared);
            } else {
                if (extendedDebugInfo) {
                    throw new TNotSerializableException(
                            cl.getName() + "\n" + debugInfoStack.toString());
                } else {
                    throw new TNotSerializableException(cl.getName());
                }
            }
        } finally {
            depth--;
            bout.setBlockDataMode(oldMode);
        }
    }

    /**
     * Writes null code to stream.
     */
    private void writeNull() throws IOException {
        bout.writeByte(TC_NULL);
    }

    /**
     * Writes given object handle to stream.
     */
    private void writeHandle(int handle) throws IOException {
        bout.writeByte(TC_REFERENCE);
        bout.writeInt(baseWireHandle + handle);
    }

    /**
     * Writes representation of given class to stream.
     */
    private void writeClass(TClass<?> cl, boolean unshared) throws TIOException {
        bout.writeByte(TC_CLASS);
        writeClassDesc(TObjectStreamClass.lookup(cl, true), false);
        handles.assign(unshared ? null : cl);
    }

    /**
     * Writes representation of given class descriptor to stream.
     */
    private void writeClassDesc(TObjectStreamClass desc, boolean unshared)
            throws TIOException
    {
        int handle;
        if (desc == null) {
            writeNull();
        } else if (!unshared && (handle = handles.lookup(desc)) != -1) {
            writeHandle(handle);
        } else if (desc.isProxy()) {
            writeProxyDesc(desc, unshared);
        } else {
            writeNonProxyDesc(desc, unshared);
        }
    }

    private boolean isCustomSubclass() {
        // Return true if this class is a custom subclass of ObjectOutputStream
        return getClass().getClassLoader()
                != TObjectOutputStream.class.getClassLoader();
    }

    /**
     * Writes class descriptor representing a dynamic proxy class to stream.
     */
    private void writeProxyDesc(TObjectStreamClass desc, boolean unshared)
            throws TIOException
    {
        bout.writeByte(TC_PROXYCLASSDESC);
        handles.assign(unshared ? null : desc);

        Class<?> cl = desc.forClass();
        Class<?>[] ifaces = cl.getInterfaces();
        bout.writeInt(ifaces.length);
        for (int i = 0; i < ifaces.length; i++) {
            bout.writeUTF(ifaces[i].getName());
        }

        bout.setBlockDataMode(true);
        if (cl != null && isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateProxyClass(cl);
        bout.setBlockDataMode(false);
        bout.writeByte(TC_ENDBLOCKDATA);

        writeClassDesc(desc.getSuperDesc(), false);
    }

    /**
     * Writes class descriptor representing a standard (i.e., not a dynamic
     * proxy) class to stream.
     */
    private void writeNonProxyDesc(ObjectStreamClass desc, boolean unshared)
            throws IOException
    {
        bout.writeByte(TC_CLASSDESC);
        handles.assign(unshared ? null : desc);

        if (protocol == PROTOCOL_VERSION_1) {
            // do not invoke class descriptor write hook with old protocol
            desc.writeNonProxy(this);
        } else {
            writeClassDescriptor(desc);
        }

        Class<?> cl = desc.forClass();
        bout.setBlockDataMode(true);
        if (cl != null && isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateClass(cl);
        bout.setBlockDataMode(false);
        bout.writeByte(TC_ENDBLOCKDATA);

        writeClassDesc(desc.getSuperDesc(), false);
    }

    /**
     * Writes given string to stream, using standard or long UTF format
     * depending on string length.
     */
    private void writeString(String str, boolean unshared) throws IOException {
        handles.assign(unshared ? null : str);
        long utflen = bout.getUTFLength(str);
        if (utflen <= 0xFFFF) {
            bout.writeByte(TC_STRING);
            bout.writeUTF(str, utflen);
        } else {
            bout.writeByte(TC_LONGSTRING);
            bout.writeLongUTF(str, utflen);
        }
    }

    /**
     * Writes given array object to stream.
     */
    private void writeArray(Object array,
            ObjectStreamClass desc,
            boolean unshared)
            throws IOException
    {
        bout.writeByte(TC_ARRAY);
        writeClassDesc(desc, false);
        handles.assign(unshared ? null : array);

        Class<?> ccl = desc.forClass().getComponentType();
        if (ccl.isPrimitive()) {
            if (ccl == Integer.TYPE) {
                int[] ia = (int[]) array;
                bout.writeInt(ia.length);
                bout.writeInts(ia, 0, ia.length);
            } else if (ccl == Byte.TYPE) {
                byte[] ba = (byte[]) array;
                bout.writeInt(ba.length);
                bout.write(ba, 0, ba.length, true);
            } else if (ccl == Long.TYPE) {
                long[] ja = (long[]) array;
                bout.writeInt(ja.length);
                bout.writeLongs(ja, 0, ja.length);
            } else if (ccl == Float.TYPE) {
                float[] fa = (float[]) array;
                bout.writeInt(fa.length);
                bout.writeFloats(fa, 0, fa.length);
            } else if (ccl == Double.TYPE) {
                double[] da = (double[]) array;
                bout.writeInt(da.length);
                bout.writeDoubles(da, 0, da.length);
            } else if (ccl == Short.TYPE) {
                short[] sa = (short[]) array;
                bout.writeInt(sa.length);
                bout.writeShorts(sa, 0, sa.length);
            } else if (ccl == Character.TYPE) {
                char[] ca = (char[]) array;
                bout.writeInt(ca.length);
                bout.writeChars(ca, 0, ca.length);
            } else if (ccl == Boolean.TYPE) {
                boolean[] za = (boolean[]) array;
                bout.writeInt(za.length);
                bout.writeBooleans(za, 0, za.length);
            } else {
                throw new InternalError();
            }
        } else {
            Object[] objs = (Object[]) array;
            int len = objs.length;
            bout.writeInt(len);
            if (extendedDebugInfo) {
                debugInfoStack.push(
                        "array (class \"" + array.getClass().getName() +
                                "\", size: " + len  + ")");
            }
            try {
                for (int i = 0; i < len; i++) {
                    if (extendedDebugInfo) {
                        debugInfoStack.push(
                                "element of array (index: " + i + ")");
                    }
                    try {
                        writeObject0(objs[i], false);
                    } finally {
                        if (extendedDebugInfo) {
                            debugInfoStack.pop();
                        }
                    }
                }
            } finally {
                if (extendedDebugInfo) {
                    debugInfoStack.pop();
                }
            }
        }
    }

    /**
     * Writes given enum constant to stream.
     */
    private void writeEnum(Enum<?> en,
            ObjectStreamClass desc,
            boolean unshared)
            throws IOException
    {
        bout.writeByte(TC_ENUM);
        ObjectStreamClass sdesc = desc.getSuperDesc();
        writeClassDesc((sdesc.forClass() == Enum.class) ? desc : sdesc, false);
        handles.assign(unshared ? null : en);
        writeString(en.name(), false);
    }

    /**
     * Writes representation of a "ordinary" (i.e., not a String, Class,
     * ObjectStreamClass, array, or enum constant) serializable object to the
     * stream.
     */
    private void writeOrdinaryObject(Object obj,
            ObjectStreamClass desc,
            boolean unshared)
            throws IOException
    {
        if (extendedDebugInfo) {
            debugInfoStack.push(
                    (depth == 1 ? "root " : "") + "object (class \"" +
                            obj.getClass().getName() + "\", " + obj.toString() + ")");
        }
        try {
            desc.checkSerialize();

            bout.writeByte(TC_OBJECT);
            writeClassDesc(desc, false);
            handles.assign(unshared ? null : obj);
            if (desc.isExternalizable() && !desc.isProxy()) {
                writeExternalData((Externalizable) obj);
            } else {
                writeSerialData(obj, desc);
            }
        } finally {
            if (extendedDebugInfo) {
                debugInfoStack.pop();
            }
        }
    }

    /**
     * Writes externalizable data of given object by invoking its
     * writeExternal() method.
     */
    private void writeExternalData(Externalizable obj) throws IOException {
        TObjectOutputStream.PutFieldImpl oldPut = curPut;
        curPut = null;

        if (extendedDebugInfo) {
            debugInfoStack.push("writeExternal data");
        }
        SerialCallbackContext oldContext = curContext;
        try {
            curContext = null;
            if (protocol == PROTOCOL_VERSION_1) {
                obj.writeExternal(this);
            } else {
                bout.setBlockDataMode(true);
                obj.writeExternal(this);
                bout.setBlockDataMode(false);
                bout.writeByte(TC_ENDBLOCKDATA);
            }
        } finally {
            curContext = oldContext;
            if (extendedDebugInfo) {
                debugInfoStack.pop();
            }
        }

        curPut = oldPut;
    }

    /**
     * Writes instance data for each serializable class of given object, from
     * superclass to subclass.
     */
    private void writeSerialData(Object obj, ObjectStreamClass desc)
            throws IOException
    {
        ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
        for (int i = 0; i < slots.length; i++) {
            ObjectStreamClass slotDesc = slots[i].desc;
            if (slotDesc.hasWriteObjectMethod()) {
                TObjectOutputStream.PutFieldImpl oldPut = curPut;
                curPut = null;
                SerialCallbackContext oldContext = curContext;

                if (extendedDebugInfo) {
                    debugInfoStack.push(
                            "custom writeObject data (class \"" +
                                    slotDesc.getName() + "\")");
                }
                try {
                    curContext = new SerialCallbackContext(obj, slotDesc);
                    bout.setBlockDataMode(true);
                    slotDesc.invokeWriteObject(obj, this);
                    bout.setBlockDataMode(false);
                    bout.writeByte(TC_ENDBLOCKDATA);
                } finally {
                    curContext.setUsed();
                    curContext = oldContext;
                    if (extendedDebugInfo) {
                        debugInfoStack.pop();
                    }
                }

                curPut = oldPut;
            } else {
                defaultWriteFields(obj, slotDesc);
            }
        }
    }

    /**
     * Fetches and writes values of serializable fields of given object to
     * stream.  The given class descriptor specifies which field values to
     * write, and in which order they should be written.
     */
    private void defaultWriteFields(TObject obj, TObjectStreamClass desc)
            throws TIOException
    {
        TClass<?> cl = desc.forClass();
        if (cl != null && obj != null && !cl.isInstance(obj)) {
            throw new ClassCastException();
        }

        desc.checkDefaultSerialize();

        int primDataSize = desc.getPrimDataSize();
        if (primVals == null || primVals.length < primDataSize) {
            primVals = new byte[primDataSize];
        }
        desc.getPrimFieldValues(obj, primVals);
        bout.write(primVals, 0, primDataSize, false);

        TObjectStreamField[] fields = desc.getFields(false);
        Object[] objVals = new Object[desc.getNumObjFields()];
        int numPrimFields = fields.length - objVals.length;
        desc.getObjFieldValues(obj, objVals);
        for (int i = 0; i < objVals.length; i++) {
            if (extendedDebugInfo) {
                debugInfoStack.push(
                        "field (class \"" + desc.getName() + "\", name: \"" +
                                fields[numPrimFields + i].getName() + "\", type: \"" +
                                fields[numPrimFields + i].getType() + "\")");
            }
            try {
                writeObject0(objVals[i],
                        fields[numPrimFields + i].isUnshared());
            } finally {
                if (extendedDebugInfo) {
                    debugInfoStack.pop();
                }
            }
        }
    }

    /**
     * Attempts to write to stream fatal IOException that has caused
     * serialization to abort.
     */
    private void writeFatalException(TIOException ex) throws TIOException {
        /*
         * Note: the serialization specification states that if a second
         * IOException occurs while attempting to serialize the original fatal
         * exception to the stream, then a StreamCorruptedException should be
         * thrown (section 2.1).  However, due to a bug in previous
         * implementations of serialization, StreamCorruptedExceptions were
         * rarely (if ever) actually thrown--the "root" exceptions from
         * underlying streams were thrown instead.  This historical behavior is
         * followed here for consistency.
         */
        clear();
        boolean oldMode = bout.setBlockDataMode(false);
        try {
            bout.writeByte(TC_EXCEPTION);
            writeObject0(ex, false);
            clear();
        } finally {
            bout.setBlockDataMode(oldMode);
        }
    }

    /**
     * Converts specified span of float values into byte values.
     */
    // REMIND: remove once hotspot inlines Float.floatToIntBits
    private static native void floatsToBytes(float[] src, int srcpos,
            byte[] dst, int dstpos,
            int nfloats);

    /**
     * Converts specified span of double values into byte values.
     */
    // REMIND: remove once hotspot inlines Double.doubleToLongBits
    private static native void doublesToBytes(double[] src, int srcpos,
            byte[] dst, int dstpos,
            int ndoubles);

    /**
     * Default PutField implementation.
     */
    private class PutFieldImpl extends TObjectOutputStream.PutField {

        /** class descriptor describing serializable fields */
        private final TObjectStreamClass desc;
        /** primitive field values */
        private final byte[] primVals;
        /** object field values */
        private final Object[] objVals;

        /**
         * Creates PutFieldImpl object for writing fields defined in given
         * class descriptor.
         */
        PutFieldImpl(TObjectStreamClass desc) {
            this.desc = desc;
            primVals = new byte[desc.getPrimDataSize()];
            objVals = new Object[desc.getNumObjFields()];
        }

        public void put(String name, boolean val) {
            TBits.putBoolean(primVals, getFieldOffset(name, Boolean.TYPE), val);
        }

        public void put(String name, byte val) {
            primVals[getFieldOffset(name, Byte.TYPE)] = val;
        }

        public void put(String name, char val) {
            TBits.putChar(primVals, getFieldOffset(name, Character.TYPE), val);
        }

        public void put(String name, short val) {
            TBits.putShort(primVals, getFieldOffset(name, Short.TYPE), val);
        }

        public void put(String name, int val) {
            TBits.putInt(primVals, getFieldOffset(name, Integer.TYPE), val);
        }

        public void put(String name, float val) {
            TBits.putFloat(primVals, getFieldOffset(name, Float.TYPE), val);
        }

        public void put(String name, long val) {
            TBits.putLong(primVals, getFieldOffset(name, Long.TYPE), val);
        }

        public void put(String name, double val) {
            TBits.putDouble(primVals, getFieldOffset(name, Double.TYPE), val);
        }

        public void put(String name, Object val) {
            objVals[getFieldOffset(name, Object.class)] = val;
        }

        // deprecated in ObjectOutputStream.PutField
        public void write(TObjectOutput out) throws TIOException {
            /*
             * Applications should *not* use this method to write PutField
             * data, as it will lead to stream corruption if the PutField
             * object writes any primitive data (since block data mode is not
             * unset/set properly, as is done in OOS.writeFields()).  This
             * broken implementation is being retained solely for behavioral
             * compatibility, in order to support applications which use
             * OOS.PutField.write() for writing only non-primitive data.
             *
             * Serialization of unshared objects is not implemented here since
             * it is not necessary for backwards compatibility; also, unshared
             * semantics may not be supported by the given ObjectOutput
             * instance.  Applications which write unshared objects using the
             * PutField API must use OOS.writeFields().
             */
            if (TObjectOutputStream.this != out) {
                throw new IllegalArgumentException("wrong stream");
            }
            out.write(primVals, 0, primVals.length);

            TObjectStreamField[] fields = desc.getFields(false);
            int numPrimFields = fields.length - objVals.length;
            // REMIND: warn if numPrimFields > 0?
            for (int i = 0; i < objVals.length; i++) {
                if (fields[numPrimFields + i].isUnshared()) {
                    throw new TIOException(TString.wrap("cannot write unshared object"));
                }
                out.writeObject(objVals[i]);
            }
        }

        /**
         * Writes buffered primitive data and object fields to stream.
         */
        void writeFields() throws TIOException {
            bout.write(primVals, 0, primVals.length, false);

            TObjectStreamField[] fields = desc.getFields(false);
            int numPrimFields = fields.length - objVals.length;
            for (int i = 0; i < objVals.length; i++) {
                if (extendedDebugInfo) {
                    debugInfoStack.push(
                            "field (class \"" + desc.getName() + "\", name: \"" +
                                    fields[numPrimFields + i].getName() + "\", type: \"" +
                                    fields[numPrimFields + i].getType() + "\")");
                }
                try {
                    writeObject0(objVals[i],
                            fields[numPrimFields + i].isUnshared());
                } finally {
                    if (extendedDebugInfo) {
                        debugInfoStack.pop();
                    }
                }
            }
        }

        /**
         * Returns offset of field with given name and type.  A specified type
         * of null matches all types, Object.class matches all non-primitive
         * types, and any other non-null type matches assignable types only.
         * Throws IllegalArgumentException if no matching field found.
         */
        private int getFieldOffset(String name, Class<?> type) {
            TObjectStreamField field = desc.getField(name, type);
            if (field == null) {
                throw new IllegalArgumentException("no such field " + name +
                        " with type " + type);
            }
            return field.getOffset();
        }
    }

    /**
     * Buffered output stream with two modes: in default mode, outputs data in
     * same format as DataOutputStream; in "block data" mode, outputs data
     * bracketed by block data markers (see object serialization specification
     * for details).
     */
    private static class BlockDataOutputStream
            extends TOutputStream implements TDataOutput
    {
        /** maximum data block length */
        private static final int MAX_BLOCK_SIZE = 1024;
        /** maximum data block header length */
        private static final int MAX_HEADER_SIZE = 5;
        /** (tunable) length of char buffer (for writing strings) */
        private static final int CHAR_BUF_SIZE = 256;

        /** buffer for writing general/block data */
        private final byte[] buf = new byte[MAX_BLOCK_SIZE];
        /** buffer for writing block data headers */
        private final byte[] hbuf = new byte[MAX_HEADER_SIZE];
        /** char buffer for fast string writes */
        private final char[] cbuf = new char[CHAR_BUF_SIZE];

        /** block data mode */
        private boolean blkmode = false;
        /** current offset into buf */
        private int pos = 0;

        /** underlying output stream */
        private final TOutputStream out;
        /** loopback stream (for data writes that span data blocks) */
        private final TDataOutputStream dout;

        /**
         * Creates new BlockDataOutputStream on top of given underlying stream.
         * Block data mode is turned off by default.
         */
        BlockDataOutputStream(TOutputStream out) {
            this.out = out;
            dout = new TDataOutputStream(this);
        }

        /**
         * Sets block data mode to the given mode (true == on, false == off)
         * and returns the previous mode value.  If the new mode is the same as
         * the old mode, no action is taken.  If the new mode differs from the
         * old mode, any buffered data is flushed before switching to the new
         * mode.
         */
        boolean setBlockDataMode(boolean mode) throws TIOException {
            if (blkmode == mode) {
                return blkmode;
            }
            drain();
            blkmode = mode;
            return !blkmode;
        }

        /**
         * Returns true if the stream is currently in block data mode, false
         * otherwise.
         */
        boolean getBlockDataMode() {
            return blkmode;
        }

        /* ----------------- generic output stream methods ----------------- */
        /*
         * The following methods are equivalent to their counterparts in
         * OutputStream, except that they partition written data into data
         * blocks when in block data mode.
         */

        public void write(int b) throws TIOException {
            if (pos >= MAX_BLOCK_SIZE) {
                drain();
            }
            buf[pos++] = (byte) b;
        }

        public void write(byte[] b) throws TIOException {
            write(b, 0, b.length, false);
        }

        public void write(byte[] b, int off, int len) throws TIOException {
            write(b, off, len, false);
        }

        public void flush() throws TIOException {
            drain();
            out.flush();
        }

        public void close() throws TIOException {
            flush();
            out.close();
        }

        /**
         * Writes specified span of byte values from given array.  If copy is
         * true, copies the values to an intermediate buffer before writing
         * them to underlying stream (to avoid exposing a reference to the
         * original byte array).
         */
        void write(byte[] b, int off, int len, boolean copy)
                throws TIOException
        {
            if (!(copy || blkmode)) {           // write directly
                drain();
                out.write(b, off, len);
                return;
            }

            while (len > 0) {
                if (pos >= MAX_BLOCK_SIZE) {
                    drain();
                }
                if (len >= MAX_BLOCK_SIZE && !copy && pos == 0) {
                    // avoid unnecessary copy
                    writeBlockHeader(MAX_BLOCK_SIZE);
                    out.write(b, off, MAX_BLOCK_SIZE);
                    off += MAX_BLOCK_SIZE;
                    len -= MAX_BLOCK_SIZE;
                } else {
                    int wlen = Math.min(len, MAX_BLOCK_SIZE - pos);
                    System.arraycopy(b, off, buf, pos, wlen);
                    pos += wlen;
                    off += wlen;
                    len -= wlen;
                }
            }
        }

        /**
         * Writes all buffered data from this stream to the underlying stream,
         * but does not flush underlying stream.
         */
        void drain() throws TIOException {
            if (pos == 0) {
                return;
            }
            if (blkmode) {
                writeBlockHeader(pos);
            }
            out.write(buf, 0, pos);
            pos = 0;
        }

        /**
         * Writes block data header.  Data blocks shorter than 256 bytes are
         * prefixed with a 2-byte header; all others start with a 5-byte
         * header.
         */
        private void writeBlockHeader(int len) throws TIOException {
            if (len <= 0xFF) {
                hbuf[0] = TC_BLOCKDATA;
                hbuf[1] = (byte) len;
                out.write(hbuf, 0, 2);
            } else {
                hbuf[0] = TC_BLOCKDATALONG;
                TBits.putInt(hbuf, 1, len);
                out.write(hbuf, 0, 5);
            }
        }


        /* ----------------- primitive data output methods ----------------- */
        /*
         * The following methods are equivalent to their counterparts in
         * DataOutputStream, except that they partition written data into data
         * blocks when in block data mode.
         */

        public void writeBoolean(boolean v) throws TIOException {
            if (pos >= MAX_BLOCK_SIZE) {
                drain();
            }
            TBits.putBoolean(buf, pos++, v);
        }

        public void writeByte(int v) throws TIOException {
            if (pos >= MAX_BLOCK_SIZE) {
                drain();
            }
            buf[pos++] = (byte) v;
        }

        public void writeChar(int v) throws TIOException {
            if (pos + 2 <= MAX_BLOCK_SIZE) {
                TBits.putChar(buf, pos, (char) v);
                pos += 2;
            } else {
                dout.writeChar(v);
            }
        }

        public void writeShort(int v) throws TIOException {
            if (pos + 2 <= MAX_BLOCK_SIZE) {
                TBits.putShort(buf, pos, (short) v);
                pos += 2;
            } else {
                dout.writeShort(v);
            }
        }

        public void writeInt(int v) throws TIOException {
            if (pos + 4 <= MAX_BLOCK_SIZE) {
                TBits.putInt(buf, pos, v);
                pos += 4;
            } else {
                dout.writeInt(v);
            }
        }

        public void writeFloat(float v) throws TIOException {
            if (pos + 4 <= MAX_BLOCK_SIZE) {
                TBits.putFloat(buf, pos, v);
                pos += 4;
            } else {
                dout.writeFloat(v);
            }
        }

        public void writeLong(long v) throws TIOException {
            if (pos + 8 <= MAX_BLOCK_SIZE) {
                TBits.putLong(buf, pos, v);
                pos += 8;
            } else {
                dout.writeLong(v);
            }
        }

        public void writeDouble(double v) throws TIOException {
            if (pos + 8 <= MAX_BLOCK_SIZE) {
                TBits.putDouble(buf, pos, v);
                pos += 8;
            } else {
                dout.writeDouble(v);
            }
        }

        public void writeBytes(String s) throws TIOException {
            int endoff = s.length();
            int cpos = 0;
            int csize = 0;
            for (int off = 0; off < endoff; ) {
                if (cpos >= csize) {
                    cpos = 0;
                    csize = Math.min(endoff - off, CHAR_BUF_SIZE);
                    s.getChars(off, off + csize, cbuf, 0);
                }
                if (pos >= MAX_BLOCK_SIZE) {
                    drain();
                }
                int n = Math.min(csize - cpos, MAX_BLOCK_SIZE - pos);
                int stop = pos + n;
                while (pos < stop) {
                    buf[pos++] = (byte) cbuf[cpos++];
                }
                off += n;
            }
        }

        public void writeChars(TString s) throws TIOException {
            int endoff = s.length();
            for (int off = 0; off < endoff; ) {
                int csize = Math.min(endoff - off, CHAR_BUF_SIZE);
                s.getChars(off, off + csize, cbuf, 0);
                writeChars(cbuf, 0, csize);
                off += csize;
            }
        }

        public void writeUTF(TString s) throws TIOException {
            writeUTF(s, getUTFLength(s));
        }


        /* -------------- primitive data array output methods -------------- */
        /*
         * The following methods write out spans of primitive data values.
         * Though equivalent to calling the corresponding primitive write
         * methods repeatedly, these methods are optimized for writing groups
         * of primitive data values more efficiently.
         */

        void writeBooleans(boolean[] v, int off, int len) throws TIOException {
            int endoff = off + len;
            while (off < endoff) {
                if (pos >= MAX_BLOCK_SIZE) {
                    drain();
                }
                int stop = Math.min(endoff, off + (MAX_BLOCK_SIZE - pos));
                while (off < stop) {
                    TBits.putBoolean(buf, pos++, v[off++]);
                }
            }
        }

        void writeChars(char[] v, int off, int len) throws TIOException {
            int limit = MAX_BLOCK_SIZE - 2;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 1;
                    int stop = Math.min(endoff, off + avail);
                    while (off < stop) {
                        TBits.putChar(buf, pos, v[off++]);
                        pos += 2;
                    }
                } else {
                    dout.writeChar(v[off++]);
                }
            }
        }

        void writeShorts(short[] v, int off, int len) throws TIOException {
            int limit = MAX_BLOCK_SIZE - 2;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 1;
                    int stop = Math.min(endoff, off + avail);
                    while (off < stop) {
                        TBits.putShort(buf, pos, v[off++]);
                        pos += 2;
                    }
                } else {
                    dout.writeShort(v[off++]);
                }
            }
        }

        void writeInts(int[] v, int off, int len) throws TIOException {
            int limit = MAX_BLOCK_SIZE - 4;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 2;
                    int stop = Math.min(endoff, off + avail);
                    while (off < stop) {
                        TBits.putInt(buf, pos, v[off++]);
                        pos += 4;
                    }
                } else {
                    dout.writeInt(v[off++]);
                }
            }
        }

        void writeFloats(float[] v, int off, int len) throws TIOException {
            int limit = MAX_BLOCK_SIZE - 4;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 2;
                    int chunklen = Math.min(endoff - off, avail);
                    floatsToBytes(v, off, buf, pos, chunklen);
                    off += chunklen;
                    pos += chunklen << 2;
                } else {
                    dout.writeFloat(v[off++]);
                }
            }
        }

        void writeLongs(long[] v, int off, int len) throws TIOException {
            int limit = MAX_BLOCK_SIZE - 8;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 3;
                    int stop = Math.min(endoff, off + avail);
                    while (off < stop) {
                        TBits.putLong(buf, pos, v[off++]);
                        pos += 8;
                    }
                } else {
                    dout.writeLong(v[off++]);
                }
            }
        }

        void writeDoubles(double[] v, int off, int len) throws TIOException {
            int limit = MAX_BLOCK_SIZE - 8;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 3;
                    int chunklen = Math.min(endoff - off, avail);
                    doublesToBytes(v, off, buf, pos, chunklen);
                    off += chunklen;
                    pos += chunklen << 3;
                } else {
                    dout.writeDouble(v[off++]);
                }
            }
        }

        /**
         * Returns the length in bytes of the UTF encoding of the given string.
         */
        long getUTFLength(String s) {
            int len = s.length();
            long utflen = 0;
            for (int off = 0; off < len; ) {
                int csize = Math.min(len - off, CHAR_BUF_SIZE);
                s.getChars(off, off + csize, cbuf, 0);
                for (int cpos = 0; cpos < csize; cpos++) {
                    char c = cbuf[cpos];
                    if (c >= 0x0001 && c <= 0x007F) {
                        utflen++;
                    } else if (c > 0x07FF) {
                        utflen += 3;
                    } else {
                        utflen += 2;
                    }
                }
                off += csize;
            }
            return utflen;
        }

        /**
         * Writes the given string in UTF format.  This method is used in
         * situations where the UTF encoding length of the string is already
         * known; specifying it explicitly avoids a prescan of the string to
         * determine its UTF length.
         */
        void writeUTF(TString s, long utflen) throws TIOException {
            if (utflen > 0xFFFFL) {
                throw new TUTFDataFormatException();
            }
            writeShort((int) utflen);
            if (utflen == (long) s.length()) {
                writeBytes(s);
            } else {
                writeUTFBody(s);
            }
        }

        /**
         * Writes given string in "long" UTF format.  "Long" UTF format is
         * identical to standard UTF, except that it uses an 8 byte header
         * (instead of the standard 2 bytes) to convey the UTF encoding length.
         */
        void writeLongUTF(String s) throws TIOException {
            writeLongUTF(s, getUTFLength(s));
        }

        /**
         * Writes given string in "long" UTF format, where the UTF encoding
         * length of the string is already known.
         */
        void writeLongUTF(String s, long utflen) throws TIOException {
            writeLong(utflen);
            if (utflen == (long) s.length()) {
                writeBytes(s);
            } else {
                writeUTFBody(s);
            }
        }

        /**
         * Writes the "body" (i.e., the UTF representation minus the 2-byte or
         * 8-byte length header) of the UTF encoding for the given string.
         */
        private void writeUTFBody(TString s) throws TIOException {
            int limit = MAX_BLOCK_SIZE - 3;
            int len = s.length();
            for (int off = 0; off < len; ) {
                int csize = Math.min(len - off, CHAR_BUF_SIZE);
                s.getChars(off, off + csize, cbuf, 0);
                for (int cpos = 0; cpos < csize; cpos++) {
                    char c = cbuf[cpos];
                    if (pos <= limit) {
                        if (c <= 0x007F && c != 0) {
                            buf[pos++] = (byte) c;
                        } else if (c > 0x07FF) {
                            buf[pos + 2] = (byte) (0x80 | ((c >> 0) & 0x3F));
                            buf[pos + 1] = (byte) (0x80 | ((c >> 6) & 0x3F));
                            buf[pos + 0] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                            pos += 3;
                        } else {
                            buf[pos + 1] = (byte) (0x80 | ((c >> 0) & 0x3F));
                            buf[pos + 0] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                            pos += 2;
                        }
                    } else {    // write one byte at a time to normalize block
                        if (c <= 0x007F && c != 0) {
                            write(c);
                        } else if (c > 0x07FF) {
                            write(0xE0 | ((c >> 12) & 0x0F));
                            write(0x80 | ((c >> 6) & 0x3F));
                            write(0x80 | ((c >> 0) & 0x3F));
                        } else {
                            write(0xC0 | ((c >> 6) & 0x1F));
                            write(0x80 | ((c >> 0) & 0x3F));
                        }
                    }
                }
                off += csize;
            }
        }
    }

    /**
     * Lightweight identity hash table which maps objects to integer handles,
     * assigned in ascending order.
     */
    private static class HandleTable {

        /* number of mappings in table/next available handle */
        private int size;
        /* size threshold determining when to expand hash spine */
        private int threshold;
        /* factor for computing size threshold */
        private final float loadFactor;
        /* maps hash value -> candidate handle value */
        private int[] spine;
        /* maps handle value -> next candidate handle value */
        private int[] next;
        /* maps handle value -> associated object */
        private TObject[] objs;

        /**
         * Creates new HandleTable with given capacity and load factor.
         */
        HandleTable(int initialCapacity, float loadFactor) {
            this.loadFactor = loadFactor;
            spine = new int[initialCapacity];
            next = new int[initialCapacity];
            objs = new TObject[initialCapacity];
            threshold = (int) (initialCapacity * loadFactor);
            clear();
        }

        /**
         * Assigns next available handle to given object, and returns handle
         * value.  Handles are assigned in ascending order starting at 0.
         */
        int assign(TObject obj) {
            if (size >= next.length) {
                growEntries();
            }
            if (size >= threshold) {
                growSpine();
            }
            insert(obj, size);
            return size++;
        }

        /**
         * Looks up and returns handle associated with given object, or -1 if
         * no mapping found.
         */
        int lookup(Object obj) {
            if (size == 0) {
                return -1;
            }
            int index = hash(obj) % spine.length;
            for (int i = spine[index]; i >= 0; i = next[i]) {
                if (objs[i] == obj) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Resets table to its initial (empty) state.
         */
        void clear() {
            TArrays.fill(spine, -1);
            TArrays.fill(objs, 0, size, null);
            size = 0;
        }

        /**
         * Returns the number of mappings currently in table.
         */
        int size() {
            return size;
        }

        /**
         * Inserts mapping object -> handle mapping into table.  Assumes table
         * is large enough to accommodate new mapping.
         */
        private void insert(TObject obj, int handle) {
            int index = hash(obj) % spine.length;
            objs[handle] = obj;
            next[handle] = spine[index];
            spine[index] = handle;
        }

        /**
         * Expands the hash "spine" -- equivalent to increasing the number of
         * buckets in a conventional hash table.
         */
        private void growSpine() {
            spine = new int[(spine.length << 1) + 1];
            threshold = (int) (spine.length * loadFactor);
            TArrays.fill(spine, -1);
            for (int i = 0; i < size; i++) {
                insert(objs[i], i);
            }
        }

        /**
         * Increases hash table capacity by lengthening entry arrays.
         */
        private void growEntries() {
            int newLength = (next.length << 1) + 1;
            int[] newNext = new int[newLength];
            System.arraycopy(next, 0, newNext, 0, size);
            next = newNext;

            TObject[] newObjs = new TObject[newLength];
            System.arraycopy(objs, 0, newObjs, 0, size);
            objs = newObjs;
        }

        /**
         * Returns hash value for given object.
         */
        private int hash(Object obj) {
            return System.identityHashCode(obj) & 0x7FFFFFFF;
        }
    }

    /**
     * Lightweight identity hash table which maps objects to replacement
     * objects.
     */
    private static class ReplaceTable {

        /* maps object -> index */
        private final TObjectOutputStream.HandleTable htab;
        /* maps index -> replacement object */
        private TObject[] reps;

        /**
         * Creates new ReplaceTable with given capacity and load factor.
         */
        ReplaceTable(int initialCapacity, float loadFactor) {
            htab = new TObjectOutputStream.HandleTable(initialCapacity, loadFactor);
            reps = new TObject[initialCapacity];
        }

        /**
         * Enters mapping from object to replacement object.
         */
        void assign(TObject obj, TObject rep) {
            int index = htab.assign(obj);
            while (index >= reps.length) {
                grow();
            }
            reps[index] = rep;
        }

        /**
         * Looks up and returns replacement for given object.  If no
         * replacement is found, returns the lookup object itself.
         */
        TObject lookup(TObject obj) {
            int index = htab.lookup(obj);
            return (index >= 0) ? reps[index] : obj;
        }

        /**
         * Resets table to its initial (empty) state.
         */
        void clear() {
            TArrays.fill(reps, 0, htab.size(), null);
            htab.clear();
        }

        /**
         * Returns the number of mappings currently in table.
         */
        int size() {
            return htab.size();
        }

        /**
         * Increases table capacity.
         */
        private void grow() {
            TObject[] newReps = new TObject[(reps.length << 1) + 1];
            System.arraycopy(reps, 0, newReps, 0, reps.length);
            reps = newReps;
        }
    }

    /**
     * Stack to keep debug information about the state of the
     * serialization process, for embedding in exception messages.
     */
    private static class DebugTraceInfoStack {
        private final TList<String> stack;

        DebugTraceInfoStack() {
            stack = new TArrayList<>();
        }

        /**
         * Removes all of the elements from enclosed list.
         */
        void clear() {
            stack.clear();
        }

        /**
         * Removes the object at the top of enclosed list.
         */
        void pop() {
            stack.remove(stack.size()-1);
        }

        /**
         * Pushes a String onto the top of enclosed list.
         */
        void push(String entry) {
            stack.add("\t- " + entry);
        }

        /**
         * Returns a string representation of this object
         */
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            if (!stack.isEmpty()) {
                for(int i = stack.size(); i > 0; i-- ) {
                    buffer.append(stack.get(i-1) + ((i != 1) ? "\n" : ""));
                }
            }
            return buffer.toString();
        }
    }

}
