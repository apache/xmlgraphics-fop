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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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

    /**
     * The total geometrical area covered by this <code>Area</code>, including
     * content rectangle, padding, borders and spaces or margins.  The
     * <code>width</code> of this <code>Rectangle</code> is the
     * <code>inline-progression-dimension</code> of the area, and the
     * <code>height</code> is the <code>block-progression-dimension</code>.
     * <p>The spaces are always implicitly defined with respect to the borders.
     * The only way in which individual margins/spaces can be derived is with
     * respect to the padding rectangle, so this rectangle is always associated
     * with a point offset to the top left corner of the padding rectangle. 
     * Note that spaces/margins are dynamic, in that they are frequently
     * adjusted or eliminated in the course of layout.
     * */
    protected SpacesRectangle spaces = null;
    /**
     * @return the space
     */
    protected SpacesRectangle getSpaces() {
        return spaces;
    }
    /** Geometrical area embraced by the border rectangle of this area.  Note
     * that borders may be collapsed.
     */
    protected BorderRectangle borders = null;
    /**
     * @return the border
     */
    protected BorderRectangle getBorders() {
        return borders;
    }
    /** Geometrical area embraced by the padding rectangle of this area.
     * N.B. The background (if any) is rendered in the padding rectangle. */
    protected PaddingRectangle padding = null;
    /**
     * @return the padding
     */
    protected PaddingRectangle getPadding() {
        return padding;
    }
    /** Geometrical area embraced by the content rectangle of this area */
    protected ContentRectangle content = null;
    /**
     * @return the content
     */
    protected ContentRectangle getContent() {
        return content;
    }

    protected void setMargins(
    		double before, double after, double start, double end) {
        spaces.setBefore(before);
        spaces.setAfter(after);
        spaces.setStart(start);
        spaces.setEnd(end);
    }
    /** Translates this area into position in its parent area */
    protected AffineTransform translation = null;
	/**
	 * @return the translation
	 */
	protected AffineTransform getTranslation() {
		return translation;
	}
	/**
	 * @param translation to set
	 */
	protected void setTranslation(AffineTransform translation) {
		this.translation = translation;
	}
    /** The writing-mode of the generating FO */
    protected int contentWritingMode = 0;
    /** True if the <code>writing-mode</code> of the content area is
     * horizontal */
    protected boolean contentIsHorizontal = true;
    /** True if the the <code>writing-mode</code> of the content area is
     * left-to-right */
    protected boolean contentLeftToRight = true;
    /** The writing-mode of the parent of the generating FO.  This may
     * differ from the writing mode of the generating FO if this is a
     * <code>reference-area</code>. */
    protected int frameWritingMode = 0;
    /** True if the <code>writing-mode</code> of the frames of this area is
     * horizontal.  May differ from contentIsHorizontal if this is a
     * <code>reference-area</code>. */
    protected boolean frameIsHorizontal = true;
    /** True if the the <code>writing-mode</code> of the frames of this area is
     * left-to-right.  May differ from contentIsHorizontal if this is a
     * <code>reference-area</code>. */
    protected boolean frameLeftToRight = true;


    private void setup() {
        try {
            contentWritingMode = generatedBy.getWritingMode();
            contentIsHorizontal = WritingMode.isHorizontal(contentWritingMode);
            contentLeftToRight = WritingMode.isLeftToRight(contentWritingMode);
            frameWritingMode =
                ((FONode)generatedBy.getParent()).getWritingMode();
            frameIsHorizontal = WritingMode.isHorizontal(frameWritingMode);
            frameLeftToRight = WritingMode.isLeftToRight(frameWritingMode);
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
        content = new ContentRectangle(this, contentWritingMode);
        //padding = new PaddingRectangle(frameWritingMode, content, 0.0, 0.0);
        borders = padding.getBorders();
        spaces = borders.getSpaces();
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
     * Constructs an <code>Area</code> which is the root of a tree, and is
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

    public class AreaGeometry extends Rectangle2D.Double {
        protected int writingMode;
        protected boolean isLeftToRight;
        protected boolean isHorizontal;

        public AreaGeometry(int writingMode) {
            super();
            this.writingMode = writingMode;
            try {
                isHorizontal = WritingMode.isHorizontal(writingMode);
                isLeftToRight = WritingMode.isLeftToRight(writingMode);
            } catch (PropertyException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        public AreaGeometry(int writingMode,
                double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
            this(writingMode);
            setRect(ipOrigin, bpOrigin, ipDim, bpDim);
        }
        
        public void setRect(
                double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
            // Now work out what belongs where
            Point2D origin = null;
            Point2D wideHigh = null;
            try {
                origin = WritingMode.dimsRelToAbs(
                        ipOrigin, bpOrigin, writingMode);
                wideHigh = WritingMode.dimsRelToAbs(
                        ipDim, bpDim, writingMode);
            } catch (PropertyException e) {
                throw new RuntimeException(e.getMessage());
            }
            setRect(origin.getX(), origin.getY(),
                    wideHigh.getX(), wideHigh.getY());
        }

        public int getWritingMode() {
            return writingMode;
        }

        /**
         * Gets the <code>block-progression-dimension</code> of the area
         * geometry in millipoints.  This value is taken from the appropriate
         * dimension of the <code>Rectangle2D</code> representing this area.
         * @return the <code>block-progression-dimension</code> in millipoints
         */
        public int getBPDim() {
            return (int)(getBPDimPts()*1000);
        }

        /**
         * Gets the <code>block-progression-dimension</code> of the area
         * geometry in points.  This value is taken from the appropriate dimension
         * of the <code>Rectangle2D</code> representing this area.
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
                if (isHorizontal) {
                    return getHeight();
                } else {
                    return getWidth();
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
        public void setBPDimPts(double pts) {
            synchronized (this) {
                // TODO - check this
                if (isHorizontal) {
                    setRect(getX(), getY(), getWidth(), pts);
                } else {
                    setRect(getX(), getY(), pts, getHeight());
                }
            }
        }

        /**
         * Gets the <code>inline-progression-dimension</code> of the contents of
         * this area in millipoints.  This value is taken from the appropriate
         * dimension of the <code>Rectangle2D</code> representing this area.
         * N.B. The method is synchronized only on this object.
         * @return the <code>inline-progression-dimension</code> in millipoints
         */
        public int getIPDim() {
            return (int)(getIPDimPts()*1000);
        }

        /**
         * Gets the <code>inline-progression-dimension</code> of the area
         * geometry in points.  This value is taken from the appropriate dimension
         * of the <code>Rectangle2D</code> representing this area.
         * N.B. The method is synchronized only on this object.
         * @return the <code>inline-progression-dimension</code> in points
         */
        public double getIPDimPts() {
            synchronized (this) {
                // TODO - check this
                if (isHorizontal){
                    return getWidth();
                } else {
                    return getHeight();
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
         * Sets the <code>inline-progression-dimension</code> of the area
         * geometry, in points.  This value is applied to the appropriate
         * dimension of the <code>Rectangle2D</code> representing this area.
         * N.B. The method is synchronized only on this object.
         * @param pts <code>inline-progression-dimension</code> to set, in points
         */
        public void setIPDimPts(double pts) {
            synchronized (this) {
                // Check this
                if (isHorizontal){
                    setRect(getX(), getY(), pts, getHeight());
                } else {
                    setRect(getX(), getY(), getWidth(), pts);
                }
            }
        }
    }

    /**
     * <code>AreaFrame</code> is the basic type for the geometry of a rectangle
     * enclosing another rectangle, e.g., a padding rectangle. 
     * @author pbw
     * @version $Revision: 1.1.2.2 $ $Name:  $
     */
    public class AreaFrame extends AreaGeometry {

        /** The framed rectangle */
        protected Rectangle2D contents = null;
        /** The offset from <code>this</code> origin to the origin of the framed
         * rectangle */
        protected Point2D contentOffset = null;

        /**
         * 
         */
        public AreaFrame(int writingMode) {
            super(writingMode);
            contents = new Rectangle2D.Double();
            contentOffset = new Point2D.Double();
        }

        /**
         * Instantiates a frame with 0-width edges.
         * @param contents the contained rectangle
         */
        public AreaFrame(int writingMode, AreaGeometry contents) {
            super(writingMode);
            setRect(contents);
            this.contents = contents;
            this.contentOffset = new Point2D.Double();
        }

        /**
         * Instantiates a new framing rectangle with the given origin point, the
         * given width and height, the given content rectangle, and the given
         * offset from the origin of the framing rectangle to the origin of the
         * content rectangle.
         * @param x x-value of the origin of the framing rectangle in user space
         * units
         * @param y y-value of the origin of the framing rectangle in user space
         * units
         * @param w width of the framing rectangle in user space units
         * @param h height of the framing rectangle in user space units
         * @param contents the framed rectangle
         * @param contentOffset the offset to the origin point of the framed
         * rectangle from the origin point of <code>this</code> framing rectangle.
         */
        public AreaFrame(int writingMode,
                double ipOrigin, double bpOrigin, double ipDim, double bpDim,
                AreaGeometry contents, Point2D contentOffset) {
            super(writingMode, ipOrigin, bpOrigin, ipDim,  bpDim);
            this.contents = contents;
            this.contentOffset = contentOffset;
        }

        /**
         * Instantiates a new framing rectangle from the given rectangle, given
         * contents, and given offset from the origin of the framing rectangle to
         * the origin of the framed rectangle.  The dimensions and location of the
         * given rectangle are copied into the dimensions of the new framing
         * rectangle.
         * @param rect the framing rectangle
         * @param contents the framed rectangle
         * @param contentOffset offset from origin of the framing rectangle to the
         * origin of the framed rectangle
         */
        public AreaFrame(int writingMode,
                Rectangle2D rect, AreaGeometry contents,
                Point2D contentOffset) {
            this(writingMode, rect.getX(), rect.getY(),
                    rect.getWidth(), rect.getHeight(),
                    contents, contentOffset);
        }

        /**
         * Sets the contents rectangle.  The dimensions of <code>this</code> are
         * adjusted to the difference between the current framed contents and
         * the new framed contents.  The offset is not affected.
         * @param contents the new framed contents
         */
        public void setContents(Rectangle2D contents) {
            setRect(getX(), getY(),
                    getWidth() + (contents.getWidth() - this.contents.getWidth()),
                    getHeight() + (contents.getWidth() - this.contents.getWidth()));
            contents = this.contents;
        }

        public Rectangle2D getContents() {
            return contents;
        }

        /**
         * Sets the offset from the origin of <code>this</code> to the origin of
         * the framed contents rectangle.  The dimensions of the framed contents
         * are not affected, but the dimensions of <code>this</code> are changed
         * by the difference between the current offset and the new offset in the
         * X and Y axes.
         * @param offset the new offset to the framed rectangle
         */
        public void setContentOffset(Point2D offset) {
            setStart(offset.getX());
            setBefore(offset.getY());
            contentOffset = offset;
        }

        public Point2D getContentOffset() {
            return contentOffset;
        }

        /**
         * Sets the before edge width of the frame.  The <code>contents</code> size
         * is unaffected, but the size of the frame (<code>this</code>) will
         * change.  The height will vary by the difference between the previous and
         * new before edge.  The <code>contentOffset</code> will also change by the
         * same amount.  Note that the origin of this frame (<code>getX(),
         * getY()</code>) will not change.
         * @param before
         */
        public void setBefore(double before) {
            double diff = before - contentOffset.getY();
            setRect(getX(), getY(),
                    getWidth(), getHeight() + diff);
            contentOffset.setLocation(contentOffset.getX(), before);
        }

        /**
         * Sets the start edge width of the frame.  The <code>contents</code> size
         * is unaffected, but the size of the frame (<code>this</code>) will
         * change.  The width will vary by the difference between the previous and
         * new start edge.  The <code>contentOffset</code> will also change by the
         * same amount.  Note that the origin of this frame (<code>getX(),
         * getY()</code>) will not change.
         * @param start
         */
        public void setStart(double start) {
            double diff = start - contentOffset.getY();
            setRect(getX(), getY(),
                    getWidth() + diff, getHeight());
            contentOffset.setLocation(start, contentOffset.getX());
        }

        /**
         * Sets the after edge width of the frame.  The <code>contents</code> size
         * and the <code>contentOffset</code> are unaffected, but the size of the
         * frame (<code>this</code>) will change.  The height will vary by the
         * difference between the previous and new after edge.  Note that the
         * origin of this frame (<code>getX(), getY()</code>) will not change.
         * @param after
         */
        public void setAfter(double after) {
            double diff = after - (getY() - contentOffset.getY() - contents.getY());
            setRect(getX(), getY(), getWidth(), getHeight() + diff);
        }

        /**
         * Sets the end edge width of the frame.  The <code>contents</code> size
         * and the <code>contentOffset</code> are unaffected, but the size of the
         * frame (<code>this</code>) will change.  The width will vary by the
         * difference between the previous and new end edge.  Note that the
         * origin of this frame (<code>getX(), getY()</code>) will not change.
         * @param end
         */
        public void setEnd(double end) {
            double diff = end - (getX() - contentOffset.getX() - contents.getX());
            setRect(getX(), getY(), getWidth() + diff, getHeight());
        }
    }

}
