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
package org.teavm.classlib.sun.security.util;

import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.AllPermission;
import java.security.Permission;
import java.security.SecurityPermission;

/**
 * Created by vasek on 5. 7. 2016.
 */
public final class TSecurityConstants {
    public static final String FILE_DELETE_ACTION = "delete";
    public static final String FILE_EXECUTE_ACTION = "execute";
    public static final String FILE_READ_ACTION = "read";
    public static final String FILE_WRITE_ACTION = "write";
    public static final String FILE_READLINK_ACTION = "readlink";
    public static final String SOCKET_RESOLVE_ACTION = "resolve";
    public static final String SOCKET_CONNECT_ACTION = "connect";
    public static final String SOCKET_LISTEN_ACTION = "listen";
    public static final String SOCKET_ACCEPT_ACTION = "accept";
    public static final String SOCKET_CONNECT_ACCEPT_ACTION = "connect,accept";
    public static final String PROPERTY_RW_ACTION = "read,write";
    public static final String PROPERTY_READ_ACTION = "read";
    public static final String PROPERTY_WRITE_ACTION = "write";
    public static final AllPermission ALL_PERMISSION = new AllPermission();
    public static final NetPermission SPECIFY_HANDLER_PERMISSION = new NetPermission("specifyStreamHandler");
    public static final NetPermission SET_PROXYSELECTOR_PERMISSION = new NetPermission("setProxySelector");
    public static final NetPermission GET_PROXYSELECTOR_PERMISSION = new NetPermission("getProxySelector");
    public static final NetPermission SET_COOKIEHANDLER_PERMISSION = new NetPermission("setCookieHandler");
    public static final NetPermission GET_COOKIEHANDLER_PERMISSION = new NetPermission("getCookieHandler");
    public static final NetPermission SET_RESPONSECACHE_PERMISSION = new NetPermission("setResponseCache");
    public static final NetPermission GET_RESPONSECACHE_PERMISSION = new NetPermission("getResponseCache");
    public static final RuntimePermission CREATE_CLASSLOADER_PERMISSION = new RuntimePermission("createClassLoader");
    public static final RuntimePermission CHECK_MEMBER_ACCESS_PERMISSION = new RuntimePermission("accessDeclaredMembers");
    public static final RuntimePermission MODIFY_THREAD_PERMISSION = new RuntimePermission("modifyThread");
    public static final RuntimePermission MODIFY_THREADGROUP_PERMISSION = new RuntimePermission("modifyThreadGroup");
    public static final RuntimePermission GET_PD_PERMISSION = new RuntimePermission("getProtectionDomain");
    public static final RuntimePermission GET_CLASSLOADER_PERMISSION = new RuntimePermission("getClassLoader");
    public static final RuntimePermission STOP_THREAD_PERMISSION = new RuntimePermission("stopThread");
    public static final RuntimePermission GET_STACK_TRACE_PERMISSION = new RuntimePermission("getStackTrace");
    public static final SecurityPermission CREATE_ACC_PERMISSION = new SecurityPermission("createAccessControlContext");
    public static final SecurityPermission GET_COMBINER_PERMISSION = new SecurityPermission("getDomainCombiner");
    public static final SecurityPermission GET_POLICY_PERMISSION = new SecurityPermission("getPolicy");
    public static final SocketPermission LOCAL_LISTEN_PERMISSION = new SocketPermission("localhost:0", "listen");

    private TSecurityConstants() {
    }

    public static class AWT {
        private static final String AWTFactory = "sun.awt.AWTPermissionFactory";
        private static final TPermissionFactory<?> factory = permissionFactory();
        public static final Permission TOPLEVEL_WINDOW_PERMISSION = newAWTPermission("showWindowWithoutWarningBanner");
        public static final Permission ACCESS_CLIPBOARD_PERMISSION = newAWTPermission("accessClipboard");
        public static final Permission CHECK_AWT_EVENTQUEUE_PERMISSION = newAWTPermission("accessEventQueue");
        public static final Permission TOOLKIT_MODALITY_PERMISSION = newAWTPermission("toolkitModality");
        public static final Permission READ_DISPLAY_PIXELS_PERMISSION = newAWTPermission("readDisplayPixels");
        public static final Permission CREATE_ROBOT_PERMISSION = newAWTPermission("createRobot");
        public static final Permission WATCH_MOUSE_PERMISSION = newAWTPermission("watchMousePointer");
        public static final Permission SET_WINDOW_ALWAYS_ON_TOP_PERMISSION = newAWTPermission("setWindowAlwaysOnTop");
        public static final Permission ALL_AWT_EVENTS_PERMISSION = newAWTPermission("listenToAllAWTEvents");
        public static final Permission ACCESS_SYSTEM_TRAY_PERMISSION = newAWTPermission("accessSystemTray");

        private AWT() {
        }

        private static TPermissionFactory<?> permissionFactory() {
            return null;
        }

        private static Permission newAWTPermission(String var0) {
            return factory == null?null:factory.newPermission(var0);
        }
    }
}
