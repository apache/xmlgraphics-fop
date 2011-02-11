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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.xmlgraphics.java2d.color.ColorConverter;
import org.apache.xmlgraphics.java2d.color.ColorUtil;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.Completable;
import org.apache.fop.afp.Factory;
import org.apache.fop.afp.StructuredData;
import org.apache.fop.afp.goca.GraphicsAreaBegin;
import org.apache.fop.afp.goca.GraphicsAreaEnd;
import org.apache.fop.afp.goca.GraphicsBox;
import org.apache.fop.afp.goca.GraphicsChainedSegment;
import org.apache.fop.afp.goca.GraphicsCharacterString;
import org.apache.fop.afp.goca.GraphicsData;
import org.apache.fop.afp.goca.GraphicsFillet;
import org.apache.fop.afp.goca.GraphicsFullArc;
import org.apache.fop.afp.goca.GraphicsImage;
import org.apache.fop.afp.goca.GraphicsLine;
import org.apache.fop.afp.goca.GraphicsSetArcParameters;
import org.apache.fop.afp.goca.GraphicsSetCharacterSet;
import org.apache.fop.afp.goca.GraphicsSetCurrentPosition;
import org.apache.fop.afp.goca.GraphicsSetLineType;
import org.apache.fop.afp.goca.GraphicsSetLineWidth;
import org.apache.fop.afp.goca.GraphicsSetPatternSymbol;
import org.apache.fop.afp.goca.GraphicsSetProcessColor;

/**
 * Top-level GOCA graphics object.
 *
 * Acts as container and factory of all other graphic objects
 */
public class GraphicsObject extends AbstractDataObject {

    /** the graphics data */
    private GraphicsData currentData = null;

    /** list of objects contained within this container */
    protected List/*<GraphicsData>*/ objects
        = new java.util.ArrayList/*<GraphicsData>*/();

    /** the graphics state */
    private final GraphicsState graphicsState = new GraphicsState();


    /** color  converter  */
    private ColorConverter colorConverter = null;

    /**
     * Default constructor
     *
     * @param factory the object factory
     * @param name the name of graphics object
     */
    public GraphicsObject(Factory factory, String name) {
        super(factory, name);
    }

    /** {@inheritDoc} */
    @Override
    public void setViewport(AFPDataObjectInfo dataObjectInfo) {
        super.setViewport(dataObjectInfo);

        AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();
        int width = objectAreaInfo.getWidth();
        int height = objectAreaInfo.getHeight();
        int widthRes = objectAreaInfo.getWidthRes();
        int heightRes = objectAreaInfo.getHeightRes();
        final int leftEdge = 0;
        final int topEdge = 0;
        GraphicsDataDescriptor graphicsDataDescriptor = factory.createGraphicsDataDescriptor(
                    leftEdge, width, topEdge, height, widthRes, heightRes);

        getObjectEnvironmentGroup().setDataDescriptor(graphicsDataDescriptor);
    }

    /** @param object the structured data */
    public void addObject(StructuredData object) {
        if (currentData == null) {
            newData();
        } else if (currentData.getDataLength() + object.getDataLength()
                >= GraphicsData.MAX_DATA_LEN) {
            // graphics data full so transfer current incomplete segment to new data
            GraphicsChainedSegment currentSegment
                = (GraphicsChainedSegment)currentData.removeCurrentSegment();
            currentSegment.setName(newData().createSegmentName());
            currentData.addSegment(currentSegment);
        }
        currentData.addObject(object);
    }

    /**
     * Gets the current graphics data, creating a new one if necessary
     *
     * @return the current graphics data
     */
    private GraphicsData getData() {
        if (this.currentData == null) {
            return newData();
        }
        return this.currentData;
    }

    /**
     * Creates a new graphics data
     *
     * @return a newly created graphics data
     */
    private GraphicsData newData() {
        if (currentData != null) {
            currentData.setComplete(true);
        }
        this.currentData = factory.createGraphicsData();
        objects.add(currentData);
        return currentData;
    }

    /**
     * Sets the current color
     *
     * @param color the active color to use
     */
    public void setColor(Color color) {
        if (!ColorUtil.isSameColor(color, graphicsState.color)) {
            addObject(new GraphicsSetProcessColor(colorConverter.convert(color)));
            graphicsState.color = color;
        }
    }


    /**
     * Sets the color converter
     *
     * @param colorConverter ColorConverter to filter the color
     *           when creating a GraphicsSetProcessColor.
     */
    public void setColorConverter(ColorConverter colorConverter) {
       this.colorConverter = colorConverter;
    }


    /**
     * Sets the current position
     *
     * @param coords the x and y coordinates of the current position
     */
    public void setCurrentPosition(int[] coords) {
        addObject(new GraphicsSetCurrentPosition(coords));
    }

    /**
     * Sets the line width
     *
     * @param lineWidth the line width multiplier
     */
    public void setLineWidth(int lineWidth) {
        if (lineWidth != graphicsState.lineWidth) {
            addObject(new GraphicsSetLineWidth(lineWidth));
            graphicsState.lineWidth = lineWidth;
        }
    }

