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

/**
 * A collection of utility method for XML handling.
 */
public class XMLUtil implements XMLConstants {

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

}
