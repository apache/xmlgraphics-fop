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
 * Created on 7/06/2004
 * $Id$
 */
package org.apache.fop.area;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class BorderRectangle extends Area.AreaFrame {

    public BorderRectangle(Area area, int writingMode) {
        area.super(writingMode);
        contents = new PaddingRectangle(area, writingMode);
        contentOffset = new Point2D.Double();
        spaces = new SpacesRectangle(area, writingMode);
        spaces.setContents(this);
    }

    /**
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param contents
	 * @param contentOffset
	 */
	public BorderRectangle(Area area, int writingMode,
            double x, double y, double w, double h,
			PaddingRectangle contents, Point2D contentOffset) {
		area.super(writingMode, x, y, w, h, contents, contentOffset);
        spaces = new SpacesRectangle(area, writingMode);
        spaces.setContents(this);
	}

	/**
	 * @param rect
	 * @param contents
	 * @param contentOffset
	 */
	public BorderRectangle(Area area, int writingMode, Rectangle2D rect,
            PaddingRectangle contents, Point2D contentOffset) {
		area.super(writingMode, rect, contents, contentOffset);
        spaces = new SpacesRectangle(area, writingMode);
        spaces.setContents(this);
	}

    private SpacesRectangle spaces = null;

    public SpacesRectangle getSpaces() {
        return spaces;
    }
}
