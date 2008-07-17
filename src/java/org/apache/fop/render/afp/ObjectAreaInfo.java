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

/**
 * A common class used to convey locations,
 * dimensions and resolutions of data objects.
 */
public class ObjectAreaInfo {
    private int x;
    private int y;
    private int width;
    private int height;
    private int widthRes;
    private int heightRes;
    private int rotation = 0;

    /**
     * Sets the x position of the data object
     * 
     * @param x the x position of the data object
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y position of the data object
     * 
     * @param y the y position of the data object
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Sets the data object width
     * 
     * @param width the width of the data object
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Sets the data object height
     * 
     * @param height the height of the data object
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Sets the width resolution
     * 
     * @param widthRes the width resolution
     */
    public void setWidthRes(int widthRes) {
        this.widthRes = widthRes;
    }

    /**
     * Sets the height resolution
     * 
     * @param heightRes the height resolution
     */
    public void setHeightRes(int heightRes) {
        this.heightRes = heightRes;
    }

    /**
     * Returns the x coordinate of this data object
     * 
     * @return the x coordinate of this data object
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate of this data object
     * 
     * @return the y coordinate of this data object
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the width of this data object
     * 
     * @return the width of this data object
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this data object
     * 
     * @return the height of this data object
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the width resolution of this data object
     * 
     * @return the width resolution of this data object
     */
    public int getWidthRes() {
        return widthRes;
    }

    /**
     * Returns the height resolution of this data object
     * 
     * @return the height resolution of this data object
     */
    public int getHeightRes() {
        return heightRes;
    }

    /**
     * Returns the rotation of this data object
     * 
     * @return the rotation of this data object
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * Sets the data object rotation
     * 
     * @param rotation the data object rotation
     */
    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "x=" + x
        + ", y=" + y
        + ", width=" + width
        + ", height=" + height
        + ", widthRes=" + widthRes
        + ", heightRes=" + heightRes
        + ", rotation=" + rotation;
    }
}
