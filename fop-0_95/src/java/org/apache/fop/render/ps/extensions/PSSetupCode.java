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

package org.apache.fop.render.ps.extensions;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This is the pass-through value object for the PostScript extension.
 */
public class PSSetupCode extends PSExtensionAttachment {
    /**
     * element name
     */
    protected static final String ELEMENT = "ps-setup-code";
    
    private static final String ATT_NAME = "name";

    /**
     * name attribute
     */
    protected String name = null;

    /**
     * No-argument contructor.
     */
    public PSSetupCode() {
    }
    
    /**
     * Default constructor.
     * @param name the name of the setup code object, may be null
     * @param content the content of the setup code object
     */
    public PSSetupCode(String name, String content) {
        super(content);
        this.name = name;
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

    /** {@inheritDoc} */
    public String getCategory() {
        return CATEGORY;
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "PSSetupCode(name=" + getName() + ", content='" + getContent() + "')";
    }

    /**
     * @return the element name
     * @see org.apache.fop.render.ps.extensions.PSExtensionAttachment#getElement()
     */
    protected String getElement() {
        return ELEMENT;
    }
    
    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (name != null && name.length() > 0) {
            atts.addAttribute(null, ATT_NAME, ATT_NAME, "CDATA", name);
        }
        String element = getElement();
        handler.startElement(CATEGORY, element, element, atts);
        if (content != null && content.length() > 0) {
            char[] chars = content.toCharArray();
            handler.characters(chars, 0, chars.length);
        }
        handler.endElement(CATEGORY, element, element);
    }
}
