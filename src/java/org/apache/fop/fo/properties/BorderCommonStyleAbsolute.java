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
 * Base class for border-&lt;absolute&gt;-style properties, providing the
 * methods necessary to resolve corresponding relative properties.
 * 
 * @author pbw
 * @version $Revision$ $Name$
 */
public class BorderCommonStyleAbsolute
extends BorderCommonStyle
implements AbsoluteCorrespondingProperty {
    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.AbsoluteCorrespondingProperty#getWritingMode()
     */
    public int getWritingMode(FONode foNode)
    throws PropertyException {
        PropertyValue wm = foNode.getPropertyValue(PropNames.WRITING_MODE);
        return EnumType.getEnumValue(wm);
    }

    public int getCorrespondingProperty(FONode foNode)
    throws PropertyException {
        return getCorrespondingRelativeProperty(foNode);
    }
    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.AbsoluteCorrespondingProperty#getCorrespondingRelativeProperty()
     */
    public int getCorrespondingRelativeProperty(FONode foNode)
    throws PropertyException {
        throw new PropertyException("Called from superclass");
    }

    /** Array of relative border style properties,
     * indexed by relative edge constants */
    private static int[] relBorderStyleProps = {
            PropNames.NO_PROPERTY
            ,PropNames.BORDER_BEFORE_STYLE
            ,PropNames.BORDER_AFTER_STYLE
            ,PropNames.BORDER_START_STYLE
            ,PropNames.BORDER_END_STYLE
    };

    /**
     * Gets the relative border style property corresponding to the given
     * absolute edge
     * @param foNode the node on which the property is being defined
     * @param absoluteEdge
     * @return the relative border style property index
     * @throws PropertyException
     */
    protected int getCorrespondingRelativeStyleProperty(
            FONode foNode, int absoluteEdge)
    throws PropertyException {
        int relEdge = WritingMode.getCorrespondingRelativeEdge(
                getWritingMode(foNode), absoluteEdge);
        return relBorderStyleProps[relEdge];
    }

    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.AbsoluteCorrespondingProperty#overridesCorresponding()
     */
    public boolean overridesCorresponding(FONode foNode) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.Property#isCorrespondingAbsolute()
     */
    public static boolean isCorrespondingAbsolute() {
        return true;
    }
}
