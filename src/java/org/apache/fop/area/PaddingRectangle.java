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
 * Created on 6/06/2004
 * $Id$
 */
package org.apache.fop.area;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class PaddingRectangle extends AreaFrame {

    public PaddingRectangle(Area area, Area.AreaGeometry content) {
        super(area, content);
        borders = new BorderRectangle(area, this);
    }

    /**
	 * @param contents
	 * @param contentOffset
	 */
	public PaddingRectangle(Area area,
            double ipOffset, double bpOffset, double ipDim, double bpDim,
			ContentRectangle contents, Point2D contentOffset) {
		super(area, ipOffset, bpOffset, ipDim, bpDim, contents, contentOffset);
        borders = new BorderRectangle(area, this);
	}

	/**
	 * @param rect
	 * @param contents
	 * @param contentOffset
	 */
	public PaddingRectangle(Area area, Rectangle2D rect,
            ContentRectangle contents, Point2D contentOffset) {
		super(area, rect, contents, contentOffset);
        borders = new BorderRectangle(area, this);
	}

    private BorderRectangle borders = null;

    public BorderRectangle getBorders() {
        return borders;
    }
}
