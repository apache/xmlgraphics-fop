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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.fop.render.afp.AFPLineDataInfo;
import org.apache.fop.render.afp.AFPTextDataInfo;
import org.apache.fop.render.afp.fonts.AFPFont;

/**
 * Pages contain the data objects that comprise a presentation document. Each
 * page has a set of data objects associated with it. Each page within a
 * document is independent from any other page, and each must establish its own
 * environment parameters.
 *
 * The page is the level in the document component hierarchy that is used for
 * printing or displaying a document's content. The data objects contained in
 * the page envelope in the data stream are presented when the page is
 * presented. Each data object has layout information associated with it that
 * directs the placement and orientation of the data on the page. In addition,
 * each page contains layout information that specifies the measurement units,
 * page width, and page depth.
 *
 * A page is initiated by a begin page structured field and terminated by an end
 * page structured field. Structured fields that define objects and active
 * environment groups or that specify attributes of the page may be encountered
 * in page state.
 *
 */
public abstract class AbstractPageObject extends AbstractNamedAFPObject {

    /** The active environment group for the page */
    protected ActiveEnvironmentGroup activeEnvironmentGroup = null;

    /** The current presentation text object */
    private PresentationTextObject currentPresentationTextObject = null;

    /** The list of tag logical elements */
    protected List/*<TagLogicalElement>*/ tagLogicalElements = null;

    /** The list of the include page segments */
    protected List/*<IncludePageSegment>*/ includePageSegments = null;

    /** The list of objects within this resource container */
    protected List/*<AbstractStructuredAFPObject>*/ objects = new java.util.ArrayList();

    /** The page width */
    private int width;

    /** The page height */
    private int height;

    /** The page rotation */
    protected int rotation = 0;

    /** The page state */
    protected boolean complete = false;

    /** The width resolution */
    private int widthRes;

    /** The height resolution */
    private int heightRes;

    /** the object factory */
    protected final Factory factory;

    /**
     * Default constructor
     *
     * @param factory the object factory
     */
    public AbstractPageObject(Factory factory) {
        this.factory = factory;
    }

    /**
     * Main constructor
     *
     * @param factory the object factory
     * @param name the name of this page object
     */
    public AbstractPageObject(Factory factory, String name) {
        super(name);
        this.factory = factory;
    }

    /**
     * Construct a new page object for the specified name argument, the page
     * name should be an 8 character identifier.
     *
     * @param factory
     *            the object factory.
     * @param name
     *            the name of the page.
     * @param width
     *            the width of the page.
     * @param height
     *            the height of the page.
     * @param rotation
     *            the rotation of the page.
     * @param widthRes
     *            the width resolution of the page.
     * @param heightRes
     *            the height resolution of the page.
     */
    public AbstractPageObject(Factory factory,
            String name, int width, int height, int rotation,
            int widthRes, int heightRes) {
        super(name);

        this.factory = factory;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.widthRes = widthRes;
        this.heightRes = heightRes;
    }

    /**
     * Helper method to create a map coded font object on the current page, this
     * method delegates the construction of the map coded font object to the
     * active environment group on the page.
     *
     * @param fontReference
     *            the font number used as the resource identifier
     * @param font
     *            the font
     * @param size
     *            the point size of the font
     */
    public void createFont(int fontReference, AFPFont font, int size) {
        getActiveEnvironmentGroup().createFont(fontReference, font, size, 0);
    }

    /**
     * Helper method to create a line on the current page, this method delegates
     * to the presentation text object in order to construct the line.
     *
     * @param lineDataInfo the line data information.
     */
    public void createLine(AFPLineDataInfo lineDataInfo) {
        getPresentationTextObject().createLineData(lineDataInfo);
    }

    /**
     * Helper method to create text on the current page, this method delegates
     * to the presentation text object in order to construct the text.
     *
     * @param textDataInfo
     *            the afp text data
     */
    public void createText(AFPTextDataInfo textDataInfo) {
        getPresentationTextObject().createTextData(textDataInfo);
    }

