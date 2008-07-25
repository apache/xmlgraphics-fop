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

package org.apache.fop.fo.expr;

import java.awt.Color;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.util.ColorUtil;

/**
 * Class for handling NC Name objects
 */
public class NCnameProperty extends Property {

    private final String ncName;

    /**
     * Constructor
     * @param ncName string representing the ncName
     */
    public NCnameProperty(String ncName) {
        this.ncName = ncName;
    }

    /**
     * If a system color, return the corresponding value.
     *
     * @param foUserAgent
     *     Reference to FOP user agent - keeps track of cached ColorMaps for ICC colors
     * @return Color object corresponding to the NCName
     */
    public Color getColor(FOUserAgent foUserAgent)  {
        try {
            return ColorUtil.parseColorString(foUserAgent, ncName);
        } catch (PropertyException e) {
            //Not logging this error since for properties like "border" you would get an awful
            //lot of error messages for things like "solid" not being valid colors.
            //log.error("Can't create color value: " + e.getMessage());
            return null;
        }
    }

    /**
     * @return the name as a String (should be specified with quotes!)
     */
    public String getString() {
        return this.ncName;
    }

    /**
     * @return the name as an Object.
     */
    public Object getObject() {
        return this.ncName;
    }

    /**
     * @return ncName for this
     */
    public String getNCname() {
        return this.ncName;
    }

}
