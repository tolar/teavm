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
package org.teavm.classlib.sun.reflect.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.teavm.classlib.java.lang.TClass;
import org.teavm.classlib.java.lang.annotation.TAnnotation;
import org.teavm.classlib.sun.reflect.TConstantPool;

/**
 * Created by vasek on 9. 7. 2016.
 */
public class TAnnotationParser {
    private static final Annotation[] EMPTY_ANNOTATIONS_ARRAY = new Annotation[0];
    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    public TAnnotationParser() {
    }

    public static Map<Class<? extends Annotation>, Annotation> parseAnnotations(byte[] var0, TConstantPool var1, TClass<?> var2) {
        if(var0 == null) {
            return Collections.emptyMap();
        } else {
            try {
                return parseAnnotations2(var0, var1, var2, (TClass[])null);
            } catch (BufferUnderflowException var4) {
                throw new AnnotationFormatError("Unexpected end of annotations.");
            } catch (IllegalArgumentException var5) {
                throw new AnnotationFormatError(var5);
            }
        }
    }

    @SafeVarargs
    static Map<Class<? extends Annotation>, Annotation> parseSelectAnnotations(byte[] var0, TConstantPool var1, TClass<?> var2, TClass... var3) {
        if(var0 == null) {
            return Collections.emptyMap();
        } else {
            try {
                return parseAnnotations2(var0, var1, var2, var3);
            } catch (BufferUnderflowException var5) {
                throw new AnnotationFormatError("Unexpected end of annotations.");
            } catch (IllegalArgumentException var6) {
                throw new AnnotationFormatError(var6);
            }
        }
    }

    private static Map<Class<? extends Annotation>, Annotation> parseAnnotations2(byte[] var0, TConstantPool var1, TClass<?> var2, TClass<? extends TAnnotation>[] var3) {
        return null;
    }

    public static Annotation[][] parseParameterAnnotations(byte[] var0, TConstantPool var1, TClass<?> var2) {
        try {
            return parseParameterAnnotations2(var0, var1, var2);
        } catch (BufferUnderflowException var4) {
            throw new AnnotationFormatError("Unexpected end of parameter annotations.");
        } catch (IllegalArgumentException var5) {
            throw new AnnotationFormatError(var5);
        }
    }

    private static Annotation[][] parseParameterAnnotations2(byte[] var0, TConstantPool var1, TClass<?> var2) {
        ByteBuffer var3 = ByteBuffer.wrap(var0);
        int var4 = var3.get() & 255;
        Annotation[][] var5 = new Annotation[var4][];


        return var5;
    }

    static Annotation parseAnnotation(ByteBuffer var0, TConstantPool var1, TClass<?> var2, boolean var3) {
        return parseAnnotation2(var0, var1, var2, var3, (TClass[])null);
    }

    private static Annotation parseAnnotation2(ByteBuffer var0, TConstantPool var1, TClass<?> var2, boolean var3, TClass<? extends Annotation>[] var4) {
        int var5 = var0.getShort() & '\uffff';
        TClass var6 = null;
        String var7 = "[unknown]";

        try {
            try {
                var7 = var1.getUTF8At(var5);
            } catch (IllegalArgumentException var18) {
                var6 = var1.getClassAt(var5);
            }
        } catch (NoClassDefFoundError var19) {
            if(var3) {
                throw new TypeNotPresentException(var7, var19);
            }

            skipAnnotation(var0, false);
            return null;
        } catch (TypeNotPresentException var20) {
            if(var3) {
                throw var20;
            }

            skipAnnotation(var0, false);
            return null;
        }

        if(var4 != null && !contains(var4, var6)) {
            skipAnnotation(var0, false);
            return null;
        } else {
            TAnnotationType var8 = null;

            try {
                var8 = TAnnotationType.getInstance(var6);
            } catch (IllegalArgumentException var17) {
                skipAnnotation(var0, false);
                return null;
            }

            Map var9 = var8.memberTypes();
            LinkedHashMap var10 = new LinkedHashMap(var8.memberDefaults());
            int var11 = var0.getShort() & '\uffff';

            for(int var12 = 0; var12 < var11; ++var12) {
                int var13 = var0.getShort() & '\uffff';
                String var14 = var1.getUTF8At(var13);
                Class var15 = (Class)var9.get(var14);
                if(var15 == null) {
                    skipMemberValue(var0);
                } else {
                    Object var16 = parseMemberValue(var15, var0, var1, var2);

                    var10.put(var14, var16);
                }
            }

            return annotationForMap(var6, var10);
        }
    }