    /**
     * Helper method to mark the end of the page. This should end the control
     * sequence on the current presentation text object.
     */
    public void endPage() {
        if (currentPresentationTextObject != null) {
            currentPresentationTextObject.endControlSequence();
        }
        complete = true;
    }

    /**
     * Ends the presentation text object
     */
    protected void endPresentationObject() {
        if (currentPresentationTextObject != null) {
            currentPresentationTextObject.endControlSequence();
            currentPresentationTextObject = null;
        }
    }

    /**
     * Helper method to create a presentation text object
     * on the current page and to return the object.
     *
     * @return the presentation text object
     */
    private PresentationTextObject getPresentationTextObject() {
        if (currentPresentationTextObject == null) {
            PresentationTextObject presentationTextObject
                = factory.createPresentationTextObject();
            addObject(presentationTextObject);
            this.currentPresentationTextObject = presentationTextObject;
        }
        return currentPresentationTextObject;
    }

    /**
     * Creates a TagLogicalElement on the page.
     *
     * @param name
     *            the name of the tag
     * @param value
     *            the value of the tag
     */
    public void createTagLogicalElement(String name, String value) {
        TagLogicalElement tle = new TagLogicalElement(name, value);
        if (tagLogicalElements == null) {
            tagLogicalElements = new java.util.ArrayList/*<TagLogicalElement>*/();
        }
        tagLogicalElements.add(tle);
    }

    /**
     * Creates a NoOperation on the page.
     *
     * @param content the byte data
     */
    public void createNoOperation(String content) {
        addObject(new NoOperation(content));
    }

    /**
     * Creates an IncludePageSegment on the current page.
     *
     * @param name
     *            the name of the page segment
     * @param x
     *            the x coordinate of the page segment.
     * @param y
     *            the y coordinate of the page segment.
     */
    public void createIncludePageSegment(String name, int x, int y) {
        IncludePageSegment ips = factory.createIncludePageSegment(name, x, y);
        getIncludePageSegments().add(ips);
    }

    /**
     * Returns the include page segments list
     *
     * @return the include page segments list
     */
    private List getIncludePageSegments() {
        if (this.includePageSegments == null) {
            this.includePageSegments = new java.util.ArrayList/*<IncludePageSegment>*/();
        }
        return this.includePageSegments;
    }

    /**
     * Returns the ActiveEnvironmentGroup associated with this page.
     *
     * @return the ActiveEnvironmentGroup object
     */
    public ActiveEnvironmentGroup getActiveEnvironmentGroup() {
        if (activeEnvironmentGroup == null) {
            // every page object must have an ActiveEnvironmentGroup
            this.activeEnvironmentGroup
                = factory.createActiveEnvironmentGroup(width, height, widthRes, heightRes);

            if (rotation != 0) {
                switch (rotation) {
                    case 90:
                        activeEnvironmentGroup.setObjectAreaPosition(width, 0, rotation);
                        break;
                    case 180:
                        activeEnvironmentGroup.setObjectAreaPosition(width, height, rotation);
                        break;
                    case 270:
                        activeEnvironmentGroup.setObjectAreaPosition(0, height, rotation);
                        break;
                    default:
                }
            }
        }
        return activeEnvironmentGroup;
    }

    /**
     * Returns an indication if the page is complete
     *
     * @return whether this page is complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Returns the height of the page
     *
     * @return the height of the page
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the width of the page
     *
     * @return the width of the page
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the rotation of the page
     *
     * @return the rotation of the page
     */
    public int getRotation() {
        return rotation;
    }

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
        writeObjects(this.objects, os);
    }

    /**
     * Adds an AFP object reference to this page
     *
     * @param obj an AFP object
     */
    public void addObject(Object obj) {
        objects.add(obj);
    }
}
