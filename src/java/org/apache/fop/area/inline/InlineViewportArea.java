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
 * Created on 24/07/2004
 * $Id$
 */
package org.apache.fop.area.inline;

import org.apache.fop.area.ReferenceArea;
import org.apache.fop.area.Viewport;
import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;


/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public abstract class InlineViewportArea extends InlineArea implements Viewport {

    /**
     * @param pageSeq
     * @param generatedBy
     * @param parent
     * @param sync
     */
    public InlineViewportArea(FoPageSequence pageSeq, FONode generatedBy,
            Node parent, Object sync) {
        super(pageSeq, generatedBy, parent, sync);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#setReferenceArea(org.apache.fop.area.ReferenceArea)
     */
    public void setReferenceArea(ReferenceArea ref) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#getReferenceArea()
     */
    public ReferenceArea getReferenceArea() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#setClip(boolean)
     */
    public void setClip(boolean clip) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.fop.area.Viewport#getClip()
     */
    public boolean getClip() {
        // TODO Auto-generated method stub
        return false;
    }

}

//private static final String tag = "$Name$";
//private static final String revision = "$Revision$";