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
import java.util.ArrayList;

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

    /** The total geometrical area covered by this <code>Area</code>, including
     * content rectangle, padding, borders and spaces.  The <code>width</code>
     * of this <code>Rectangle</code> is the <code>inline-progression-dimension
     * </code> of the area, and the <code>height</code> is the
     * <code>block-progression-dimension</code>.  */
    protected Rectangle2D space = new Rectangle2D.Float();
    protected Rectangle2D border = null;
    protected Rectangle2D padding = null;
    protected Rectangle2D content = new Rectangle2D.Float();
    /** True if the the <code>writing-mode</code> of the content area is
     * horizontal */
    protected boolean contentIsHorizontal = true;
    /** True if the the <code>writing-mode</code> of the content area is
     * left-to-right */
    protected boolean contentLeftToRight = true;

    /**
     * @return the border
     */
    public Rectangle2D getBorder() {
        return border;
    }
    /**
     * @param border to set
     */
    public void setBorder(Rectangle2D border) {
        this.border = border;
    }
    /**
     * @return the content
     */
    public Rectangle2D getContent() {
        return content;
    }
    /**
     * @param content to set
     */
    public void setContent(Rectangle2D content) {
        this.content = content;
    }
    /**
     * @return the padding
     */
    public Rectangle2D getPadding() {
        return padding;
    }
    /**
     * @param padding to set
     */
    public void setPadding(Rectangle2D padding) {
        this.padding = padding;
    }
    /**
     * @return the space
     */
    public Rectangle2D getSpace() {
        return space;
    }
    /**
     * @param space to set
     */
    public void setSpace(Rectangle2D space) {
        this.space = space;
    }

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
     * Constructs an <code>Area</code> with a null rectangular area
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     * @param parent <code>Node</code> of this
     * @param sync the object on which this area is synchronized <code>for tree
     * operations</code>.
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
     * N.B. The method is synchronized only on this object.
     * @return the <code>block-progression-dimension</code> in points
     */
    public double getBPDimPts() {
        synchronized (this) {
            // TODO - check this.  Should the rectangle just be rotated as
            // required?  This is incompatible with transform in space
            // requests at block reference-areas.  OK if the transform is
            // only for area rotations.  This depnds on how Java handles the
            // layout of text in horizontal locales.
            if (contentIsHorizontal) {
                return content.getHeight();
            } else {
                return content.getWidth();
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
     * N.B. The method is synchronized only on this object.
     * @param pts <code>block-progression-dimension</code> to set, in points
     */
    public void setBPDimPts(float pts) {
        synchronized (this) {
            // TODO - check this
            if (contentIsHorizontal) {
                content.setRect(
                        content.getX(),content.getY(), content.getWidth(), pts);
            } else {
                content.setRect(
                        content.getX(),content.getY(), pts, content.getHeight());
            }
        }
    }

    /**
     * Gets the <code>inline-progression-dimension</code> of the contents of
     * this area in millipoints.  This value is taken from the appropriate
     * dimension of the <code>Rectangle2D</code> representing this area.  If no
     * <code>Rectangle2D</code> exists, a zero-dimensioned default is first
     * created, then the zero value is returned.
     * N.B. The method is synchronized only on this object.
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
     * N.B. The method is synchronized only on this object.
     * @return the <code>inline-progression-dimension</code> in points
     */
    public double getIPDimPts() {
        synchronized (this) {
            // TODO - check this
            if (contentIsHorizontal){
                return content.getWidth();
            } else {
                return content.getHeight();
            }
        }
    }

    /**
     * Sets the <code>inline-progression-dimension</code> of the contents of
     * this area, in points.  This value is applied to the appropriate
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
     * N.B. The method is synchronized only on this object.
     * @param pts <code>inline-progression-dimension</code> to set, in points
     */
    public void setIPDimPts(double pts) {
        synchronized (this) {
            // Check this
            if (contentIsHorizontal){
                content.setRect(
                        content.getX(), content.getY(), pts, content.getHeight());
            } else {
                content.setRect(
                        content.getX(), content.getY(), content.getWidth(), pts);
            }
        }
    }

    /** An initially null range of minima and maxima for
     * <code>inline-progression-dimension</code> and
     * <code>block-progression-dimension</code>.
     */
    protected AreaRange pageSpaceRange = new AreaRange();
    /**
     * @return the pageSpaceRange
     */
    public AreaRange getPageSpaceRange() {
        return pageSpaceRange;
    }
    /**
     * @param pageSpaceRange to set
     */
    public void setPageSpaceRange(AreaRange pageSpaceRange) {
        this.pageSpaceRange = pageSpaceRange;
    }

    /** Initial size of the <code>listeners</code> array */
    private static final int INITIAL_LISTENER_SIZE = 4;
    /** Array of registered <code>AreaListener</code>s */
    private ArrayList listeners = null;
    /**
     * Registers a listener to be notified on any change of dimension in the
     * <code>Rectangle2D</code> content
     * @param listener to be notified
     */
    public void registerAreaListener(AreaListener listener) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ArrayList(INITIAL_LISTENER_SIZE);
            }
            listeners.add(listener);
        }
    }

    /**
     * Notifies any registered listener of a change of dimensions in the
     * <code>Rectangle2D</code> content
     */
    protected void notifyListeners() {
        for (int i = 0; i < listeners.size(); i++) {
            synchronized (this) {
                ((AreaListener)(listeners.get(i))).setDimensions(content);
            }
        }
    }

}
