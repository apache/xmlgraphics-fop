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
 * Created on 14/06/2004
 * $Id$
 */
package org.apache.fop.area;

import org.apache.fop.area.Area.AreaGeometry;


/**
 * Defines the <i>content rectangle</i> of an area.  It is the central class
 * in the management of the layout geometry of areas.  The other generated
 * rectangular areas are defined in terms of this area. 
 * @author pbw
 * @version $Revision$ $Name$
 */
public class ContentRectangle extends AreaGeometry {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private final Area area;

    /**
     * @param area the containing <code>Area</code> instance
     */
    public ContentRectangle(Area area) {
        area.super(area.contentWritingMode);
        this.area = area;
        padding = new PaddingRectangle(area, this);
    }

    /**
     * @param area the containing <code>Area</code> instance
     * @param ipOrigin
     * @param bpOrigin
     * @param ipDim
     * @param bpDim
     */
    public ContentRectangle(Area area,
            double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
        area.super(area.contentWritingMode, ipOrigin, bpOrigin, ipDim, bpDim);
        // Get the padding writing mode
        this.area = area;
        padding = new PaddingRectangle(area, this);
    }

    /**
     * Gets the writing-mode applicable to the content-rectangle
     * @see org.apache.fop.area.Area.AreaGeometry#getWritingMode()
     */
    public int getWritingMode() {
        return getContentWritingMode();
    }

    /** The <code>PaddingRectangle</code> <code>AreaFrame</code> around
     * <code>this</code> */
    private PaddingRectangle padding = null;

    /**
     * Gets the containing <code>PaddingRectangle</code>
     * @return the padding-ractangle
     */
    public PaddingRectangle getPadding() {
        return padding;
    }

    /**
     * Sets the containing <code>PaddingRectangle</code> to the argument
     * @param padding the padding-rectangle
     */
    public void setPadding(PaddingRectangle padding) {
        this.padding = padding; 
    }

    /**
     * Sets the offset and dimensions for this<i>content-rectangle</i>, and
     * then sets <code>this</code> as the contents of the <code>padding</code>
     * <code>AreaFrame</code>
     * @param ipOrigin {@inheritDoc}
     * @param bpOrigin {@inheritDoc}
     * @param ipDim {@inheritDoc}
     * @param bpDim {@inheritDoc}
     * @see org.apache.fop.area.Area.AreaGeometry#setRect(double, double, double, double)
     */
    public void setRect(
            double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
        super.setRect(ipOrigin, bpOrigin, ipDim, bpDim);
        padding.setContents(this);
    }

    /* (non-Javadoc)
     * @see org.apache.fop.area.Area.AreaGeometry#getFrameRelativeDimensions()
     */
    public DimensionDbl getFrameRelativeDimensions() {
        switch (getRotationToFrame()) {
        case 0:
        case 180:
            return super.getFrameRelativeDimensions();
        case 90:
        case 270:
            return new DimensionDbl(getHeight(), getWidth());
        default:
            area.log.warning("Illegal rotation: " + getRotationToFrame());
            return super.getFrameRelativeDimensions();
        }
    }

    /**
     * Gets the width of this <code>AreaGeometry</code> as seen from any
     * enclosing frame
     * @return the frame-view width
     */
    protected double getFrameRelativeWidth() {
        return getFrameRelativeDimensions().getWidth();
    }
    /**
     * Gets the height of this <code>AreaGeometry</code> as seen from any
     * enclosing frame
     * @return the frame-view height
     */
    protected double getFrameRelativeHeight() {
        return getFrameRelativeDimensions().getHeight();
    }

    /**
     * {@inheritDoc}
     * <p>Any registered listeners are notified of the change in the
     * dimension.
     */
    public void setIPDimPts(double pts) {
        super.setIPDimPts(pts);
        padding.setContents(this);
        area.notifyListeners();
    }

    /**
     * {@inheritDoc}
     * <p>Any registered listeners are notified of the change in the
     * dimension.
     */
    public void setBPDimPts(double pts) {
        super.setBPDimPts(pts);
        padding.setContents(this);
        area.notifyListeners();
    }

}