    /**
     * Sets the line type
     *
     * @param lineType the line type
     */
    public void setLineType(byte lineType) {
        if (lineType != graphicsState.lineType) {
            addObject(new GraphicsSetLineType(lineType));
            graphicsState.lineType = lineType;
        }
    }

    /**
     * Sets whether the following shape is to be filled.
     *
     * @param fill true if the following shape is to be filled
     */
    public void setFill(boolean fill) {
        setPatternSymbol(fill
                ? GraphicsSetPatternSymbol.SOLID_FILL
                : GraphicsSetPatternSymbol.NO_FILL);
    }

    /**
     * Sets the fill pattern of the next shape.
     *
     * @param patternSymbol the fill pattern of the next shape
     */
    public void setPatternSymbol(byte patternSymbol) {
        if (patternSymbol != graphicsState.patternSymbol) {
            addObject(new GraphicsSetPatternSymbol(patternSymbol));
            graphicsState.patternSymbol = patternSymbol;
        }
    }

    /**
     * Sets the character set to use
     *
     * @param characterSet the character set (font) reference
     */
    public void setCharacterSet(int characterSet) {
        if (characterSet != graphicsState.characterSet) {
            addObject(new GraphicsSetCharacterSet(characterSet));
            graphicsState.characterSet = characterSet;
        }
    }

    /**
     * Adds a line at the given x/y coordinates
     *
     * @param coords the x/y coordinates (can be a series)
     */
    public void addLine(int[] coords) {
        addLine(coords, false);
    }

    /**
     * Adds a line at the given x/y coordinates
     *
     * @param coords the x/y coordinates (can be a series)
     * @param relative relative true for a line at current position (relative to)
     */
    public void addLine(int[] coords, boolean relative) {
        addObject(new GraphicsLine(coords, relative));
    }

    /**
     * Adds a box at the given coordinates
     *
     * @param coords the x/y coordinates
     */
    public void addBox(int[] coords) {
        addObject(new GraphicsBox(coords));
    }

    /**
     * Adds a fillet (curve) at the given coordinates
     *
     * @param coords the x/y coordinates
     */
    public void addFillet(int[] coords) {
        addFillet(coords, false);
    }

    /**
     * Adds a fillet (curve) at the given coordinates
     *
     * @param coords the x/y coordinates
     * @param relative relative true for a fillet (curve) at current position (relative to)
     */
    public void addFillet(int[] coords, boolean relative) {
        addObject(new GraphicsFillet(coords, relative));
    }

    /**
     * Sets the arc parameters
     *
     * @param xmaj the maximum value of the x coordinate
     * @param ymin the minimum value of the y coordinate
     * @param xmin the minimum value of the x coordinate
     * @param ymaj the maximum value of the y coordinate
     */
    public void setArcParams(int xmaj, int ymin, int xmin, int ymaj) {
        addObject(new GraphicsSetArcParameters(xmaj, ymin, xmin, ymaj));
    }

    /**
     * Adds a full arc
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param mh the integer portion of the multiplier
     * @param mhr the fractional portion of the multiplier
     */
    public void addFullArc(int x, int y, int mh, int mhr) {
        addObject(new GraphicsFullArc(x, y, mh, mhr));
    }

    /**
     * Adds an image
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the image width
     * @param height the image height
     * @param imgData the image data
     */
    public void addImage(int x, int y, int width, int height, byte[] imgData) {
        addObject(new GraphicsImage(x, y, width, height, imgData));
    }

    /**
     * Adds a string
     *
     * @param str the string
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void addString(String str, int x, int y) {
        addObject(new GraphicsCharacterString(str, x, y));
    }

    /**
     * Begins a graphics area (start of fill)
     */
    public void beginArea() {
        addObject(new GraphicsAreaBegin());
    }

    /**
     * Ends a graphics area (end of fill)
     */
    public void endArea() {
        addObject(new GraphicsAreaEnd());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "GraphicsObject: " + getName();
    }

    /**
     * Creates a new graphics segment
     */
    public void newSegment() {
        getData().newSegment();
        graphicsState.lineWidth = 0; //Looks like a new segment invalidates the graphics state
    }

    /** {@inheritDoc} */
    @Override
    public void setComplete(boolean complete) {
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Completable completedObject = (Completable)it.next();
            completedObject.setComplete(true);
        }
        super.setComplete(complete);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeStart(OutputStream os) throws IOException {
        super.writeStart(os);
        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.GRAPHICS);
        os.write(data);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
        writeObjects(objects, os);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.GRAPHICS);
        os.write(data);
    }

    /** the internal graphics state */
    private static final class GraphicsState {

        private GraphicsState() {
        }

        /** the current color */
        private Color color;

        /** the current line type */
        private byte lineType;

        /** the current line width */
        private int lineWidth;

        /** the current fill pattern */
        private byte patternSymbol;

        /** the current character set */
        private int characterSet;
    }
}