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
package org.teavm.classlib.java.lang.reflect;

import java.lang.reflect.Constructor;

import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.annotation.TAnnotation;
import org.teavm.classlib.java.security.TAccessController;
import org.teavm.classlib.java.security.TPermission;
import org.teavm.classlib.sun.reflect.TReflectionFactory;

/**
 * Created by vasek on 4. 7. 2016.
 */
public class TAccessibleObject implements TAnnotatedElement {

    /**
     * The Permission object that is used to check whether a client
     * has sufficient privilege to defeat Java language access
     * control checks.
     */
    static final private TPermission ACCESS_PERMISSION =
            new TReflectPermission("suppressAccessChecks");

    /**
     * Convenience method to set the {@code accessible} flag for an
     * array of objects with a single security check (for efficiency).
     *
     * <p>First, if there is a security manager, its
     * {@code checkPermission} method is called with a
     * {@code ReflectPermission("suppressAccessChecks")} permission.
     *
     * <p>A {@code SecurityException} is raised if {@code flag} is
     * {@code true} but accessibility of any of the elements of the input
     * {@code array} may not be changed (for example, if the element
     * object is a {@link Constructor} object for the class {@link
     * java.lang.Class}).  In the event of such a SecurityException, the
     * accessibility of objects is set to {@code flag} for array elements
     * upto (and excluding) the element for which the exception occurred; the
     * accessibility of elements beyond (and including) the element for which
     * the exception occurred is unchanged.
     *
     * @param array the array of AccessibleObjects
     * @param flag  the new value for the {@code accessible} flag
     *              in each object
     * @throws SecurityException if the request is denied.
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     */
    public static void setAccessible(TAccessibleObject[] array, boolean flag)
            throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        //if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        for (int i = 0; i < array.length; i++) {
            setAccessible0(array[i], flag);
        }
    }

    /**
     * Set the {@code accessible} flag for this object to
     * the indicated boolean value.  A value of {@code true} indicates that
     * the reflected object should suppress Java language access
     * checking when it is used.  A value of {@code false} indicates
     * that the reflected object should enforce Java language access checks.
     *
     * <p>First, if there is a security manager, its
     * {@code checkPermission} method is called with a
     * {@code ReflectPermission("suppressAccessChecks")} permission.
     *
     * <p>A {@code SecurityException} is raised if {@code flag} is
     * {@code true} but accessibility of this object may not be changed
     * (for example, if this element object is a {@link Constructor} object for
     * the class {@link java.lang.Class}).
     *
     * <p>A {@code SecurityException} is raised if this object is a {@link
     * java.lang.reflect.Constructor} object for the class
     * {@code java.lang.Class}, and {@code flag} is true.
     *
     * @param flag the new value for the {@code accessible} flag
     * @throws SecurityException if the request is denied.
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     */
    public void setAccessible(boolean flag) throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        //if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        setAccessible0(this, flag);
    }

    /* Check that you aren't exposing java.lang.Class.<init> or sensitive
       fields in java.lang.Class. */
    private static void setAccessible0(org.teavm.classlib.java.lang.reflect.TAccessibleObject obj, boolean flag)
            throws SecurityException
    {
        obj.override = flag;
    }

    /**
     * Get the value of the {@code accessible} flag for this object.
     *
     * @return the value of the object's {@code accessible} flag
     */
    public boolean isAccessible() {
        return override;
    }

    /**
     * Constructor: only used by the Java Virtual Machine.
     */
    protected TAccessibleObject() {}

    // Indicates whether language-level access checks are overridden
    // by this object. Initializes to "false". This field is used by
    // Field, Method, and Constructor.
    //
    // NOTE: for security purposes, this field must not be visible
    // outside this package.
    boolean override;

    // Reflection factory used by subclasses for creating field,
    // method, and constructor accessors. Note that this is called
    // very early in the bootstrapping process.
    static final TReflectionFactory reflectionFactory =
            TAccessController.doPrivileged(
                    new TReflectionFactory.GetReflectionFactoryAction());

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    public <T extends TAnnotation> T getAnnotation(TClass<T> annotationClass) {
        throw new AssertionError("All subclasses should override this method");
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isAnnotationPresent(TClass<? extends TAnnotation> annotationClass) {
        return false;
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    public <T extends TAnnotation> T[] getAnnotationsByType(TClass<T> annotationClass) {
        throw new AssertionError("All subclasses should override this method");
    }

    /**
     * @since 1.5
     */
    public TAnnotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    public <T extends TAnnotation> T getDeclaredAnnotation(TClass<T> annotationClass) {
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotation is the same as
        // getAnnotation.
        return getAnnotation(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    public <T extends TAnnotation> T[] getDeclaredAnnotationsByType(TClass<T> annotationClass) {
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotationsByType is the same as
        // getAnnotationsByType.
        return getAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public TAnnotation[] getDeclaredAnnotations()  {
        throw new AssertionError("All subclasses should override this method");
    }


    // Shared access checking logic.

    // For non-public members or members in package-private classes,
    // it is necessary to perform somewhat expensive security checks.
    // If the security check succeeds for a given class, it will
    // always succeed (it is not affected by the granting or revoking
    // of permissions); we speed up the check in the common case by
    // remembering the last Class for which the check succeeded.
    //
    // The simple security check for Constructor is to see if
    // the caller has already been seen, verified, and cached.
    // (See also Class.newInstance(), which uses a similar method.)
    //
    // A more complicated security check cache is needed for Method and Field
    // The cache can be either null (empty cache), a 2-array of {caller,target},
    // or a caller (with target implicitly equal to this.clazz).
    // In the 2-array case, the target is always different from the clazz.
    volatile Object securityCheckCache;

    void checkAccess(TClass<?> caller, TClass<?> clazz, Object obj, int modifiers)
            throws IllegalAccessException
    {
        return;
    }

    // Keep all this slow stuff out of line:
    void slowCheckMemberAccess(Class<?> caller, Class<?> clazz, Object obj, int modifiers,
            Class<?> targetClass)
            throws IllegalAccessException
    {
        //TReflection.ensureMemberAccess(caller, clazz, obj, modifiers);

        // Success: Update the cache.
        Object cache = ((targetClass == clazz)
                ? caller
                : new Class<?>[] { caller, targetClass });

        // Note:  The two cache elements are not volatile,
        // but they are effectively final.  The Java memory model
        // guarantees that the initializing stores for the cache
        // elements will occur before the volatile write.
        securityCheckCache = cache;         // write volatile
    }
}
