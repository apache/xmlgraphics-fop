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

import java.io.Serializable;

import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * This is the pass-through value object for the PostScript extension.
 */
public class AFPPageSetup implements ExtensionAttachment, Serializable {

    /** The category URI for this extension attachment. */
    public static final String CATEGORY = "apache:fop:extensions:afp";

    private String elementName;

    private String name;

    private String value;

    /**
     * Default constructor.
     * @param name the name of the setup code object, may be null
     */
    public AFPPageSetup(String name) {
        this.elementName = name;
    }

    /** @return the name */
    public String getElementName() {
        return elementName;
    }

    /** @return the name */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the setup code object.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value 
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value
     * @param source The value name to set.
     */
    public void setValue(String source) {
        this.value = source;
    }

    /** @see org.apache.fop.fo.extensions.ExtensionAttachment#getCategory() */
    public String getCategory() {
        return CATEGORY;
    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        return "AFPPageSetup(element-name=" + getElementName() + " name=" + getName() + ")";
    }

}
