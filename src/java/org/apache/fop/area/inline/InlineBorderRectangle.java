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
 * Created on 17/06/2004
 * $Id$
 */
package org.apache.fop.area.inline;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.fop.area.AllocationRectangle;
import org.apache.fop.area.Area;
import org.apache.fop.area.BorderRectangle;
import org.apache.fop.area.PaddingRectangle;


/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class InlineBorderRectangle extends BorderRectangle implements
        AllocationRectangle {

    /**
     * @param area
     * @param content
     */
    public InlineBorderRectangle(Area area, PaddingRectangle content) {
        super(area, content);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param area
     * @param ipOffset
     * @param bpOffset
     * @param ipDim
     * @param bpDim
     * @param contents
     * @param contentOffset
     */
    public InlineBorderRectangle(Area area, double ipOffset, double bpOffset,
            double ipDim, double bpDim, PaddingRectangle contents,
            Point2D contentOffset) {
        super(area, ipOffset, bpOffset, ipDim, bpDim, contents, contentOffset);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param area
     * @param rect
     * @param contents
     * @param contentOffset
     */
    public InlineBorderRectangle(Area area, Rectangle2D rect,
            PaddingRectangle contents, Point2D contentOffset) {
        super(area, rect, contents, contentOffset);
        // TODO Auto-generated constructor stub
    }

}
