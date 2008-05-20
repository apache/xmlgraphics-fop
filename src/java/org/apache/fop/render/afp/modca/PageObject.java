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

import org.apache.fop.render.afp.tools.StringUtils;

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
public class PageObject extends AbstractResourceGroupContainer {

//    /**
//     * The page segment count
//     */
//    private int pageSegmentCount = 0;
//
//    /**
//     * The current page segment
//     */
//    private PageSegment currentPageSegment = null;

    /**
     * Construct a new page object for the specified name argument, the page
     * name should be an 8 character identifier.
     *
     * @param name
     *            the name of the page.
     * @param width
     *            the width of the page.
     * @param height
     *            the height of the page.
     * @param rotation
     *            the rotation of the page.
     * @param widthResolution
     *            the width resolution of the page.
     * @param heightResolution
     *            the height resolution of the page.
     */
    public PageObject(String name, int width, int height, int rotation,
            int widthResolution, int heightResolution) {
        super(name, width, height, rotation, widthResolution, heightResolution);
    }

    /**
     * Creates an IncludePageOverlay on the page.
     *
     * @param name
     *            the name of the overlay
     * @param x
     *            the x position of the overlay
     * @param y
     *            the y position of the overlay
     * @param orientation
     *            the orientation required for the overlay
     */
    public void createIncludePageOverlay(String name, int x, int y, int orientation) {
        getActiveEnvironmentGroup().createOverlay(name);
        IncludePageOverlay ipo = new IncludePageOverlay(name, x, y, orientation);
        addObject(ipo);
    }

//    /**
//     * @return a new page segment object
//     */
//    private PageSegment createPageSegment() {
//        String name = PAGE_SEGMENT_NAME_PREFIX
//        + StringUtils.lpad(String.valueOf(++pageSegmentCount), '0', 5);
//        PageSegment pageSegment = new PageSegment(name);
//        return pageSegment;
//    }
    
//    /**
//     * @return the current page segment
//     */
//    private PageSegment getCurrentPageSegment() {
//        if (currentPageSegment == null) {
//            this.currentPageSegment = createPageSegment();        
//            super.addObject(currentPageSegment);
//        }
//        return this.currentPageSegment;
//    }
//    
//    private static final String PAGE_SEGMENT_NAME_PREFIX = "PSG";

//    /**
//     * Starts a new page segment and makes it current.
//     */
//    public void startPageSegment() {
//        getCurrentPageSegment();
//    }
//
//    /**
//     * Ends the current page segment.
//     */
//    public void endPageSegment() {
//        this.currentPageSegment = null;
//    }

//    /**
//     * {@inheritDoc}
//     */
//    protected void addObject(AbstractAFPObject obj) {
////        if (currentPageSegment != null) {
////            getCurrentPageSegment().addObject(obj);
////        } else {
//            super.addObject(obj);
////        }
//    }

    /**
     * This method will create shading on the page using the specified
     * coordinates (the shading contrast is controlled via the red, green blue
     * parameters, by converting this to grayscale).
     *
     * @param x
     *            the x coordinate of the shading
     * @param y
     *            the y coordinate of the shading
     * @param w
     *            the width of the shaded area
     * @param h
     *            the height of the shaded area
     * @param red
     *            the red value
     * @param green
     *            the green value
     * @param blue
     *            the blue value
     */
    public void createShading(int x, int y, int w, int h, int red, int green, int blue) {
        int xCoord = 0;
        int yCoord = 0;
        int areaWidth = 0;
        int areaHeight = 0;
        switch (rotation) {
            case 90:
                xCoord = areaWidth - y - h;
                yCoord = x;
                areaWidth = h;
                areaHeight = w;
                break;
            case 180:
                xCoord = areaWidth - x - w;
                yCoord = areaHeight - y - h;
                areaWidth = w;
                areaHeight = h;
                break;
            case 270:
                xCoord = y;
                yCoord = areaHeight - x - w;
                areaWidth = h;
                areaHeight = w;
                break;
            default:
                xCoord = x;
                yCoord = y;
                areaWidth = w;
                areaHeight = h;
                break;
        }

        // Convert the color to grey scale
        float shade = (float) ((red * 0.3) + (green * 0.59) + (blue * 0.11));

        int grayscale = Math.round((shade / 255) * 16);

        String imageName = "IMG"
            + StringUtils.lpad(String.valueOf(getResourceCount() + 1),
            '0', 5);

        IMImageObject imImageObject = new IMImageObject(imageName);
        ImageOutputControl imageOutputControl = new ImageOutputControl(0, 0);
        ImageInputDescriptor imageInputDescriptor = new ImageInputDescriptor();
        ImageCellPosition imageCellPosition = new ImageCellPosition(xCoord, yCoord);
        imageCellPosition.setXFillSize(areaWidth);
        imageCellPosition.setYFillSize(areaHeight);
        imageCellPosition.setXSize(64);
        imageCellPosition.setYSize(8);

        //defining this as a resource
        ImageRasterData imageRasterData = new ImageRasterData(
                ImageRasterPattern.getRasterData(grayscale));

        imImageObject.setImageOutputControl(imageOutputControl);
        imImageObject.setImageInputDescriptor(imageInputDescriptor);
        imImageObject.setImageCellPosition(imageCellPosition);
        imImageObject.setImageRasterData(imageRasterData);
        addObject(imImageObject);
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
        data[5] = (byte) 0xAF; // Structured field id byte 3
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
        data[5] = (byte) 0xAF; // Structured field id byte 3
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
    public String toString() {
        return this.getName();
    }
}