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

import org.apache.fop.area.Area.AreaGeometry;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class BorderRectangle extends AreaFrame {

    public BorderRectangle(Area area, PaddingRectangle content) {
        super(area, content);
        spaces = new SpacesRectangle(area, this);
    }

	public BorderRectangle(Area area,
            double ipOffset, double bpOffset, double ipDim, double bpDim,
			PaddingRectangle contents, Point2D contentOffset) {
        super(area, ipOffset, bpOffset, ipDim, bpDim, contents, contentOffset);
        spaces = new SpacesRectangle(area, this);
	}

	/**
	 * @param rect
	 * @param contents
	 * @param contentOffset
	 */
	public BorderRectangle(Area area, Rectangle2D rect,
            PaddingRectangle contents, Point2D contentOffset) {
		super(area, rect, contents, contentOffset);
        spaces = new SpacesRectangle(area, this);
	}

    private SpacesRectangle spaces = null;

    public SpacesRectangle getSpaces() {
        return spaces;
    }

    public void setContents(AreaGeometry contents) {
        super.setContents(contents);
        spaces.setContents(this);
    }

    /**
     * {@inheritDoc}
     * <p>The containing <code>SpacesRectangle</code> is notified of the
     * change.
     */
    public void setTop(double top) {
        super.setTop(top);
        spaces.setContents(this);
    }

    /**
     * {@inheritDoc}
     * <p>The containing <code>SpacesRectangle</code> is notified of the
     * change.
     */
    public void setLeft(double left) {
        super.setLeft(left);
        spaces.setContents(this);
    }

    /**
     * {@inheritDoc}
     * <p>The containing <code>SpacesRectangle</code> is notified of the
     * change.
     */
    public void setBottom(double bottom) {
        super.setBottom(bottom);
        spaces.setContents(this);
    }

    /**
     * {@inheritDoc}
     * <p>The containing <code>SpacesRectangle</code> is notified of the
     * change.
     */
    public void setRight(double right) {
        super.setRight(right);
        spaces.setContents(this);
    }

}
