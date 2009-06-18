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
 * Created on 22/04/2004
 * $Id$
 */
package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public abstract class BorderColorCorresponding extends ColorTransparent
        implements
            CorrespondingProperty {
    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.CorrespondingProperty#getWritingMode(org.apache.fop.fo.FONode)
     */
    public int getWritingMode(FONode foNode)
    throws PropertyException {
        PropertyValue wm = foNode.getPropertyValue(PropNames.WRITING_MODE);
        return EnumType.getEnumValue(wm);
    }
    /* (non-Javadoc)
     * @see org.apache.fop.fo.properties.CorrespondingProperty#getCorrespondingProperty(org.apache.fop.fo.FONode)
     */
    public int getCorrespondingProperty(FONode foNode)
    throws PropertyException {
        throw new PropertyException("Called from superclass");
    }
}
