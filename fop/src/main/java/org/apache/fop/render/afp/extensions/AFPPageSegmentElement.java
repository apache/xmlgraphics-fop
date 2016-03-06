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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * This class extends the org.apache.fop.extensions.ExtensionObj class. The
 * object faciliates extraction of elements from formatted objects based on
 * the static list as defined in the AFPElementMapping implementation.
 * <p/>
 */
public class AFPPageSegmentElement extends AFPPageSetupElement {

    private static final String ATT_RESOURCE_SRC = "resource-file";

    /**
     * Constructs an AFP object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param name the name of the afp element
     */
    public AFPPageSegmentElement(FONode parent, String name) {
        super(parent, name);
    }


    private AFPPageSegmentSetup getPageSetupAttachment() {
        return (AFPPageSegmentSetup)getExtensionAttachment();
    }


    /** {@inheritDoc} */
    public void processNode(String elementName, Locator locator,
                            Attributes attlist, PropertyList propertyList)
                                throws FOPException {

        AFPPageSegmentSetup pageSetup = getPageSetupAttachment();
        super.processNode(elementName, locator, attlist, propertyList);


        String attr = attlist.getValue(ATT_RESOURCE_SRC);

        if (attr != null && attr.length() > 0) {
            pageSetup.setResourceSrc(attr);
        }

    }

    /** {@inheritDoc} */
    protected ExtensionAttachment instantiateExtensionAttachment() {
        return new AFPPageSegmentSetup(getLocalName());
    }

    /**
     * This is the pass-through value object for the AFP extension.
     */
    public static class AFPPageSegmentSetup extends AFPPageSetup {

        private static final long serialVersionUID = 1L;

        private String resourceSrc;

        /**
         * Default constructor.
         *
         * @param elementName the name of the setup code object, may be null
         */
        public AFPPageSegmentSetup(String elementName) {
            super(elementName);
        }

        /**
         * Returns the source URI for the page segment.
         * @return the source URI
         */
        public String getResourceSrc() {
            return resourceSrc;
        }

        /**
         * Sets the source URI for the page segment.
         * @param resourceSrc the source URI
         */
        public void setResourceSrc(String resourceSrc) {
            this.resourceSrc = resourceSrc.trim();
        }


        /** {@inheritDoc} */
        public void toSAX(ContentHandler handler) throws SAXException {
            AttributesImpl atts = new AttributesImpl();
            if (name != null && name.length() > 0) {
                atts.addAttribute(null, ATT_NAME, ATT_NAME, "CDATA", name);
            }
            if (value != null && value.length() > 0) {
                atts.addAttribute(null, ATT_VALUE, ATT_VALUE, "CDATA", value);
            }

            if (resourceSrc != null && resourceSrc.length() > 0) {
                atts.addAttribute(null, ATT_RESOURCE_SRC, ATT_RESOURCE_SRC, "CDATA", resourceSrc);
            }

            handler.startElement(CATEGORY, elementName, elementName, atts);
            if (content != null && content.length() > 0) {
                char[] chars = content.toCharArray();
                handler.characters(chars, 0, chars.length);
            }
            handler.endElement(CATEGORY, elementName, elementName);
        }

        /** {@inheritDoc} */
        public String toString() {
            return "AFPPageSegmentSetup(element-name=" + getElementName()
                + " name=" + getName()
                + " value=" + getValue()
                + " resource=" + getResourceSrc() + ")";
        }

    }


}
