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

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class AbstractViewport
extends Area
implements Viewport {
    
    /** The viewport rectange */
    protected Rectangle2D viewArea = null;
    
    /** Does the viewport rectange clip the reference-area? */
    protected boolean clip = true;

    /** The reference-area of the viewport/reference pair */
    protected ReferenceArea refArea;

    /**
     * @param pageSeq
     * @param generatedBy
     * @param parent
     * @param sync
     */
    public AbstractViewport(FoPageSequence pageSeq, FONode generatedBy,
            Node parent, Object sync) {
        super(pageSeq, generatedBy, parent, sync);
    }
    /**
     * @param pageSeq
     * @param generatedBy
     */
    public AbstractViewport(FoPageSequence pageSeq, FONode generatedBy) {
        super(pageSeq, generatedBy);
    }
    /**
     * @param pageSeq
     * @param generatedBy
     * @param viewArea the viewport rectangle
     * @param parent
     * @param sync
     */
    public AbstractViewport(
            Rectangle2D viewArea,
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
        this.viewArea = viewArea;
    }
    /**
     * @param pageSeq
     * @param generatedBy
     * @param viewArea the viewport rectangle
     */
    public AbstractViewport(
            Rectangle2D viewArea,
            FoPageSequence pageSeq,
            FONode generatedBy) {
        super(pageSeq, generatedBy);
        this.viewArea = viewArea;
    }
    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#getViewArea()
     */
    public Rectangle2D getViewArea() {
        synchronized (sync) {
            return viewArea;
        }
    }
    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#setViewArea(java.awt.geom.Rectangle2D)
     */
    public void setViewArea(Rectangle2D viewArea) {
        synchronized (sync) {
            this.viewArea = viewArea;
        }
    }
    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#setReferenceArea(org.apache.fop.area.ReferenceArea)
     */
    public void setReferenceArea(ReferenceArea ref) {
        synchronized (sync) {
            refArea = ref;
        }
    }
    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#getReferenceArea()
     */
    public ReferenceArea getReferenceArea() {
        synchronized (sync) {
            return refArea;
        }
    }
    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#setClip(boolean)
     */
    public void setClip(boolean clip) {
        synchronized (sync) {
            this.clip = clip;
        }
    }
    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#getClip()
     */
    public boolean getClip() {
        synchronized (sync) {
            return clip;
        }
    }
    
}
