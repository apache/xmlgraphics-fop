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
import org.apache.fop.area.Area.AreaGeometry;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class PaddingRectangle extends AreaFrame {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";


    /**
     * Instantiates a <code>PaddingRectangle</code> within the given instance
     * of <code>Area</code>, with the given <code>ContentRectangle</code> as
     * content. 
     * @param area the containing area instance
     * @param content the <i>content-rectangle</i> framed by this padding
     */
    public PaddingRectangle(Area area, ContentRectangle content) {
        super(area, content);
        borders = new BorderRectangle(area, this);
    }

	/**
     * Instantiates a <code>PaddingRectangle</code> within the given instance
     * of <code>Area</code>, with the given offset and dimension values, the
     * given <code>ContentRectangle</code> as content at the given offset from
     * the origin corner of the <i>padding-rectangle</i>. 
     * @param area the containing area instance
	 * @param ipOffset
	 * @param bpOffset
	 * @param ipDim
	 * @param bpDim
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

    public void setContents(AreaGeometry contents) {
        super.setContents(contents);
        borders.setContents(this);
    }

    /**
     * {@inheritDoc}
     * <p>The containing <code>BorderRectangle</code> is notified of the
     * change.
     */
    public void setTop(double top) {
        super.setTop(top);
        borders.setContents(this);
    }

    /**
     * {@inheritDoc}
     * <p>The containing <code>BorderRectangle</code> is notified of the
     * change.
     */
    public void setLeft(double left) {
        super.setLeft(left);
        borders.setContents(this);
    }

    /**
     * {@inheritDoc}
     * <p>The containing <code>BorderRectangle</code> is notified of the
     * change.
     */
    public void setBottom(double bottom) {
        super.setBottom(bottom);
        borders.setContents(this);
    }

    /**
     * {@inheritDoc}
     * <p>The containing <code>BorderRectangle</code> is notified of the
     * change.
     */
    public void setRight(double right) {
        super.setRight(right);
        borders.setContents(this);
    }

}
