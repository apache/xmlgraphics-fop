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

package org.apache.fop.render.java2d;

import java.awt.Color;

/**
 * This class holds settings used when rendering with Java2D.
 */
public class Java2DRenderingSettings {

    /** false: paints a non-transparent white background, true: for a transparent background */
    private Color pageBackgroundColor = Color.WHITE;

    /**
     * Returns the page background color.
     * @return the page background color or null if the page background is transparent
     */
    public Color getPageBackgroundColor() {
        return this.pageBackgroundColor;
    }

    /**
     * Sets the page background color.
     * @param color the page background color or null if the page background shall be transparent
     */
    public void setPageBackgroundColor(Color color) {
        this.pageBackgroundColor = color;
    }

    /**
     * Indicates whether the pages have a transparent background or if it's painted in a
     * particular color.
     * @return true if the pages have a transparent background
     */
    public boolean hasTransparentPageBackground() {
        return this.pageBackgroundColor == null;
    }
}
