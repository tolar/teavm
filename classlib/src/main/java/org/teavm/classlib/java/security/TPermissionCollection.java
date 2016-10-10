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
package org.teavm.classlib.java.security;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public abstract class TPermissionCollection implements java.io.Serializable {

    private static final long serialVersionUID = -6727011328946861783L;

    // when set, add will throw an exception.
    private volatile boolean readOnly;

    /**
     * Adds a permission object to the current collection of permission objects.
     *
     * @param permission the Permission object to add.
     *
     * @exception SecurityException -  if this PermissionCollection object
     *                                 has been marked readonly
     * @exception IllegalArgumentException - if this PermissionCollection
     *                object is a homogeneous collection and the permission
     *                is not of the correct type.
     */
    public abstract void add(TPermission permission);

    /**
     * Checks to see if the specified permission is implied by
     * the collection of Permission objects held in this PermissionCollection.
     *
     * @param permission the Permission object to compare.
     *
     * @return true if "permission" is implied by the  permissions in
     * the collection, false if not.
     */
    public abstract boolean implies(TPermission permission);

    /**
     * Returns an enumeration of all the Permission objects in the collection.
     *
     * @return an enumeration of all the Permissions.
     */
    public abstract Enumeration<TPermission> elements();

    /**
     * Marks this PermissionCollection object as "readonly". After
     * a PermissionCollection object
     * is marked as readonly, no new Permission objects can be added to it
     * using {@code add}.
     */
    public void setReadOnly() {
        readOnly = true;
    }

    /**
     * Returns true if this PermissionCollection object is marked as readonly.
     * If it is readonly, no new Permission objects can be added to it
     * using {@code add}.
     *
     * <p>By default, the object is <i>not</i> readonly. It can be set to
     * readonly by a call to {@code setReadOnly}.
     *
     * @return true if this PermissionCollection object is marked as readonly,
     * false otherwise.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns a string describing this PermissionCollection object,
     * providing information about all the permissions it contains.
     * The format is:
     * <pre>
     * super.toString() (
     *   // enumerate all the Permission
     *   // objects and call toString() on them,
     *   // one per line..
     * )</pre>
     *
     * {@code super.toString} is a call to the {@code toString}
     * method of this
     * object's superclass, which is Object. The result is
     * this PermissionCollection's type name followed by this object's
     * hashcode, thus enabling clients to differentiate different
     * PermissionCollections object, even if they contain the same permissions.
     *
     * @return information about this PermissionCollection object,
     *         as described above.
     *
     */
    public String toString() {
        Enumeration<TPermission> enum_ = elements();
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()+" (\n");
        while (enum_.hasMoreElements()) {
            try {
                sb.append(" ");
                sb.append(enum_.nextElement().toString());
                sb.append("\n");
            } catch (NoSuchElementException e){
                // ignore
            }
        }
        sb.append(")\n");
        return sb.toString();
    }
}