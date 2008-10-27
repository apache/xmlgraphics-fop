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
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.AFPState;
import org.apache.fop.fonts.FontInfo;

/**
 * AFP information structure for drawing the XML document.
 */
public final class AFPInfo {
    /** see WIDTH */
    private int width;

    /** see HEIGHT */
    private int height;

    /** see XPOS */
    private int x;

    /** see YPOS */
    private int y;

    /** see HANDLER_CONFIGURATION */
    private Configuration handlerConfiguration;

    /** see AFP_FONT_INFO */
    private FontInfo fontInfo;

    /** See AFP_STATE */
    private AFPState state;

    /** See AFP_RESOURCE_MANAGER */
    private AFPResourceManager resourceManager;

    /** true if SVG should be rendered as a bitmap instead of natively */
    private boolean paintAsBitmap;

    /** the resource information */
    private AFPResourceInfo resourceInfo;

    /**
     * Returns the width.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width.
     *
     * @param width The pageWidth to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the height.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height.
     *
     * @param height The height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the handler configuration
     *
     * @return the handler configuration
     */
    public Configuration getHandlerConfiguration() {
        return this.handlerConfiguration;
    }

    /**
     * Sets the handler configuration
     *
     * @param cfg the handler configuration
     */
    public void setHandlerConfiguration(Configuration cfg) {
        this.handlerConfiguration = cfg;
    }

    /**
     * Return the font info
     *
     * @return the font info
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /**
     * Returns the current AFP state
     *
     * @return the current AFP state
     */
    public AFPState getState() {
        return this.state;
    }

    /**
     * Returns the AFPResourceManager
     *
     * @return the AFPResourceManager
     */
    public AFPResourceManager getResourceManager() {
        return this.resourceManager;
    }

    /**
     * Returns true if supports color
     *
     * @return true if supports color
     */
    public boolean isColorSupported() {
        return getState().isColorImages();
    }

    /**
     * Returns the current x position coordinate
     *
     * @return the current x position coordinate
     */
    protected int getX() {
        return x;
    }

    /**
     * Returns the current y position coordinate
     *
     * @return the current y position coordinate
     */
    protected int getY() {
        return y;
    }

    /**
     * Returns the resolution
     *
     * @return the resolution
     */
    protected int getResolution() {
        return getState().getResolution();
    }

    /**
     * Returns the number of bits per pixel to use
     * @return the number of bits per pixel to use
     */
    protected int getBitsPerPixel() {
        return getState().getBitsPerPixel();
    }

    /**
     * Sets the current x position coordinate
     *
     * @param x the current x position coordinate
     */
    protected void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the current y position coordinate
     *
     * @param y the current y position coordinate
     */
    protected void setY(int y) {
        this.y = y;
    }

    /**
     * Sets the current font info
     *
     * @param fontInfo the current font info
     */
    protected void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }

    /**
     * Sets the AFP state
     *
     * @param state the AFP state
     */
    public void setState(AFPState state) {
        this.state = state;
    }

    /**
     * Sets the AFPResourceManager
     *
     * @param resourceManager the AFPResourceManager
     */
    public void setResourceManager(AFPResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Sets true if SVG should be rendered as a bitmap instead of natively
     *
     * @param b boolean value
     */
    public void setPaintAsBitmap(boolean b) {
        this.paintAsBitmap = b;
    }

    /**
     * Returns true if SVG should be rendered as a bitmap instead of natively
     *
     * @return true if SVG should be rendered as a bitmap instead of natively
     */
    public boolean paintAsBitmap() {
        return this.paintAsBitmap;
    }

    /**
     * Returns true if text should be stroked when painted
     *
     * @return true if text should be stroked when painted
     */
    public boolean strokeText() {
        boolean strokeText = false;
        if (handlerConfiguration != null) {
            strokeText = handlerConfiguration.getChild("stroke-text", true).getValueAsBoolean(strokeText);
        }
        return strokeText;
    }

    /**
     * Sets the resource information
     *
     * @param resourceInfo the resource information
     */
    public void setResourceInfo(AFPResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    /**
     * Returns the resource information
     *
     * @return the resource information
     */
    public AFPResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "AFPInfo{width=" + width
            + ", height=" + height
            + ", x=" + x
            + ", y=" + y
            + ", cfg=" + handlerConfiguration
            + ", fontInfo=" + fontInfo
            + ", resourceManager=" + resourceManager
            + ", state=" + state
            + ", paintAsBitmap=" + paintAsBitmap
            + ", resourceInfo=" + resourceInfo
        + "}";
    }

}