/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.afp.tools;

import java.io.ByteArrayOutputStream;

/**
 * Library of utility useful conversion methods.
 *
 */
public final class BinaryUtils {

    /**
     * Convert an int into the corresponding byte array by encoding each
     * two hexadecimal digits as a char. This will return a byte array
     * to the length specified by bufsize.
     * @param integer The int representation.
     * @param bufsize The required byte array size.
     */
    public static byte[] convert(int integer, int bufsize) {

        StringBuffer buf = new StringBuffer(Integer.toHexString(integer));
        if (buf.length() % 2 == 0) {
            // Ignore even number of digits
        } else {
            // Convert to an even number of digits
            buf.insert(0, "0");
        }
        int size = buf.length() / 2;
        while (size < bufsize) {
            buf.insert(0, "00");
            size++;
        };
        return convert(buf.toString());

    }

    /**
     * Convert an int into the corresponding byte array by encoding each
     * two hexadecimal digits as a char.
     * @param integer The int representation
     */
    public static byte[] convert(int integer) {

        return convert(Integer.toHexString(integer));

    }

    /**
     * Convert a String of hexadecimal digits into the corresponding
     * byte array by encoding each two hexadecimal digits as a byte.
     * @param digits The hexadecimal digits representation.
     */
    public static byte[] convert(String digits) {

        if (digits.length() % 2 == 0) {
            // Even number of digits, so ignore
        } else {
            // Convert to an even number of digits
            digits = "0" + digits;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < digits.length(); i += 2) {
            char c1 = digits.charAt(i);
            char c2 = digits.charAt(i + 1);
            byte b = 0;
            if ((c1 >= '0') && (c1 <= '9'))
                b += ((c1 - '0') * 16);
            else if ((c1 >= 'a') && (c1 <= 'f'))
                b += ((c1 - 'a' + 10) * 16);
            else if ((c1 >= 'A') && (c1 <= 'F'))
                b += ((c1 - 'A' + 10) * 16);
            else
                throw new IllegalArgumentException("Bad hexadecimal digit");
            if ((c2 >= '0') && (c2 <= '9'))
                b += (c2 - '0');
            else if ((c2 >= 'a') && (c2 <= 'f'))
                b += (c2 - 'a' + 10);
            else if ((c2 >= 'A') && (c2 <= 'F'))
                b += (c2 - 'A' + 10);
            else
                throw new IllegalArgumentException("Bad hexadecimal digit");
            baos.write(b);
        }
        return (baos.toByteArray());

    }

    /**
     * Convert the specified short into a byte array.
     * @param value The value to be converted.
     * @param array The array to receive the data.
     * @param offset The offset into the byte array for the start of the value.
     */
    public static void shortToByteArray(
        short value,
        byte[] array,
        int offset) {
        array[offset] = (byte) (value >>> 8);
        array[offset + 1] = (byte) value;
    }

    /**
     * Convert the specified short into a byte array.
     * @param value The value to be converted.
     * @return The byte array
     */
    public static byte[] shortToByteArray(short value) {
        byte[] serverValue = new byte[2];
        shortToByteArray(value, serverValue, 0);
        return serverValue;
    }

}
