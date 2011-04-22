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

package org.apache.fop.render.afp.extensions;

/**
 * An enumeration for placement instruction for AFP extensions.
 */
public enum ExtensionPlacement {

    /** Place the extension at its default/usual position. */
    DEFAULT,
    /** Place the extension right before the "End" structured field. */
    BEFORE_END;

    /**
     * Returns the XML value that corresponds to this enum value.
     * @return the XML value
     */
    public String getXMLValue() {
        String xmlName = name().toLowerCase();
        xmlName = xmlName.replace('_', '-');
        return xmlName;
    }

    /**
     * Returns the enum value from the given XML value for this enumeration.
     * @param value the XML value
     * @return the enum value
     */
    public static ExtensionPlacement fromXMLValue(String value) {
        String name = value.toUpperCase();
        name = name.replace('-', '_');
        return ExtensionPlacement.valueOf(name);
    }

}
