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
 * Created on 27/02/2004
 * $Id$
 */
package org.apache.fop.area;
import java.awt.geom.Rectangle2D;
/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public interface ViewportI {
    /**
     * Set if this viewport should clip.
     * @param c true if this viewport should clip
     */
    public abstract void setClip(boolean c);
    /**
     * Get the view area rectangle of this viewport.
     * @return the rectangle for this viewport
     * TODO Thread safety
     */
    public abstract Rectangle2D getViewArea();
    /**
     * @param viewArea to set
     */
    public abstract void setViewArea(Rectangle2D viewArea);
    /**
     * Get the page reference area with the contents.
     * @return the page reference area
     * TODO Thread safety
     */
    public abstract PageRefArea getPageRefArea();
}