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

import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.PropertyValue;
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
public class BorderCommonStyleRelative
extends BorderCommonStyle
implements RelativeCorrespondingProperty {
    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.RelativeCorrespondingProperty#getWritingMode(org.apache.fop.fo.FONode)
     */
    public int getWritingMode(FONode foNode)
    throws PropertyException {
        PropertyValue wm = foNode.getPropertyValue(PropNames.WRITING_MODE);
        return EnumType.getEnumValue(wm);
    }

    public int getCorrespondingProperty(FONode foNode)
    throws PropertyException {
        return getCorrespondingAbsoluteProperty(foNode);
    }
    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.RelativeCorrespondingProperty#getCorrespondingAbsoluteProperty(org.apache.fop.fo.FONode)
     */
    public int getCorrespondingAbsoluteProperty(FONode foNode)
    throws PropertyException {
        throw new PropertyException("Called from superclass");
    }

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
    protected int getCorrespondingAbsoluteStyleProperty(
            FONode foNode, int relativeEdge)
    throws PropertyException {
        int absEdge = WritingMode.getCorrespondingAbsoluteEdge(
                getWritingMode(foNode), relativeEdge);
        return absBorderStyleProps[absEdge];
    }

    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.RelativeCorrespondingProperty#correspondingOverrides(org.apache.fop.fo.FONode)
     */
    public boolean correspondingOverrides(FONode foNode) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.Property#isCorrespondingRelative()
     */
    public static boolean isCorrespondingRelative() {
        return true;
    }

}
