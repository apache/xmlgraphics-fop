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

package org.apache.fop.afp;


/**
 * Filled rectangle painting information
 */
public class RectanglePaintInfo implements PaintInfo {

    private final float x;
    private final float y;
    private final float width;
    private final float height;

    /**
     * Main constructor
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width
     * @param height the height
     */
    public RectanglePaintInfo(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the x coordinate
     *
     * @return the x coordinate
     */
    protected float getX() {
        return x;
    }

    /**
     * Returns the y coordinate
     *
     * @return the y coordinate
     */
    protected float getY() {
        return y;
    }

    /**
     * Returns the width
     *
     * @return the width
     */
    protected float getWidth() {
        return width;
    }

    /**
     * Returns the height
     *
     * @return the height
     */
    protected float getHeight() {
        return height;
    }

}
