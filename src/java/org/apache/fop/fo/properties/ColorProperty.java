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

/* $Id: ColorTypeProperty.java 377045 2006-02-11 20:23:47Z jeremias $ */

package org.apache.fop.fo.properties;

import java.awt.Color;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.ColorUtil;

/**
 * Superclass for properties that wrap Color values
 */
public class ColorProperty extends Property  {
    
    /**
     * The color represented by this property.
     */
    protected final Color color;

    
    /**
     * Inner class for creating instances of ColorTypeProperty
     */
    public static class Maker extends PropertyMaker {

        /**
         * @param propId the id of the property for which a Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * Return a ColorProperty object based on the passed Property object.
         * This method is called if the Property object built by the parser
         * isn't the right type for this property.
         * 
         * @param p
         *            The Property object return by the expression parser
         * @param propertyList
         *            The PropertyList object being built for this FO.
         * @param fo
         *            The parent FO for the FO whose property is being made.
         * @return A Property of the correct type or null if the parsed value
         *         can't be converted to the correct type.
         * @throws PropertyException
         *             for invalid or inconsistent FO input
         * @see org.apache.fop.fo.properties.PropertyMaker#convertProperty(
         *          org.apache.fop.fo.properties.Property,
         *      org.apache.fop.fo.PropertyList, org.apache.fop.fo.FObj)
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo) 
                    throws PropertyException {
            if (p instanceof ColorProperty) {
                return p;
            }
            Color val = p.getColor();
            if (val != null) {
                return new ColorProperty(val);
            }
            return convertPropertyDatatype(p, propertyList, fo);
        }

    }


    /**
     * Set the color given a particular String. For a full List of supported
     * values please see ColorUtil.
     * 
     * @param value RGB value as String to be parsed
     * @throws PropertyException if the value can't be parsed
     * @see ColorUtil#parseColorString(String)
     */
    public ColorProperty(String value) throws PropertyException {
        this.color = ColorUtil.parseColorString(value);
    }

    /**
     * Create a new ColorProperty with a given color.
     * 
     * @param value the color to use.
     */
    public ColorProperty(Color value) {
        this.color = value;
    }
    
    /**
     * Returns an AWT instance of this color
     * @return float the AWT color represented by this ColorType instance
     */
    public Color getColor() {
        return color;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return ColorUtil.colorTOsRGBString(color);
    }

    /**
     * Can't convert to any other types
     * @return this.colorType
     */
    public ColorProperty getColorProperty() {
        return this;
    }

    /**
     * @return this.colorType cast as an Object
     */
    public Object getObject() {
        return this;
    }
}

