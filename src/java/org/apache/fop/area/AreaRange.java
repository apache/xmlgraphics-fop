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
 * Created on 30/05/2004
 * $Id$
 */
package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
/**
 * A <code>AreaRange</code> contains minimum and maximum values for
 * <code>inline-progression-dimension</code> and
 * <code>block-progression-dimension</code>.
 * @author pbw
 * @version $Revision$ $Name$
 */
public class AreaRange {

    /** 
     * The x-axis length is the mimimum <code>inline-progression-dimension</code>.
     *  The y-axis length is the mimimum <code>block-progression-dimension</code>.
     */
    protected Rectangle2D minima = null;
    /** 
     * The x-axis length is the maximum <code>inline-progression-dimension</code>.
     *  The y-axis length is the maximum <code>block-progression-dimension</code>.
     */
    protected Rectangle2D maxima = null;
    /**
     */
    public AreaRange() { }

    public AreaRange(
            float iPDimMin, float iPDimMax, float bPDimMin, float bPDimMax) {
        minima = new Rectangle2D.Double(0, 0, iPDimMin, bPDimMin);
        maxima = new Rectangle2D.Double(0, 0, iPDimMax, bPDimMax);
    }

    public AreaRange(Rectangle2D minima, Rectangle2D maxima) {
        this.minima = minima;
        this.maxima = maxima;
    }

    /**
     * Gets the <code>block-progression-dimension</code> maximum value,
     * in points
     * N.B. The method is synchronized only on this object.
     * @return the <code>block-progression-dimension</code> maximum value
     */
    public Double getBPDimMax() {
        synchronized (this) {
            if (maxima == null) {
                return null;
            }
            return new Double(maxima.getHeight());
        }
    }

    /**
     * Sets the <code>block-progression-dimension</code> maximum value,
     * in points
     * N.B. The method is synchronized only on this object.
     * @param dimMax <code>block-progression-dimension</code> maximum value
     * to set
     */
    public void setBPDimMax(double dimMax) {
        synchronized (this) {
            if (maxima == null) {
                maxima = new Rectangle2D.Double(0, 0, 0, 0);
            }
            maxima.setRect(
                    maxima.getMinX(), maxima.getMinY(), maxima.getWidth(),
                    dimMax);
        }
    }

    /**
     * Gets the <code>block-progression-dimension</code> minimum value,
     * in points
     * N.B. The method is synchronized only on this object.
     * @return the <code>block-progression-dimension</code> minimum value
     */
    public Double getBPDimMin() {
        synchronized (this) {
            if (minima == null) {
                return null;
            }
            return new Double(minima.getHeight());
        }
    }

    /**
     * Sets the <code>block-progression-dimension</code> minimum value,
     * in points
     * N.B. The method is synchronized only on this object.
     * @param dimMin <code>block-progression-dimension</code> minimum value
     * to set
     */
    public void setBPDimMin(double dimMin) {
        synchronized (this) {
            if (minima == null) {
                minima = new Rectangle2D.Double(0, 0, 0, 0);
            }
            minima.setRect(maxima.getMinX(),
            		maxima.getMinY(), maxima.getWidth(),
                    dimMin);
        }
    }

    /**
     * Gets the <code>inline-progression-dimension</code> maximum value,
     * in points
     * N.B. The method is synchronized only on this object.
     * @return the <code>inline-progression-dimension</code> maximum value
     */
    public Double getIPDimMax() {
        synchronized(this) {
            if (maxima == null) {
                return null;
            }
            return new Double(maxima.getWidth());
        }
    }

    /**
     * Sets the <code>inline-progression-dimension</code> maximum value,
     * in points
     * N.B. The method is synchronized only on this object.
     * @param dimMax <code>inline-progression-dimension</code> maximum value
     * to set
     */
    public void setIPDimMax(double dimMax) {
        synchronized (this) {
            if (maxima == null) {
                maxima = new Rectangle2D.Double(0, 0, 0, 0);
            }
            maxima.setRect(
                    maxima.getMinX(), maxima.getMinY(),
					dimMax, maxima.getHeight());
        }
    }

    /**
     * Gets the <code>inline-progression-dimension</code> mimimum value,
     * in points
     * N.B. The method is synchronized only on this object.
     * @return the <code>inline-progression-dimension</code> minimum value
     */
    public Double getIPDimMin() {
        synchronized (this) {
            if (minima == null) {
                return null;
            }
            return new Double(minima.getWidth());
        }
    }

    /**
     * Sets the <code>inline-progression-dimension</code> minimum value,
     * in millipoints
     * N.B. The method is synchronized only on this object.
     * @param dimMin <code>inline-progression-dimension</code> minimum value
     * to set
     */
    public void setIPDimMin(double dimMin) {
        synchronized (this) {
            if (minima == null) {
                minima = new Rectangle2D.Double(0, 0, 0, 0);
            }
            minima.setRect(
                    minima.getMinX(), minima.getMinY(),
					dimMin, minima.getHeight());
        }
    }

}
