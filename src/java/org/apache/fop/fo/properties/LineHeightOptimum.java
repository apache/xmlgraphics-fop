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
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class LineHeightOptimum extends Property  {
    public static final int dataTypes = LENGTH | PERCENTAGE;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = FORMATTING;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = LENGTH_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return Ems.makeEms(null, PropNames.LINE_HEIGHT_OPTIMUM, 1.2d);
    }
    public static final int inherited = COMPUTED;

    public int getInherited() {
        return inherited;
    }

}

