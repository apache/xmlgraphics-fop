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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
public final class ActiveEnvironmentGroup extends AbstractNamedAFPObject {

    /**
     * Default name for the active environment group
     */
    private static final String DEFAULT_NAME = "AEG00001";

    /**
     * The collection of MapCodedFont objects
     */
    private ArrayList _mapCodedFonts = new ArrayList();

    /**
     * The Object Area Descriptor for the active environment group
     */
    private ObjectAreaDescriptor _objectAreaDescriptor = null;

    /**
     * The Object Area Position for the active environment group
     */
    private ObjectAreaPosition _objectAreaPosition = null;

    /**
     * The PresentationTextDescriptor for the active environment group
     */
    private PresentationTextDescriptor _presentationTextDataDescriptor = null;

    /**
     * The PageDescriptor for the active environment group
     */
    private PageDescriptor _pageDescriptor = null;

    /**
     * The collection of MapPageOverlay objects
     */
    private ArrayList _mapPageOverlays = new ArrayList();

    /**
     * Default constructor for the ActiveEnvironmentGroup.
     * @param width the page width
     * @param height the page height
     */
    public ActiveEnvironmentGroup(int width, int height) {

        this(DEFAULT_NAME, width, height);

    }

    /**
     * Constructor for the ActiveEnvironmentGroup, this takes a
     * name parameter which must be 8 characters long.
     * @param name the active environment group name
     * @param width the page width
     * @param height the page height
     */
    public ActiveEnvironmentGroup(String name, int width, int height) {

        super(name);

        // Create PageDescriptor
        _pageDescriptor = new PageDescriptor(width, height);

        // Create ObjectAreaDescriptor
        _objectAreaDescriptor = new ObjectAreaDescriptor(width, height);

        // Create PresentationTextDataDescriptor
        _presentationTextDataDescriptor =
            new PresentationTextDescriptor(width, height);

    }

    /**
     * Set the position of the object area
     * @param x the x offset
     * @param y the y offset
     * @param rotation the rotation
     */
    public void setPosition(int x, int y, int rotation) {

        // Create ObjectAreaPosition
        _objectAreaPosition = new ObjectAreaPosition(x, y, rotation);

    }

    /**
     * Accessor method to obtain the PageDescriptor object of the
     * active environment group.
     * @return the page descriptor object
     */
    public PageDescriptor getPageDescriptor() {

        return _pageDescriptor;

    }

    /**
     * Accessor method to obtain the PresentationTextDataDescriptor object of
     * the active environment group.
     * @return the presentation text descriptor
     */
    public PresentationTextDescriptor getPresentationTextDataDescriptor() {

        return _presentationTextDataDescriptor;

    }

    /**
     * Accessor method to write the AFP datastream for the active environment group.
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        writeStart(os);

        writeObjectList(_mapCodedFonts, os);

        writeObjectList(_mapPageOverlays, os);

        _pageDescriptor.writeDataStream(os);

        if (_objectAreaDescriptor != null && _objectAreaPosition != null) {
            _objectAreaDescriptor.writeDataStream(os);
            _objectAreaPosition.writeDataStream(os);
        }

        _presentationTextDataDescriptor.writeDataStream(os);

        writeEnd(os);

    }

    /**
     * Helper method to write the start of the active environment group.
     * @param os The stream to write to
     */
    private void writeStart(OutputStream os)
        throws IOException {

        byte[] data = new byte[17];

        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA8; // Structured field id byte 2
        data[5] = (byte) 0xC9; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

       os.write(data);

    }

    /**
     * Helper method to write the end of the active environment group.
     * @param os The stream to write to
     */
    private void writeEnd(OutputStream os)
        throws IOException {

        byte[] data = new byte[17];

        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA9; // Structured field id byte 2
        data[5] = (byte) 0xC9; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

        os.write(data);

    }

    /**
     * Method to create a map coded font object
     * @param fontReference the font number used as the resource identifier
     * @param font the font
     * @param size the point size of the font
     * @param orientation the orientation of the font (e.g. 0, 90, 180, 270)
     */
    public void createFont(
        byte fontReference,
        AFPFont font,
        int size,
        int orientation) {

        MapCodedFont mcf = getCurrentMapCodedFont();

        if (mcf == null) {
            mcf = new MapCodedFont();
            _mapCodedFonts.add(mcf);
        }

        try {

            mcf.addFont(
                fontReference,
                font,
                size,
                orientation);

        } catch (MaximumSizeExceededException msee) {

            mcf = new MapCodedFont();
            _mapCodedFonts.add(mcf);

            try {

                mcf.addFont(
                    fontReference,
                    font,
                    size,
                    orientation);

            } catch (MaximumSizeExceededException ex) {

                // Should never happen (but log just in case)
                log.error("createFont():: resulted in a MaximumSizeExceededException");

            }

        }

    }

    /**
     * Actually creates the MPO object.
     * Also creates the supporting object (an IPO)
     * @param name the name of the overlay to be used
     */
    public void createOverlay(String name) {

        MapPageOverlay mpo = getCurrentMapPageOverlay();

        if (mpo == null) {
            mpo = new MapPageOverlay();
            _mapPageOverlays.add(mpo);
        }

        try {

            mpo.addOverlay(name);

        } catch (MaximumSizeExceededException msee) {
            mpo = new MapPageOverlay();
            _mapPageOverlays.add(mpo);
            try {
                mpo.addOverlay(name);
            } catch (MaximumSizeExceededException ex) {
                // Should never happen (but log just in case)
                log.error("createOverlay():: resulted in a MaximumSizeExceededException");
            }
        }
    }

    /**
     * Getter method for the most recent MapCodedFont added to the
     * Active Environment Group (returns null if no MapCodedFonts exist)
     * @return the most recent Map Coded Font.
     */
    private MapCodedFont getCurrentMapCodedFont() {

        int size = _mapCodedFonts.size();
        if (size > 0) {
            return (MapCodedFont) _mapCodedFonts.get(_mapCodedFonts.size() - 1);
        } else {
            return null;
        }

    }

    /**
     * Getter method for the most recent MapPageOverlay added to the
     * Active Environment Group (returns null if no MapPageOverlay exist)
     * @return the most recent Map Coded Font
     */
    private MapPageOverlay getCurrentMapPageOverlay() {

        int size = _mapPageOverlays.size();
        if (size > 0) {
            return (MapPageOverlay) _mapPageOverlays.get(
                _mapPageOverlays.size() - 1);
        } else {
            return null;
        }

    }

}