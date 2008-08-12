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

package org.apache.fop.render.intermediate.extensions;

import java.awt.Point;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.util.XMLUtil;

/**
 * Action class which represents a "go-to" action to an absolute coordinate on a page.
 */
public class GoToXYAction extends AbstractAction implements BookmarkExtensionConstants {

    private int pageIndex;
    private Point targetLocation;

    /**
     * Creates a new instance.
     * @param pageIndex the page index (0-based) of the target page
     * @param targetLocation the absolute location on the page (coordinates in millipoints)
     */
    public GoToXYAction(int pageIndex, Point targetLocation) {
        this.pageIndex = pageIndex;
        this.targetLocation = targetLocation;
    }

    /**
     * Returns the page index of the target page.
     * @return the page index (0-based)
     */
    public int getPageIndex() {
        return this.pageIndex;
    }

    /**
     * Returns the absolute coordinates of the target location on the page.
     * @return the target location (coordinates in millipoints)
     */
    public Point getTargetLocation() {
        return this.targetLocation;
    }

    /**
     * Sets the absolute coordinates of the target location on the page.
     * @param location the location (coordinates in millipoints)
     */
    public void setTargetLocation(Point location) {
        this.targetLocation = location;
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(null, "page-index", "page-index",
                XMLUtil.CDATA, Integer.toString(pageIndex));
        atts.addAttribute(null, "x", "x", XMLUtil.CDATA, Integer.toString(targetLocation.x));
        atts.addAttribute(null, "y", "y", XMLUtil.CDATA, Integer.toString(targetLocation.y));
        handler.startElement(GOTO_XY.getNamespaceURI(),
                GOTO_XY.getLocalName(), GOTO_XY.getQName(), atts);
        handler.endElement(GOTO_XY.getNamespaceURI(),
                GOTO_XY.getLocalName(), GOTO_XY.getQName());
    }

}
