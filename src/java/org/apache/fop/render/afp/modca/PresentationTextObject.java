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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * The Presentation Text object is the data object used in document processing
 * environments for representing text which has been prepared for presentation.
 * Text, as used here, means an ordered string of characters, such as graphic
 * symbols, numbers, and letters, that are suitable for the specific purpose of
 * representing coherent information. Text which has been prepared for
 * presentation has been reduced to a primitive form through explicit
 * specification of the characters and their placement in the presentation
 * space. Control sequences which designate specific control functions may be
 * embedded within the text. These functions extend the primitive form by
 * applying specific characteristics to the text when it is presented. The
 * collection of the graphic characters and control codes is called Presentation
 * Text, and the object that contains the Presentation Text is called the
 * PresentationText object.
 *
 */
public class PresentationTextObject extends AbstractNamedAFPObject {

    /**
     * Default name for the presentation text object
     */
    private static final String DEFAULT_NAME = "PTO00001";

    /**
     * The current presentation text data
     */
    private PresentationTextData currentPresentationTextData = null;

    /**
     * The presentation text data list
     */
    private List/*<PresentationTextData>*/ presentationTextDataList = null;

    /**
     * Default constructor for the PresentationTextObject
     */
    public PresentationTextObject() {
        this(DEFAULT_NAME);
    }

    /**
     * Construct a new PresentationTextObject for the specified name argument,
     * the name should be an 8 character identifier.
     * @param name the name of this presentation object
     */
    public PresentationTextObject(String name) {
        super(name);
    }

    /**
     * Create the presentation text data for the byte array of data.
     *
     * @param fontNum
     *            The font resource identifier.
     * @param x
     *            The x coordinate for the text data.
     * @param y
     *            The y coordinate for the text data.
     * @param col
     *            The text color.
     * @param vsci
     *            The variable space character increment.
     * @param ica
     *            The inter character increment.
     * @param data
     *            The text data to be created.
     */
    public void createTextData(int fontNum, int x, int y, Color col,
            int vsci, int ica, byte[] data) {
        // Use a default orientation of zero
        createTextData(fontNum, x, y, 0, col, vsci, ica, data);
    }

    /**
     * Create the presentation text data for the byte array of data.
     *
     * @param fontRef
     *            The font resource identifier.
     * @param x
     *            The x coordinate for the text data.
     * @param y
     *            The y coordinate for the text data.
     * @param orientation
     *            The orientation of the text data.
     * @param col
     *            The text color.
     * @param vsci
     *            The variable space character increment.
     * @param ica
     *            The inter character adjustment.
     * @param data
     *            The text data to be created.
     */
    public void createTextData(int fontRef, int x, int y, int orientation,
        Color col, int vsci, int ica, byte[] data) {
        if (currentPresentationTextData == null) {
            startPresentationTextData();
        }
        try {
            currentPresentationTextData.createTextData(fontRef, x, y,
                orientation, col, vsci, ica, data);
        } catch (MaximumSizeExceededException msee) {
            endPresentationTextData();
            createTextData(fontRef, x, y, orientation, col, vsci, ica, data);
        }
    }

    /**
     * Drawing of lines using the starting and ending coordinates, thickness.
     *
     * @param x1
     *            The first x coordinate of the line.
     * @param y1
     *            The first y coordinate of the line.
     * @param x2
     *            The second x coordinate of the line.
     * @param y2
     *            The second y coordinate of the line.
     * @param thickness
     *            The thickness of the line.
     * @param col
     *            The text color.
     */
    public void createLineData(int x1, int y1, int x2, int y2, int thickness, Color col) {
        // Default orientation
        createLineData(x1, y1, x2, y2, thickness, 0, col);
    }

    /**
     * Drawing of lines using the starting and ending coordinates, thickness and
     * orientation arguments.
     *
     * @param x1
     *            The first x coordinate of the line.
     * @param y1
     *            The first y coordinate of the line.
     * @param x2
     *            The second x coordinate of the line.
     * @param y2
     *            The second y coordinate of the line.
     * @param thickness
     *            The thickness of the line.
     * @param orientation
     *            The orientation of the line.
     * @param col
     *            The text color.
     */
    public void createLineData(int x1, int y1, int x2, int y2, int thickness,
        int orientation, Color col) {
        if (currentPresentationTextData == null) {
            startPresentationTextData();
        }
        try {
            currentPresentationTextData.createLineData(x1, y1, x2, y2,
                thickness, orientation, col);
        } catch (MaximumSizeExceededException msee) {
            endPresentationTextData();
            createLineData(x1, y1, x2, y2, thickness, orientation, col);
        }
    }

    /**
     * Helper method to mark the start of the presentation text data
     */
    private void startPresentationTextData() {
        if (presentationTextDataList == null) {
            presentationTextDataList = new java.util.ArrayList/*<PresentationTextData>*/();
        }
        if (presentationTextDataList.size() == 0) {
            currentPresentationTextData = new PresentationTextData(true);
        } else {
            currentPresentationTextData = new PresentationTextData();
        }
        presentationTextDataList.add(currentPresentationTextData);
    }

    /**
     * Helper method to mark the end of the presentation text data
     */
    private void endPresentationTextData() {
        this.currentPresentationTextData = null;
    }

    /**
     * Accessor method to write the AFP datastream for the PresentationTextObject.
     * @param os The stream to write to
     * @throws java.io.IOException thrown if an I/O exception of some sort has occurred
     */
    public void write(OutputStream os) throws IOException {
        writeStart(os);
        writeObjects(this.presentationTextDataList, os);
        writeEnd(os);
    }

    /**
     * Returns the name of this presentation text object
     * @return the name of this presentation text object
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA8; // Structured field id byte 2
        data[5] = (byte) 0x9B; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA9; // Structured field id byte 2
        data[5] = (byte) 0x9B; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }

    /**
     * A control sequence is a sequence of bytes that specifies a control
     * function. A control sequence consists of a control sequence introducer
     * and zero or more parameters. The control sequence can extend multiple
     * presentation text data objects, but must eventually be terminated. This
     * method terminates the control sequence.
     */
    public void endControlSequence() {
        if (currentPresentationTextData == null) {
            startPresentationTextData();
        }
        try {
            currentPresentationTextData.endControlSequence();
        } catch (MaximumSizeExceededException msee) {
            endPresentationTextData();
            endControlSequence();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        if (presentationTextDataList != null) {
            return presentationTextDataList.toString();
        }
        return null;
    }
}
