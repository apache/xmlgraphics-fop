/*
   Copyright 2004 The Apache Software Foundation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 * Created on 26/01/2004
 * $Id$
 */
package org.apache.fop.area;

import java.awt.geom.Rectangle2D;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.FoPageSequence;
import org.apache.fop.fo.properties.WritingMode;

/**
 * The base class for all gemetrical areas.  <code>Area</code> extends
 * <code>AreaNode</code> because all areas will find themselves in a tree of
 * some kind.  It represents its geometry with a
 * <code>Rectangle2D.Double</code>, whose dimensions are expressed in Java
 * and XSL points, i.e. 1/72 of an inch, which facilitates conversions to
 * integral millipoints.  It also allows the use of Java facilities like
 * <code>java.awt.GraphicsConfiguration.getNormalizingTransform()</code>
 * which, "Returns a AffineTransform that can be concatenated with the default
 * AffineTransform of a GraphicsConfiguration so that 72 units in user space
 * equals 1 inch in device space."
 * @author pbw
 * @version $Revision$ $Name$
 */
public class Area extends AreaNode implements Cloneable  {

    /** Current inline progression dimension.  May be unknown. */
    protected Integer iPDim = null;
    /** Maximum required inline progression dimension.  May be unknown. */
    protected Integer iPDimMax = null;
    /** Mimimum required inline progression dimension.  May be unknown. */
    protected Integer iPDimMin = null;
    /** Current block progression dimension.  May be unknown. */
    protected Integer bPDim = null;
    /** Maximum required block progression dimension.  May be unknown. */
    protected Integer bPDimMax = null;
    /** Mimimum required block progression dimension.  May be unknown. */
    protected Integer bPDimMin = null;
    /** The geometrical area.  The <code>width</code> of this
     * <code>Rectangle</code> is the <code>inline-progression-dimension</code>
     * of the area, and the <code>height</code> is the
     * <code>bllock-progression-dimension</code>.  */
    protected Rectangle2D area = null;
    /** True if the the <code>writing-mode</code> of the content area is
     * horizontal */
    protected boolean contentIsHorizontal = true;
    /** True if the the <code>writing-mode</code> of the content area is
     * left-to-right */
    protected boolean contentLeftToRight = true;

