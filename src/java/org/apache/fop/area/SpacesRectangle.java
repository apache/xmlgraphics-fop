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
import java.util.ArrayList;

import org.apache.fop.area.Area.AreaGeometry;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class SpacesRectangle extends AreaFrame {

    public SpacesRectangle(Area area, BorderRectangle content) {
        super(area, content);
    }

    /**
	 * @param contents
	 * @param contentOffset
	 */
	public SpacesRectangle(Area area,
            double ipOffset, double bpOffset, double ipDim, double bpDim,
			BorderRectangle contents, Point2D contentOffset) {
        super(area, ipOffset, bpOffset, ipDim, bpDim, contents, contentOffset);
	}

	/**
	 * @param rect
	 * @param contents
	 * @param contentOffset
	 */
	public SpacesRectangle(Area area, Rectangle2D rect,
            BorderRectangle contents, Point2D contentOffset) {
		super(area, rect, contents, contentOffset);
	}

    public void setContents(AreaGeometry contents) {
        super.setContents(contents);
        notifyListeners(this);
    }

    /** Initial size of the <code>listeners</code> array */
    private static final int INITIAL_SPACES_LISTENER_SIZE = 1;
    /** Array of registered <code>AreaListener</code>s */
    private ArrayList listeners = null;
    /**
     * Registers a listener to be notified on any change of dimension in the
     * <code>spaces</code> <code>AreaFrame</code>. 
     * @param listener to be notified
     */
    public void registerAreaListener(AreaListener listener) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ArrayList(INITIAL_SPACES_LISTENER_SIZE);
            }
            listeners.add(listener);
        }
    }

    /**
     * Notifies any registered listener of a change of dimensions in the
     * <code>Rectangle2D</code> content
     */
    protected void notifyListeners(Area.AreaGeometry geometry) {
        for (int i = 0; i < listeners.size(); i++) {
            synchronized (this) {
                ((AreaListener)(listeners.get(i))).setDimensions(geometry);
            }
        }
    }

}
