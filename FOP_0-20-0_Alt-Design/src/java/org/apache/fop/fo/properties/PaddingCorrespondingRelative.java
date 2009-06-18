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
 * Created on 29/04/2004
 * $Id$
 */
package org.apache.fop.fo.properties;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public abstract class PaddingCorrespondingRelative extends AbstractCorrespondingProperty {

    /** Array of absolute padding properties,
     * indexed by absolute edge constants */
    private static int[] absPaddingProps = {
            PropNames.NO_PROPERTY
            ,PropNames.PADDING_TOP
            ,PropNames.PADDING_BOTTOM
            ,PropNames.PADDING_LEFT
            ,PropNames.PADDING_RIGHT
    };

    /**
     * Gets the absolute padding property corresponding to the given
     * relative edge
     * @param foNode the node on which the property is being defined
     * @param relativeEdge
     * @return the absolute padding property index
     * @throws PropertyException
     */
    protected int getCorrespondingPaddingProperty(
            FONode foNode, int relativeEdge)
    throws PropertyException {
        int relEdge = WritingMode.getCorrespondingAbsoluteEdge(
                foNode.getWritingMode(), relativeEdge);
        return absPaddingProps[relEdge];
    }

    public boolean isCorrespondingRelative() {
        return true;
    }

}
