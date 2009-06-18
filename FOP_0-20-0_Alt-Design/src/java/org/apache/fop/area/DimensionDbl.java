/*
 *
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 6/07/2004
 * $Id$
 */
package org.apache.fop.area;

import java.awt.geom.Dimension2D;


/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class DimensionDbl extends Dimension2D {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private double width = 0.0;
    private double height = 0.0;

    /**
     * Instantiates a dimension with 0.0 width and height
     */
    public DimensionDbl() {}

    public DimensionDbl(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /* (non-Javadoc)
     * @see java.awt.geom.Dimension2D#getHeight()
     */
    public double getHeight() {
        return height;
    }

    /* (non-Javadoc)
     * @see java.awt.geom.Dimension2D#getWidth()
     */
    public double getWidth() {
        return width;
    }

    /* (non-Javadoc)
     * @see java.awt.geom.Dimension2D#setSize(double, double)
     */
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

}
