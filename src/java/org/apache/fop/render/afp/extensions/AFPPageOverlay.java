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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This extension allows to include an AFP Page Overlay resource. It is implemented as an extension
 * attachment ({@link ExtensionAttachment}).
 */
public class AFPPageOverlay extends AFPExtensionAttachment {

    private static final long serialVersionUID = 8548056652642588919L;

    /** X coordinate attribute */
    protected static final String ATT_X = "X";
    /** X coordinate attribute */
    protected static final String ATT_Y = "Y";

    /**
     * The x coordinate
     */
    private int x = 0;

    /**
     * The y coordinate
     */
    private int y = 0;

    /**
     * Default constructor.
     */
    public AFPPageOverlay() {
        super(AFPElementMapping.INCLUDE_PAGE_OVERLAY);
    }

    /**
     * returns X coordinate
     * @return x integer
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the X coordinate
     * @param x The integer to be set.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * returns Y coordinate
     * @return y integer
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the Y coordinate
     * @param y The integer to be set.
     */
    public void setY(int y) {
        this.y = y;
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (name != null && name.length() > 0) {
            atts.addAttribute(null, ATT_NAME, ATT_NAME, "CDATA", name);
        }
        handler.startElement(CATEGORY, elementName, elementName, atts);
        handler.endElement(CATEGORY, elementName, elementName);
    }

    /** {@inheritDoc} */
    public String toString() {
        return getClass().getName() + "(element-name=" + getElementName()
            + " name=" + getName() + " x=" + getX() + " y=" + getY() + ")";
    }
}
