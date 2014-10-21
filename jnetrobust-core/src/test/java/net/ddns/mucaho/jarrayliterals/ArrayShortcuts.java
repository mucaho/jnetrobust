/**
 * (C) Copyright 2014 mucaho (https://github.com/mucaho).
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

package net.ddns.mucaho.jarrayliterals;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Shortcuts for creating arbitrary arrays (e.g. for parameterized unit tests).
 * <p/>
 * This class has two overloaded method.
 * The {@link ArrayShortcuts#$(Object...) <code><T>$(T...): T[]</code>} method is used for
 * creating an array of a generic element type and expects a non-zero,
 * variable amount of arguments.
 * The {@link ArrayShortcuts#$(Object) <code><T>$(T): T[]</code>} method is used for
 * creating a higher-dimension array of a generic element type and expects a single argument.
 * <br>
 * This class has fields that start with {@linkplain $}. The fields represent zero-sized
 * arrays of a specific element type.
 * <br>
 * Note that both the methods and fields return boxed wrapper arrays instead of their respective,
 * primitive data type arrays.
 * <p/>
 * Be sure to use {@link ArrayShortcuts#$null $null} instead of the regular <b>null</b>
 * when nesting arrays.
 * <br>
 * Additionally, there are methods which automatically cast <code>int</code> arguments to
 * <code>Byte</code>s {@link ArrayShortcuts#$B(int...) <code><T>$B(int...): Byte[]</code>} or
 * <code>short</code>s {@link ArrayShortcuts#$S(int...) <code><T>$S(int...): Short[]</code>}.
 * Be careful that these 2 methods trade performance for brevity.
 * <p/>
 * An utility method {@link ArrayShortcuts#toString() toString} which prints
 * <i>anything</i> (including multidimensional arrays) is available.
 *
 * @author mucaho
 */
public class ArrayShortcuts {
    /*
     * other primitive types: Boolean.class, Character.class, Void.class
     */
    @SuppressWarnings("unchecked")
    private static final Set<Class<?>> PRIMITIVE_NUMBER_TYPES = new HashSet<Class<?>>(
            Arrays.asList(
                    byte.class, Byte.class,
                    short.class, Short.class,
                    int.class, Integer.class,
                    long.class, Long.class,
                    float.class, Float.class,
                    double.class, Double.class));

    private static boolean isPrimitiveNumber(Class<?> clazz) {
        return PRIMITIVE_NUMBER_TYPES.contains(clazz);
    }


    /**
     * Prints a generic object (this includes single-/multi-dimensional arrays).
     * In essence prints anything (as long as it has the {@link Object#toString() toString()}
     * implemented).
     * <br>
     * Numbers are suffixed with the first capital letter of their respective types.
     *
     * @param arr anything
     * @return String representation of <b>arr</b>
     */
    public static String toString(Object arr) {
        return toString(arr, 0);
    }

    private static String ident(int amount) {
        String out = "\n";
        for (int i = 0; i < amount; ++i)
            out += " ";

        return out;
    }

    private static String toString(Object arr, int depth) {
        String out = "";

        if (arr == null) {
            out += arr;
        } else if (!arr.getClass().isArray()) {
            out += arr.toString();
            Class<?> objClass = arr.getClass();
            if (isPrimitiveNumber(objClass)) {
                out += objClass.getSimpleName().substring(0, 1);
            }
        } else {
            out += ident(depth);
            out += "[";

            int arrLength = Array.getLength(arr);
            for (int i = 0; i < arrLength; ++i) {
                if (i != 0) out += ", ";
                out += toString(Array.get(arr, i), depth + 1);
                if (i < arrLength - 1) out += " ";
            }

            out += "]";
            out += ident(depth - 1);
        }

        return out;
    }


    /**
     * Shortcut for returning a higher dimension array containing only the parameter.
     * Note that the parameter itself can be an array.
     *
     * @param param A single parameter.
     * @return Values    Array containing the parameter only.
     */
    public static <T> Object $(T param) {
        return createArray(getClass(param), param);
    }

