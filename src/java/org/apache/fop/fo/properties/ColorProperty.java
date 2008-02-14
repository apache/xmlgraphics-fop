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

package org.apache.fop.fo.properties;

import java.awt.Color;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.ColorUtil;

/**
 * Class for properties that wrap Color values
 */
public final class ColorProperty extends Property  {
    
    /** cache holding canonical ColorProperty instances */
    private static final PropertyCache cache = new PropertyCache();
    
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
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo) 
                    throws PropertyException {
            if (p instanceof ColorProperty) {
                return p;
            }
            FObj fobj = (fo == null ? propertyList.getFObj() : fo);
            FOUserAgent ua = (fobj == null ? null : fobj.getUserAgent());
            Color val = p.getColor(ua);
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
     * @param foUserAgent FOP user agent
     * @param value RGB value as String to be parsed
     * @throws PropertyException if the value can't be parsed
     * @see ColorUtil#parseColorString(FOUserAgent, String)
     */
    public static ColorProperty getInstance(FOUserAgent foUserAgent, String value) throws PropertyException {
        ColorProperty instance = new ColorProperty(
                                       ColorUtil.parseColorString(
                                               foUserAgent, value));
        return (ColorProperty) cache.fetch(instance);
    }

    /**
     * Returns an instance of a color property given a color
     * @param color the color value
     * @return the color property
     */
    public static ColorProperty getInstance(Color color) {
        return (ColorProperty) cache.fetch(new ColorProperty(color));
    }
    
    /**
     * Create a new ColorProperty with a given color.
     * 
     * @param value the color to use.
     */
    private ColorProperty(Color value) {
        this.color = value;
    }
    
    /**
     * Returns an AWT instance of this color
     * @param foUserAgent FOP user agent
     * @return float the AWT color represented by this ColorType instance
     */
    public Color getColor(FOUserAgent foUserAgent) {
        return color;
    }

    /** {@inheritDoc} */
    public String toString() {
        return ColorUtil.colorToString(color);
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
    
    /** {@inheritDoc} */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o instanceof ColorProperty) {
            return ((ColorProperty) o).color.equals(this.color);
        }
        return false;
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        return this.color.hashCode();
    }
}

