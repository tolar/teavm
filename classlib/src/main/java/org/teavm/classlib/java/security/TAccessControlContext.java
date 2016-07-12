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

import org.teavm.classlib.sun.security.util.TSecurityConstants;

/**
 * @author: Vaclav Tolar, (vaclav_tolar@kb.cz, vaclav.tolar@cleverlance.com, vaclav.tolar@gmail.com)
 * Date: 2016-07-11
 */
public final class TAccessControlContext {

    // isPrivileged and isAuthorized are referenced by the VM - do not remove
    // or change their names
    private boolean isPrivileged;
    private boolean isAuthorized = false;

    // Note: This field is directly used by the virtual machine
    // native codes. Don't touch it.
    private TAccessControlContext privilegedContext;

    private TDomainCombiner combiner = null;

    // limited privilege scope
    private TAccessControlContext parent;
    private boolean isWrapped;

    // is constrained by limited privilege scope?
    private boolean isLimited;





    public TAccessControlContext(TAccessControlContext acc,
            TDomainCombiner combiner) {

        this(acc, combiner, false);
    }

    TAccessControlContext(TAccessControlContext acc,
            TDomainCombiner combiner,
            boolean preauthorized) {
        if (!preauthorized) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(TSecurityConstants.CREATE_ACC_PERMISSION);
                this.isAuthorized = true;
            }
        } else {
            this.isAuthorized = true;
        }


        // we do not need to run the combine method on the
        // provided ACC.  it was already "combined" when the
        // context was originally retrieved.
        //
        // at this point in time, we simply throw away the old
        // combiner and use the newly provided one.
        this.combiner = combiner;
    }

    /**
     * package private for AccessController
     *
     * This "argument wrapper" context will be passed as the actual context
     * parameter on an internal doPrivileged() call used in the implementation.
     */
    TAccessControlContext(TProtectionDomain caller, TDomainCombiner combiner,
            TAccessControlContext parent, TAccessControlContext context,
            TPermission[] perms)
    {
        /*
         * Combine the domains from the doPrivileged() context into our
         * wrapper context, if necessary.
         */
        TProtectionDomain[] callerPDs = null;
        if (caller != null) {
            callerPDs = new TProtectionDomain[] { caller };
        }
        this.combiner = combiner;


        /*
         * For a doPrivileged() with limited privilege scope, initialize
         * the relevant fields.
         *
         * The limitedContext field contains the union of all domains which
         * are enclosed by this limited privilege scope. In other words,
         * it contains all of the domains which could potentially be checked
         * if none of the limiting permissions implied a requested permission.
         */
        if (parent != null) {
            this.isLimited = true;
            this.isWrapped = true;
            this.parent = parent;
            this.privilegedContext = context; // used in checkPermission2()
        }
        this.isAuthorized = true;
    }


    /**
     * package private constructor for AccessController.getContext()
     */



    /**
     * Returns this context's context.
     */

    /**
     * Returns true if this context is privileged.
     */
    boolean isPrivileged()
    {
        return isPrivileged;
    }

    /**
     * get the assigned combiner from the privileged or inherited context
     */
    TDomainCombiner getAssignedCombiner() {
        TAccessControlContext acc;
        if (isPrivileged) {
            acc = privilegedContext;
        } else {
            acc = TAccessController.getInheritedAccessControlContext();
        }
        if (acc != null) {
            return acc.combiner;
        }
        return null;
    }

    public TDomainCombiner getDomainCombiner() {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(TSecurityConstants.GET_COMBINER_PERMISSION);
        }
        return getCombiner();
    }

    /**
     * package private for AccessController
     */
    TDomainCombiner getCombiner() {
        return combiner;
    }

    boolean isAuthorized() {
        return isAuthorized;
    }


    public void checkPermission(TPermission perm)
            throws TAccessControlException
    {
        boolean dumpDebug = false;

        if (perm == null) {
            throw new NullPointerException("permission can't be null");
        }

        /*
         * iterate through the ProtectionDomains in the context.
         * Stop at the first one that doesn't allow the
         * requested permission (throwing an exception).
         *
         */

        /* if ctxt is null, all we had on the stack were system domains,
           or the first domain was a Privileged system domain. This
           is to make the common case for system code very fast */




        checkPermission2(perm);
    }

    /*
     * Check the domains associated with the limited privilege scope.
     */
    private void checkPermission2(TPermission perm) {
        if (!isLimited) {
            return;
        }

        /*
         * Check the doPrivileged() context parameter, if present.
         */
        if (privilegedContext != null) {
            privilegedContext.checkPermission2(perm);
        }

        /*
         * Ignore the limited permissions and parent fields of a wrapper
         * context since they were already carried down into the unwrapped
         * context.
         */
        if (isWrapped) {
            return;
        }


        /*
         * Check the limited privilege scope up the call stack or the inherited
         * parent thread call stack of this ACC.
         */
        if (parent != null) {
                parent.checkPermission2(perm);
        }
    }

    /**
     * Take the stack-based context (this) and combine it with the
     * privileged or inherited context, if need be. Any limited
     * privilege scope is flagged regardless of whether the assigned
     * context comes from an immediately enclosing limited doPrivileged().
     * The limited privilege scope can indirectly flow from the inherited
     * parent thread or an assigned context previously captured by getContext().
     */
    TAccessControlContext optimize() {
        // the assigned (privileged or inherited) context
        TAccessControlContext acc;
        TDomainCombiner combiner = null;
        TAccessControlContext parent = null;

        if (isPrivileged) {
            acc = privilegedContext;
            if (acc != null) {
                /*
                 * If the context is from a limited scope doPrivileged() then
                 * copy the permissions and parent fields out of the wrapper
                 * context that was created to hold them.
                 */
                if (acc.isWrapped) {
                    parent = acc.parent;
                }
            }
        } else {
            acc = TAccessController.getInheritedAccessControlContext();
            if (acc != null) {
                /*
                 * If the inherited context is constrained by a limited scope
                 * doPrivileged() then set it as our parent so we will process
                 * the non-domain-related state.
                 */
                if (acc.isLimited) {
                    parent = acc;
                }
            }
        }


        // acc.context could be null if only system code was involved;
        // in that case, ignore the assigned context

        // if there is no enclosing limited privilege scope on the stack or
        // inherited from a parent thread
        boolean skipLimited = ((acc == null || !acc.isWrapped) && parent == null);

        if (acc != null && acc.combiner != null) {
            // let the assigned acc's combiner do its thing

            // No need to clone current and assigned.context
            // combine() will not update them
            combiner = acc.combiner;
        } else {

        }

        // Reuse existing ACC
        this.combiner = combiner;
        this.isPrivileged = false;

        return this;
    }








    /**
     * Checks two AccessControlContext objects for equality.
     * Checks that <i>obj</i> is
     * an AccessControlContext and has the same set of ProtectionDomains
     * as this context.
     * <P>
     * @param obj the object we are testing for equality with this object.
     * @return true if <i>obj</i> is an AccessControlContext, and has the
     * same set of ProtectionDomains as this context, false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (! (obj instanceof TAccessControlContext))
            return false;

        TAccessControlContext that = (TAccessControlContext) obj;

        if (!equalContext(that))
            return false;

        if (!equalLimitedContext(that))
            return false;

        return true;
    }

    /*
     * Compare for equality based on state that is free of limited
     * privilege complications.
     */
    private boolean equalContext(TAccessControlContext that) {

        if (this.combiner == null && that.combiner != null)
            return false;

        if (this.combiner != null && !this.combiner.equals(that.combiner))
            return false;

        return true;
    }



    /*
     * Compare for equality based on state that is captured during a
     * call to AccessController.getContext() when a limited privilege
     * scope is in effect.
     */
    private boolean equalLimitedContext(TAccessControlContext that) {
        if (that == null)
            return false;

        /*
         * If neither instance has limited privilege scope then we're done.
         */
        if (!this.isLimited && !that.isLimited)
            return true;

        /*
         * If only one instance has limited privilege scope then we're done.
         */
        if (!(this.isLimited && that.isLimited))
            return false;

        /*
         * Wrapped instances should never escape outside the implementation
         * this class and AccessController so this will probably never happen
         * but it only makes any sense to compare if they both have the same
         * isWrapped state.
         */
        if ((this.isWrapped && !that.isWrapped) ||
                (!this.isWrapped && that.isWrapped)) {
            return false;
        }


        if (!(this.containsAllLimits(that) && that.containsAllLimits(this)))
            return false;

        /*
         * Skip through any wrapped contexts.
         */
        TAccessControlContext thisNextPC = getNextPC(this);
        TAccessControlContext thatNextPC = getNextPC(that);

        /*
         * The protection domains and combiner of a privilegedContext are
         * not relevant because they have already been included in the context
         * of this instance by optimize() so we only care about any limited
         * privilege state they may have.
         */
        if (thisNextPC == null && thatNextPC != null && thatNextPC.isLimited)
            return false;

        if (thisNextPC != null && !thisNextPC.equalLimitedContext(thatNextPC))
            return false;

        if (this.parent == null && that.parent != null)
            return false;

        if (this.parent != null && !this.parent.equals(that.parent))
            return false;

        return true;
    }

    /*
     * Follow the privilegedContext link making our best effort to skip
     * through any wrapper contexts.
     */
    private static TAccessControlContext getNextPC(TAccessControlContext acc) {
        while (acc != null && acc.privilegedContext != null) {
            acc = acc.privilegedContext;
            if (!acc.isWrapped)
                return acc;
        }
        return null;
    }



    private boolean containsAllLimits(TAccessControlContext that) {
        boolean match = false;
        return match;
    }




}
