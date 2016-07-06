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
package org.teavm.classlib.sun.misc;

import java.util.Properties;

/**
 * Created by vasek on 4. 7. 2016.
 */
public class TVM {
    private static boolean suspended = false;
    /** @deprecated */
    @Deprecated
    public static final int STATE_GREEN = 1;
    /** @deprecated */
    @Deprecated
    public static final int STATE_YELLOW = 2;
    /** @deprecated */
    @Deprecated
    public static final int STATE_RED = 3;
    private static volatile boolean booted = false;
    private static final Object lock = new Object();
    private static long directMemory = 67108864L;
    private static boolean pageAlignDirectMemory;
    private static boolean defaultAllowArraySyntax = false;
    private static boolean allowArraySyntax;
    private static final Properties savedProps;
    private static volatile int finalRefCount;
    private static volatile int peakFinalRefCount;
    private static final int JVMTI_THREAD_STATE_ALIVE = 1;
    private static final int JVMTI_THREAD_STATE_TERMINATED = 2;
    private static final int JVMTI_THREAD_STATE_RUNNABLE = 4;
    private static final int JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 1024;
    private static final int JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 16;
    private static final int JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 32;

    public TVM() {
    }

    /** @deprecated */
    @Deprecated
    public static boolean threadsSuspended() {
        return suspended;
    }

    public static boolean allowThreadSuspension(ThreadGroup var0, boolean var1) {
        return var0.allowThreadSuspension(var1);
    }

    /** @deprecated */
    @Deprecated
    public static boolean suspendThreads() {
        suspended = true;
        return true;
    }

    /** @deprecated */
    @Deprecated
    public static void unsuspendThreads() {
        suspended = false;
    }

    /** @deprecated */
    @Deprecated
    public static void unsuspendSomeThreads() {
    }

    /** @deprecated */
    @Deprecated
    public static final int getState() {
        return 1;
    }

    /** @deprecated */
    @Deprecated
    public static void registerVMNotification(TVMNotification var0) {
    }

    /** @deprecated */
    @Deprecated
    public static void asChange(int var0, int var1) {
    }

    /** @deprecated */
    @Deprecated
    public static void asChange_otherthread(int var0, int var1) {
    }

    public static void booted() {
        Object var0 = lock;
        synchronized(lock) {
            booted = true;
            lock.notifyAll();
        }
    }

    public static boolean isBooted() {
        return booted;
    }

    public static void awaitBooted() throws InterruptedException {
        Object var0 = lock;
        synchronized(lock) {
            while(!booted) {
                lock.wait();
            }

        }
    }

    public static long maxDirectMemory() {
        return directMemory;
    }

    public static boolean isDirectMemoryPageAligned() {
        return pageAlignDirectMemory;
    }

    public static boolean allowArraySyntax() {
        return allowArraySyntax;
    }

    public static boolean isSystemDomainLoader(ClassLoader var0) {
        return var0 == null;
    }

    public static String getSavedProperty(String var0) {
        if(savedProps.isEmpty()) {
            throw new IllegalStateException("Should be non-empty if initialized");
        } else {
            return savedProps.getProperty(var0);
        }
    }

    public static void saveAndRemoveProperties(Properties var0) {
        if(booted) {
            throw new IllegalStateException("System initialization has completed");
        } else {
            savedProps.putAll(var0);
            String var1 = (String)var0.remove("sun.nio.MaxDirectMemorySize");
            if(var1 != null) {
                if(var1.equals("-1")) {
                    directMemory = Runtime.getRuntime().maxMemory();
                } else {
                    long var2 = Long.parseLong(var1);
                    if(var2 > -1L) {
                        directMemory = var2;
                    }
                }
            }

            var1 = (String)var0.remove("sun.nio.PageAlignDirectMemory");
            if("true".equals(var1)) {
                pageAlignDirectMemory = true;
            }

            var1 = var0.getProperty("sun.lang.ClassLoader.allowArraySyntax");
            allowArraySyntax = var1 == null?defaultAllowArraySyntax:Boolean.parseBoolean(var1);
            var0.remove("java.lang.Integer.IntegerCache.high");
            var0.remove("sun.zip.disableMemoryMapping");
            var0.remove("sun.java.launcher.diag");
            var0.remove("sun.cds.enableSharedLookupCache");
        }
    }

    public static void initializeOSEnvironment() {
        if(!booted) {
            TOSEnvironment.initialize();
        }

    }

    public static int getFinalRefCount() {
        return finalRefCount;
    }

    public static int getPeakFinalRefCount() {
        return peakFinalRefCount;
    }

    public static void addFinalRefCount(int var0) {
        finalRefCount += var0;
        if(finalRefCount > peakFinalRefCount) {
            peakFinalRefCount = finalRefCount;
        }

    }

    public static Thread.State toThreadState(int var0) {
        return (var0 & 4) != 0? Thread.State.RUNNABLE:((var0 & 1024) != 0? Thread.State.BLOCKED:((var0 & 16) != 0? Thread.State.WAITING:((var0 & 32) != 0? Thread.State.TIMED_WAITING:((var0 & 2) != 0? Thread.State.TERMINATED:((var0 & 1) == 0? Thread.State.NEW: Thread.State.RUNNABLE)))));
    }

    public static native ClassLoader latestUserDefinedLoader();

    private static native void initialize();

    static {
        allowArraySyntax = defaultAllowArraySyntax;
        savedProps = new Properties();
        finalRefCount = 0;
        peakFinalRefCount = 0;
        initialize();
    }
}
