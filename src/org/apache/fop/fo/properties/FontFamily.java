package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.FontFamilySet;
import org.apache.fop.datatypes.StringType;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Property;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class FontFamily extends Property  {
    public static final int dataTypes = COMPLEX | INHERIT | FONTSET;
    public static final int traitMapping = FONT_SELECTION;
    public static final int initialValueType = NOTYPE_IT;
    public static final int SERIF = 1;
    public static final int SANS_SERIF = 2;
    public static final int CURSIVE = 3;
    public static final int FANTASY = 4;
    public static final int MONOSPACE = 5;
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"serif"
        ,"sans-serif"
        ,"cursive"
        ,"fantasy"
        ,"monospace"
    };
    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap(rwEnums.length);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put((Object)rwEnums[i],
                                (Object) Ints.consts.get(i));
        }
    }

    public /**/static/**/ PropertyValue refineParsing
                                    (FONode foNode, PropertyValue value)
                    throws PropertyException
    {
        // There is no point in attempting to validate the enumeration
        // tokens, because all NCNames and all Literals are valid.
        // A PropertyValueList, which itself implements propertyValue,
        // has the structure
        // (PropertyValue|PropertyValueList)+
        // Multiple members represent values that were comma-separated
        // in the original expression.  PropertyValueList members
        // represent values that were space-separated in the original
        // expression.  So, if a prioritised list of font generic or
        // family names was provided, the NCNames of font families will
        // be at the top level, and any font family names
        // that contained spaces will be in PropertyValueLists.

        return refineParsing(foNode, value, NOT_NESTED);
    }

    public /**/static/**/ PropertyValue refineParsing
                    (FONode foNode, PropertyValue value, boolean nested)
                    throws PropertyException
    {
        int property = value.getProperty();
        int type = value.getType();
        // First, check that we have a list
        if (type != PropertyValue.LIST) {
            if ( ! nested && type == PropertyValue.INHERIT) {
                return ((Inherit)value).resolve(foNode);
            }
            if ( ! (value instanceof StringType))
                throw new PropertyException
                    ("Invalid " + value.getClass().getName() +
                        " PropertyValue for font-family");
            return new FontFamilySet(property,
                    new String[] {((StringType)value).getString() });
        }
        PropertyValueList list = (PropertyValueList)value;
        String[] strings = new String[list.size()];
        int i = 0;          // the strings index
        Iterator scan = list.iterator();
        while (scan.hasNext()) {
            Object pvalue = scan.next();
            String name = "";
            if (pvalue instanceof PropertyValue) {
                int ptype = ((PropertyValue)pvalue).getType();
                if (ptype == PropertyValue.LIST) {
                    // build a font name according to
                    // 7.8.2 "font-family" <family-name>
                    Iterator font = ((PropertyValueList)pvalue).iterator();
                    while (font.hasNext())
                        name = name + (name.length() == 0 ? "" : " ")
                                + ((StringType)(font.next())).getString();
                    strings[i++] = name;
                    continue;
                }
                else if (pvalue instanceof StringType) {
                    name = ((StringType)pvalue).getString();
                    strings[i++] = name;
                    continue;
                }
            }
            throw new PropertyException
                    ("Invalid " + value.getClass().getName() +
                        " PropertyValue for font-family");
        }
        // Construct the FontFamilySet property value
        return new FontFamilySet(property, strings);
    }

}

