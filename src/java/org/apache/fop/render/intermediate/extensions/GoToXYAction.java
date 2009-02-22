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
public class GoToXYAction extends AbstractAction implements DocumentNavigationExtensionConstants {

    private int pageIndex = -1;
    private Point targetLocation;

    /**
     * Creates a new instance with yet unknown location.
     * @param id the identifier for this action
     */
    public GoToXYAction(String id) {
        this(id, -1, null);
    }

    /**
     * Creates a new instance.
     * @param id the identifier for this action
     * @param pageIndex the index (0-based) of the target page, -1 if the page index is
     *                  still unknown
     * @param targetLocation the absolute location on the page (coordinates in millipoints),
     *                  or null, if the position isn't known, yet
     */
    public GoToXYAction(String id, int pageIndex, Point targetLocation) {
        setID(id);
        if (pageIndex < 0 && targetLocation != null) {
            throw new IllegalArgumentException(
                    "Page index may not be null if target location is known!");
        }
        setPageIndex(pageIndex);
        setTargetLocation(targetLocation);
    }

    /**
     * Sets the index of the target page.
     * @param pageIndex the index (0-based) of the target page
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
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
    public boolean isComplete() {
        return (getPageIndex() >= 0) && (getTargetLocation() != null);
    }

    /** {@inheritDoc} */
    public boolean isSame(AbstractAction other) {
        if (other == null) {
            throw new NullPointerException("other must not be null");
        }
        if (!(other instanceof GoToXYAction)) {
            return false;
        }
        GoToXYAction otherAction = (GoToXYAction)other;
        if (getPageIndex() != otherAction.getPageIndex()) {
            return false;
        }
        if (getTargetLocation() == null || otherAction.getTargetLocation() == null) {
            return false;
        }
        if (!getTargetLocation().equals(otherAction.getTargetLocation())) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        if (getTargetLocation() == null) {
            setTargetLocation(new Point(0, 0));
        }
        AttributesImpl atts = new AttributesImpl();
        if (isComplete()) {
            atts.addAttribute(null, "id", "id", XMLUtil.CDATA, getID());
            atts.addAttribute(null, "page-index", "page-index",
                    XMLUtil.CDATA, Integer.toString(pageIndex));
            atts.addAttribute(null, "x", "x", XMLUtil.CDATA, Integer.toString(targetLocation.x));
            atts.addAttribute(null, "y", "y", XMLUtil.CDATA, Integer.toString(targetLocation.y));
        } else {
            atts.addAttribute(null, "idref", "idref", XMLUtil.CDATA, getID());
        }
        handler.startElement(GOTO_XY.getNamespaceURI(),
                GOTO_XY.getLocalName(), GOTO_XY.getQName(), atts);
        handler.endElement(GOTO_XY.getNamespaceURI(),
                GOTO_XY.getLocalName(), GOTO_XY.getQName());
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GoToXY: ID=" + getID()
            + ", page=" + getPageIndex()
            + ", loc=" + getTargetLocation() + ", "
            + (isComplete() ? "complete" : "INCOMPLETE");
    }

}
