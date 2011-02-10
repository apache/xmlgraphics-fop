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

package org.apache.fop.datatypes;

import java.io.UnsupportedEncodingException;


/**
 * This class contains method to deal with the <uri-specification> datatype from XSL-FO.
 */
public final class URISpecification {

    private URISpecification() {
    }

    /**
     * Get the URL string from a wrapped URL.
     *
     * @param href the input wrapped URL
     * @return the raw URL
     */
    public static String getURL(String href) {
        /*
         * According to section 5.11 a <uri-specification> is:
         * "url(" + URI + ")"
         * according to 7.28.7 a <uri-specification> is:
         * URI
         * So handle both.
         */
        href = href.trim();
        if (href.startsWith("url(") && (href.indexOf(")") != -1)) {
            href = href.substring(4, href.lastIndexOf(")")).trim();
            if (href.startsWith("'") && href.endsWith("'")) {
                href = href.substring(1, href.length() - 1);
            } else if (href.startsWith("\"") && href.endsWith("\"")) {
                href = href.substring(1, href.length() - 1);
            }
        } else {
            // warn
        }
        return href;
    }

    private static final String PUNCT = ",;:$&+=";
    private static final String RESERVED = PUNCT + "?/[]@";

    private static boolean isValidURIChar(char ch) {
        return true;
    }

    private static boolean isDigit(char ch) {
        return (ch >= '0' && ch <= '9');
    }

    private static boolean isAlpha(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'A' && ch <= 'z');
    }

    private static boolean isHexDigit(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
    }

    private static boolean isReserved(char ch) {
        if (RESERVED.indexOf(ch) >= 0) {
            return true;
        } else if ('#' == ch) {
            //# is not a reserved character but is used for the fragment
            return true;
        }
        return false;
    }

    private static boolean isUnreserved(char ch) {
        if (isDigit(ch) || isAlpha(ch)) {
            return true;
        } else if ("_-!.~\'()*".indexOf(ch) >= 0) {
            //remaining unreserved characters
            return true;
        }
        return false;
    }

    private static final char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static void appendEscape(StringBuffer sb, byte b) {
        sb.append('%').append(HEX_DIGITS[(b >> 4) & 0x0f]).append(HEX_DIGITS[(b >> 0) & 0x0f]);
    }

    /**
     * Escapes any illegal URI character in a given URI, for example, it escapes a space to "%20".
     * Note: This method does not "parse" the URI and therefore does not treat the individual
     * components (user-info, path, query etc.) individually.
     * @param uri the URI to inspect
     * @return the escaped URI
     */
    public static String escapeURI(String uri) {
        uri = getURL(uri);
        StringBuffer sb = new StringBuffer();
        for (int i = 0, c = uri.length(); i < c; i++) {
            char ch = uri.charAt(i);
            if (ch == '%') {
                if (i < c - 3 && isHexDigit(uri.charAt(i + 1)) && isHexDigit(uri.charAt(i + 2))) {
                    sb.append(ch);
                    continue;
                }
            }
            if (isReserved(ch) || isUnreserved(ch)) {
                //Note: this may not be accurate for some very special cases.
                sb.append(ch);
            } else {
                try {
                    byte[] utf8 = Character.toString(ch).getBytes("UTF-8");
                    for (int j = 0, cj = utf8.length; j < cj; j++) {
                        appendEscape(sb, utf8[j]);
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new Error("Incompatible JVM. UTF-8 not supported.");
                }
            }
        }
        return sb.toString();
    }

}
