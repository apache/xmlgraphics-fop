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

import org.apache.fop.render.afp.fonts.AFPFont;

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
    private List/*<MapCodedFonts>*/ mapCodedFonts = null;

    /** the collection of MapDataResource objects */
    private final List mapDataResources = null;

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
        pageDescriptor = new PageDescriptor(width, height, widthRes, heightRes);

        // Create ObjectAreaDescriptor
        objectAreaDescriptor = new ObjectAreaDescriptor(width, height,
                widthRes, heightRes);

        // Create PresentationTextDataDescriptor
        presentationTextDataDescriptor = new PresentationTextDescriptor(width, height,
                    widthRes, heightRes);
    }

    /**
     * Set the position of the object area
     *
     * @param x the x offset
     * @param y the y offset
     * @param rotation the rotation
     */
    public void setPosition(int x, int y, int rotation) {
        // Create ObjectAreaPosition
        objectAreaPosition = new ObjectAreaPosition(x, y, rotation);
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

    private List getMapCodedFonts() {
        if (mapCodedFonts == null) {
            mapCodedFonts = new java.util.ArrayList();
        }
        return mapCodedFonts;
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
        MapCodedFont mcf = getCurrentMapCodedFont();
        if (mcf == null) {
            mcf = factory.createMapCodedFont();
            getMapCodedFonts().add(mcf);
        }

        try {
            mcf.addFont(fontRef, font, size, orientation);
        } catch (MaximumSizeExceededException msee) {
            mcf = factory.createMapCodedFont();
            getMapCodedFonts().add(mcf);

            try {
                mcf.addFont(fontRef, font, size, orientation);
            } catch (MaximumSizeExceededException ex) {
                // Should never happen (but log just in case)
                log.error("createFont():: resulted in a MaximumSizeExceededException");
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
        int size = getMapCodedFonts().size();
        if (size > 0) {
            return (MapCodedFont)mapCodedFonts.get(size - 1);
        } else {
            return null;
        }
    }

//  private List getMapDataResources() {
//  if (mapDataResources == null) {
//      mapDataResources = new java.util.ArrayList();
//  }
//  return mapDataResources;
//}

//    /**
//     * Method to create a map data resource object
//     * @param dataObjectAccessor a data object accessor
//     */
//    protected void createMapDataResource(DataObjectAccessor dataObjectAccessor) {
//        getMapDataResources().add(new MapDataResource(dataObjectAccessor));
//    }
}