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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.fop.afp.Factory;
import org.apache.fop.afp.fonts.AFPFont;

/**
 * An Active Environment Group (AEG) is associated with each page,
 * and is contained in the page's begin-end envelope in the data stream.
 * The active environment group contains layout and formatting information
 * that defines the measurement units and size of the page, and may contain
 * resource information.
 *
 * Any objects that are required for page presentation and that are to be
 * treated as resource objects must be mapped with a map structured field
 * in the AEG. The scope of an active environment group is the scope of its
 * containing page or overlay.
 *
 */
public final class ActiveEnvironmentGroup extends AbstractEnvironmentGroup {

    /** The collection of MapCodedFont objects */
    private final List/*<MapCodedFonts>*/ mapCodedFonts
        = new java.util.ArrayList/*<MapCodedFonts>*/();

    /** the collection of MapPageSegments objects */
    private List mapPageSegments = null;

    /** the Object Area Descriptor for the active environment group */
    private ObjectAreaDescriptor objectAreaDescriptor = null;

    /** the Object Area Position for the active environment group */
    private ObjectAreaPosition objectAreaPosition = null;

    /** the PresentationTextDescriptor for the active environment group */
    private PresentationTextDescriptor presentationTextDataDescriptor = null;

    /** the PageDescriptor for the active environment group */
    private PageDescriptor pageDescriptor = null;

    /** the resource manager */
    private final Factory factory;

    /**
     * Constructor for the ActiveEnvironmentGroup, this takes a
     * name parameter which must be 8 characters long.
     *
     * @param factory the object factory
     * @param name the active environment group name
     * @param width the page width
     * @param height the page height
     * @param widthRes the page width resolution
     * @param heightRes the page height resolution
     */
    public ActiveEnvironmentGroup(Factory factory,
            String name, int width, int height, int widthRes, int heightRes) {
        super(name);

        this.factory = factory;

        // Create PageDescriptor
        this.pageDescriptor
            = factory.createPageDescriptor(width, height, widthRes, heightRes);

        // Create ObjectAreaDescriptor
        this.objectAreaDescriptor
            = factory.createObjectAreaDescriptor(width, height, widthRes, heightRes);

        // Create PresentationTextDataDescriptor
        this.presentationTextDataDescriptor
            = factory.createPresentationTextDataDescriptor(width, height,
                widthRes, heightRes);
    }

    /**
     * Set the position of the object area
     *
     * @param x the x offset
     * @param y the y offset
     * @param rotation the rotation
     */
    public void setObjectAreaPosition(int x, int y, int rotation) {
        this.objectAreaPosition = factory.createObjectAreaPosition(x, y, rotation);
    }

    /**
     * Accessor method to obtain the PageDescriptor object of the
     * active environment group.
     *
     * @return the page descriptor object
     */
    public PageDescriptor getPageDescriptor() {
        return pageDescriptor;
    }

    /**
     * Accessor method to obtain the PresentationTextDataDescriptor object of
     * the active environment group.
     *
     * @return the presentation text descriptor
     */
    public PresentationTextDescriptor getPresentationTextDataDescriptor() {
        return presentationTextDataDescriptor;
    }

    /** {@inheritDoc} */
    public void writeContent(OutputStream os) throws IOException {
        super.writeTriplets(os);

        writeObjects(mapCodedFonts, os);
        writeObjects(mapDataResources, os);
        writeObjects(mapPageOverlays, os);
        writeObjects(mapPageSegments, os);

        if (pageDescriptor != null) {
            pageDescriptor.writeToStream(os);
        }
        if (objectAreaDescriptor != null && objectAreaPosition != null) {
            objectAreaDescriptor.writeToStream(os);
            objectAreaPosition.writeToStream(os);
        }
        if (presentationTextDataDescriptor != null) {
            presentationTextDataDescriptor.writeToStream(os);
        }
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.ACTIVE_ENVIRONMENT_GROUP);
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.ACTIVE_ENVIRONMENT_GROUP);
        os.write(data);
    }

    /**
     * Method to create a map coded font object
     *
     * @param fontRef the font number used as the resource identifier
     * @param font the font
     * @param size the point size of the font
     * @param orientation the orientation of the font (e.g. 0, 90, 180, 270)
     */
    public void createFont(int fontRef, AFPFont font, int size, int orientation) {
        MapCodedFont mapCodedFont = getCurrentMapCodedFont();
        if (mapCodedFont == null) {
            mapCodedFont = factory.createMapCodedFont();
            mapCodedFonts.add(mapCodedFont);
        }

        try {
            mapCodedFont.addFont(fontRef, font, size, orientation);
        } catch (MaximumSizeExceededException msee) {
            mapCodedFont = factory.createMapCodedFont();
            mapCodedFonts.add(mapCodedFont);

            try {
                mapCodedFont.addFont(fontRef, font, size, orientation);
            } catch (MaximumSizeExceededException ex) {
                // Should never happen (but log just in case)
                LOG.error("createFont():: resulted in a MaximumSizeExceededException");
            }
        }
    }

    /**
     * Getter method for the most recent MapCodedFont added to the
     * Active Environment Group (returns null if no MapCodedFonts exist)
     *
     * @return the most recent Map Coded Font.
     */
    private MapCodedFont getCurrentMapCodedFont() {
        int size = mapCodedFonts.size();
        if (size > 0) {
            return (MapCodedFont)mapCodedFonts.get(size - 1);
        } else {
            return null;
        }
    }

    /**
     * Add map page segment.
     * @param name of segment to add
     */
    public void addMapPageSegment(String name) {
        try {
            needMapPageSegment().addPageSegment(name);
        } catch (MaximumSizeExceededException e) {
            //Should not happen, handled internally
            throw new IllegalStateException("Internal error: " + e.getMessage());
        }
    }

    private MapPageSegment getCurrentMapPageSegment() {
        return (MapPageSegment)getLastElement(this.mapPageSegments);
    }

    private MapPageSegment needMapPageSegment() {
        if (this.mapPageSegments == null) {
            this.mapPageSegments = new java.util.ArrayList();
        }
        MapPageSegment seg = getCurrentMapPageSegment();
        if (seg == null || seg.isFull()) {
            seg = new MapPageSegment();
            this.mapPageSegments.add(seg);
        }
        return seg;
    }

}
