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

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Pseudo-property class for common border width values occurring in a
 * number of classes.
 */
public abstract class BorderCommonWidth extends AbstractCorrespondingProperty  {
    public static final int THIN = 1;
    public static final int MEDIUM = 2;
    public static final int THICK = 3;

    private static final String[] rwEnums = {
	null
	,"thin"
	,"medium"
	,"thick"
    };

    private static final double[] mappedPoints = {
	0d
	,0.5d
	,1d
	,2d
    };
    
    // N.B. If these values change, all initial values expressed in these
    // terms must be manually changed.

    /**
     * @param node  the FONode with an expressing the property
     * @param property  the property index
     * @param enumval  the mappedEnum enumeration value
     * @return <tt>Numeric[]</tt> containing the values corresponding
     * to the MappedNumeric enumeration constants for border width
     */
    public Numeric getMappedLength(FONode node, int property, int enumval)
	throws PropertyException
    {
	return 
	    Length.makeLength(property, mappedPoints[enumval], Length.PT);
    }

    public int getEnumIndex(String enumval) throws PropertyException {
        return enumValueToIndex(enumval, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }

}

