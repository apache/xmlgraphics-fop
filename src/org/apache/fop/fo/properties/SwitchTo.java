package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Property;

import java.util.Iterator;

public class SwitchTo extends Property  {
    public static final int dataTypes = COMPLEX;
    public static final int traitMapping = ACTION;
    public static final int initialValueType = ENUM_IT;
    public static final int XSL_PRECEDING = 1;
    public static final int XSL_FOLLOWING = 2;
    public static final int XSL_ANY = 3;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.SWITCH_TO, XSL_ANY);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"xsl-preceding"
        ,"xsl-following"
        ,"xsl-any"
    };

    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue list)
                    throws PropertyException
    {
        // Check for the enumeration.  Look for a list of NCNames.
        // N.B. it may be possible to perform further checks on the
        // validity of the NCNames - do they match multi-case case names.
        if ( ! (list instanceof PropertyValueList))
            return super.refineParsing(PropNames.SWITCH_TO, foNode, list);

        PropertyValueList ssList =
                            spaceSeparatedList((PropertyValueList)list);
        Iterator iter = ssList.iterator();
        while (iter.hasNext()) {
            Object value = iter.next();
            if ( ! (value instanceof NCName))
                throw new PropertyException
                    ("switch-to requires a list of NCNames");
        }
        return list;
    }
    public int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

