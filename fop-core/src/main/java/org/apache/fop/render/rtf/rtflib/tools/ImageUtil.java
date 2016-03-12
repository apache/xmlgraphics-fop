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

package org.apache.fop.render.rtf.rtflib.tools;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

/**
 * <p>Miscellaneous utilities for images handling.
 * This class belongs to the <fo:external-graphic> tag processing.</p>
 *
 * <p>This work was authored by Andreas Putz (a.putz@skynamics.com).</p>
 */
public final class ImageUtil {

    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Private constructor.
     */
    private ImageUtil() {
    }


    //////////////////////////////////////////////////
    // @@ Public static methods
    //////////////////////////////////////////////////

    /**
     * Determines the digits from a string.
     *
     * @param value String with digits
     *
     * @return -1      There is no digit
     *         number  The digits as integer
     */
    public static int getInt(String value) {
        String retString = "";
        StringBuffer s = new StringBuffer(value);
        int len = s.length();

        for (int i = 0; i < len; i++) {
            if (Character.isDigit(s.charAt(i))) {
                retString += s.charAt(i);
            } else {
                //for example "600.0pt" has to be exited,
                //when the dot is reached.
                break;
            }
        }

        if (retString.length() == 0) {
            return -1;
        } else {
            return Integer.parseInt(retString);
        }
    }

    /**
     * Checks the string for percent character at the end of string.
     *
     * @param value String with digits
     *
     * @return true    The string contains a % value
     *         false   Other string
     */
    public static boolean isPercent(String value) {
        if (value.endsWith("%")) {
            return true;

        }

        return false;
    }

    /**
     * Compares two hexadecimal values.
     *
     * @param pattern Target
     * @param data Data
     * @param searchAt Position to start compare
     * @param searchForward Direction to compare byte arrays
     *
     * @return true    If equal
     *         false   If different
     */
    public static boolean compareHexValues(byte[] pattern, byte[] data, int searchAt,
                                            boolean searchForward) {
        if (searchAt >= data.length) {
            return false;

        }

        int pLen = pattern.length;

        if (searchForward) {
            if (pLen >= (data.length - searchAt)) {
                return false;

            }

            for (int i = 0; i < pLen; i++) {
                if (pattern[i] != data[searchAt + i]) {
                    return false;
                }
            }

            return true;
        } else {
            if (pLen > (searchAt + 1)) {
                return false;

            }

            for (int i = 0; i < pLen; i++) {
                if (pattern[pLen - i - 1] != data[searchAt - i]) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Determines a integer value from a hexadecimal byte array.
     *
     * @param data Image
     * @param startAt Start index to read from
     * @param length Number of data elements to read
     * @param searchForward True if searching forward, False if not (??)
     *
     * @return integer
     */
    public static int getIntFromByteArray(byte[] data, int startAt, int length,
                                           boolean searchForward) {
        int bit = 8;
        int bitMoving = length * bit;
        int retVal = 0;

        if (startAt >= data.length) {
            return retVal;

        }

        if (searchForward) {
            if (length >= (data.length - startAt)) {
                return retVal;

            }

            for (int i = 0; i < length; i++) {
                bitMoving -= bit;
                int iData = (int) data[startAt + i];
                if (iData < 0) {
                    iData += 256;
                }
                retVal += iData << bitMoving;
            }
        } else {
            if (length > (startAt + 1)) {
                return retVal;

            }

            for (int i = 0; i < length; i++) {
                bitMoving -= bit;
                int iData = (int) data[startAt - i];
                if (iData < 0) {
                    iData += 256;
                }
                retVal += iData << bitMoving;            }
        }

        return retVal;
    }
}
