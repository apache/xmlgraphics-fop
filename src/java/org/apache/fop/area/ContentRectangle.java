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

    /**
     * @param writingMode
     */
    public ContentRectangle(Area area) {
        area.super(area.contentWritingMode);
        padding = new PaddingRectangle(area, this);
    }

    /**
     * @param writingMode
     * @param ipOrigin
     * @param bpOrigin
     * @param ipDim
     * @param bpDim
     */
    public ContentRectangle(Area area,
            double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
        area.super(area.contentWritingMode, ipOrigin, bpOrigin, ipDim, bpDim);
        // Get the padding writing mode
        padding = new PaddingRectangle(area, this);
    }

    public int getWritingMode() {
        return getContentWritingMode();
    }

    private PaddingRectangle padding = null;

    public PaddingRectangle getPadding() {
        return padding;
    }

    public void setPadding(PaddingRectangle padding) {
        this.padding = padding; 
    }

    public void setRect(
            double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
        super.setRect(ipOrigin, bpOrigin, ipDim, bpDim);
        padding.setContents(this);
    }

}
