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
public abstract class PaddingCorrespondingAbsolute
extends AbstractCorrespondingProperty {

    /** Array of relative padding properties,
     * indexed by relative edge constants */
    private static int[] relPaddingProps = {
            PropNames.NO_PROPERTY
            ,PropNames.PADDING_BEFORE
            ,PropNames.PADDING_AFTER
            ,PropNames.PADDING_START
            ,PropNames.PADDING_END
    };

    /**
     * Gets the relative padding property corresponding to the given
     * absolute edge
     * @param foNode the node on which the property is being defined
     * @param absoluteEdge
     * @return the relative padding property index
     * @throws PropertyException
     */
    protected int getCorrespondingPaddingProperty(
            FONode foNode, int absoluteEdge)
    throws PropertyException {
        int relEdge = WritingMode.getCorrespondingRelativeEdge(
                foNode.getWritingMode(), absoluteEdge);
        return relPaddingProps[relEdge];
    }

    public boolean isCorrespondingAbsolute() {
        return true;
    }

}
