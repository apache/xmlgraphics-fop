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
 * The base class for all geometrical areas.  <code>Area</code> extends
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
     * Gets the <i>content-rectangle</i> of this 
     * @return the content
     */
    protected ContentRectangle getContent() {
        return content;
    }

    protected void setBeforeMargin(double before) {
        spaces.setBefore(before);
    }
    protected void setAfterMargin(double after) {
        spaces.setAfter(after);
    }
    protected void setStartMargin(double start) {
        spaces.setStart(start);
    }
    protected void setEndMargin(double end) {
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
    protected int contentWritingMode;
    /** True if the <code>writing-mode</code> of the content area is
     * horizontal */
    protected boolean contentIsHorizontal = true;
    /** True if the the <code>writing-mode</code> of the content area is
     * left-to-right */
    protected boolean contentLeftToRight = true;
    /** The rotation trait for the content rectangle of this area */
    protected int contentRotation;
    /** The writing-mode of the parent of the generating FO.  This may
     * differ from the writing mode of the generating FO if this is a
     * <code>reference-area</code>. */
    protected int frameWritingMode;

    protected void setup() {
        try {
            contentWritingMode = generatedBy.getWritingMode();
            contentIsHorizontal = WritingMode.isHorizontal(contentWritingMode);
            contentLeftToRight = WritingMode.isLeftToRight(contentWritingMode);
            contentRotation = generatedBy.getRefOrientation();
            frameWritingMode = contentWritingMode;
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
        setupFrames();
    }

    protected void setupFrames() {
        content = new ContentRectangle(this);
        padding = content.getPadding();
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

    /**
     * Returns a <code>Rectangle2D</code> constructed from the normailized
     * values of offset and dimensions expressed in terms of 
     * <i>inline-progression-direction</i> and
     * <i>block-progression-direction</i>
     * @param ipOffset
     * @param bpOffset
     * @param ipDim the <i>inline-progression-dimension</i>
     * @param bpDim the <i>block-progression-dimension</i>
     * @param wMode
     * @return
     * @throws PropertyException
     */
    public static Rectangle2D.Double rectRelToAbs(
            double ipOffset, double bpOffset, double ipDim, double bpDim,
            int wMode) throws PropertyException {
        if (WritingMode.isHorizontal(wMode)) {
            return new Rectangle2D.Double(ipOffset, bpOffset, ipDim, bpDim);
        }
        return new Rectangle2D.Double(bpOffset, ipOffset, bpDim, ipDim);
    }

    /**
     * Normalizes a pair of values representing an
     * <i>inline-progression-dimension</i> and a
     * <i>block-progression-dimension</i> by converting them to a
     * <i>Point2D</i> representing the corresponding X and Y values in
     * Java 2D user co-ordinates.
     * @param ipDim the <i>inline-progression-dimension</i>
     * @param bpDim the <i>block-progression-dimension</i>
     * @param writingMode
     * @return the corresponding x, y values
     * @throws PropertyException
     */
    public static DimensionDbl dimsRelToAbs (
            double ipDim, double bpDim, int writingMode)
    throws PropertyException {
        if (WritingMode.isHorizontal(writingMode)) {
            return new DimensionDbl(ipDim, bpDim);
        }
        return new DimensionDbl(bpDim, ipDim);
    }

    /**
     * Normalizes a <code>DimensonDbl</code> representing an
     * <i>inline-progression-dimension</i> (<i>width</i>) and a
     * <i>block-progression-dimension</i> (<i>height</i>) by converting them to
     * a <code>DimensonDbl</code> representing the corresponding width and
     * height values in Java 2D user co-ordinates.
     * @param in the dimensions expressed as <i>inline-progression-dimension</i>
     * and <i>block-progression-dimension</i>
     * @param writingMode
     * @return the corresponding Java2D width, height values
     * @throws PropertyException
     */
    public static DimensionDbl dimsRelToAbs (DimensionDbl in, int writingMode)
    throws PropertyException {
        if (WritingMode.isHorizontal(writingMode)) {
            return in;
        }
        double width, height;
        width = in.getHeight();
        height = in.getWidth();
        in.setSize(width, height);
        return in;
    }

    /**
     * Returns a <code>Rectangle2D</code> constructed from the normailized
     * values of offset and dimensions expressed in terms of 
     * <i>inline-progression-direction</i> and
     * <i>block-progression-direction</i>
     * @param offset
     * @param wideHigh
     * @param writingMode
     * @return
     * @throws PropertyException
     */
    public static Rectangle2D dimsRelToAbs (
            Point2D offset, DimensionDbl wideHigh, int writingMode)
    throws PropertyException {
        if (WritingMode.isHorizontal(writingMode)) {
            return new Rectangle2D.Double(
                    offset.getX(), offset.getY(),
                    wideHigh.getWidth(), wideHigh.getHeight());
        }
        return new Rectangle2D.Double(
                offset.getY(), offset.getX(),
                wideHigh.getHeight(), wideHigh.getWidth());
    }

    /**
     * A nested class of Area, representing the geometry of one of the frames
     * associated with this area.  These include the content-rectangle,
     * border-rectangle, padding-rectangle, spaces-rectangle and
     * allocation-rectangle.
     * @author pbw
     * @version $Revision$ $Name$
     */
    public class AreaGeometry extends Rectangle2D.Double {
        protected int writingMode;
        protected boolean isLeftToRight;
        protected boolean isHorizontal;

        /**
         * Creates an empty rectangle, with height and width of 0.0 at an
         * offset of 0.0,0.0, with the given writing-mode
         * @param writingMode
         */
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

        /**
         * Creates a rectangle with an origin determined by the parameters
         * <code>ipOrigin</code> and <code>bpOrigin</code>, and with width and
         * height determined by the parameters <code>ipDim</code> and
         * <code>bpDim</code>.  The translation of the relative offsets and
         * dimensions into absolute directions is determined by the parameter
         * <code>writingMode</code>.
         * @param writingMode the <i>writing-mode</i> of the instantiated
         * rectangular area, expressed as an enumerated constant from the
         * set given in <code>WritingMode</code>.
         * @param ipOrigin the origin point along the
         * <i>inline-progression-direction</i> axis
         * @param bpOrigin the origin point along the
         * <i>block-progression-direction</i> axis
         * @param ipDim the size of the rectangular area along the
         * <i>inline-progression-direction</i> axis
         * @param bpDim the size of the rectangular area along the
         * i>block-progression-direction</i> axis
         */
        public AreaGeometry(int writingMode,
                double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
            this(writingMode);
            try {
                setRect(rectRelToAbs(
                        ipOrigin, bpOrigin, ipDim, bpDim, writingMode));
            } catch (PropertyException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Creates a rectangle whose dimensions and offset are expressed in the
         * parameter <code>geometry</code>.  The rectangular area has the
         * given <i>writing-mode</i>
         * @param writingMode the <i>writing-mode</i> of the instantiated
         * rectangular area, expressed as an enumerated constant from the
         * set given in <code>WritingMode</code>.
         * @param geometry the dimensions and offset of the geometrical area
         * represented by <code>this</code>.
         */
        public AreaGeometry(int writingMode, Rectangle2D geometry) {
            this(writingMode);
            setRect(geometry);
        }

        /**
         * Overrides <code>Rectangle2D</code> method to set the dimensions
         * and offset of the rectangular area.  The dimensions and offset are
         * expressed in relative terms, which must be translated into absolute
         * terms according to the <i>writing-mode</i> of <code>this</code>.
         * @param ipOrigin the origin point along the
         * <i>inline-progression-direction</i> axis
         * @param bpOrigin the origin point along the
         * <i>block-progression-direction</i> axis
         * @param ipDim the size of the rectangular area along the
         * <i>inline-progression-direction</i> axis
         * @param bpDim the size of the rectangular area along the
         * i>block-progression-direction</i> axis
         * @see java.awt.geom.Rectangle2D#setRect(double, double, double, double)
         */
        public void setRect(
                double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
            // Now work out what belongs where
            try {
                setRect(rectRelToAbs(
                        ipOrigin, bpOrigin, ipDim, bpDim, writingMode));
            } catch (PropertyException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Gets the writing-mode of this rectangular area, expressed as an
         * enumerated constant from <code>WritingMode</code>
         * @return the emnumerated writing-mode
         */
        public int getWritingMode() {
            return writingMode;
        }

        /**
         * Gets the writing-mode of the <i>content-rectangle</i> of
         * <code>this</code> <code>Area</code>
         * @return the writing-mode of the <i>content-rectangle</i>
         */
        public int getContentWritingMode() {
            return contentWritingMode;
        }

        /**
         * Gets the writing-mode of the framing rectangles of the content of
         * <code>this</code> <code>Area</code>
         * @return the writing-mode of the framing rectangles
         */
        public int getFrameWritingMode() {
            return contentWritingMode;
        }

        /**
         * Gets the reference-orientation applying to the contents of this
         * area, expressed as a normalized angle; one of 0, 90, 180 or 270.
         * TODO - should this simply be 0.  Should reference-orientation on
         * all non-reference areas be set to 0; i.e. not rotated with respect
         * to the ancestor reference-area?
         * BEWARE - this is set to 0
         * @return the reference-orientation
         */
        public int getContentRotation() {
            return contentRotation;
        }

        public int getRotationToFrame() {
            return 0;
        }

        /**
         * Gets the reference-oroientation applying to the parent
         * <code>FONode</code> of the generating <code>FONode</code>.  This
         * rotation applied to frames surrounding the <i>content-rectangle</i>. 
         * @return the parent's reference-orientation
         */
        public int getFrameRotation() {
            return contentRotation;
        }

        public int getRotationToContent() {
            return 0;
        }

        /**
         * Gets the dimensions of this <code>AreaGeometry</code> as seen
         * from any surrounding frame within this <code>Area</code>.  For all
         * <code>AreaGeometry</code>s except <code>ContentRectangle</code>s,
         * the relative dimensions are the same for frame and contents; i.e.
         * height is height and width is width.
         * @return the adjusted dimensions
         */
        protected DimensionDbl getFrameRelativeDimensions() {
            return new DimensionDbl(getWidth(), getHeight());
        }

        /**
         * Gets the width of this <code>AreaGeometry</code> as seen from any
         * enclosing frame.  For all
         * <code>AreaGeometry</code>s except <code>ContentRectangle</code>s,
         * the relative dimensions are the same for frame and contents; i.e.
         * height is height and width is width.
         * @return the frame-view width
         */
        protected double getFrameRelativeWidth() {
            return getWidth();
        }

        /**
         * Gets the height of this <code>AreaGeometry</code> as seen from any
         * enclosing frame.  For all <code>AreaGeometry</code>s except
         * <code>ContentRectangle</code>s, the relative dimensions are the
         * same for frame and contents; i.e. height is height and width is
         * width.
         * @return the frame-view height
         */
        protected double getFrameRelativeHeight() {
            return getHeight();
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
                // only for area rotations.  This depends on how Java handles
                // the layout of text in horizontal locales.
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
         * <code>Rectangle2D</code> representing this area. 
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
         * <code>Rectangle2D</code> representing this area.
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
         * dimension of the <code>Rectangle2D</code> representing this area.
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

}
