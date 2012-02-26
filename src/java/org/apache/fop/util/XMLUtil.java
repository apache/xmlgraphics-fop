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

package org.apache.fop.util;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A collection of utility method for XML handling.
 */
public final class XMLUtil implements XMLConstants {

    private XMLUtil() {
    }

    /**
     * Returns an attribute value as a boolean value.
     * @param attributes the Attributes object
     * @param name the name of the attribute
     * @param defaultValue the default value if the attribute is not specified
     * @return the attribute value as a boolean
     */
    public static boolean getAttributeAsBoolean(Attributes attributes, String name,
            boolean defaultValue) {
        String s = attributes.getValue(name);
        if (s == null) {
            return defaultValue;
        } else {
            return Boolean.valueOf(s).booleanValue();
        }
    }

    /**
     * Returns an attribute value as a int value.
     * @param attributes the Attributes object
     * @param name the name of the attribute
     * @param defaultValue the default value if the attribute is not specified
     * @return the attribute value as an int
     */
    public static int getAttributeAsInt(Attributes attributes, String name,
            int defaultValue) {
        String s = attributes.getValue(name);
        if (s == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(s);
        }
    }

    /**
     * Returns an attribute value as a int value.
     * @param attributes the Attributes object
     * @param name the name of the attribute
     * @return the attribute value as an int
     * @throws SAXException if the attribute is missing
     */
    public static int getAttributeAsInt(Attributes attributes, String name) throws SAXException {
        String s = attributes.getValue(name);
        if (s == null) {
            throw new SAXException("Attribute '" + name + "' is missing");
        } else {
            return Integer.parseInt(s);
        }
    }

    /**
     * Returns an attribute value as a Integer value.
     * @param attributes the Attributes object
     * @param name the name of the attribute
     * @return the attribute value as an Integer or null if the attribute is missing
     */
    public static Integer getAttributeAsInteger(Attributes attributes, String name) {
        String s = attributes.getValue(name);
        if (s == null) {
            return null;
        } else {
            return new Integer(s);
        }
    }

    /**
     * Returns an attribute value as a Rectangle2D value. The string value is expected as 4
     * double-precision numbers separated by whitespace.
     * @param attributes the Attributes object
     * @param name the name of the attribute
     * @return the attribute value as an Rectangle2D
     */
    public static Rectangle2D getAttributeAsRectangle2D(Attributes attributes, String name) {
        String s = attributes.getValue(name).trim();
        double[] values = ConversionUtils.toDoubleArray(s, "\\s");
        if (values.length != 4) {
            throw new IllegalArgumentException("Rectangle must consist of 4 double values!");
        }
        return new Rectangle2D.Double(values[0], values[1], values[2], values[3]);
    }

    /**
     * Returns an attribute value as a Rectangle value. The string value is expected as 4
     * integer numbers separated by whitespace.
     * @param attributes the Attributes object
     * @param name the name of the attribute
     * @return the attribute value as an Rectangle
     */
    public static Rectangle getAttributeAsRectangle(Attributes attributes, String name) {
        String s = attributes.getValue(name);
        if (s == null) {
            return null;
        }
        int[] values = ConversionUtils.toIntArray(s.trim(), "\\s");
        if (values.length != 4) {
            throw new IllegalArgumentException("Rectangle must consist of 4 int values!");
        }
        return new Rectangle(values[0], values[1], values[2], values[3]);
    }

    /**
     * Returns an attribute value as a integer array. The string value is expected as 4
     * integer numbers separated by whitespace.
     * @param attributes the Attributes object
     * @param name the name of the attribute
     * @return the attribute value as an int array
     */
    public static int[] getAttributeAsIntArray(Attributes attributes, String name) {
        String s = attributes.getValue(name);
        if (s == null) {
            return null;
        } else {
            return ConversionUtils.toIntArray(s.trim(), "\\s");
        }
    }

    /**
     * Adds an attribute to a given {@link AttributesImpl} instance.
     * @param atts the attributes collection
     * @param attribute the attribute to add
     * @param value the attribute's CDATA value
     */
    public static void addAttribute(AttributesImpl atts,
            org.apache.xmlgraphics.util.QName attribute, String value) {
        atts.addAttribute(attribute.getNamespaceURI(),
                attribute.getLocalName(), attribute.getQName(), XMLUtil.CDATA, value);
    }