    public static Annotation annotationForMap(final TClass<? extends TAnnotation> var0, final Map<String, Object> var1) {
        return null;
    }

    public static Object parseMemberValue(Class<?> var0, ByteBuffer var1, TConstantPool var2, TClass<?> var3) {
        Object var4 = null;
        byte var5 = var1.get();
        switch(var5) {
            case 64:
                var4 = parseAnnotation(var1, var2, var3, true);
                break;
            case 91:
                return parseArray(var0, var1, var2, var3);
            case 99:
                break;
            case 101:
                break;
            default:
                var4 = parseConst(var5, var1, var2);
        }


        return var4;
    }

    private static Object parseConst(int var0, ByteBuffer var1, TConstantPool var2) {
        int var3 = var1.getShort() & '\uffff';
        switch(var0) {
            case 66:
                return Byte.valueOf((byte)var2.getIntAt(var3));
            case 67:
                return Character.valueOf((char)var2.getIntAt(var3));
            case 68:
                return Double.valueOf(var2.getDoubleAt(var3));
            case 70:
                return Float.valueOf(var2.getFloatAt(var3));
            case 73:
                return Integer.valueOf(var2.getIntAt(var3));
            case 74:
                return Long.valueOf(var2.getLongAt(var3));
            case 83:
                return Short.valueOf((short)var2.getIntAt(var3));
            case 90:
                return Boolean.valueOf(var2.getIntAt(var3) != 0);
            case 115:
                return var2.getUTF8At(var3);
            default:
                throw new AnnotationFormatError("Invalid member-value tag in annotation: " + var0);
        }
    }






    private static Object parseEnumValue(Class<? extends Enum> var0, ByteBuffer var1, TConstantPool var2, TClass<?> var3) {
        int var4 = var1.getShort() & '\uffff';
        String var5 = var2.getUTF8At(var4);
        int var6 = var1.getShort() & '\uffff';
        String var7 = var2.getUTF8At(var6);

        return Enum.valueOf(var0, var7);
    }

    private static Object parseArray(Class<?> var0, ByteBuffer var1, TConstantPool var2, TClass<?> var3) {
        int var4 = var1.getShort() & '\uffff';
        Class var5 = var0.getComponentType();
        if(var5 == Byte.TYPE) {
            return parseByteArray(var4, var1, var2);
        } else if(var5 == Character.TYPE) {
            return parseCharArray(var4, var1, var2);
        } else if(var5 == Double.TYPE) {
            return parseDoubleArray(var4, var1, var2);
        } else if(var5 == Float.TYPE) {
            return parseFloatArray(var4, var1, var2);
        } else if(var5 == Integer.TYPE) {
            return parseIntArray(var4, var1, var2);
        } else if(var5 == Long.TYPE) {
            return parseLongArray(var4, var1, var2);
        } else if(var5 == Short.TYPE) {
            return parseShortArray(var4, var1, var2);
        } else if(var5 == Boolean.TYPE) {
            return parseBooleanArray(var4, var1, var2);
        } else if(var5 == String.class) {
            return parseStringArray(var4, var1, var2);
        } else if(var5 == Class.class) {
            return null;
        } else if(var5.isEnum()) {
            return parseEnumArray(var4, var5, var1, var2, var3);
        } else {
            assert var5.isAnnotation();

            return parseAnnotationArray(var4, var5, var1, var2, var3);
        }
    }

