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

import org.apache.fop.datatypes.Ems;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class LineHeight extends Property  {
    public static final int dataTypes =
        COMPOUND| PERCENTAGE | LENGTH | NUMBER | MAPPED_LENGTH | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = FORMATTING;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = NOTYPE_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int NORMAL = 1;
    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


    private static final String[] rwEnums = {
        null
        ,"normal"
    };

    public int getEnumIndex(String enumval) throws PropertyException {
        if (enumval.equals("normal")) return 1;
        throw new PropertyException("Invalid enumeration: " + enumval);
    }

    public String getEnumText(int index) throws PropertyException {
        if (index == 1) return "normal";
        throw new PropertyException("Invalid enumval index: " + index);
    }

    public Numeric getMappedLength(FONode node, int enumval)
        throws PropertyException
    {
        if (enumval != NORMAL)
            throw new PropertyException("Invalid MAPPED_LENGTH ENUM: "
                                        + enumval);
        return Ems.makeEms(node, PropNames.LINE_HEIGHT, 1.2d); // normal
    }
}

