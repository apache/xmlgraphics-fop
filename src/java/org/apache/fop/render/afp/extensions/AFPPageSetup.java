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
import org.apache.fop.util.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This is the pass-through value object for the PostScript extension.
 */
public class AFPPageSetup implements ExtensionAttachment, Serializable, XMLizable  {

    private static final long serialVersionUID = 7190606822558332901L;

    /** The category URI for this extension attachment. */
    public static final String CATEGORY = "apache:fop:extensions:afp";

    private String elementName;

    private String name;

    private String value;

    private String content;

    /**
     * Default constructor.
     * 
     * @param elementName the name of the setup code object, may be null
     */
    public AFPPageSetup(String elementName) {
        this.elementName = elementName;
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

    /**
     * @return the data
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the data
     * @param content The byte data to set.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "AFPPageSetup(element-name=" + getElementName() + " name=" + getName() + ")";
    }

    private static final String ATT_NAME = "name";
    private static final String ATT_VALUE = "value";

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (name != null && name.length() > 0) {
            atts.addAttribute(null, ATT_NAME, ATT_NAME, "CDATA", name);
        }
        if (value != null && value.length() > 0) {
            atts.addAttribute(null, ATT_VALUE, ATT_VALUE, "CDATA", value);
        }
        handler.startElement(CATEGORY, elementName, elementName, atts);
        if (content != null && content.length() > 0) {
            char[] chars = content.toCharArray();
            handler.characters(chars, 0, chars.length);
        }
        handler.endElement(CATEGORY, elementName, elementName);
    }
}