    private static Object parseByteArray(int var0, ByteBuffer var1, TConstantPool var2) {
        byte[] var3 = new byte[var0];
        boolean var4 = false;
        byte var5 = 0;

        for(int var6 = 0; var6 < var0; ++var6) {
            var5 = var1.get();
            if(var5 == 66) {
                int var7 = var1.getShort() & '\uffff';
                var3[var6] = (byte)var2.getIntAt(var7);
            } else {
                skipMemberValue(var5, var1);
                var4 = true;
            }
        }

        return var4?null:var3;
    }

    private static Object parseCharArray(int var0, ByteBuffer var1, TConstantPool var2) {
        char[] var3 = new char[var0];
        boolean var4 = false;
        byte var5 = 0;

        for(int var6 = 0; var6 < var0; ++var6) {
            var5 = var1.get();
            if(var5 == 67) {
                int var7 = var1.getShort() & '\uffff';
                var3[var6] = (char)var2.getIntAt(var7);
            } else {
                skipMemberValue(var5, var1);
                var4 = true;
            }
        }

        return var4?null:var3;
    }

    private static Object parseDoubleArray(int var0, ByteBuffer var1, TConstantPool var2) {
        double[] var3 = new double[var0];
        boolean var4 = false;
        byte var5 = 0;

        for(int var6 = 0; var6 < var0; ++var6) {
            var5 = var1.get();
            if(var5 == 68) {
                int var7 = var1.getShort() & '\uffff';
                var3[var6] = var2.getDoubleAt(var7);
            } else {
                skipMemberValue(var5, var1);
                var4 = true;
            }
        }

        return var4?null:var3;
    }

    private static Object parseFloatArray(int var0, ByteBuffer var1, TConstantPool var2) {
        float[] var3 = new float[var0];
        boolean var4 = false;
        byte var5 = 0;

        for(int var6 = 0; var6 < var0; ++var6) {
            var5 = var1.get();
            if(var5 == 70) {
                int var7 = var1.getShort() & '\uffff';
                var3[var6] = var2.getFloatAt(var7);
            } else {
                skipMemberValue(var5, var1);
                var4 = true;
            }
        }

        return var4?null:var3;
    }

    private static Object parseIntArray(int var0, ByteBuffer var1, TConstantPool var2) {
        int[] var3 = new int[var0];
        boolean var4 = false;
        byte var5 = 0;

        for(int var6 = 0; var6 < var0; ++var6) {
            var5 = var1.get();
            if(var5 == 73) {
                int var7 = var1.getShort() & '\uffff';
                var3[var6] = var2.getIntAt(var7);
            } else {
                skipMemberValue(var5, var1);
                var4 = true;
            }
        }

        return var4?null:var3;
    }

    private static Object parseLongArray(int var0, ByteBuffer var1, TConstantPool var2) {
        long[] var3 = new long[var0];
        boolean var4 = false;
        byte var5 = 0;

        for(int var6 = 0; var6 < var0; ++var6) {
            var5 = var1.get();
            if(var5 == 74) {
                int var7 = var1.getShort() & '\uffff';
                var3[var6] = var2.getLongAt(var7);
            } else {
                skipMemberValue(var5, var1);
                var4 = true;
            }
        }

        return var4?null:var3;
    }

    private static Object parseShortArray(int var0, ByteBuffer var1, TConstantPool var2) {
        short[] var3 = new short[var0];
        boolean var4 = false;
        byte var5 = 0;

        for(int var6 = 0; var6 < var0; ++var6) {
            var5 = var1.get();
            if(var5 == 83) {
                int var7 = var1.getShort() & '\uffff';
                var3[var6] = (short)var2.getIntAt(var7);
            } else {
                skipMemberValue(var5, var1);
                var4 = true;
            }
        }

        return var4?null:var3;
    }

