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
 * Created on 20/04/2004
 * $Id$
 */
package org.apache.fop.fo.properties;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Base class for border-&lt;relative&gt;-style properties, providing the
 * methods necessary to resolve corresponding absolute properties.
 * 
 * @author pbw
 * @version $Revision$ $Name$
 */
public abstract class BorderCommonStyleRelative
extends BorderCommonStyle {

    /** Array of absolute border style properties,
     * indexed by absolute edge constants */
    private static int[] absBorderStyleProps = {
            PropNames.NO_PROPERTY
            ,PropNames.BORDER_TOP_STYLE
            ,PropNames.BORDER_BOTTOM_STYLE
            ,PropNames.BORDER_LEFT_STYLE
            ,PropNames.BORDER_RIGHT_STYLE
    };

    /**
     * Gets the absolute border style property corresponding to the given
     * relative edge
     * @param foNode the node on which the property is being defined
     * @param relativeEdge
     * @return the absolute border style property index
     * @throws PropertyException
     */
    protected int getCorrespondingStyleProperty(
            FONode foNode, int relativeEdge)
    throws PropertyException {
        int absEdge = WritingMode.getCorrespondingAbsoluteEdge(
                foNode.getWritingMode(), relativeEdge);
        return absBorderStyleProps[absEdge];
    }

    public boolean isCorrespondingRelative() {
        return true;
    }
}
