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

import java.awt.geom.Rectangle2D.Double;

/**
 * Defines the <i>content rectangle</i> of an area.  It is the central class
 * in the management of the layout geometry of areas.  The other generated
 * rectangular areas are accessed through and defined in terms of this area. 
 * @author pbw
 * @version $Revision$ $Name$
 */
public class ContentRectangle extends Double {

	/**
	 * Creates an empty contents object.
	 */
	public ContentRectangle() {
		super();
        padding = new PaddingRectangle();
        padding.setContents(this);
	}

	/**
     * Creates a contents object from the given origin (<code>x, y</code>)
     * width (<code>w</code>) and height (<code>h</code>).
	 * @param x x-origin
	 * @param y y-origin
	 * @param w width
	 * @param h height
	 */
	public ContentRectangle(double x, double y, double w, double h) {
		super(x, y, w, h);
        padding = new PaddingRectangle();
        padding.setContents(this);
	}

	private PaddingRectangle padding = null;

    public PaddingRectangle getPadding() {
        return padding;
    }

    public void setRect(double x, double y, double w, double h) {
        super.setRect(x, y, w, h);
        padding.setContents(this);
    }
}
