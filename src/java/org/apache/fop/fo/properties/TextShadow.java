/*
 * $Id$
 *
 *
 * Copyright 1999-2003 The Apache Software Foundation.
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
 *  
 */
package org.apache.fop.fo.properties;

import java.util.Iterator;

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.ShadowEffect;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class TextShadow extends ColorNonTransparent  {
    public static final int dataTypes = COMPLEX | NONE | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = RENDERING;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = NONE_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int inherited = COMPUTED;

    public int getInherited() {
        return inherited;
    }



    /**
     * Refine list of lists of individual shadow effects.
     * 'list' is a PropertyValueList containing, at the top level,
     * a sequence of PropertyValueLists, each representing a single
     * shadow effect.  A shadow effect must contain, at a minimum, an
     * inline-progression offset and a block-progression offset.  It may
     * also optionally contain a blur radius.  This set of two or three
     * <tt>Length</tt>s may be preceded or followed by a color
     * specifier.
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue list)
                    throws PropertyException
    {
        int property = list.getProperty();
        if ( ! (list instanceof PropertyValueList)) {
            return super.refineParsing(PropNames.TEXT_SHADOW, foNode, list);
        }
        if (((PropertyValueList)list).size() == 0)
            throw new PropertyException
                ("text-shadow requires PropertyValueList of effects");
        PropertyValueList newlist = new PropertyValueList(property);
        Iterator effects = ((PropertyValueList)list).iterator();
        while (effects.hasNext()) {
            newlist.add(new ShadowEffect(PropNames.TEXT_SHADOW,
                        (PropertyValueList)(effects.next())));
        }
        return newlist;
    }

}

