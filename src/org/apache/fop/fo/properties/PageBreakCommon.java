package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.datatypes.indirect.FromParent;
import org.apache.fop.datatypes.indirect.FromNearestSpecified;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.properties.Property;

import java.util.HashMap;

public class PageBreakCommon extends Property  {
    public static final int dataTypes = SHORTHAND | AUTO | ENUM | INHERIT;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = AUTO_IT;
    public static final int ALWAYS = 1;
    public static final int AVOID = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"always"
        ,"avoid"
        ,"left"
        ,"right"
    };
    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap(rwEnums.length);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put((Object)rwEnums[i],
                                (Object) Ints.consts.get(i));
        }
    }
    public /**/static/**/ int getEnumIndex(String enum) {
        return ((Integer)(rwEnumHash.get(enum))).intValue();
    }
    public /**/static/**/ String getEnumText(int index) {
        return rwEnums[index];
    }

    /*
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the verified value
     */
    public /**/static/**/ PropertyValue refineParsing
                                    (FONode foNode, PropertyValue value)
                    throws PropertyException
    {
	int property = value.getProperty();
	int beforeAfter, previousNext;
	switch (property) {
	case PropNames.PAGE_BREAK_BEFORE:
	    beforeAfter = PropNames.BREAK_BEFORE;
	    previousNext = PropNames.KEEP_WITH_PREVIOUS;
	    break;
	case PropNames.PAGE_BREAK_AFTER:
	    beforeAfter = PropNames.BREAK_AFTER;
	    previousNext = PropNames.KEEP_WITH_NEXT;
	    break;
	default:
	    throw new PropertyException
		("Unknown property in PageBreakCommon: "
		    + PropNames.getPropertyName(property));
	}
        if (value instanceof Inherit |
                value instanceof FromParent |
                    value instanceof FromNearestSpecified |
                        value instanceof Auto)
        {
            return refineExpansionList
                (foNode, ShorthandPropSets.expandAndCopySHand(value));
        }
        if (value instanceof NCName) {
            EnumType enum = null;
            String ncname = ((NCName)value).getNCName();
            try {
                enum = new EnumType(value.getProperty(), ncname);
            } catch (PropertyException e) {
                throw new PropertyException
                ("Unrecognized NCName in page-break-after: " + ncname);
            }
            PropertyValueList list = new PropertyValueList(property);
            switch (enum.getEnumValue()) {
            case ALWAYS:
                list.add(new EnumType(beforeAfter, "page"));
                list.add(new Auto(previousNext));
                return list;
            case AVOID:
                list.add(new Auto(beforeAfter));
                list.add(new EnumType(previousNext, "always"));
                return list;
            case LEFT:
                list.add(new EnumType(beforeAfter, "even-page"));
                list.add(new Auto(previousNext));
                return list;
            case RIGHT:
                list.add(new EnumType(beforeAfter, "odd-page"));
                list.add(new Auto(previousNext));
                return list;
            }
        }

        throw new PropertyException
            ("Invalid value for '" + PropNames.getPropertyName(property)
                + "': " + value.getClass().getName());
    }
}

