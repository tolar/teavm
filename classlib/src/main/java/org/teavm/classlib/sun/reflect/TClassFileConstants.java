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
package org.teavm.classlib.sun.reflect;

/**
 * Created by vasek on 4. 7. 2016.
 */
interface TClassFileConstants {
    byte opc_aconst_null = 1;
    byte opc_sipush = 17;
    byte opc_ldc = 18;
    byte opc_iload_0 = 26;
    byte opc_iload_1 = 27;
    byte opc_iload_2 = 28;
    byte opc_iload_3 = 29;
    byte opc_lload_0 = 30;
    byte opc_lload_1 = 31;
    byte opc_lload_2 = 32;
    byte opc_lload_3 = 33;
    byte opc_fload_0 = 34;
    byte opc_fload_1 = 35;
    byte opc_fload_2 = 36;
    byte opc_fload_3 = 37;
    byte opc_dload_0 = 38;
    byte opc_dload_1 = 39;
    byte opc_dload_2 = 40;
    byte opc_dload_3 = 41;
    byte opc_aload_0 = 42;
    byte opc_aload_1 = 43;
    byte opc_aload_2 = 44;
    byte opc_aload_3 = 45;
    byte opc_aaload = 50;
    byte opc_astore_0 = 75;
    byte opc_astore_1 = 76;
    byte opc_astore_2 = 77;
    byte opc_astore_3 = 78;
    byte opc_pop = 87;
    byte opc_dup = 89;
    byte opc_dup_x1 = 90;
    byte opc_swap = 95;
    byte opc_i2l = -123;
    byte opc_i2f = -122;
    byte opc_i2d = -121;
    byte opc_l2i = -120;
    byte opc_l2f = -119;
    byte opc_l2d = -118;
    byte opc_f2i = -117;
    byte opc_f2l = -116;
    byte opc_f2d = -115;
    byte opc_d2i = -114;
    byte opc_d2l = -113;
    byte opc_d2f = -112;
    byte opc_i2b = -111;
    byte opc_i2c = -110;
    byte opc_i2s = -109;
    byte opc_ifeq = -103;
    byte opc_if_icmpeq = -97;
    byte opc_goto = -89;
    byte opc_ireturn = -84;
    byte opc_lreturn = -83;
    byte opc_freturn = -82;
    byte opc_dreturn = -81;
    byte opc_areturn = -80;
    byte opc_return = -79;
    byte opc_getstatic = -78;
    byte opc_putstatic = -77;
    byte opc_getfield = -76;
    byte opc_putfield = -75;
    byte opc_invokevirtual = -74;
    byte opc_invokespecial = -73;
    byte opc_invokestatic = -72;
    byte opc_invokeinterface = -71;
    byte opc_arraylength = -66;
    byte opc_new = -69;
    byte opc_athrow = -65;
    byte opc_checkcast = -64;
    byte opc_instanceof = -63;
    byte opc_ifnull = -58;
    byte opc_ifnonnull = -57;
    byte CONSTANT_Class = 7;
    byte CONSTANT_Fieldref = 9;
    byte CONSTANT_Methodref = 10;
    byte CONSTANT_InterfaceMethodref = 11;
    byte CONSTANT_NameAndType = 12;
    byte CONSTANT_String = 8;
    byte CONSTANT_Utf8 = 1;
    short ACC_PUBLIC = 1;
}
