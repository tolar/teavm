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

import java.security.PrivilegedActionException;

import org.teavm.classlib.sun.misc.TCallerSensitive;

/**
 * @author: Vaclav Tolar, (vaclav_tolar@kb.cz, vaclav.tolar@cleverlance.com, vaclav.tolar@gmail.com)
 * Date: 2016-07-12
 */
public final class TAccessController {

    /**
     * Don't allow anyone to instantiate an AccessController
     */
    private TAccessController() { }


    @TCallerSensitive
    public static native <T> T doPrivileged(TPrivilegedAction<T> action);



    @TCallerSensitive
    public static native <T> T doPrivileged(TPrivilegedAction<T> action,
            TAccessControlContext context);







    @TCallerSensitive
    public static native <T> T
    doPrivileged(TPrivilegedExceptionAction<T> action)
            throws PrivilegedActionException;









    @TCallerSensitive
    public static native <T> T
    doPrivileged(TPrivilegedExceptionAction<T> action,
            TAccessControlContext context)
            throws PrivilegedActionException;






    /**
     * Returns the AccessControl context. i.e., it gets
     * the protection domains of all the callers on the stack,
     * starting at the first class with a non-null
     * ProtectionDomain.
     *
     * @return the access control context based on the current stack or
     *         null if there was only privileged system code.
     */

    private static native TAccessControlContext getStackAccessControlContext();


    /**
     * Returns the "inherited" AccessControl context. This is the context
     * that existed when the thread was created. Package private so
     * AccessControlContext can use it.
     */

    static native TAccessControlContext getInheritedAccessControlContext();






}
