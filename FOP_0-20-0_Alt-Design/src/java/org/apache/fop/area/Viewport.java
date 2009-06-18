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
 * Created on 19/02/2004
 * $Id$
 */
package org.apache.fop.area;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public interface Viewport {
    
    /**
     * Sets the reference-area of this viewport/reference pair
     * @param ref
     */
    public abstract void setReferenceArea(ReferenceArea ref);
    
    /**
     * @return the reference-area of this viewport/reference pair
     */
    public abstract ReferenceArea getReferenceArea();

    /**
     * @param clip does this viewport clip its reference area?
     */
    public abstract void setClip(boolean clip);
    
    /**
     * @return whether this viewport clips its reference area
     */
    public abstract boolean getClip();
}