    /**
     * Shortcut for returning an array containing the parameters(s).
     * <br>
     * <b>(1)</b> Parameters are interpreted as a list of parameters.
     * In this case the parameters are returned as an array of those parameters.
     * Note that the parameters themselves can be arrays.
     * <br>
     * <b>(2)</b> Parameter is interpreted as a single array. In that case the same array will be
     * returned. However, if you want to create a higher dimension array instead, you can call the
     * other method by casting the array to an Object
     * <code>{@link ArrayShortcuts#$(Object) $( (Object) params )}</code>.
     *
     * @param params <b>(1)</b> A list of parameters <i>OR</i>
     *               <b>(2)</b> A single parameter array.
     * @return            <b>(1)</b> Array containing the list of parameters <i>OR</i>
     * <b>(2)</b> The single parameter array.
     */
    public static <T> Object $(T... params) {
        Class<T> componentType = getClass(params[0]);

        //e.g. if params were Object[], but could be in fact Boolean[][]
        if (params.getClass().getComponentType() != componentType) {
            boolean isCommonComponentType = false;

            for (int i = 1; i < params.length; i++) {
                if (getClass(params[i]) == componentType) {
                    isCommonComponentType = true;
                    continue;
                } else {
                    isCommonComponentType = false;
                    break;
                }
            }

            if (isCommonComponentType) { //e.g. Object[] was in fact Boolean[][]
                return createArray(componentType, params);
            }
        }


        return params;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getClass(T param) {
        return (Class<T>) (param != null ? param.getClass() : Void.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] createArray(Class<T> klazz, T param) {
        T[] out = (T[]) Array.newInstance(klazz, 1);
        Array.set(out, 0, param);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static <T, S> T[] createArray(Class<T> klazz, S... params) {
        T[] out = (T[]) Array.newInstance(klazz, params.length);
        for (int i = 0; i < params.length; i++) {
            Array.set(out, i, params[i]);
        }
        return out;
    }

    /**
     * Shortcut for returning an array of Bytes. All parameters passed to this
     * method are returned in an <code>Byte[]</code> array.
     *
     * @param params Values to be returned in an <code>Byte[]</code> array.
     * @return Values passed to this method.
     */
    public static Object $B(int... params) {
        Byte[] out = new Byte[params.length];
        for (int i = 0; i < out.length; ++i) {
            out[i] = (byte) params[i];
        }
        return out;
    }

    /**
     * Shortcut for returning an array of Shorts. All parameters passed to this
     * method are returned in an <code>Short[]</code> array.
     *
     * @param params Values to be returned in an <code>Short[]</code> array.
     * @return Values passed to this method.
     */
    public static Object $S(int... params) {
        Short[] out = new Short[params.length];
        for (int i = 0; i < out.length; ++i) {
            out[i] = (short) params[i];
        }
        return out;
    }


    /**
     * Shortcut for null. Use this instead of the regular null when nesting arrays.
     */
    public final static Void $null = null;

    /**
     * Shortcut for an empty <code>Object[0]</code> array.
     */
    public final static Object $ = new Object[0];

    /**
     * Shortcut for returning an empty array of objects.
     *
     * @return an empty <code>Object[]</code> array.
     */
    public static Object $() {
        return $;
    }

    /**
     * Shortcut for an empty <code>Byte[0]</code> array.
     * <br>
     * Do not confuse with {@link ArrayShortcuts#$b $b}. <code>B</code> stands for "Byte".
     */
    public final static Object $B = new Byte[0];

    /**
     * Shortcut for an empty <code>Short[0]</code> array.
     */
    public final static Object $S = new Short[0];

    /**
     * Shortcut for an empty <code>Integer[0]</code> array.
     */
    public final static Object $I = new Integer[0];

    /**
     * Shortcut for an empty <code>Long[0]</code> array.
     */
    public final static Object $L = new Long[0];

    /**
     * Shortcut for an empty <code>Float[0]</code> array.
     */
    public final static Object $F = new Float[0];

    /**
     * Shortcut for an empty <code>Double[0]</code> array.
     */
    public final static Object $D = new Double[0];

    /**
     * Shortcut for an empty <code>Boolean[0]</code> array.
     * <br>
     * Do not confuse with {@link ArrayShortcuts#$B $B}. <code>b</code> stands for "bit".
     */
    public final static Object $b = new Boolean[0];

    /**
     * Shortcut for an empty <code>Character[0]</code> array.
     */
    public final static Object $C = new Character[0];

}
