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

package org.apache.fop.fo.flow;

import java.awt.Color;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.SpaceProperty;

/**
 * Common change bar base class. Handles change bar properties and validates child nodes.
 */
public abstract class ChangeBar extends FObj {

    /**
     * Constructs a ChangeBar element with common parts for both begin and end change bars.
     *
     * @param parent The parent node
     */
    public ChangeBar(FONode parent) {
        super(parent);
    }

    /**
     * The change bar class (required).
     */
    protected String changeBarClass;

    /**
     * The change bar color.
     */
    protected Color color;

    /**
     * The change bar offset.
     */
    protected Length offset;

    /**
     * The change bar placement.
     */
    protected int placement = -1;

    /**
     * The change bar style.
     */
    protected int style = -1;

    /**
     * The change bar width.
     */
    protected Length width;

    /**
     * The actual line height.
     */
    protected SpaceProperty lineHeight;

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);

        changeBarClass = pList.get(PR_CHANGE_BAR_CLASS).getString();
        color = pList.get(PR_CHANGE_BAR_COLOR).getColor(getUserAgent());
        offset = pList.get(PR_CHANGE_BAR_OFFSET).getLength();
        placement = pList.get(PR_CHANGE_BAR_PLACEMENT).getEnum();
        style = pList.get(PR_CHANGE_BAR_STYLE).getEnum();
        width = pList.get(PR_CHANGE_BAR_WIDTH).getLength();
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
    }

    /** {@inheritDoc} */
    protected void validateChildNode(
            Locator loc,
            String namespaceURI,
            String localName) throws ValidationException {
        // no children allowed
        invalidChildError(loc, namespaceURI, localName);
    }

    /** {@inheritDoc} */
    public void processNode(String elementName, Locator locator,
                            Attributes attlist, PropertyList pList) throws FOPException {
        super.processNode(elementName, locator, attlist, pList);

        if (inMarker()) {
            PropertyList newPList = new StaticPropertyList(this, null);
            newPList.addAttributesToList(attlist);
            bind(newPList);
        }

        if (changeBarClass == null || changeBarClass.isEmpty()) {
            missingPropertyError("change-bar-class");
        }

        if (findAncestor(FO_FLOW) == -1
            && findAncestor(FO_STATIC_CONTENT) == -1) {
            getFOValidationEventProducer().changeBarWrongAncestor(this, getName(), locator);
          }
    }

    /**
     * Adds the current change bar to the active change bar list.
     */
    protected void push() {
        getRoot().getLastPageSequence().pushChangeBar(this);
    }

    /**
     * Removes the starting counterpart of the current change bar from the active change bar list.
     */
    protected void pop() {
        getRoot().getLastPageSequence().popChangeBar(this);
    }

    /**
     * Returns the starting counterpart of the current (ending) change bar.
     *
     * @return The starting counterpart of the current (ending) change bar
     */
    protected ChangeBar getChangeBarBegin() {
        return getRoot().getLastPageSequence().getChangeBarBegin(this);
    }

    /**
     * Returns the change bar class.
     *
     * @return The change bar class
     */
    public String getChangeBarClass() {
        return changeBarClass;
    }

    /**
     * Returns the change bar color.
     *
     * @return The change bar color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the change bar offset.
     *
     * @return The change bar offset
     */
    public Length getOffset() {
        return offset;
    }

    /**
     * Returns the change bar placement.
     *
     * @return The change bar placement
     */
    public int getPlacement() {
        return placement;
    }

    /**
     * Returns the change bar style.
     *
     * @return The change bar style
     */
    public int getStyle() {
        return style;
    }

    /**
     * Returns the change bar width.
     *
     * @return The change bar width
     */
    public Length getWidth() {
        return width;
    }

    /**
     * Returns the line height.
     *
     * @return The line height
     */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

}
