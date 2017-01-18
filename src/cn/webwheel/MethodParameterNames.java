/*
 * Copyright 2017 XueSong Guo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.webwheel;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.TreeMap;

/**
 * Tools for retrieving parameters' names of methods from bytecode
 */
public class MethodParameterNames {

    /**
     * lookup names
     * @param method a method instance
     * @return names, null if there's no such information in bytecode
     * @throws IOException io error
     */
    public static String[] lookup(Method method) throws IOException {
        return lookup(method.getName(), desc(method), method.getParameterTypes().length, getClassAsStream(method.getDeclaringClass()));
    }

    private static InputStream getClassAsStream(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        if(classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return getClassAsStream(classLoader, clazz.getName());
    }

    private static InputStream getClassAsStream(ClassLoader classLoader, String className) {
        String name = className.replace('.', '/') + ".class";
        InputStream asStream = classLoader.getResourceAsStream(name);
        if(asStream == null) {
            asStream = MethodParameterNames.class.getResourceAsStream(name);
        }
        return asStream;
    }

    private static String desc(Class cls) {
        if (cls.isArray()) {
            return "[" + desc(cls.getComponentType());
        }
        if (cls == byte.class) {
            return "B";
        }
        if (cls == char.class) {
            return "C";
        }
        if (cls == double.class) {
            return "D";
        }
        if (cls == float.class) {
            return "F";
        }
        if (cls == int.class) {
            return "I";
        }
        if (cls == long.class) {
            return "J";
        }
        if (cls == short.class) {
            return "S";
        }
        if (cls == boolean.class) {
            return "Z";
        }
        return "L" + cls.getName().replace('.', '/') + ";";
    }

    private static String desc(Method method) {
        StringBuilder sb = new StringBuilder("(");
        Class<?>[] pts = method.getParameterTypes();
        for (int i = 0; i < pts.length; i++) {
            sb.append(desc(pts[i]));
        }
        sb.append(")");
        if (method.getReturnType() == Void.class) {
            sb.append("V");
        } else {
            sb.append(desc(method.getReturnType()));
        }
        return sb.toString();
    }

    private static String[] lookup(String name, String desc, int parameterCount, InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        dis.readFully(new byte[4]);
        int minor_version = dis.readUnsignedShort();
        int major_version = dis.readUnsignedShort();
        int constant_pool_count = dis.readUnsignedShort();
        String[] strings = new String[constant_pool_count];
        for (int i = 1; i < constant_pool_count; i++) {
            int tag = dis.readUnsignedByte();
            switch (tag) {
                case 7://Class
                    dis.readUnsignedShort();
                    break;
                case 9://Fieldref
                case 10://Methodref
                case 11://InterfaceMethodref
                    dis.readUnsignedShort();
                    dis.readUnsignedShort();
                    break;
                case 8://String
                    dis.readUnsignedShort();
                    break;
                case 3://Integer
                case 4://Float
                    dis.readInt();
                    break;
                case 5://Long
                case 6://Double
                    dis.readLong();
                    break;
                case 12://NameAndType
                    dis.readUnsignedShort();
                    dis.readUnsignedShort();
                    break;
                case 1://Utf8
                    strings[i] = dis.readUTF();
                    break;
                case 15://MethodHandle
                    dis.readUnsignedByte();
                    dis.readUnsignedShort();
                    break;
                case 16://MethodType
                    dis.readUnsignedShort();
                    break;
                case 18://InvokeDynamic
                    dis.readUnsignedShort();
                    dis.readUnsignedShort();
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        int access_flags = dis.readUnsignedShort();
        int this_class = dis.readUnsignedShort();
        int super_class = dis.readUnsignedShort();
        int interfaces_count = dis.readUnsignedShort();
        for (int i = 0; i < interfaces_count; i++) {
            dis.readUnsignedShort();
        }
        int fields_count = dis.readUnsignedShort();
        for (int i = 0; i < fields_count; i++) {
            dis.readUnsignedShort();
            dis.readUnsignedShort();
            dis.readUnsignedShort();
            int attributes_count = dis.readUnsignedShort();
            for (int j = 0; j < attributes_count; j++) {
                dis.readUnsignedShort();
                int attribute_length = dis.readInt();
                dis.readFully(new byte[attribute_length]);
            }
        }
        int methods_count = dis.readUnsignedShort();
        for (int i = 0; i < methods_count; i++) {
            int m_access_flags = dis.readUnsignedShort();
            int name_index = dis.readUnsignedShort();
            int descriptor_index = dis.readUnsignedShort();
            int attributes_count = dis.readUnsignedShort();
            boolean find = name.equals(strings[name_index]) && desc.equals(strings[descriptor_index]);
            for (int j = 0; j < attributes_count; j++) {
                int attribute_name_index = dis.readUnsignedShort();
                int attribute_length = dis.readInt();
                byte[] data = new byte[attribute_length];
                dis.readFully(data);
                if (find && strings[attribute_name_index].equals("Code")) {
                    return parseCode(data, (m_access_flags & 0x8)/*ACC_STATIC*/ != 0, parameterCount, strings);
                }
            }
            if (find) {
                return null;
            }
        }
        int attributes_count = dis.readUnsignedShort();
        for (int j = 0; j < attributes_count; j++) {
            int attribute_name_index = dis.readUnsignedShort();
            int attribute_length = dis.readInt();
            dis.readFully(new byte[attribute_length]);
        }
        return null;
    }

    private static String[] parseCode(byte[] data, boolean isStaticMethod, int parameterCount, String[] strings) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        int max_stack = dis.readUnsignedShort();
        int max_locals = dis.readUnsignedShort();
        int code_length = dis.readInt();
        byte[] code = new byte[code_length];
        dis.readFully(code);
        int exception_table_length = dis.readUnsignedShort();
        for (int i = 0; i < exception_table_length; i++) {
            dis.readUnsignedShort();
            dis.readUnsignedShort();
            dis.readUnsignedShort();
            dis.readUnsignedShort();
        }
        int attributes_count = dis.readUnsignedShort();
        for (int i = 0; i < attributes_count; i++) {
            int attribute_name_index = dis.readUnsignedShort();
            int attribute_length = dis.readInt();
            if (strings[attribute_name_index].equals("LocalVariableTable")) {
                int local_variable_table_length = dis.readUnsignedShort();
                TreeMap<Integer, String> map = new TreeMap<Integer, String>();
                for (int j = 0; j < local_variable_table_length; j++) {
                    dis.readUnsignedShort();
                    dis.readUnsignedShort();
                    int name_index = dis.readUnsignedShort();
                    dis.readUnsignedShort();
                    int index = dis.readUnsignedShort();
                    map.put(index, strings[name_index]);
                }
                String[] names = new String[parameterCount];
                String[] names2 = map.values().toArray(new String[0]);
                int offset = isStaticMethod ? 0 : 1;
                System.arraycopy(names2, offset, names, 0, names.length);
                return names;
            } else {
                dis.readFully(new byte[attribute_length]);
            }
        }
        return null;
    }
}
