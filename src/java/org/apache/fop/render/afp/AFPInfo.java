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

package org.apache.fop.render.afp;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.afp.modca.AFPDataStream;

/**
 * AFP information structure for drawing the XML document.
 */
public final class AFPInfo {
    /** see WIDTH */
    protected int width;
    /** see HEIGHT */
    protected int height;
    /** see XPOS */
    protected int currentXPosition;
    /** see YPOS */
    protected int currentYPosition;
    /** see HANDLER_CONFIGURATION */
    protected Configuration cfg;

    /** see AFP_FONT_INFO */
    protected FontInfo fontInfo;
    /** See AFP_DATASTREAM */
    protected AFPDataStream afpDataStream;
    /** See AFP_STATE */
    protected AFPState afpState;
    /** see AFP_GRAYSCALE */
    protected boolean grayscale;
    /** see AFP_RESOLUTION */
    protected int resolution;
    /** see AFP_BITS_PER_PIXEL */
    protected int bitsPerPixel;

    /**
     * Returns the width.
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width.
     * @param width The pageWidth to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the height.
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height.
     * @param height The height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return Configuration the handler configuration
     */
    public Configuration getHandlerConfiguration() {
        return this.cfg;
    }

    /**
     * @return FontInfo the font info
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /**
     * @return Map the current page fonts
     */
    public AFPState getState() {
        return this.afpState;
    }

    /**
     * @return AFPDataStream the afp datastream
     */
    public AFPDataStream getAFPDataStream() {
        return this.afpDataStream;
    }
    
    /**
     * @return true if supports color
     */
    public boolean isColorSupported() {
        return !this.grayscale;
    }
}