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
    /** The rotation trait for the content rectangle of this area */
    protected int contentRotation = 0;
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
    /** The rotation trait for the framing rectangles of this area */
    protected int frameRotation = 0;


    protected void setup() {
        try {
            contentWritingMode = generatedBy.getWritingMode();
            contentIsHorizontal = WritingMode.isHorizontal(contentWritingMode);
            contentLeftToRight = WritingMode.isLeftToRight(contentWritingMode);
            contentRotation = generatedBy.getRefOrientation();
            frameWritingMode =
                ((FONode)generatedBy.getParent()).getWritingMode();
            frameIsHorizontal = WritingMode.isHorizontal(frameWritingMode);
            frameLeftToRight = WritingMode.isLeftToRight(frameWritingMode);
            frameRotation =
                ((FONode)generatedBy.getParent()).getRefOrientation();
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
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
            try {
                setRect(WritingMode.rectRelToAbs(
                        ipOrigin, bpOrigin, ipDim, bpDim, writingMode));
            } catch (PropertyException e) {
                throw new RuntimeException(e);
            }
        }

        public AreaGeometry(int writingMode, Rectangle2D geometry) {
            this(writingMode);
            setRect(geometry);
        }

        public void setRect(
                double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
            // Now work out what belongs where
            try {
                setRect(WritingMode.rectRelToAbs(
                        ipOrigin, bpOrigin, ipDim, bpDim, writingMode));
            } catch (PropertyException e) {
                throw new RuntimeException(e);
            }
        }

        public int getWritingMode() {
            return writingMode;
        }

        public int getContentWritingMode() {
            return contentWritingMode;
        }

        public int getFrameWritingMode() {
            return frameWritingMode;
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

}
