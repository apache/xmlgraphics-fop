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

import java.awt.geom.AffineTransform;

import org.apache.fop.area.ReferenceArea;
import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;


/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public abstract class InlineReferenceArea extends InlineArea implements
        ReferenceArea {
    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    /**
     * @param pageSeq
     * @param generatedBy
     * @param parent
     * @param sync
     */
    public InlineReferenceArea(FoPageSequence pageSeq, FONode generatedBy,
            Node parent, Object sync) {
        super(pageSeq, generatedBy, parent, sync);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.apache.fop.area.ReferenceArea#setCoordTransformer(java.awt.geom.AffineTransform)
     */
    public void setCoordTransformer(AffineTransform matrix) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.fop.area.ReferenceArea#getCoordTransformer()
     */
    public AffineTransform getCoordTransformer() {
        // TODO Auto-generated method stub
        return null;
    }

}

