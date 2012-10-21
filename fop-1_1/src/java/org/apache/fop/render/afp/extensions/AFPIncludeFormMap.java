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

import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This extension allows to include an AFP form map resource. It is implemented as an extension
 * attachment ({@link org.apache.fop.fo.extensions.ExtensionAttachment}).
 */
public class AFPIncludeFormMap extends AFPExtensionAttachment {

    private static final long serialVersionUID = 8548056652642588914L;

    /** src attribute containing the URI to the form map resource */
    protected static final String ATT_SRC = "src";

    /**
     * the URI identifying the form map resource.
     */
    protected URI src;

    /**
     * Default constructor.
     */
    public AFPIncludeFormMap() {
        super(AFPElementMapping.INCLUDE_FORM_MAP);
    }

    /**
     * Returns the URI of the form map.
     * @return the form map URI
     */
    public URI getSrc() {
        return this.src;
    }

    /**
     * Sets the URI of the form map.
     * @param value the form map URI
     */
    public void setSrc(URI value) {
        this.src = value;
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (name != null && name.length() > 0) {
            atts.addAttribute(null, ATT_NAME, ATT_NAME, "CDATA", name);
        }
        if (this.src != null) {
            atts.addAttribute(null, ATT_SRC, ATT_SRC, "CDATA", this.src.toASCIIString());
        }
        handler.startElement(CATEGORY, elementName, elementName, atts);
        handler.endElement(CATEGORY, elementName, elementName);
    }

    /** {@inheritDoc} */
    public String toString() {
        return getClass().getName() + "(element-name=" + getElementName()
            + " name=" + getName() + " src=" + getSrc() + ")";
    }
}