    private void setup() {
        try {
            contentIsHorizontal =
                WritingMode.isHorizontal(generatedBy.getWritingMode());
            contentLeftToRight =
                WritingMode.isLeftToRight(generatedBy.getWritingMode());
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    /**
     * Constructs an <code>Area</code> which is based on the given
     * <code>Rectangle2D</code>
     * @param area the rectangular area
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     * @param parent <code>Node</code> of this
     * @param sync the object on which this area is synchronized
     */
    public Area(
            Rectangle2D area,
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        this(pageSeq, generatedBy, parent, sync);
        this.area = area;
    }

    /**
     * Constructs an <code>Area</code> with a null rectangular area
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     * @param parent <code>Node</code> of this
     * @param sync the object on which this area is synchronized
     * @throws IndexOutOfBoundsException
     */
    public Area(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
        setup();
    }

    /**
     * Constructs an <code>Area</code> with the given rectangular area,
     * which is the root of a tree, and is synchronized on itself.
     * @param area the rectangular area
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     */
    public Area(
            Rectangle2D area,
            FoPageSequence pageSeq,
            FONode generatedBy) {
        this(pageSeq, generatedBy);
        this.area = area;
    }

    /**
     * Construct an <code>Area</code> which is the root of a tree, and is
     * synchronized on itself
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     */
    public Area(
            FoPageSequence pageSeq,
            FONode generatedBy) {
        super(pageSeq, generatedBy);
        setup();
    }

    /**
     * Gets the <code>block-progression-dimension</code> of the contents of
     * this area in millipoints.  This value is taken from the appropriate
     * dimension of the <code>Rectangle2D</code> representing this area.  If no
     * <code>Rectangle2D</code> exists, a zero-dimensioned default is first
     * created, then the zero value is returned.
     * @return the <code>block-progression-dimension</code> in millipoints
     */
    public int getBPDim() {
        return (int)(getBPDimPts()*1000);
    }

    /**
     * Gets the <code>block-progression-dimension</code> of the contents of
     * this area in points.  This value is taken from the appropriate dimension
     * of the <code>Rectangle2D</code> representing this area.  If no
     * <code>Rectangle2D</code> exists, a zero-dimensioned default is first
     * created, then the zero value is returned.
     * @return the <code>block-progression-dimension</code> in points
     */
    public double getBPDimPts() {
        synchronized (sync) {
            if (area == null) {
                return 0;
            }
            if (contentIsHorizontal) {
                return area.getHeight();
            } else {
                return area.getWidth();
            }
        }
    }

    /**
     * Sets the <code>block-progression-dimension</code> of the contents of
     * this area to the specified value in millipoints.
     * This value is applied to the appropriate dimension of the
     * <code>Rectangle2D</code> representing this area.  If no
     * <code>Rectangle2D</code> exists, a zero-dimensioned default is first
     * created, then the value is applied.
     * @param millipts <code>block-progression-dimension</code> to set, in
     * millipoints
     */
    public void setBPDim(int millipts) {
        setBPDimPts(millipts/1000.0f);
    }

    /**
     * Sets the <code>block-progression-dimension</code> of the contents of
     * this area to the specified value in points.
     * This value is applied to the appropriate dimension of the
     * <code>Rectangle2D</code> representing this area.  If no
     * <code>Rectangle2D</code> exists, a zero-dimensioned default is first
     * created, then the value is applied.
     * @param pts <code>block-progression-dimension</code> to set, in points
     */
    public void setBPDimPts(double pts) {
        synchronized (sync) {
            if (area == null) {
                area = new Rectangle2D.Double();
            }
            if (contentIsHorizontal) {
                area.setRect(
                        area.getX(),area.getY(), area.getWidth(), pts);
            } else {
                area.setRect(
                        area.getX(),area.getY(), pts, area.getHeight());
            }
        }
    }

    /**
     * Gets the <code>block-progression-dimension</code> maximum value,
     * in millipoints
     * @return the <code>block-progression-dimension</code> maximum value
     */
    public Integer getBPDimMax() {
        synchronized (sync) {
            return bPDimMax;
        }
    }

    /**
     * Sets the <code>block-progression-dimension</code> maximum value,
     * in millipoints
     * @param dimMax <code>block-progression-dimension</code> maximum value
     * to set
     */
    public void setBPDimMax(Integer dimMax) {
        synchronized (sync) {
            bPDimMax = dimMax;
        }
    }

    /**
     * Gets the <code>block-progression-dimension</code> minimum value,
     * in millipoints
     * @return the <code>block-progression-dimension</code> minimum value
     */
    public Integer getBPDimMin() {
        synchronized (sync) {
            return bPDimMin;
        }
    }

    /**
     * Sets the <code>block-progression-dimension</code> minimum value,
     * in millipoints
     * @param dimMin <code>block-progression-dimension</code> minimum value
     * to set
     */
    public void setBPDimMin(Integer dimMin) {
        synchronized (sync) {
            bPDimMin = dimMin;
        }
    }

    /**
     * Gets the <code>inline-progression-dimension</code> of the contents of
     * this area in millipoints.  This value is taken from the appropriate
     * dimension of the <code>Rectangle2D</code> representing this area.  If no
     * <code>Rectangle2D</code> exists, a zero-dimensioned default is first
     * created, then the zero value is returned.
     * @return the <code>inline-progression-dimension</code> in millipoints
     */
    public int getIPDim() {
        return (int)(getIPDimPts()*1000);
    }

    /**
     * Gets the <code>inline-progression-dimension</code> of the contents of
     * this area in points.  This value is taken from the appropriate dimension
     * of the <code>Rectangle2D</code> representing this area.  If no
     * <code>Rectangle2D</code> exists, a zero-dimensioned default is first
     * created, then the zero value is returned.
     * @return the <code>inline-progression-dimension</code> in points
     */
    public double getIPDimPts() {
        synchronized (sync) {
            if (area == null) {
                area = new Rectangle2D.Double();
            }
            if (contentIsHorizontal){
                return area.getWidth();
            } else {
                return area.getHeight();
            }
        }
    }

    /**
     * Sets the <code>inline-progression-dimension</code> of the contents of
     * this area, in millipoints.  This value is applied to the appropriate
     * dimension of the <code>Rectangle2D</code> representing this area.  If no
     * <code>Rectangle2D</code> exists, a zero-dimensioned default is first
     * created, then the value is applied.
     * @param millipts <code>inline-progression-dimension</code> to set, in
     * millipoints
     */
    public void setIPDim(int millipts) {
        setIPDimPts(millipts/1000.0f);
    }

    /**
     * Sets the <code>inline-progression-dimension</code> of the contents of
     * this area, in points.  This value is applied to the appropriate
     * dimension of the <code>Rectangle2D</code> representing this area.  If no
     * <code>Rectangle2D</code> exists, a zero-dimensioned default is first
     * created, then the value is applied.
     * @param pts <code>inline-progression-dimension</code> to set, in points
     */
    public void setIPDimPts(double pts) {
        synchronized (sync) {
            if (area == null) {
                area = new Rectangle2D.Double();
            }
            if (contentIsHorizontal){
                area.setRect(area.getX(), area.getY(), pts, area.getHeight());
            } else {
                area.setRect(area.getX(), area.getY(), area.getWidth(), pts);
            }
        }
    }

    /**
     * Gets the <code>inline-progression-dimension</code> maximum value,
     * in millipoints
     * @return the <code>inline-progression-dimension</code> maximum value
     */
    public Integer getIPDimMax() {
        synchronized(sync) {
            return iPDimMax;
        }
    }

    /**
     * Sets the <code>inline-progression-dimension</code> maximum value,
     * in millipoints
     * @param dimMax <code>inline-progression-dimension</code> maximum value
     * to set
     */
    public void setIPDimMax(Integer dimMax) {
        synchronized (sync) {
            iPDimMax = dimMax;
        }
    }

    /**
     * Gets the <code>inline-progression-dimension</code> mimimum value,
     * in millipoints
     * @return the <code>inline-progression-dimension</code> minimum value
     */
    public Integer getIPDimMin() {
        synchronized (sync) {
            return iPDimMin;
        }
    }

    /**
     * Sets the <code>inline-progression-dimension</code> minimum value,
     * in millipoints
     * @param dimMin <code>inline-progression-dimension</code> minimum value
     * to set
     */
    public void setIPDimMin(Integer dimMin) {
        synchronized (sync) {
            iPDimMin = dimMin;
        }
    }

}
