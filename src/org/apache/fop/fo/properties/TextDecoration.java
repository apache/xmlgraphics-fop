package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.TextDecorations;
import org.apache.fop.datatypes.TextDecorator;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Property;

import java.util.LinkedList;
import java.util.Iterator;

public class TextDecoration extends Property  {
    public static final int dataTypes = COMPLEX | NONE | INHERIT;
    public static final int traitMapping = NEW_TRAIT;
    public static final int initialValueType = TEXT_DECORATION_IT;
    public /*static*/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new TextDecorations
                            (PropNames.TEXT_DECORATION, NO_DECORATION);
    }
    public static final int inherited = NO;

    /** Text decoration constant */
    public static final byte
      NO_DECORATION = 0
         ,UNDERLINE = 1
          ,OVERLINE = 2
      ,LINE_THROUGH = 4
             ,BLINK = 8
                    ;

    private static final String[] alternatives = {
                                null
                                ,"underline"
                                ,"overline"
                                ,"line-through"
                                ,"blink"
                            };

    public static final ROStringArray enums
                                        = new ROStringArray(alternatives);

    private static final int[] decorations = {
                                NO_DECORATION
                                ,UNDERLINE
                                ,OVERLINE
                                ,LINE_THROUGH
                                ,BLINK
                            };

    public /*static*/ PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue list)
                    throws PropertyException
    {
        byte onMask = NO_DECORATION;
        byte offMask = NO_DECORATION;
        Iterator iter;
        PropertyValueList ssList = null;
        LinkedList strings = new LinkedList();
        if ( ! (list instanceof PropertyValueList)) {
            if ( ! (list instanceof NCName))
                throw new PropertyException
                    ("text-decoration require list of NCNames");
            strings.add(((NCName)list).getNCName());
        } else { // list is a PropertyValueList
            ssList = spaceSeparatedList((PropertyValueList)list);
            iter = ((PropertyValueList)ssList).iterator();
            while (iter.hasNext()) {
                Object value = iter.next();
                if ( ! (value instanceof NCName))
                    throw new PropertyException
                        ("text-decoration requires a list of NCNames");
                strings.add(((NCName)value).getNCName());
            }
        }
        iter = strings.iterator();
        while (iter.hasNext()) {
            int i;
            String str, str2;
            boolean negate;
            negate = false;
            str = (String)iter.next();
            str2 = str;
            if (str.indexOf("no-") == 0) {
                str2 = str.substring(3);
                negate = true;
            }
            try {
                i = getEnumIndex(str2);
            } catch (PropertyException e) {
                throw new PropertyException
                                ("text-decoration: unknown value " + str);
            }
            if (negate) offMask |= decorations[i];
            else         onMask |= decorations[i];
        }
        if ((offMask & onMask) != 0)
            throw new PropertyException
                ("Contradictory instructions for text-decoration " +
                    list.toString());
        return new TextDecorator(list.getProperty(), onMask, offMask);
    }

}

