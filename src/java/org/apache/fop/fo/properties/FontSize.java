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

import java.util.HashMap;

import org.apache.fop.datatypes.Ems;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class FontSize extends Property  {
    public static final int dataTypes =
                        PERCENTAGE | LENGTH | MAPPED_LENGTH | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = FORMATTING| RENDERING;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = LENGTH_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int XX_SMALL = 1;
    public static final int X_SMALL = 2;
    public static final int SMALL = 3;
    public static final int MEDIUM = 4;
    public static final int LARGE = 5;
    public static final int X_LARGE = 6;
    public static final int XX_LARGE = 7;
    public static final int LARGER = 8;
    public static final int SMALLER = 9;

    // N.B. This foundational value MUST be an absolute length
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength(null, MEDIUM);
    }

    public static final int inherited = COMPUTED;

    public int getInherited() {
        return inherited;
    }


    private static final String[] rwEnums = {
        null
        ,"xx-small"
        ,"x-small"
        ,"small"
        ,"medium"
        ,"large"
        ,"x-large"
        ,"xx-large"
        ,"larger"
        ,"smaller"
    };

    // N.B. this is a combination of points and ems
    private static final double[] mappedLengths = {
        0d
        ,7d         // xx-small
        ,8.3d       // x-small
        ,10d        // small
        ,12d        // medium
        ,14.4d      // large
        ,17.3d      // x-large
        ,20.7d      // xx-large
        ,1.2d       // larger
        ,0.83d      // smaller
    };

    public Numeric getMappedLength(FONode node, int enumval)
        throws PropertyException
    {
        if (enumval == LARGER || enumval == SMALLER)
            return Ems.makeEms
                            (node, PropNames.FONT_SIZE, mappedLengths[enumval]);
        return
            Length.makeLength
                    (PropNames.FONT_SIZE, mappedLengths[enumval], Length.PT);
    }

    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap((int)(rwEnums.length / 0.75) + 1);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put(rwEnums[i],
                                Ints.consts.get(i));
        }
    }

}