    private static Object parseBooleanArray(int var0, ByteBuffer var1, TConstantPool var2) {
        boolean[] var3 = new boolean[var0];
        boolean var4 = false;
        byte var5 = 0;

        for(int var6 = 0; var6 < var0; ++var6) {
            var5 = var1.get();
            if(var5 == 90) {
                int var7 = var1.getShort() & '\uffff';
                var3[var6] = var2.getIntAt(var7) != 0;
            } else {
                skipMemberValue(var5, var1);
                var4 = true;
            }
        }

        return var4?null:var3;
    }

    private static Object parseStringArray(int var0, ByteBuffer var1, TConstantPool var2) {
        String[] var3 = new String[var0];
        boolean var4 = false;
        byte var5 = 0;

        for(int var6 = 0; var6 < var0; ++var6) {
            var5 = var1.get();
            if(var5 == 115) {
                int var7 = var1.getShort() & '\uffff';
                var3[var6] = var2.getUTF8At(var7);
            } else {
                skipMemberValue(var5, var1);
                var4 = true;
            }
        }

        return var4?null:var3;
    }



    private static Object parseEnumArray(int var0, Class<? extends Enum<?>> var1, ByteBuffer var2, TConstantPool var3, TClass<?> var4) {
        Object[] var5 = (Object[])((Object[])Array.newInstance(var1, var0));
        boolean var6 = false;
        byte var7 = 0;

        for(int var8 = 0; var8 < var0; ++var8) {
            var7 = var2.get();
            if(var7 == 101) {
                var5[var8] = parseEnumValue(var1, var2, var3, var4);
            } else {
                skipMemberValue(var7, var2);
                var6 = true;
            }
        }

        return var6?null:var5;
    }

    private static Object parseAnnotationArray(int var0, Class<? extends Annotation> var1, ByteBuffer var2, TConstantPool var3, TClass<?> var4) {
        Object[] var5 = (Object[])((Object[])Array.newInstance(var1, var0));
        boolean var6 = false;
        byte var7 = 0;

        for(int var8 = 0; var8 < var0; ++var8) {
            var7 = var2.get();
            if(var7 == 64) {
                var5[var8] = parseAnnotation(var2, var3, var4, true);
            } else {
                skipMemberValue(var7, var2);
                var6 = true;
            }
        }

        return var6?null:var5;
    }



    private static void skipAnnotation(ByteBuffer var0, boolean var1) {
        if(var1) {
            var0.getShort();
        }

        int var2 = var0.getShort() & '\uffff';

        for(int var3 = 0; var3 < var2; ++var3) {
            var0.getShort();
            skipMemberValue(var0);
        }

    }

    private static void skipMemberValue(ByteBuffer var0) {
        byte var1 = var0.get();
        skipMemberValue(var1, var0);
    }

    private static void skipMemberValue(int var0, ByteBuffer var1) {
        switch(var0) {
            case 64:
                skipAnnotation(var1, true);
                break;
            case 91:
                skipArray(var1);
                break;
            case 101:
                var1.getInt();
                break;
            default:
                var1.getShort();
        }

    }

    private static void skipArray(ByteBuffer var0) {
        int var1 = var0.getShort() & '\uffff';

        for(int var2 = 0; var2 < var1; ++var2) {
            skipMemberValue(var0);
        }

    }

    private static boolean contains(Object[] var0, Object var1) {
        Object[] var2 = var0;
        int var3 = var0.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Object var5 = var2[var4];
            if(var5 == var1) {
                return true;
            }
        }

        return false;
    }

    public static Annotation[] toArray(Map<Class<? extends Annotation>, Annotation> var0) {
        return (Annotation[])var0.values().toArray(EMPTY_ANNOTATION_ARRAY);
    }

    static Annotation[] getEmptyAnnotationArray() {
        return EMPTY_ANNOTATION_ARRAY;
    }


}
