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

import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.lang.reflect.TField;
import org.teavm.classlib.sun.reflect.TReflection;
import org.teavm.classlib.sun.reflect.misc.TReflectUtil;

public class TObjectStreamField
        implements Comparable<Object>
{

    /** field name */
    private final TString name;
    /** canonical JVM signature of field type */
    private final TString signature;
    /** field type (Object.class if unknown non-primitive type) */
    private final TClass<?> type;
    /** whether or not to (de)serialize field values as unshared */
    private final boolean unshared;
    /** corresponding reflective field object, if any */
    private final TField field;
    /** offset of field value in enclosing field group */
    private int offset = 0;

    /**
     * Create a Serializable field with the specified type.  This field should
     * be documented with a <code>serialField</code> tag.
     *
     * @param   name the name of the serializable field
     * @param   type the <code>Class</code> object of the serializable field
     */
    public TObjectStreamField(TString name, TClass<?> type) {
        this(name, type, false);
    }

    /**
     * Creates an TObjectStreamField representing a serializable field with the
     * given name and type.  If unshared is false, values of the represented
     * field are serialized and deserialized in the default manner--if the
     * field is non-primitive, object values are serialized and deserialized as
     * if they had been written and read by calls to writeObject and
     * readObject.  If unshared is true, values of the represented field are
     * serialized and deserialized as if they had been written and read by
     * calls to writeUnshared and readUnshared.
     *
     * @param   name field name
     * @param   type field type
     * @param   unshared if false, write/read field values in the same manner
     *          as writeObject/readObject; if true, write/read in the same
     *          manner as writeUnshared/readUnshared
     * @since   1.4
     */
    public TObjectStreamField(TString name, TClass<?> type, boolean unshared) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.type = type;
        this.unshared = unshared;
        signature = getClassSignature(type).intern();
        field = null;
    }

    /**
     * Creates an TObjectStreamField representing a field with the given name,
     * signature and unshared setting.
     */
    TObjectStreamField(TString name, TString signature, boolean unshared) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.signature = signature.intern();
        this.unshared = unshared;
        field = null;

        switch (signature.charAt(0)) {
            case 'Z': type = Boolean.TYPE; break;
            case 'B': type = Byte.TYPE; break;
            case 'C': type = Character.TYPE; break;
            case 'S': type = Short.TYPE; break;
            case 'I': type = Integer.TYPE; break;
            case 'J': type = Long.TYPE; break;
            case 'F': type = Float.TYPE; break;
            case 'D': type = Double.TYPE; break;
            case 'L':
            case '[': type = Object.class; break;
            default: throw new IllegalArgumentException("illegal signature");
        }
    }

    /**
     * Creates an TObjectStreamField representing the given field with the
     * specified unshared setting.  For compatibility with the behavior of
     * earlier serialization implementations, a "showType" parameter is
     * necessary to govern whether or not a getType() call on this
     * TObjectStreamField (if non-primitive) will return Object.class (as
     * opposed to a more specific reference type).
     */
    TObjectStreamField(Field field, boolean unshared, boolean showType) {
        this.field = field;
        this.unshared = unshared;
        name = field.getName();
        Class<?> ftype = field.getType();
        type = (showType || ftype.isPrimitive()) ? ftype : Object.class;
        signature = getClassSignature(ftype).intern();
    }

    /**
     * Get the name of this field.
     *
     * @return  a <code>String</code> representing the name of the serializable
     *          field
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of the field.  If the type is non-primitive and this
     * <code>TObjectStreamField</code> was obtained from a deserialized {@link
     * ObjectStreamClass} instance, then <code>Object.class</code> is returned.
     * Otherwise, the <code>Class</code> object for the type of the field is
     * returned.
     *
     * @return  a <code>Class</code> object representing the type of the
     *          serializable field
     */
    public Class<?> getType() {
        if (System.getSecurityManager() != null) {
            TClass<?> caller = TReflection.getCallerClass();
            if (TReflectUtil.needsPackageAccessCheck(caller.getClassLoader(), type.getClassLoader())) {
                TReflectUtil.checkPackageAccess(type);
            }
        }
        return type;
    }

    /**
     * Returns character encoding of field type.  The encoding is as follows:
     * <blockquote><pre>
     * B            byte
     * C            char
     * D            double
     * F            float
     * I            int
     * J            long
     * L            class or interface
     * S            short
     * Z            boolean
     * [            array
     * </pre></blockquote>
     *
     * @return  the typecode of the serializable field
     */
    // REMIND: deprecate?
    public char getTypeCode() {
        return signature.charAt(0);
    }

    /**
     * Return the JVM type signature.
     *
     * @return  null if this field has a primitive type.
     */
    // REMIND: deprecate?
    public String getTypeString() {
        return isPrimitive() ? null : signature;
    }

    /**
     * Offset of field within instance data.
     *
     * @return  the offset of this field
     * @see #setOffset
     */
    // REMIND: deprecate?
    public int getOffset() {
        return offset;
    }

    /**
     * Offset within instance data.
     *
     * @param   offset the offset of the field
     * @see #getOffset
     */
    // REMIND: deprecate?
    protected void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Return true if this field has a primitive type.
     *
     * @return  true if and only if this field corresponds to a primitive type
     */
    // REMIND: deprecate?
    public boolean isPrimitive() {
        char tcode = signature.charAt(0);
        return ((tcode != 'L') && (tcode != '['));
    }

    /**
     * Returns boolean value indicating whether or not the serializable field
     * represented by this TObjectStreamField instance is unshared.
     *
     * @return {@code true} if this field is unshared
     *
     * @since 1.4
     */
    public boolean isUnshared() {
        return unshared;
    }

    /**
     * Compare this field with another <code>TObjectStreamField</code>.  Return
     * -1 if this is smaller, 0 if equal, 1 if greater.  Types that are
     * primitives are "smaller" than object types.  If equal, the field names
     * are compared.
     */
    // REMIND: deprecate?
    public int compareTo(Object obj) {
        java.io.ObjectStreamField other = (java.io.ObjectStreamField) obj;
        boolean isPrim = isPrimitive();
        if (isPrim != other.isPrimitive()) {
            return isPrim ? -1 : 1;
        }
        return name.compareTo(other.name);
    }

    /**
     * Return a string that describes this field.
     */
    public String toString() {
        return signature + ' ' + name;
    }

    /**
     * Returns field represented by this TObjectStreamField, or null if
     * TObjectStreamField is not associated with an actual field.
     */
    Field getField() {
        return field;
    }

    /**
     * Returns JVM type signature of field (similar to getTypeString, except
     * that signature strings are returned for primitive fields as well).
     */
    TString getSignature() {
        return signature;
    }

    /**
     * Returns JVM type signature for given class.
     */
    private static String getClassSignature(TClass<?> cl) {
        StringBuilder sbuf = new StringBuilder();
        while (cl.isArray()) {
            sbuf.append('[');
            cl = cl.getComponentType();
        }
        if (cl.isPrimitive()) {
            if (cl == Integer.TYPE) {
                sbuf.append('I');
            } else if (cl == Byte.TYPE) {
                sbuf.append('B');
            } else if (cl == Long.TYPE) {
                sbuf.append('J');
            } else if (cl == Float.TYPE) {
                sbuf.append('F');
            } else if (cl == Double.TYPE) {
                sbuf.append('D');
            } else if (cl == Short.TYPE) {
                sbuf.append('S');
            } else if (cl == Character.TYPE) {
                sbuf.append('C');
            } else if (cl == Boolean.TYPE) {
                sbuf.append('Z');
            } else if (cl == Void.TYPE) {
                sbuf.append('V');
            } else {
                throw new InternalError();
            }
        } else {
            sbuf.append('L' + cl.getName().replace('.', '/') + ';');
        }
        return sbuf.toString();
    }
}