    /**
     * Adds an attribute to a given {@link AttributesImpl} instance. The attribute will be
     * added in the default namespace.
     * @param atts the attributes collection
     * @param localName the local name of the attribute
     * @param value the attribute's CDATA value
     */
    public static void addAttribute(AttributesImpl atts, String localName, String value) {
        atts.addAttribute("", localName, localName, XMLUtil.CDATA, value);
    }

    /**
     * Encode a glyph position adjustments array as a string, where the string value
     * adheres to the following syntax:
     *
     * count ( 'Z' repeat | number )
     *
     * where each token is separated by whitespace, except that 'Z' followed by repeat
     * are considered to be a single token with no intervening whitespace, and where
     * 'Z' repeat encodes repeated zeroes.
     * @param dp the adjustments array
     * @param paCount the number of entries to encode from adjustments array
     * @return the encoded value
     */
    public static String encodePositionAdjustments ( int[][] dp, int paCount ) {
        assert dp != null;
        StringBuffer sb = new StringBuffer();
        int na = paCount;
        int nz = 0;
        sb.append ( na );
        for ( int i = 0; i < na; i++ ) {
            int[] pa = dp [ i ];
            for ( int k = 0; k < 4; k++ ) {
                int a = pa [ k ];
                if ( a != 0 ) {
                    encodeNextAdjustment ( sb, nz, a ); nz = 0;
                } else {
                    nz++;
                }
            }
        }
        encodeNextAdjustment ( sb, nz, 0 );
        return sb.toString();
    }

    /**
     * Encode a glyph position adjustments array as a string, where the string value
     * adheres to the following syntax:
     *
     * count ( 'Z' repeat | number )
     *
     * where each token is separated by whitespace, except that 'Z' followed by repeat
     * are considered to be a single token with no intervening whitespace.
     * @param dp the adjustments array
     * @return the encoded value
     */
    public static String encodePositionAdjustments ( int[][] dp ) {
        assert dp != null;
        return encodePositionAdjustments ( dp, dp.length );
    }

    private static void encodeNextAdjustment ( StringBuffer sb, int nz, int a ) {
        encodeZeroes ( sb, nz );
        encodeAdjustment ( sb, a );
    }

    private static void encodeZeroes ( StringBuffer sb, int nz ) {
        if ( nz > 0 ) {
            sb.append ( ' ' );
            if ( nz == 1 ) {
                sb.append ( '0' );
            } else {
                sb.append ( 'Z' );
                sb.append ( nz );
            }
        }
    }

    private static void encodeAdjustment ( StringBuffer sb, int a ) {
        if ( a != 0 ) {
            sb.append ( ' ' );
            sb.append ( a );
        }
    }

    /**
     * Decode a string as a glyph position adjustments array, where the string
     * shall adhere to the syntax specified by {@link #encodePositionAdjustments}.
     * @param value the encoded value
     * @return the position adjustments array
     */
    public static int[][] decodePositionAdjustments ( String value ) {
        int[][] dp = null;
        if ( value != null ) {
            String[] sa = value.split ( "\\s" );
            if ( sa != null ) {
                if ( sa.length > 0 ) {
                    int na = Integer.parseInt ( sa[0] );
                    dp = new int [ na ] [ 4 ];
                    for ( int i = 1, n = sa.length, k = 0; i < n; i++ ) {
                        String s = sa [ i ];
                        if ( s.charAt(0) == 'Z' ) {
                            int nz = Integer.parseInt ( s.substring ( 1 ) );
                            k += nz;
                        } else {
                            dp [ k / 4 ] [ k % 4 ] = Integer.parseInt ( s );
                            k += 1;
                        }
                    }
                }
            }
        }
        return dp;
    }

    /**
     * Returns an attribute value as a glyph position adjustments array. The string value
     * is expected to be a non-empty sequence of either Z<repeat> or <number>, where the
     * former encodes a repeat count (of zeroes) and the latter encodes a integer number,
     * and where each item is separated by whitespace.
     * @param attributes the Attributes object
     * @param name the name of the attribute
     * @return the position adjustments array
     */
    public static int[][] getAttributeAsPositionAdjustments(Attributes attributes, String name) {
        String s = attributes.getValue(name);
        if (s == null) {
            return null;
        } else {
            return decodePositionAdjustments(s.trim());
        }
    }

}
