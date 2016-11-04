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
package org.teavm.classlib.java.io;

import static org.teavm.classlib.java.io.TObjectStreamClass.processQueue;

import java.security.PrivilegedActionException;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TEnum;
import org.teavm.classlib.java.lang.TObject;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.lang.ref.TReferenceQueue;
import org.teavm.classlib.java.lang.reflect.TModifier;
import org.teavm.classlib.java.security.TAccessControlContext;
import org.teavm.classlib.java.security.TAccessController;
import org.teavm.classlib.java.security.TPrivilegedExceptionAction;
import org.teavm.classlib.java.util.TArrays;
import org.teavm.classlib.java.util.THashMap;
import org.teavm.classlib.java.util.concurrent.TConcurrentHashMap;
import org.teavm.classlib.java.util.concurrent.TConcurrentMap;

/**
 * Created by vasek on 29. 10. 2016.
 */
public class TObjectInputStream
        extends TInputStream implements TObjectInput, TObjectStreamConstants
{
    /** handle value representing null */
    private static final int NULL_HANDLE = -1;

    /** marker for unshared objects in internal handle table */
    private static final Object unsharedMarker = new Object();

    /** table mapping primitive type names to corresponding class objects */
    private static final THashMap<String, Class<?>> primClasses
            = new THashMap<>(8, 1.0F);
    static {
        primClasses.put("boolean", boolean.class);
        primClasses.put("byte", byte.class);
        primClasses.put("char", char.class);
        primClasses.put("short", short.class);
        primClasses.put("int", int.class);
        primClasses.put("long", long.class);
        primClasses.put("float", float.class);
        primClasses.put("double", double.class);
        primClasses.put("void", void.class);
    }

    private static class Caches {
        /** cache of subclass security audit results */
        static final TConcurrentMap<TObjectStreamClass.WeakClassKey,Boolean> subclassAudits =
                new TConcurrentHashMap<>();

        /** queue for WeakReferences to audited subclasses */
        static final TReferenceQueue<Class<?>> subclassAuditsQueue =
                new TReferenceQueue<>();
    }

    /** filter stream for handling block data conversion */
    private final BlockDataInputStream bin;
    /** validation callback list */
    private final ValidationList vlist;
    /** recursion depth */
    private int depth;
    /** whether stream is closed */
    private boolean closed;

    /** wire handle -> obj/exception map */
    private final HandleTable handles;
    /** scratch field for passing handle values up/down call stack */
    private int passHandle = NULL_HANDLE;
    /** flag set when at end of field value block with no TC_ENDBLOCKDATA */
    private boolean defaultDataEnd = false;

    /** buffer for reading primitive field values */
    private byte[] primVals;

    /** if true, invoke readObjectOverride() instead of readObject() */
    private final boolean enableOverride;
    /** if true, invoke resolveObject() */
    private boolean enableResolve;

    /**
     * Context during upcalls to class-defined readObject methods; holds
     * object currently being deserialized and descriptor for current class.
     * Null when not during readObject upcall.
     */
    private TSerialCallbackContext curContext;

    public TObjectInputStream(TInputStream in) throws TIOException {
        verifySubclass();
        bin = new BlockDataInputStream(in);
        handles = new HandleTable(10);
        vlist = new ValidationList();
        enableOverride = false;
        readStreamHeader();
        bin.setBlockDataMode(true);
    }


    protected TObjectInputStream() throws TIOException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        bin = null;
        handles = null;
        vlist = null;
        enableOverride = true;
    }

    public final Object readObject()
            throws TIOException, ClassNotFoundException
    {
        if (enableOverride) {
            return readObjectOverride();
        }

        // if nested read, passHandle contains handle of enclosing object
        int outerHandle = passHandle;
        try {
            Object obj = readObject0(false);
            handles.markDependency(outerHandle, passHandle);
            ClassNotFoundException ex = handles.lookupException(passHandle);
            if (ex != null) {
                throw ex;
            }
            if (depth == 0) {
                vlist.doCallbacks();
            }
            return obj;
        } finally {
            passHandle = outerHandle;
            if (closed && depth == 0) {
                clear();
            }
        }
    }

    protected Object readObjectOverride()
            throws TIOException, ClassNotFoundException
    {
        return null;
    }

    public Object readUnshared() throws TIOException, ClassNotFoundException {
        // if nested read, passHandle contains handle of enclosing object
        int outerHandle = passHandle;
        try {
            Object obj = readObject0(true);
            handles.markDependency(outerHandle, passHandle);
            ClassNotFoundException ex = handles.lookupException(passHandle);
            if (ex != null) {
                throw ex;
            }
            if (depth == 0) {
                vlist.doCallbacks();
            }
            return obj;
        } finally {
            passHandle = outerHandle;
            if (closed && depth == 0) {
                clear();
            }
        }
    }

    public void defaultReadObject()
            throws TIOException, ClassNotFoundException
    {
        TSerialCallbackContext ctx = curContext;
        if (ctx == null) {
            throw new TNotActiveException(TString.wrap("not in call to readObject"));
        }
        Object curObj = ctx.getObj();
        TObjectStreamClass curDesc = ctx.getDesc();
        bin.setBlockDataMode(false);
        defaultReadFields(curObj, curDesc);
        bin.setBlockDataMode(true);
        if (!curDesc.hasWriteObjectData()) {
            /*
             * Fix for 4360508: since stream does not contain terminating
             * TC_ENDBLOCKDATA tag, set flag so that reading code elsewhere
             * knows to simulate end-of-custom-data behavior.
             */
            defaultDataEnd = true;
        }
        ClassNotFoundException ex = handles.lookupException(passHandle);
        if (ex != null) {
            throw ex;
        }
    }

    public TObjectInputStream.GetField readFields()
            throws TIOException, ClassNotFoundException
    {
        TSerialCallbackContext ctx = curContext;
        if (ctx == null) {
            throw new TNotActiveException(TString.wrap("not in call to readObject"));
        }
        Object curObj = ctx.getObj();
        TObjectStreamClass curDesc = ctx.getDesc();
        bin.setBlockDataMode(false);
        TObjectInputStream.GetFieldImpl getField = new TObjectInputStream.GetFieldImpl(curDesc);
        getField.readFields();
        bin.setBlockDataMode(true);
        if (!curDesc.hasWriteObjectData()) {
            /*
             * Fix for 4360508: since stream does not contain terminating
             * TC_ENDBLOCKDATA tag, set flag so that reading code elsewhere
             * knows to simulate end-of-custom-data behavior.
             */
            defaultDataEnd = true;
        }

        return getField;
    }

    public void registerValidation(TObjectInputValidation obj, int prio)
            throws TNotActiveException, TInvalidObjectException
    {
        if (depth == 0) {
            throw new TNotActiveException(TString.wrap("stream inactive"));
        }
        vlist.register(obj, prio);
    }

    protected TClass<?> resolveClass(TObjectStreamClass desc)
            throws TIOException, ClassNotFoundException
    {
        TString name = desc.getName();
        try {
            return TClass.forName(name, false, latestUserDefinedLoader());
        } catch (ClassNotFoundException ex) {
            TClass<?> cl = primClasses.get(name);
            if (cl != null) {
                return cl;
            } else {
                throw ex;
            }
        }
    }

    protected Class<?> resolveProxyClass(String[] interfaces)
            throws TIOException, ClassNotFoundException
    {
        ClassLoader latestLoader = latestUserDefinedLoader();
        ClassLoader nonPublicLoader = null;
        boolean hasNonPublicInterface = false;

        // define proxy in class loader of non-public interface(s), if any
        Class<?>[] classObjs = new Class<?>[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> cl = Class.forName(interfaces[i], false, latestLoader);
            if ((cl.getModifiers() & TModifier.PUBLIC) == 0) {
                if (hasNonPublicInterface) {
                    if (nonPublicLoader != cl.getClassLoader()) {
                        throw new IllegalAccessError(
                                "conflicting non-public interface class loaders");
                    }
                } else {
                    nonPublicLoader = cl.getClassLoader();
                    hasNonPublicInterface = true;
                }
            }
            classObjs[i] = cl;
        }
        try {
//            return Proxy.getProxyClass(
//                    hasNonPublicInterface ? nonPublicLoader : latestLoader,
//                    classObjs);
            return null;
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    protected Object resolveObject(Object obj) throws TIOException {
        return obj;
    }

    /**
     * Enable the stream to allow objects read from the stream to be replaced.
     * When enabled, the resolveObject method is called for every object being
     * deserialized.
     *
     * <p>If <i>enable</i> is true, and there is a security manager installed,
     * this method first calls the security manager's
     * <code>checkPermission</code> method with the
     * <code>SerializablePermission("enableSubstitution")</code> permission to
     * ensure it's ok to enable the stream to allow objects read from the
     * stream to be replaced.
     *
     * @param   enable true for enabling use of <code>resolveObject</code> for
     *          every object being deserialized
     * @return  the previous setting before this method was invoked
     * @throws  SecurityException if a security manager exists and its
     *          <code>checkPermission</code> method denies enabling the stream
     *          to allow objects read from the stream to be replaced.
     * @see SecurityManager#checkPermission
     * @see java.io.SerializablePermission
     */
    protected boolean enableResolveObject(boolean enable)
            throws SecurityException
    {
        if (enable == enableResolve) {
            return enable;
        }
        if (enable) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        enableResolve = enable;
        return !enableResolve;
    }

    protected void readStreamHeader()
            throws TIOException, TStreamCorruptedException
    {
        short s0 = bin.readShort();
        short s1 = bin.readShort();
        if (s0 != STREAM_MAGIC || s1 != STREAM_VERSION) {
            throw new TStreamCorruptedException(
                    TString.wrap(String.format("invalid stream header: %04X%04X", s0, s1)));
        }
    }

    protected TObjectStreamClass readClassDescriptor()
            throws TIOException, ClassNotFoundException
    {
        TObjectStreamClass desc = new TObjectStreamClass();
        desc.readNonProxy(this);
        return desc;
    }

    public int read() throws TIOException {
        return bin.read();
    }

    public int read(byte[] buf, int off, int len) throws TIOException {
        if (buf == null) {
            throw new NullPointerException();
        }
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        return bin.read(buf, off, len, false);
    }

    public int available() throws TIOException {
        return bin.available();
    }

    public void close() throws TIOException {
        /*
         * Even if stream already closed, propagate redundant close to
         * underlying stream to stay consistent with previous implementations.
         */
        closed = true;
        if (depth == 0) {
            clear();
        }
        bin.close();
    }


    public boolean readBoolean() throws TIOException {
        return bin.readBoolean();
    }


    public byte readByte() throws TIOException  {
        return bin.readByte();
    }


    public int readUnsignedByte()  throws TIOException {
        return bin.readUnsignedByte();
    }


    public char readChar()  throws TIOException {
        return bin.readChar();
    }


    public short readShort()  throws TIOException {
        return bin.readShort();
    }


    public int readUnsignedShort() throws TIOException {
        return bin.readUnsignedShort();
    }


    public int readInt()  throws TIOException {
        return bin.readInt();
    }


    public long readLong()  throws TIOException {
        return bin.readLong();
    }


    public float readFloat() throws TIOException {
        return bin.readFloat();
    }


    public double readDouble() throws TIOException {
        return bin.readDouble();
    }


    public void readFully(byte[] buf) throws TIOException {
        bin.readFully(buf, 0, buf.length, false);
    }

    public void readFully(byte[] buf, int off, int len) throws TIOException {
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        bin.readFully(buf, off, len, false);
    }

    public int skipBytes(int len) throws TIOException {
        return bin.skipBytes(len);
    }

    @Deprecated
    public TString readLine() throws TIOException {
        return bin.readLine();
    }

    public TString readUTF() throws TIOException {
        return bin.readUTF();
    }

    /**
     * Provide access to the persistent fields read from the input stream.
     */
    public static abstract class GetField {

        /**
         * Get the ObjectStreamClass that describes the fields in the stream.
         *
         * @return  the descriptor class that describes the serializable fields
         */
        public abstract TObjectStreamClass getObjectStreamClass();

        public abstract boolean defaulted(String name) throws TIOException;

        public abstract boolean get(String name, boolean val)
                throws TIOException;

        public abstract byte get(String name, byte val) throws TIOException;

        public abstract char get(String name, char val) throws TIOException;

        public abstract short get(String name, short val) throws TIOException;

        public abstract int get(String name, int val) throws TIOException;

        public abstract long get(String name, long val) throws TIOException;

        public abstract float get(String name, float val) throws TIOException;

        public abstract double get(String name, double val) throws TIOException;

        public abstract Object get(String name, Object val) throws TIOException;
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
        processQueue(TObjectInputStream.Caches.subclassAuditsQueue, TObjectInputStream.Caches.subclassAudits);
        TObjectStreamClass.WeakClassKey key = new TObjectStreamClass.WeakClassKey(cl, TObjectInputStream.Caches.subclassAuditsQueue);
        Boolean result = TObjectInputStream.Caches.subclassAudits.get(key);
        if (result == null) {
            result = Boolean.valueOf(auditSubclass(cl));
            TObjectInputStream.Caches.subclassAudits.putIfAbsent(key, result);
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
//        Boolean result = AccessController.doPrivileged(
//                new PrivilegedAction<Boolean>() {
//                    public Boolean run() {
//                        for (Class<?> cl = subcl;
//                             cl != TObjectOutputStream.class;
//                             cl = cl.getSuperclass())
//                        {
//                            try {
//                                cl.getDeclaredMethod(
//                                        "readUnshared", (Class[]) null);
//                                return Boolean.FALSE;
//                            } catch (NoSuchMethodException ex) {
//                            }
//                            try {
//                                cl.getDeclaredMethod("readFields", (Class[]) null);
//                                return Boolean.FALSE;
//                            } catch (NoSuchMethodException ex) {
//                            }
//                        }
//                        return Boolean.TRUE;
//                    }
//                }
//        );
//        return result.booleanValue();
        return true;
    }

    /**
     * Clears internal data structures.
     */
    private void clear() {
        handles.clear();
        vlist.clear();
    }

    /**
     * Underlying readObject implementation.
     */
    private Object readObject0(boolean unshared) throws TIOException {
        boolean oldMode = bin.getBlockDataMode();
        if (oldMode) {
            int remain = bin.currentBlockRemaining();
            if (remain > 0) {
                throw new TOptionalDataException(remain);
            } else if (defaultDataEnd) {
                /*
                 * Fix for 4360508: stream is currently at the end of a field
                 * value block written via default serialization; since there
                 * is no terminating TC_ENDBLOCKDATA tag, simulate
                 * end-of-custom-data behavior explicitly.
                 */
                throw new TOptionalDataException(true);
            }
            bin.setBlockDataMode(false);
        }

        byte tc;
        while ((tc = bin.peekByte()) == TC_RESET) {
            bin.readByte();
            handleReset();
        }

        depth++;
        try {
            switch (tc) {
                case TC_NULL:
                    return readNull();

                case TC_REFERENCE:
                    return readHandle(unshared);

                case TC_CLASS:
                    return readClass(unshared);

                case TC_CLASSDESC:
                case TC_PROXYCLASSDESC:
                    return readClassDesc(unshared);

                case TC_STRING:
                case TC_LONGSTRING:
                    return checkResolve(readString(unshared));

                case TC_ARRAY:
                    return checkResolve(readArray(unshared));

                case TC_ENUM:
                    return checkResolve(readEnum(unshared));

                case TC_OBJECT:
                    return checkResolve(readOrdinaryObject(unshared));

                case TC_EXCEPTION:
                    TIOException ex = readFatalException();
                    throw new WriteAbortedException("writing aborted", ex);

                case TC_BLOCKDATA:
                case TC_BLOCKDATALONG:
                    if (oldMode) {
                        bin.setBlockDataMode(true);
                        bin.peek();             // force header read
                        throw new TOptionalDataException(
                                bin.currentBlockRemaining());
                    } else {
                        throw new TStreamCorruptedException(
                                TString.wrap("unexpected block data"));
                    }

                case TC_ENDBLOCKDATA:
                    if (oldMode) {
                        throw new TOptionalDataException(true);
                    } else {
                        throw new TStreamCorruptedException(
                                TString.wrap("unexpected end of block data"));
                    }

                default:
                    throw new TStreamCorruptedException(
                            TString.wrap(String.format("invalid type code: %02X", tc)));
            }
        } finally {
            depth--;
            bin.setBlockDataMode(oldMode);
        }
    }

    /**
     * If resolveObject has been enabled and given object does not have an
     * exception associated with it, calls resolveObject to determine
     * replacement for object, and updates handle table accordingly.  Returns
     * replacement object, or echoes provided object if no replacement
     * occurred.  Expects that passHandle is set to given object's handle prior
     * to calling this method.
     */
    private Object checkResolve(Object obj) throws TIOException {
        if (!enableResolve || handles.lookupException(passHandle) != null) {
            return obj;
        }
        Object rep = resolveObject(obj);
        if (rep != obj) {
            handles.setObject(passHandle, rep);
        }
        return rep;
    }

    /**
     * Reads string without allowing it to be replaced in stream.  Called from
     * within ObjectStreamClass.read().
     */
    String readTypeString() throws TIOException {
        int oldHandle = passHandle;
        try {
            byte tc = bin.peekByte();
            switch (tc) {
                case TC_NULL:
                    return (String) readNull();

                case TC_REFERENCE:
                    return (String) readHandle(false);

                case TC_STRING:
                case TC_LONGSTRING:
                    return readString(false);

                default:
                    throw new TStreamCorruptedException(
                            TString.wrap(String.format("invalid type code: %02X", tc)));
            }
        } finally {
            passHandle = oldHandle;
        }
    }

    /**
     * Reads in null code, sets passHandle to NULL_HANDLE and returns null.
     */
    private Object readNull() throws TIOException {
        if (bin.readByte() != TC_NULL) {
            throw new InternalError();
        }
        passHandle = NULL_HANDLE;
        return null;
    }

    /**
     * Reads in object handle, sets passHandle to the read handle, and returns
     * object associated with the handle.
     */
    private Object readHandle(boolean unshared) throws TIOException {
        if (bin.readByte() != TC_REFERENCE) {
            throw new InternalError();
        }
        passHandle = bin.readInt() - baseWireHandle;
        if (passHandle < 0 || passHandle >= handles.size()) {
            throw new TStreamCorruptedException(
                    TString.wrap(String.format("invalid handle value: %08X", passHandle +
                            baseWireHandle)));
        }
        if (unshared) {
            // REMIND: what type of exception to throw here?
            throw new TInvalidObjectException(TString.wrap(
                    "cannot read back reference as unshared"));
        }

        Object obj = handles.lookupObject(passHandle);
        if (obj == unsharedMarker) {
            // REMIND: what type of exception to throw here?
            throw new TInvalidObjectException(TString.wrap(
                    "cannot read back reference to unshared object"));
        }
        return obj;
    }

    /**
     * Reads in and returns class object.  Sets passHandle to class object's
     * assigned handle.  Returns null if class is unresolvable (in which case a
     * ClassNotFoundException will be associated with the class' handle in the
     * handle table).
     */
    private Class<?> readClass(boolean unshared) throws TIOException {
        if (bin.readByte() != TC_CLASS) {
            throw new InternalError();
        }
        TObjectStreamClass desc = readClassDesc(false);
        Class<?> cl = desc.forClass();
        passHandle = handles.assign(unshared ? unsharedMarker : cl);

        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            handles.markException(passHandle, resolveEx);
        }

        handles.finish(passHandle);
        return cl;
    }

    /**
     * Reads in and returns (possibly null) class descriptor.  Sets passHandle
     * to class descriptor's assigned handle.  If class descriptor cannot be
     * resolved to a class in the local VM, a ClassNotFoundException is
     * associated with the class descriptor's handle.
     */
    private TObjectStreamClass readClassDesc(boolean unshared)
            throws TIOException
    {
        byte tc = bin.peekByte();
        switch (tc) {
            case TC_NULL:
                return (TObjectStreamClass) readNull();

            case TC_REFERENCE:
                return (TObjectStreamClass) readHandle(unshared);

            case TC_PROXYCLASSDESC:
                return readProxyDesc(unshared);

            case TC_CLASSDESC:
                return readNonProxyDesc(unshared);

            default:
                throw new TStreamCorruptedException(
                        TString.wrap(String.format("invalid type code: %02X", tc)));
        }
    }

    private boolean isCustomSubclass() {
        // Return true if this class is a custom subclass of ObjectInputStream
        return getClass().getClassLoader()
                != TObjectOutputStream.class.getClassLoader();
    }

    /**
     * Reads in and returns class descriptor for a dynamic proxy class.  Sets
     * passHandle to proxy class descriptor's assigned handle.  If proxy class
     * descriptor cannot be resolved to a class in the local VM, a
     * ClassNotFoundException is associated with the descriptor's handle.
     */
    private TObjectStreamClass readProxyDesc(boolean unshared)
            throws TIOException
    {
//        if (bin.readByte() != TC_PROXYCLASSDESC) {
//            throw new InternalError();
//        }
//
//        TObjectStreamClass desc = new TObjectStreamClass();
//        int descHandle = handles.assign(unshared ? unsharedMarker : desc);
//        passHandle = NULL_HANDLE;
//
//        int numIfaces = bin.readInt();
//        TString[] ifaces = new TString[numIfaces];
//        for (int i = 0; i < numIfaces; i++) {
//            ifaces[i] = bin.readUTF();
//        }
//
//        Class<?> cl = null;
//        ClassNotFoundException resolveEx = null;
//        bin.setBlockDataMode(true);
//        try {
//            if ((cl = resolveProxyClass(ifaces)) == null) {
//                resolveEx = new ClassNotFoundException("null class");
//            } else if (!Proxy.isProxyClass(cl)) {
//                throw new TInvalidClassException("Not a proxy");
//            } else {
//                // ReflectUtil.checkProxyPackageAccess makes a test
//                // equivalent to isCustomSubclass so there's no need
//                // to condition this call to isCustomSubclass == true here.
//                ReflectUtil.checkProxyPackageAccess(
//                        getClass().getClassLoader(),
//                        cl.getInterfaces());
//            }
//        } catch (ClassNotFoundException ex) {
//            resolveEx = ex;
//        }
//        skipCustomData();
//
//        desc.initProxy(cl, resolveEx, readClassDesc(false));

//        handles.finish(descHandle);
//        passHandle = descHandle;
//        return desc;
        return null;
    }

    /**
     * Reads in and returns class descriptor for a class that is not a dynamic
     * proxy class.  Sets passHandle to class descriptor's assigned handle.  If
     * class descriptor cannot be resolved to a class in the local VM, a
     * ClassNotFoundException is associated with the descriptor's handle.
     */
    private TObjectStreamClass readNonProxyDesc(boolean unshared)
            throws TIOException
    {
        if (bin.readByte() != TC_CLASSDESC) {
            throw new InternalError();
        }

//        TObjectStreamClass desc = new TObjectStreamClass();
//        int descHandle = handles.assign(unshared ? unsharedMarker : desc);
//        passHandle = NULL_HANDLE;
//
//        TObjectStreamClass readDesc = null;
//        try {
//            readDesc = readClassDescriptor();
//        } catch (ClassNotFoundException ex) {
//            throw (TIOException) new TInvalidClassException(
//                    TString.wrap("failed to read class descriptor")).initCause(ex);
//        }
//
//        Class<?> cl = null;
//        ClassNotFoundException resolveEx = null;
//        bin.setBlockDataMode(true);
//        final boolean checksRequired = isCustomSubclass();
//        try {
//            if ((cl = resolveClass(readDesc)) == null) {
//                resolveEx = new ClassNotFoundException("null class");
//            } else if (checksRequired) {
//                TReflectUtil.checkPackageAccess(cl);
//            }
//        } catch (ClassNotFoundException ex) {
//            resolveEx = ex;
//        }
//        skipCustomData();
//
//        desc.initNonProxy(readDesc, cl, resolveEx, readClassDesc(false));
//
//        handles.finish(descHandle);
//        passHandle = descHandle;
//        return desc;
        return null;
    }

    /**
     * Reads in and returns new string.  Sets passHandle to new string's
     * assigned handle.
     */
    private TString readString(boolean unshared) throws TIOException {
        TString str;
        byte tc = bin.readByte();
        switch (tc) {
            case TC_STRING:
                str = bin.readUTF();
                break;

            case TC_LONGSTRING:
                str = bin.readLongUTF();
                break;

            default:
                throw new TStreamCorruptedException(
                        TString.wrap(String.format("invalid type code: %02X", tc)));
        }
        passHandle = handles.assign(unshared ? unsharedMarker : str);
        handles.finish(passHandle);
        return str;
    }

    /**
     * Reads in and returns array object, or null if array class is
     * unresolvable.  Sets passHandle to array's assigned handle.
     */
    private Object readArray(boolean unshared) throws TIOException {
//        if (bin.readByte() != TC_ARRAY) {
//            throw new InternalError();
//        }
//
//        TObjectStreamClass desc = readClassDesc(false);
//        int len = bin.readInt();
//
//        Object array = null;
//        Class<?> cl, ccl = null;
//        if ((cl = desc.forClass()) != null) {
//            ccl = cl.getComponentType();
//            array = TArray.newInstance(ccl, len);
//        }
//
//        int arrayHandle = handles.assign(unshared ? unsharedMarker : array);
//        ClassNotFoundException resolveEx = desc.getResolveException();
//        if (resolveEx != null) {
//            handles.markException(arrayHandle, resolveEx);
//        }
//
//        if (ccl == null) {
//            for (int i = 0; i < len; i++) {
//                readObject0(false);
//            }
//        } else if (ccl.isPrimitive()) {
//            if (ccl == Integer.TYPE) {
//                bin.readInts((int[]) array, 0, len);
//            } else if (ccl == Byte.TYPE) {
//                bin.readFully((byte[]) array, 0, len, true);
//            } else if (ccl == Long.TYPE) {
//                bin.readLongs((long[]) array, 0, len);
//            } else if (ccl == Float.TYPE) {
//                bin.readFloats((float[]) array, 0, len);
//            } else if (ccl == Double.TYPE) {
//                bin.readDoubles((double[]) array, 0, len);
//            } else if (ccl == Short.TYPE) {
//                bin.readShorts((short[]) array, 0, len);
//            } else if (ccl == Character.TYPE) {
//                bin.readChars((char[]) array, 0, len);
//            } else if (ccl == Boolean.TYPE) {
//                bin.readBooleans((boolean[]) array, 0, len);
//            } else {
//                throw new InternalError();
//            }
//        } else {
//            Object[] oa = (Object[]) array;
//            for (int i = 0; i < len; i++) {
//                oa[i] = readObject0(false);
//                handles.markDependency(arrayHandle, passHandle);
//            }
//        }
//
//        handles.finish(arrayHandle);
//        passHandle = arrayHandle;
//        return array;

        return null;
    }

    /**
     * Reads in and returns enum constant, or null if enum type is
     * unresolvable.  Sets passHandle to enum constant's assigned handle.
     */
    private TEnum<?> readEnum(boolean unshared) throws TIOException {
        if (bin.readByte() != TC_ENUM) {
            throw new InternalError();
        }

        TObjectStreamClass desc = readClassDesc(false);
        if (!desc.isEnum()) {
            throw new TInvalidClassException(TString.wrap("non-enum class: " + desc));
        }

        int enumHandle = handles.assign(unshared ? unsharedMarker : null);
        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            handles.markException(enumHandle, resolveEx);
        }

        TString name = readString(false);
        TEnum<?> result = null;
        TClass<?> cl = desc.forClass();
        if (cl != null) {
            try {
                @SuppressWarnings("unchecked")
                TEnum<?> en = TEnum.valueOf((TClass)cl, name);
                result = en;
            } catch (IllegalArgumentException ex) {
                throw (TIOException) new TInvalidObjectException(TString.wrap(
                        "enum constant " + name + " does not exist in " +
                                cl)).initCause(ex);
            }
            if (!unshared) {
                handles.setObject(enumHandle, result);
            }
        }

        handles.finish(enumHandle);
        passHandle = enumHandle;
        return result;
    }

    /**
     * Reads and returns "ordinary" (i.e., not a String, Class,
     * ObjectStreamClass, array, or enum constant) object, or null if object's
     * class is unresolvable (in which case a ClassNotFoundException will be
     * associated with object's handle).  Sets passHandle to object's assigned
     * handle.
     */
    private Object readOrdinaryObject(boolean unshared)
            throws TIOException
    {
        if (bin.readByte() != TC_OBJECT) {
            throw new InternalError();
        }

        TObjectStreamClass desc = readClassDesc(false);
        desc.checkDeserialize();

        TClass<?> cl = desc.forClass();
//        if (cl == String.class || cl == Class.class
//                || cl == TObjectStreamClass.class) {
//            throw new TInvalidClassException(TString.wrap("invalid class descriptor"));
//        }

        Object obj;
        try {
            obj = desc.isInstantiable() ? desc.newInstance() : null;
        } catch (Exception ex) {
            throw (TIOException) new TInvalidClassException(
                    desc.forClass().getName(),
                    TString.wrap("unable to create instance")).initCause(ex);
        }

        passHandle = handles.assign(unshared ? unsharedMarker : obj);
        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            handles.markException(passHandle, resolveEx);
        }

        if (desc.isExternalizable()) {
            readExternalData((TExternalizable) obj, desc);
        } else {
            readSerialData(obj, desc);
        }

        handles.finish(passHandle);

        if (obj != null &&
                handles.lookupException(passHandle) == null &&
                desc.hasReadResolveMethod())
        {
            Object rep = desc.invokeReadResolve(obj);
            if (unshared && rep.getClass().isArray()) {
                rep = cloneArray(rep);
            }
            if (rep != obj) {
                handles.setObject(passHandle, obj = rep);
            }
        }

        return obj;
    }

    /**
     * If obj is non-null, reads externalizable data by invoking readExternal()
     * method of obj; otherwise, attempts to skip over externalizable data.
     * Expects that passHandle is set to obj's handle before this method is
     * called.
     */
    private void readExternalData(TExternalizable obj, TObjectStreamClass desc)
            throws TIOException
    {
        TSerialCallbackContext oldContext = curContext;
        if (oldContext != null)
            oldContext.check();
        curContext = null;
        try {
            boolean blocked = desc.hasBlockExternalData();
            if (blocked) {
                bin.setBlockDataMode(true);
            }
            if (obj != null) {
                try {
                    obj.readExternal(this);
                } catch (ClassNotFoundException ex) {
                    /*
                     * In most cases, the handle table has already propagated
                     * a CNFException to passHandle at this point; this mark
                     * call is included to address cases where the readExternal
                     * method has cons'ed and thrown a new CNFException of its
                     * own.
                     */
                    handles.markException(passHandle, ex);
                }
            }
            if (blocked) {
                skipCustomData();
            }
        } finally {
            if (oldContext != null)
                oldContext.check();
            curContext = oldContext;
        }
        /*
         * At this point, if the externalizable data was not written in
         * block-data form and either the externalizable class doesn't exist
         * locally (i.e., obj == null) or readExternal() just threw a
         * CNFException, then the stream is probably in an inconsistent state,
         * since some (or all) of the externalizable data may not have been
         * consumed.  Since there's no "correct" action to take in this case,
         * we mimic the behavior of past serialization implementations and
         * blindly hope that the stream is in sync; if it isn't and additional
         * externalizable data remains in the stream, a subsequent read will
         * most likely throw a StreamCorruptedException.
         */
    }

    /**
     * Reads (or attempts to skip, if obj is null or is tagged with a
     * ClassNotFoundException) instance data for each serializable class of
     * object in stream, from superclass to subclass.  Expects that passHandle
     * is set to obj's handle before this method is called.
     */
    private void readSerialData(Object obj, TObjectStreamClass desc)
            throws TIOException
    {
        TObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
        for (int i = 0; i < slots.length; i++) {
            TObjectStreamClass slotDesc = slots[i].desc;

            if (slots[i].hasData) {
                if (obj == null || handles.lookupException(passHandle) != null) {
                    defaultReadFields(null, slotDesc); // skip field values
                } else if (slotDesc.hasReadObjectMethod()) {
                    TSerialCallbackContext oldContext = curContext;
                    if (oldContext != null)
                        oldContext.check();
                    try {
                        curContext = new SerialCallbackContext(obj, slotDesc);

                        bin.setBlockDataMode(true);
                        slotDesc.invokeReadObject(obj, this);
                    } catch (ClassNotFoundException ex) {
                        /*
                         * In most cases, the handle table has already
                         * propagated a CNFException to passHandle at this
                         * point; this mark call is included to address cases
                         * where the custom readObject method has cons'ed and
                         * thrown a new CNFException of its own.
                         */
                        handles.markException(passHandle, ex);
                    } finally {
                        curContext.setUsed();
                        if (oldContext!= null)
                            oldContext.check();
                        curContext = oldContext;
                    }

                    /*
                     * defaultDataEnd may have been set indirectly by custom
                     * readObject() method when calling defaultReadObject() or
                     * readFields(); clear it to restore normal read behavior.
                     */
                    defaultDataEnd = false;
                } else {
                    defaultReadFields(obj, slotDesc);
                }

                if (slotDesc.hasWriteObjectData()) {
                    skipCustomData();
                } else {
                    bin.setBlockDataMode(false);
                }
            } else {
                if (obj != null &&
                        slotDesc.hasReadObjectNoDataMethod() &&
                        handles.lookupException(passHandle) == null)
                {
                    slotDesc.invokeReadObjectNoData(obj);
                }
            }
        }
    }

    /**
     * Skips over all block data and objects until TC_ENDBLOCKDATA is
     * encountered.
     */
    private void skipCustomData() throws TIOException {
        int oldHandle = passHandle;
        for (;;) {
            if (bin.getBlockDataMode()) {
                bin.skipBlockData();
                bin.setBlockDataMode(false);
            }
            switch (bin.peekByte()) {
                case TC_BLOCKDATA:
                case TC_BLOCKDATALONG:
                    bin.setBlockDataMode(true);
                    break;

                case TC_ENDBLOCKDATA:
                    bin.readByte();
                    passHandle = oldHandle;
                    return;

                default:
                    readObject0(false);
                    break;
            }
        }
    }

    /**
     * Reads in values of serializable fields declared by given class
     * descriptor.  If obj is non-null, sets field values in obj.  Expects that
     * passHandle is set to obj's handle before this method is called.
     */
    private void defaultReadFields(Object obj, TObjectStreamClass desc)
            throws TIOException
    {
        Class<?> cl = desc.forClass();
        if (cl != null && obj != null && !cl.isInstance(obj)) {
            throw new ClassCastException();
        }

        int primDataSize = desc.getPrimDataSize();
        if (primVals == null || primVals.length < primDataSize) {
            primVals = new byte[primDataSize];
        }
        bin.readFully(primVals, 0, primDataSize, false);
        if (obj != null) {
            desc.setPrimFieldValues(obj, primVals);
        }

        int objHandle = passHandle;
        TObjectStreamField[] fields = desc.getFields(false);
        Object[] objVals = new Object[desc.getNumObjFields()];
        int numPrimFields = fields.length - objVals.length;
        for (int i = 0; i < objVals.length; i++) {
            TObjectStreamField f = fields[numPrimFields + i];
            objVals[i] = readObject0(f.isUnshared());
            if (f.getField() != null) {
                handles.markDependency(objHandle, passHandle);
            }
        }
        if (obj != null) {
            desc.setObjFieldValues(obj, objVals);
        }
        passHandle = objHandle;
    }

    /**
     * Reads in and returns IOException that caused serialization to abort.
     * All stream state is discarded prior to reading in fatal exception.  Sets
     * passHandle to fatal exception's handle.
     */
    private TIOException readFatalException() throws TIOException {
        if (bin.readByte() != TC_EXCEPTION) {
            throw new InternalError();
        }
        clear();
        return (TIOException) readObject0(false);
    }

    /**
     * If recursion depth is 0, clears internal data structures; otherwise,
     * throws a StreamCorruptedException.  This method is called when a
     * TC_RESET typecode is encountered.
     */
    private void handleReset() throws TStreamCorruptedException {
        if (depth > 0) {
            throw new TStreamCorruptedException(
                    TString.wrap("unexpected reset; recursion depth: " + depth));
        }
        clear();
    }

    /**
     * Converts specified span of bytes into float values.
     */
    // REMIND: remove once hotspot inlines Float.intBitsToFloat
    private static native void bytesToFloats(byte[] src, int srcpos,
            float[] dst, int dstpos,
            int nfloats);

    /**
     * Converts specified span of bytes into double values.
     */
    // REMIND: remove once hotspot inlines Double.longBitsToDouble
    private static native void bytesToDoubles(byte[] src, int srcpos,
            double[] dst, int dstpos,
            int ndoubles);

    /**
     * Returns the first non-null class loader (not counting class loaders of
     * generated reflection implementation classes) up the execution stack, or
     * null if only code from the null class loader is on the stack.  This
     * method is also called via reflection by the following RMI-IIOP class:
     *
     *     com.sun.corba.se.internal.util.JDKClassLoader
     *
     * This method should not be removed or its signature changed without
     * corresponding modifications to the above class.
     */
    private static ClassLoader latestUserDefinedLoader() {
        return sun.misc.VM.latestUserDefinedLoader();
    }

    /**
     * Default GetField implementation.
     */
    private class GetFieldImpl extends TObjectInputStream.GetField {

        /** class descriptor describing serializable fields */
        private final TObjectStreamClass desc;
        /** primitive field values */
        private final byte[] primVals;
        /** object field values */
        private final Object[] objVals;
        /** object field value handles */
        private final int[] objHandles;

        /**
         * Creates GetFieldImpl object for reading fields defined in given
         * class descriptor.
         */
        GetFieldImpl(TObjectStreamClass desc) {
            this.desc = desc;
            primVals = new byte[desc.getPrimDataSize()];
            objVals = new Object[desc.getNumObjFields()];
            objHandles = new int[objVals.length];
        }

        public TObjectStreamClass getObjectStreamClass() {
            return desc;
        }

        public boolean defaulted(String name) throws TIOException {
            return (getFieldOffset(name, null) < 0);
        }

        public boolean get(String name, boolean val) throws TIOException {
            int off = getFieldOffset(name, Boolean.TYPE);
            return (off >= 0) ? TBits.getBoolean(primVals, off) : val;
        }

        public byte get(String name, byte val) throws TIOException {
            int off = getFieldOffset(name, Byte.TYPE);
            return (off >= 0) ? primVals[off] : val;
        }

        public char get(String name, char val) throws TIOException {
            int off = getFieldOffset(name, Character.TYPE);
            return (off >= 0) ? TBits.getChar(primVals, off) : val;
        }

        public short get(String name, short val) throws TIOException {
            int off = getFieldOffset(name, Short.TYPE);
            return (off >= 0) ? TBits.getShort(primVals, off) : val;
        }

        public int get(String name, int val) throws TIOException {
            int off = getFieldOffset(name, Integer.TYPE);
            return (off >= 0) ? TBits.getInt(primVals, off) : val;
        }

        public float get(String name, float val) throws TIOException {
            int off = getFieldOffset(name, Float.TYPE);
            return (off >= 0) ? TBits.getFloat(primVals, off) : val;
        }

        public long get(String name, long val) throws TIOException {
            int off = getFieldOffset(name, Long.TYPE);
            return (off >= 0) ? TBits.getLong(primVals, off) : val;
        }

        public double get(String name, double val) throws TIOException {
            int off = getFieldOffset(name, Double.TYPE);
            return (off >= 0) ? TBits.getDouble(primVals, off) : val;
        }

        public Object get(String name, Object val) throws TIOException {
            int off = getFieldOffset(name, Object.class);
            if (off >= 0) {
                int objHandle = objHandles[off];
                handles.markDependency(passHandle, objHandle);
                return (handles.lookupException(objHandle) == null) ?
                        objVals[off] : null;
            } else {
                return val;
            }
        }

        /**
         * Reads primitive and object field values from stream.
         */
        void readFields() throws TIOException {
            bin.readFully(primVals, 0, primVals.length, false);

            int oldHandle = passHandle;
            TObjectStreamField[] fields = desc.getFields(false);
            int numPrimFields = fields.length - objVals.length;
            for (int i = 0; i < objVals.length; i++) {
                objVals[i] =
                        readObject0(fields[numPrimFields + i].isUnshared());
                objHandles[i] = passHandle;
            }
            passHandle = oldHandle;
        }

        /**
         * Returns offset of field with given name and type.  A specified type
         * of null matches all types, Object.class matches all non-primitive
         * types, and any other non-null type matches assignable types only.
         * If no matching field is found in the (incoming) class
         * descriptor but a matching field is present in the associated local
         * class descriptor, returns -1.  Throws IllegalArgumentException if
         * neither incoming nor local class descriptor contains a match.
         */
        private int getFieldOffset(String name, Class<?> type) {
            TObjectStreamField field = desc.getField(name, type);
            if (field != null) {
                return field.getOffset();
            } else if (desc.getLocalDesc().getField(name, type) != null) {
                return -1;
            } else {
                throw new IllegalArgumentException("no such field " + name +
                        " with type " + type);
            }
        }
    }

    /**
     * Prioritized list of callbacks to be performed once object graph has been
     * completely deserialized.
     */
    private static class ValidationList {

        private static class Callback {
            final TObjectInputValidation obj;
            final int priority;
            TObjectInputStream.ValidationList.Callback next;
            final TAccessControlContext acc;

            Callback(TObjectInputValidation obj, int priority, TObjectInputStream.ValidationList.Callback next,
                    TAccessControlContext acc)
            {
                this.obj = obj;
                this.priority = priority;
                this.next = next;
                this.acc = acc;
            }
        }

        /** linked list of callbacks */
        private TObjectInputStream.ValidationList.Callback list;

        /**
         * Creates new (empty) ValidationList.
         */
        ValidationList() {
        }

        /**
         * Registers callback.  Throws InvalidObjectException if callback
         * object is null.
         */
        void register(TObjectInputValidation obj, int priority)
                throws TInvalidObjectException
        {
            if (obj == null) {
                throw new TInvalidObjectException(TString.wrap("null callback"));
            }

            TObjectInputStream.ValidationList.Callback prev = null, cur = list;
            while (cur != null && priority < cur.priority) {
                prev = cur;
                cur = cur.next;
            }
            TAccessControlContext acc = TAccessController.getContext();
            if (prev != null) {
                prev.next = new TObjectInputStream.ValidationList.Callback(obj, priority, cur, acc);
            } else {
                list = new TObjectInputStream.ValidationList.Callback(obj, priority, list, acc);
            }
        }

        /**
         * Invokes all registered callbacks and clears the callback list.
         * Callbacks with higher priorities are called first; those with equal
         * priorities may be called in any order.  If any of the callbacks
         * throws an InvalidObjectException, the callback process is terminated
         * and the exception propagated upwards.
         */
        void doCallbacks() throws TInvalidObjectException {
            try {
                while (list != null) {
                    TAccessController.doPrivileged(
                            new TPrivilegedExceptionAction<Void>()
                            {
                                public Void run() throws TInvalidObjectException {
                                    list.obj.validateObject();
                                    return null;
                                }
                            }, list.acc);
                    list = list.next;
                }
            } catch (PrivilegedActionException ex) {
                list = null;
                throw (TInvalidObjectException) ex.getException();
            }
        }

        /**
         * Resets the callback list to its initial (empty) state.
         */
        public void clear() {
            list = null;
        }
    }

    /**
     * Input stream supporting single-byte peek operations.
     */
    private static class PeekInputStream extends TInputStream {

        /** underlying stream */
        private final TInputStream in;
        /** peeked byte */
        private int peekb = -1;

        /**
         * Creates new PeekInputStream on top of given underlying stream.
         */
        PeekInputStream(TInputStream in) {
            this.in = in;
        }

        /**
         * Peeks at next byte value in stream.  Similar to read(), except
         * that it does not consume the read value.
         */
        int peek() throws TIOException {
            return (peekb >= 0) ? peekb : (peekb = in.read());
        }

        public int read() throws TIOException {
            if (peekb >= 0) {
                int v = peekb;
                peekb = -1;
                return v;
            } else {
                return in.read();
            }
        }

        public int read(byte[] b, int off, int len) throws TIOException {
            if (len == 0) {
                return 0;
            } else if (peekb < 0) {
                return in.read(b, off, len);
            } else {
                b[off++] = (byte) peekb;
                len--;
                peekb = -1;
                int n = in.read(b, off, len);
                return (n >= 0) ? (n + 1) : 1;
            }
        }

        void readFully(byte[] b, int off, int len) throws TIOException {
            int n = 0;
            while (n < len) {
                int count = read(b, off + n, len - n);
                if (count < 0) {
                    throw new TEOFException();
                }
                n += count;
            }
        }

        public long skip(long n) throws TIOException {
            if (n <= 0) {
                return 0;
            }
            int skipped = 0;
            if (peekb >= 0) {
                peekb = -1;
                skipped++;
                n--;
            }
            return skipped + skip(n);
        }

        public int available() throws TIOException {
            return in.available() + ((peekb >= 0) ? 1 : 0);
        }

        public void close() throws TIOException {
            in.close();
        }
    }

    /**
     * Input stream with two modes: in default mode, inputs data written in the
     * same format as DataOutputStream; in "block data" mode, inputs data
     * bracketed by block data markers (see object serialization specification
     * for details).  Buffering depends on block data mode: when in default
     * mode, no data is buffered in advance; when in block data mode, all data
     * for the current data block is read in at once (and buffered).
     */
    private class BlockDataInputStream
            extends TInputStream implements TDataInput
    {
        /** maximum data block length */
        private static final int MAX_BLOCK_SIZE = 1024;
        /** maximum data block header length */
        private static final int MAX_HEADER_SIZE = 5;
        /** (tunable) length of char buffer (for reading strings) */
        private static final int CHAR_BUF_SIZE = 256;
        /** readBlockHeader() return value indicating header read may block */
        private static final int HEADER_BLOCKED = -2;

        /** buffer for reading general/block data */
        private final byte[] buf = new byte[MAX_BLOCK_SIZE];
        /** buffer for reading block data headers */
        private final byte[] hbuf = new byte[MAX_HEADER_SIZE];
        /** char buffer for fast string reads */
        private final char[] cbuf = new char[CHAR_BUF_SIZE];

        /** block data mode */
        private boolean blkmode = false;

        // block data state fields; values meaningful only when blkmode true
        /** current offset into buf */
        private int pos = 0;
        /** end offset of valid data in buf, or -1 if no more block data */
        private int end = -1;
        /** number of bytes in current block yet to be read from stream */
        private int unread = 0;

        /** underlying stream (wrapped in peekable filter stream) */
        private final TObjectInputStream.PeekInputStream in;
        /** loopback stream (for data reads that span data blocks) */
        private final TDataInputStream din;

        /**
         * Creates new BlockDataInputStream on top of given underlying stream.
         * Block data mode is turned off by default.
         */
        BlockDataInputStream(TInputStream in) {
            this.in = new TObjectInputStream.PeekInputStream(in);
            din = new TDataInputStream(this);
        }

        /**
         * Sets block data mode to the given mode (true == on, false == off)
         * and returns the previous mode value.  If the new mode is the same as
         * the old mode, no action is taken.  Throws IllegalStateException if
         * block data mode is being switched from on to off while unconsumed
         * block data is still present in the stream.
         */
        boolean setBlockDataMode(boolean newmode) throws TIOException {
            if (blkmode == newmode) {
                return blkmode;
            }
            if (newmode) {
                pos = 0;
                end = 0;
                unread = 0;
            } else if (pos < end) {
                throw new IllegalStateException("unread block data");
            }
            blkmode = newmode;
            return !blkmode;
        }

        /**
         * Returns true if the stream is currently in block data mode, false
         * otherwise.
         */
        boolean getBlockDataMode() {
            return blkmode;
        }

        /**
         * If in block data mode, skips to the end of the current group of data
         * blocks (but does not unset block data mode).  If not in block data
         * mode, throws an IllegalStateException.
         */
        void skipBlockData() throws TIOException {
            if (!blkmode) {
                throw new IllegalStateException("not in block data mode");
            }
            while (end >= 0) {
                refill();
            }
        }

        /**
         * Attempts to read in the next block data header (if any).  If
         * canBlock is false and a full header cannot be read without possibly
         * blocking, returns HEADER_BLOCKED, else if the next element in the
         * stream is a block data header, returns the block data length
         * specified by the header, else returns -1.
         */
        private int readBlockHeader(boolean canBlock) throws TIOException {
            if (defaultDataEnd) {
                /*
                 * Fix for 4360508: stream is currently at the end of a field
                 * value block written via default serialization; since there
                 * is no terminating TC_ENDBLOCKDATA tag, simulate
                 * end-of-custom-data behavior explicitly.
                 */
                return -1;
            }
            try {
                for (;;) {
                    int avail = canBlock ? Integer.MAX_VALUE : in.available();
                    if (avail == 0) {
                        return HEADER_BLOCKED;
                    }

                    int tc = in.peek();
                    switch (tc) {
                        case TC_BLOCKDATA:
                            if (avail < 2) {
                                return HEADER_BLOCKED;
                            }
                            in.readFully(hbuf, 0, 2);
                            return hbuf[1] & 0xFF;

                        case TC_BLOCKDATALONG:
                            if (avail < 5) {
                                return HEADER_BLOCKED;
                            }
                            in.readFully(hbuf, 0, 5);
                            int len = TBits.getInt(hbuf, 1);
                            if (len < 0) {
                                throw new TStreamCorruptedException(
                                        TString.wrap("illegal block data header length: " +
                                                len));
                            }
                            return len;

                        /*
                         * TC_RESETs may occur in between data blocks.
                         * Unfortunately, this case must be parsed at a lower
                         * level than other typecodes, since primitive data
                         * reads may span data blocks separated by a TC_RESET.
                         */
                        case TC_RESET:
                            in.read();
                            handleReset();
                            break;

                        default:
                            if (tc >= 0 && (tc < TC_BASE || tc > TC_MAX)) {
                                throw new TStreamCorruptedException(TString.wrap(
                                        String.format("invalid type code: %02X",
                                                tc)));
                            }
                            return -1;
                    }
                }
            } catch (TEOFException ex) {
                throw new TStreamCorruptedException(TString.wrap(
                        "unexpected EOF while reading block data header"));
            }
        }

        /**
         * Refills internal buffer buf with block data.  Any data in buf at the
         * time of the call is considered consumed.  Sets the pos, end, and
         * unread fields to reflect the new amount of available block data; if
         * the next element in the stream is not a data block, sets pos and
         * unread to 0 and end to -1.
         */
        private void refill() throws TIOException {
            try {
                do {
                    pos = 0;
                    if (unread > 0) {
                        int n =
                                in.read(buf, 0, Math.min(unread, MAX_BLOCK_SIZE));
                        if (n >= 0) {
                            end = n;
                            unread -= n;
                        } else {
                            throw new TStreamCorruptedException(TString.wrap(
                                    "unexpected EOF in middle of data block"));
                        }
                    } else {
                        int n = readBlockHeader(true);
                        if (n >= 0) {
                            end = 0;
                            unread = n;
                        } else {
                            end = -1;
                            unread = 0;
                        }
                    }
                } while (pos == end);
            } catch (TIOException ex) {
                pos = 0;
                end = -1;
                unread = 0;
                throw ex;
            }
        }

        /**
         * If in block data mode, returns the number of unconsumed bytes
         * remaining in the current data block.  If not in block data mode,
         * throws an IllegalStateException.
         */
        int currentBlockRemaining() {
            if (blkmode) {
                return (end >= 0) ? (end - pos) + unread : 0;
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * Peeks at (but does not consume) and returns the next byte value in
         * the stream, or -1 if the end of the stream/block data (if in block
         * data mode) has been reached.
         */
        int peek() throws TIOException {
            if (blkmode) {
                if (pos == end) {
                    refill();
                }
                return (end >= 0) ? (buf[pos] & 0xFF) : -1;
            } else {
                return in.peek();
            }
        }

        /**
         * Peeks at (but does not consume) and returns the next byte value in
         * the stream, or throws EOFException if end of stream/block data has
         * been reached.
         */
        byte peekByte() throws TIOException {
            int val = peek();
            if (val < 0) {
                throw new TEOFException();
            }
            return (byte) val;
        }


        /* ----------------- generic input stream methods ------------------ */
        /*
         * The following methods are equivalent to their counterparts in
         * InputStream, except that they interpret data block boundaries and
         * read the requested data from within data blocks when in block data
         * mode.
         */

        public int read() throws TIOException {
            if (blkmode) {
                if (pos == end) {
                    refill();
                }
                return (end >= 0) ? (buf[pos++] & 0xFF) : -1;
            } else {
                return in.read();
            }
        }

        public int read(byte[] b, int off, int len) throws TIOException {
            return read(b, off, len, false);
        }

        public long skip(long len) throws TIOException {
            long remain = len;
            while (remain > 0) {
                if (blkmode) {
                    if (pos == end) {
                        refill();
                    }
                    if (end < 0) {
                        break;
                    }
                    int nread = (int) Math.min(remain, end - pos);
                    remain -= nread;
                    pos += nread;
                } else {
                    int nread = (int) Math.min(remain, MAX_BLOCK_SIZE);
                    if ((nread = in.read(buf, 0, nread)) < 0) {
                        break;
                    }
                    remain -= nread;
                }
            }
            return len - remain;
        }

        public int available() throws TIOException {
            if (blkmode) {
                if ((pos == end) && (unread == 0)) {
                    int n;
                    while ((n = readBlockHeader(false)) == 0) ;
                    switch (n) {
                        case HEADER_BLOCKED:
                            break;

                        case -1:
                            pos = 0;
                            end = -1;
                            break;

                        default:
                            pos = 0;
                            end = 0;
                            unread = n;
                            break;
                    }
                }
                // avoid unnecessary call to in.available() if possible
                int unreadAvail = (unread > 0) ?
                        Math.min(in.available(), unread) : 0;
                return (end >= 0) ? (end - pos) + unreadAvail : 0;
            } else {
                return in.available();
            }
        }

        public void close() throws TIOException {
            if (blkmode) {
                pos = 0;
                end = -1;
                unread = 0;
            }
            in.close();
        }

        /**
         * Attempts to read len bytes into byte array b at offset off.  Returns
         * the number of bytes read, or -1 if the end of stream/block data has
         * been reached.  If copy is true, reads values into an intermediate
         * buffer before copying them to b (to avoid exposing a reference to
         * b).
         */
        int read(byte[] b, int off, int len, boolean copy) throws TIOException {
            if (len == 0) {
                return 0;
            } else if (blkmode) {
                if (pos == end) {
                    refill();
                }
                if (end < 0) {
                    return -1;
                }
                int nread = Math.min(len, end - pos);
                System.arraycopy(buf, pos, b, off, nread);
                pos += nread;
                return nread;
            } else if (copy) {
                int nread = in.read(buf, 0, Math.min(len, MAX_BLOCK_SIZE));
                if (nread > 0) {
                    System.arraycopy(buf, 0, b, off, nread);
                }
                return nread;
            } else {
                return in.read(b, off, len);
            }
        }

        /* ----------------- primitive data input methods ------------------ */
        /*
         * The following methods are equivalent to their counterparts in
         * DataInputStream, except that they interpret data block boundaries
         * and read the requested data from within data blocks when in block
         * data mode.
         */

        public void readFully(byte[] b) throws TIOException {
            readFully(b, 0, b.length, false);
        }

        public void readFully(byte[] b, int off, int len) throws TIOException {
            readFully(b, off, len, false);
        }

        public void readFully(byte[] b, int off, int len, boolean copy)
                throws TIOException
        {
            while (len > 0) {
                int n = read(b, off, len, copy);
                if (n < 0) {
                    throw new TEOFException();
                }
                off += n;
                len -= n;
            }
        }

        public int skipBytes(int n) throws TIOException {
            return din.skipBytes(n);
        }

        public boolean readBoolean() throws TIOException {
            int v = read();
            if (v < 0) {
                throw new TEOFException();
            }
            return (v != 0);
        }

        public byte readByte() throws TIOException {
            int v = read();
            if (v < 0) {
                throw new TEOFException();
            }
            return (byte) v;
        }

        public int readUnsignedByte() throws TIOException {
            int v = read();
            if (v < 0) {
                throw new TEOFException();
            }
            return v;
        }

        public char readChar() throws TIOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 2);
            } else if (end - pos < 2) {
                return din.readChar();
            }
            char v = TBits.getChar(buf, pos);
            pos += 2;
            return v;
        }

        public short readShort() throws TIOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 2);
            } else if (end - pos < 2) {
                return din.readShort();
            }
            short v = TBits.getShort(buf, pos);
            pos += 2;
            return v;
        }

        public int readUnsignedShort() throws TIOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 2);
            } else if (end - pos < 2) {
                return din.readUnsignedShort();
            }
            int v = TBits.getShort(buf, pos) & 0xFFFF;
            pos += 2;
            return v;
        }

        public int readInt() throws TIOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 4);
            } else if (end - pos < 4) {
                return din.readInt();
            }
            int v = TBits.getInt(buf, pos);
            pos += 4;
            return v;
        }

        public float readFloat() throws TIOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 4);
            } else if (end - pos < 4) {
                return din.readFloat();
            }
            float v = TBits.getFloat(buf, pos);
            pos += 4;
            return v;
        }

        public long readLong() throws TIOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 8);
            } else if (end - pos < 8) {
                return din.readLong();
            }
            long v = TBits.getLong(buf, pos);
            pos += 8;
            return v;
        }

        public double readDouble() throws TIOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 8);
            } else if (end - pos < 8) {
                return din.readDouble();
            }
            double v = TBits.getDouble(buf, pos);
            pos += 8;
            return v;
        }

        public TString readUTF() throws TIOException {
            return readUTFBody(readUnsignedShort());
        }

        @SuppressWarnings("deprecation")
        public TString readLine() throws TIOException {
            return din.readLine();      // deprecated, not worth optimizing
        }

        /* -------------- primitive data array input methods --------------- */
        /*
         * The following methods read in spans of primitive data values.
         * Though equivalent to calling the corresponding primitive read
         * methods repeatedly, these methods are optimized for reading groups
         * of primitive data values more efficiently.
         */

        void readBooleans(boolean[] v, int off, int len) throws TIOException {
            int stop, endoff = off + len;
            while (off < endoff) {
                if (!blkmode) {
                    int span = Math.min(endoff - off, MAX_BLOCK_SIZE);
                    in.readFully(buf, 0, span);
                    stop = off + span;
                    pos = 0;
                } else if (end - pos < 1) {
                    v[off++] = din.readBoolean();
                    continue;
                } else {
                    stop = Math.min(endoff, off + end - pos);
                }

                while (off < stop) {
                    v[off++] = TBits.getBoolean(buf, pos++);
                }
            }
        }

        void readChars(char[] v, int off, int len) throws TIOException {
            int stop, endoff = off + len;
            while (off < endoff) {
                if (!blkmode) {
                    int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 1);
                    in.readFully(buf, 0, span << 1);
                    stop = off + span;
                    pos = 0;
                } else if (end - pos < 2) {
                    v[off++] = din.readChar();
                    continue;
                } else {
                    stop = Math.min(endoff, off + ((end - pos) >> 1));
                }

                while (off < stop) {
                    v[off++] = TBits.getChar(buf, pos);
                    pos += 2;
                }
            }
        }

        void readShorts(short[] v, int off, int len) throws TIOException {
            int stop, endoff = off + len;
            while (off < endoff) {
                if (!blkmode) {
                    int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 1);
                    in.readFully(buf, 0, span << 1);
                    stop = off + span;
                    pos = 0;
                } else if (end - pos < 2) {
                    v[off++] = din.readShort();
                    continue;
                } else {
                    stop = Math.min(endoff, off + ((end - pos) >> 1));
                }

                while (off < stop) {
                    v[off++] = TBits.getShort(buf, pos);
                    pos += 2;
                }
            }
        }

        void readInts(int[] v, int off, int len) throws TIOException {
            int stop, endoff = off + len;
            while (off < endoff) {
                if (!blkmode) {
                    int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 2);
                    in.readFully(buf, 0, span << 2);
                    stop = off + span;
                    pos = 0;
                } else if (end - pos < 4) {
                    v[off++] = din.readInt();
                    continue;
                } else {
                    stop = Math.min(endoff, off + ((end - pos) >> 2));
                }

                while (off < stop) {
                    v[off++] = TBits.getInt(buf, pos);
                    pos += 4;
                }
            }
        }

        void readFloats(float[] v, int off, int len) throws TIOException {
            int span, endoff = off + len;
            while (off < endoff) {
                if (!blkmode) {
                    span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 2);
                    in.readFully(buf, 0, span << 2);
                    pos = 0;
                } else if (end - pos < 4) {
                    v[off++] = din.readFloat();
                    continue;
                } else {
                    span = Math.min(endoff - off, ((end - pos) >> 2));
                }

                bytesToFloats(buf, pos, v, off, span);
                off += span;
                pos += span << 2;
            }
        }

        void readLongs(long[] v, int off, int len) throws TIOException {
            int stop, endoff = off + len;
            while (off < endoff) {
                if (!blkmode) {
                    int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 3);
                    in.readFully(buf, 0, span << 3);
                    stop = off + span;
                    pos = 0;
                } else if (end - pos < 8) {
                    v[off++] = din.readLong();
                    continue;
                } else {
                    stop = Math.min(endoff, off + ((end - pos) >> 3));
                }

                while (off < stop) {
                    v[off++] = TBits.getLong(buf, pos);
                    pos += 8;
                }
            }
        }

        void readDoubles(double[] v, int off, int len) throws TIOException {
            int span, endoff = off + len;
            while (off < endoff) {
                if (!blkmode) {
                    span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 3);
                    in.readFully(buf, 0, span << 3);
                    pos = 0;
                } else if (end - pos < 8) {
                    v[off++] = din.readDouble();
                    continue;
                } else {
                    span = Math.min(endoff - off, ((end - pos) >> 3));
                }

                bytesToDoubles(buf, pos, v, off, span);
                off += span;
                pos += span << 3;
            }
        }

        /**
         * Reads in string written in "long" UTF format.  "Long" UTF format is
         * identical to standard UTF, except that it uses an 8 byte header
         * (instead of the standard 2 bytes) to convey the UTF encoding length.
         */
        TString readLongUTF() throws TIOException {
            return readUTFBody(readLong());
        }

        /**
         * Reads in the "body" (i.e., the UTF representation minus the 2-byte
         * or 8-byte length header) of a UTF encoding, which occupies the next
         * utflen bytes.
         */
        private TString readUTFBody(long utflen) throws TIOException {
            StringBuilder sbuf = new StringBuilder();
            if (!blkmode) {
                end = pos = 0;
            }

            while (utflen > 0) {
                int avail = end - pos;
                if (avail >= 3 || (long) avail == utflen) {
                    utflen -= readUTFSpan(sbuf, utflen);
                } else {
                    if (blkmode) {
                        // near block boundary, read one byte at a time
                        utflen -= readUTFChar(sbuf, utflen);
                    } else {
                        // shift and refill buffer manually
                        if (avail > 0) {
                            System.arraycopy(buf, pos, buf, 0, avail);
                        }
                        pos = 0;
                        end = (int) Math.min(MAX_BLOCK_SIZE, utflen);
                        in.readFully(buf, avail, end - avail);
                    }
                }
            }

            return TString.wrap(sbuf.toString());
        }

        /**
         * Reads span of UTF-encoded characters out of internal buffer
         * (starting at offset pos and ending at or before offset end),
         * consuming no more than utflen bytes.  Appends read characters to
         * sbuf.  Returns the number of bytes consumed.
         */
        private long readUTFSpan(StringBuilder sbuf, long utflen)
                throws TIOException
        {
            int cpos = 0;
            int start = pos;
            int avail = Math.min(end - pos, CHAR_BUF_SIZE);
            // stop short of last char unless all of utf bytes in buffer
            int stop = pos + ((utflen > avail) ? avail - 2 : (int) utflen);
            boolean outOfBounds = false;

            try {
                while (pos < stop) {
                    int b1, b2, b3;
                    b1 = buf[pos++] & 0xFF;
                    switch (b1 >> 4) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:   // 1 byte format: 0xxxxxxx
                            cbuf[cpos++] = (char) b1;
                            break;

                        case 12:
                        case 13:  // 2 byte format: 110xxxxx 10xxxxxx
                            b2 = buf[pos++];
                            if ((b2 & 0xC0) != 0x80) {
                                throw new TUTFDataFormatException();
                            }
                            cbuf[cpos++] = (char) (((b1 & 0x1F) << 6) |
                                    ((b2 & 0x3F) << 0));
                            break;

                        case 14:  // 3 byte format: 1110xxxx 10xxxxxx 10xxxxxx
                            b3 = buf[pos + 1];
                            b2 = buf[pos + 0];
                            pos += 2;
                            if ((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
                                throw new TUTFDataFormatException();
                            }
                            cbuf[cpos++] = (char) (((b1 & 0x0F) << 12) |
                                    ((b2 & 0x3F) << 6) |
                                    ((b3 & 0x3F) << 0));
                            break;

                        default:  // 10xx xxxx, 1111 xxxx
                            throw new TUTFDataFormatException();
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                outOfBounds = true;
            } finally {
                if (outOfBounds || (pos - start) > utflen) {
                    /*
                     * Fix for 4450867: if a malformed utf char causes the
                     * conversion loop to scan past the expected end of the utf
                     * string, only consume the expected number of utf bytes.
                     */
                    pos = start + (int) utflen;
                    throw new TUTFDataFormatException();
                }
            }

            sbuf.append(cbuf, 0, cpos);
            return pos - start;
        }

        /**
         * Reads in single UTF-encoded character one byte at a time, appends
         * the character to sbuf, and returns the number of bytes consumed.
         * This method is used when reading in UTF strings written in block
         * data mode to handle UTF-encoded characters which (potentially)
         * straddle block-data boundaries.
         */
        private int readUTFChar(StringBuilder sbuf, long utflen)
                throws TIOException
        {
            int b1, b2, b3;
            b1 = readByte() & 0xFF;
            switch (b1 >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:     // 1 byte format: 0xxxxxxx
                    sbuf.append((char) b1);
                    return 1;

                case 12:
                case 13:    // 2 byte format: 110xxxxx 10xxxxxx
                    if (utflen < 2) {
                        throw new TUTFDataFormatException();
                    }
                    b2 = readByte();
                    if ((b2 & 0xC0) != 0x80) {
                        throw new TUTFDataFormatException();
                    }
                    sbuf.append((char) (((b1 & 0x1F) << 6) |
                            ((b2 & 0x3F) << 0)));
                    return 2;

                case 14:    // 3 byte format: 1110xxxx 10xxxxxx 10xxxxxx
                    if (utflen < 3) {
                        if (utflen == 2) {
                            readByte();         // consume remaining byte
                        }
                        throw new TUTFDataFormatException();
                    }
                    b2 = readByte();
                    b3 = readByte();
                    if ((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
                        throw new TUTFDataFormatException();
                    }
                    sbuf.append((char) (((b1 & 0x0F) << 12) |
                            ((b2 & 0x3F) << 6) |
                            ((b3 & 0x3F) << 0)));
                    return 3;

                default:   // 10xx xxxx, 1111 xxxx
                    throw new TUTFDataFormatException();
            }
        }
    }

    /**
     * Unsynchronized table which tracks wire handle to object mappings, as
     * well as ClassNotFoundExceptions associated with deserialized objects.
     * This class implements an exception-propagation algorithm for
     * determining which objects should have ClassNotFoundExceptions associated
     * with them, taking into account cycles and discontinuities (e.g., skipped
     * fields) in the object graph.
     *
     * <p>General use of the table is as follows: during deserialization, a
     * given object is first assigned a handle by calling the assign method.
     * This method leaves the assigned handle in an "open" state, wherein
     * dependencies on the exception status of other handles can be registered
     * by calling the markDependency method, or an exception can be directly
     * associated with the handle by calling markException.  When a handle is
     * tagged with an exception, the HandleTable assumes responsibility for
     * propagating the exception to any other objects which depend
     * (transitively) on the exception-tagged object.
     *
     * <p>Once all exception information/dependencies for the handle have been
     * registered, the handle should be "closed" by calling the finish method
     * on it.  The act of finishing a handle allows the exception propagation
     * algorithm to aggressively prune dependency links, lessening the
     * performance/memory impact of exception tracking.
     *
     * <p>Note that the exception propagation algorithm used depends on handles
     * being assigned/finished in LIFO order; however, for simplicity as well
     * as memory conservation, it does not enforce this constraint.
     */
    // REMIND: add full description of exception propagation algorithm?
    private static class HandleTable {

        /* status codes indicating whether object has associated exception */
        private static final byte STATUS_OK = 1;
        private static final byte STATUS_UNKNOWN = 2;
        private static final byte STATUS_EXCEPTION = 3;

        /** array mapping handle -> object status */
        byte[] status;
        /** array mapping handle -> object/exception (depending on status) */
        TObject[] entries;
        /** array mapping handle -> list of dependent handles (if any) */
        TObjectInputStream.HandleTable.HandleList[] deps;
        /** lowest unresolved dependency */
        int lowDep = -1;
        /** number of handles in table */
        int size = 0;

        /**
         * Creates handle table with the given initial capacity.
         */
        HandleTable(int initialCapacity) {
            status = new byte[initialCapacity];
            entries = new TObject[initialCapacity];
            deps = new TObjectInputStream.HandleTable.HandleList[initialCapacity];
        }

        /**
         * Assigns next available handle to given object, and returns assigned
         * handle.  Once object has been completely deserialized (and all
         * dependencies on other objects identified), the handle should be
         * "closed" by passing it to finish().
         */
        int assign(Object obj) {
            if (size >= entries.length) {
                grow();
            }
            status[size] = STATUS_UNKNOWN;
            entries[size] = obj;
            return size++;
        }

        /**
         * Registers a dependency (in exception status) of one handle on
         * another.  The dependent handle must be "open" (i.e., assigned, but
         * not finished yet).  No action is taken if either dependent or target
         * handle is NULL_HANDLE.
         */
        void markDependency(int dependent, int target) {
            if (dependent == NULL_HANDLE || target == NULL_HANDLE) {
                return;
            }
            switch (status[dependent]) {

                case STATUS_UNKNOWN:
                    switch (status[target]) {
                        case STATUS_OK:
                            // ignore dependencies on objs with no exception
                            break;

                        case STATUS_EXCEPTION:
                            // eagerly propagate exception
                            markException(dependent,
                                    (ClassNotFoundException) entries[target]);
                            break;

                        case STATUS_UNKNOWN:
                            // add to dependency list of target
                            if (deps[target] == null) {
                                deps[target] = new TObjectInputStream.HandleTable.HandleList();
                            }
                            deps[target].add(dependent);

                            // remember lowest unresolved target seen
                            if (lowDep < 0 || lowDep > target) {
                                lowDep = target;
                            }
                            break;

                        default:
                            throw new InternalError();
                    }
                    break;

                case STATUS_EXCEPTION:
                    break;

                default:
                    throw new InternalError();
            }
        }

        /**
         * Associates a ClassNotFoundException (if one not already associated)
         * with the currently active handle and propagates it to other
         * referencing objects as appropriate.  The specified handle must be
         * "open" (i.e., assigned, but not finished yet).
         */
        void markException(int handle, ClassNotFoundException ex) {
            switch (status[handle]) {
                case STATUS_UNKNOWN:
                    status[handle] = STATUS_EXCEPTION;
                    entries[handle] = ex;

                    // propagate exception to dependents
                    TObjectInputStream.HandleTable.HandleList dlist = deps[handle];
                    if (dlist != null) {
                        int ndeps = dlist.size();
                        for (int i = 0; i < ndeps; i++) {
                            markException(dlist.get(i), ex);
                        }
                        deps[handle] = null;
                    }
                    break;

                case STATUS_EXCEPTION:
                    break;

                default:
                    throw new InternalError();
            }
        }

        /**
         * Marks given handle as finished, meaning that no new dependencies
         * will be marked for handle.  Calls to the assign and finish methods
         * must occur in LIFO order.
         */
        void finish(int handle) {
            int end;
            if (lowDep < 0) {
                // no pending unknowns, only resolve current handle
                end = handle + 1;
            } else if (lowDep >= handle) {
                // pending unknowns now clearable, resolve all upward handles
                end = size;
                lowDep = -1;
            } else {
                // unresolved backrefs present, can't resolve anything yet
                return;
            }

            // change STATUS_UNKNOWN -> STATUS_OK in selected span of handles
            for (int i = handle; i < end; i++) {
                switch (status[i]) {
                    case STATUS_UNKNOWN:
                        status[i] = STATUS_OK;
                        deps[i] = null;
                        break;

                    case STATUS_OK:
                    case STATUS_EXCEPTION:
                        break;

                    default:
                        throw new InternalError();
                }
            }
        }

        /**
         * Assigns a new object to the given handle.  The object previously
         * associated with the handle is forgotten.  This method has no effect
         * if the given handle already has an exception associated with it.
         * This method may be called at any time after the handle is assigned.
         */
        void setObject(int handle, Object obj) {
            switch (status[handle]) {
                case STATUS_UNKNOWN:
                case STATUS_OK:
                    entries[handle] = obj;
                    break;

                case STATUS_EXCEPTION:
                    break;

                default:
                    throw new InternalError();
            }
        }

        /**
         * Looks up and returns object associated with the given handle.
         * Returns null if the given handle is NULL_HANDLE, or if it has an
         * associated ClassNotFoundException.
         */
        Object lookupObject(int handle) {
            return (handle != NULL_HANDLE &&
                    status[handle] != STATUS_EXCEPTION) ?
                    entries[handle] : null;
        }

        /**
         * Looks up and returns ClassNotFoundException associated with the
         * given handle.  Returns null if the given handle is NULL_HANDLE, or
         * if there is no ClassNotFoundException associated with the handle.
         */
        ClassNotFoundException lookupException(int handle) {
            return (handle != NULL_HANDLE &&
                    status[handle] == STATUS_EXCEPTION) ?
                    (ClassNotFoundException) entries[handle] : null;
        }

        /**
         * Resets table to its initial state.
         */
        void clear() {
            TArrays.fill(status, 0, size, (byte) 0);
            TArrays.fill(entries, 0, size, null);
            TArrays.fill(deps, 0, size, null);
            lowDep = -1;
            size = 0;
        }

        /**
         * Returns number of handles registered in table.
         */
        int size() {
            return size;
        }

        /**
         * Expands capacity of internal arrays.
         */
        private void grow() {
            int newCapacity = (entries.length << 1) + 1;

            byte[] newStatus = new byte[newCapacity];
            TObject[] newEntries = new TObject[newCapacity];
            TObjectInputStream.HandleTable.HandleList[] newDeps = new TObjectInputStream.HandleTable.HandleList[newCapacity];

            System.arraycopy(status, 0, newStatus, 0, size);
            System.arraycopy(entries, 0, newEntries, 0, size);
            System.arraycopy(deps, 0, newDeps, 0, size);

            status = newStatus;
            entries = newEntries;
            deps = newDeps;
        }

        /**
         * Simple growable list of (integer) handles.
         */
        private static class HandleList {
            private int[] list = new int[4];
            private int size = 0;

            public HandleList() {
            }

            public void add(int handle) {
                if (size >= list.length) {
                    int[] newList = new int[list.length << 1];
                    System.arraycopy(list, 0, newList, 0, list.length);
                    list = newList;
                }
                list[size++] = handle;
            }

            public int get(int index) {
                if (index >= size) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                return list[index];
            }

            public int size() {
                return size;
            }
        }
    }

    /**
     * Method for cloning arrays in case of using unsharing reading
     */
    private static Object cloneArray(Object array) {
        if (array instanceof Object[]) {
            return ((Object[]) array).clone();
        } else if (array instanceof boolean[]) {
            return ((boolean[]) array).clone();
        } else if (array instanceof byte[]) {
            return ((byte[]) array).clone();
        } else if (array instanceof char[]) {
            return ((char[]) array).clone();
        } else if (array instanceof double[]) {
            return ((double[]) array).clone();
        } else if (array instanceof float[]) {
            return ((float[]) array).clone();
        } else if (array instanceof int[]) {
            return ((int[]) array).clone();
        } else if (array instanceof long[]) {
            return ((long[]) array).clone();
        } else if (array instanceof short[]) {
            return ((short[]) array).clone();
        } else {
            throw new AssertionError();
        }
    }

}

